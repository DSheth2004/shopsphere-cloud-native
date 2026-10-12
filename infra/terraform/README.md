# AWS deployment (cost-controlled)

This folder is the Terraform entry point for ECR, a public-subnet-only ECS/Fargate demo, Lambda/EventBridge report execution, a short-lived k3s EC2 demonstration node, S3 reports, GitHub OIDC and budget alerts. Never create a NAT Gateway, EKS node group, RDS, MSK, or ElastiCache for this capstone.

Run `terraform init`, `terraform plan`, and only then `terraform apply`. Stop EC2/Fargate after a demo and run `terraform destroy` immediately afterwards. Configure `budget_email` before applying.
