# One small k3s node in the default VPC's public subnet: no NAT gateway, no load balancer, no EKS.
data "aws_vpc" "default" {
  default = true
}

data "aws_ssm_parameter" "al2023_ami" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

resource "random_password" "admin" {
  length  = 16
  special = false
}

resource "aws_ssm_parameter" "admin_password" {
  name        = "/shopsphere/admin-password"
  description = "Storefront administrator password, read by the node at boot."
  type        = "SecureString"
  value       = random_password.admin.result

  # Keep the stored password if the random_password is ever regenerated (e.g. after re-importing into a fresh state).
  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_security_group" "node" {
  name        = "shopsphere-node"
  description = "Public HTTP for the storefront; everything else closed"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "Storefront over HTTP (Traefik ingress)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  dynamic "ingress" {
    for_each = var.ssh_cidr == "" ? [] : [var.ssh_cidr]
    content {
      description = "Optional SSH from a single address"
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = [ingress.value]
    }
  }

  # The node must reach GHCR, GitHub, get.k3s.io and the OS package mirrors, whose addresses are not fixed,
  # so egress is limited by port rather than by destination. AWS DNS, NTP and IMDS are link-local and not filtered.
  #trivy:ignore:AVD-AWS-0104
  egress {
    description = "HTTPS for image pulls, GitHub and k3s install"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  #trivy:ignore:AVD-AWS-0104
  egress {
    description = "HTTP for OS package mirrors"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "node" {
  ami                         = data.aws_ssm_parameter.al2023_ami.value
  instance_type               = var.instance_type
  iam_instance_profile        = aws_iam_instance_profile.node.name
  vpc_security_group_ids      = [aws_security_group.node.id]
  associate_public_ip_address = true
  user_data_replace_on_change = true
  user_data = templatefile("${path.module}/user-data.sh.tftpl", {
    aws_region  = var.aws_region
    repo_url    = var.repo_url
    git_branch  = var.git_branch
    admin_param = aws_ssm_parameter.admin_password.name
  })

  metadata_options {
    http_tokens   = "required"
    http_endpoint = "enabled"
  }

  root_block_device {
    volume_type           = "gp3"
    volume_size           = var.root_volume_gb
    encrypted             = true
    delete_on_termination = true
  }

  tags = { Name = "shopsphere-k3s" }

  # The AMI lookup always returns the newest Amazon Linux image; never replace the running node just because AWS
  # published a newer one or because the boot script changed after the node was built.
  lifecycle {
    ignore_changes = [ami, user_data]
  }
}

# A fixed public IP keeps the demo link the same across stop/start cycles.
resource "aws_eip" "node" {
  instance = aws_instance.node.id
  domain   = "vpc"
  tags     = { Name = "shopsphere-k3s" }
}
