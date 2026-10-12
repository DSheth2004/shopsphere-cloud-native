output "app_url" {
  description = "Public storefront link. Allow about 8 minutes after the first apply for k3s and the images to come up."
  value       = "http://${aws_eip.node.public_ip}"
}

output "instance_id" {
  description = "Set this as the EC2_INSTANCE_ID repository variable for the CD workflow."
  value       = aws_instance.node.id
}

output "github_deploy_role_arn" {
  description = "Set this as the AWS_DEPLOY_ROLE_ARN repository variable for the CD workflow."
  value       = aws_iam_role.github_deploy.arn
}

output "admin_email" {
  value = "admin@shopsphere.local"
}

output "admin_password" {
  description = "Read with: terraform output -raw admin_password"
  value       = random_password.admin.result
  sensitive   = true
}

output "shell_command" {
  description = "Open a shell on the node without SSH keys."
  value       = "aws ssm start-session --region ${var.aws_region} --target ${aws_instance.node.id}"
}

output "stop_command" {
  value = "aws ec2 stop-instances --region ${var.aws_region} --instance-ids ${aws_instance.node.id}"
}

output "start_command" {
  value = "aws ec2 start-instances --region ${var.aws_region} --instance-ids ${aws_instance.node.id}"
}
