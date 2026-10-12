# AWS deployment (cost-controlled)

This Terraform creates the public ShopSphere environment on a single EC2 instance running k3s. It is sized for a student credit budget: about $1.30 per day while running and about $0.20 per day while stopped (see [docs/COST.md](../../docs/COST.md)).

## What it creates

| File | Resources |
| --- | --- |
| `main.tf` | Security group (HTTP 80 only, optional SSH from one CIDR), `t3.medium` Amazon Linux 2023 instance with encrypted gp3 volume and IMDSv2, Elastic IP, random admin password stored as an SSM SecureString |
| `iam.tf` | Instance role (SSM Session Manager + read of its own parameter), GitHub Actions OIDC provider and a deploy role that can only run `AWS-RunShellScript` on this instance |
| `cost-controls.tf` | Monthly AWS Budget with email alerts, EventBridge Scheduler that stops the instance every night |
| `user-data.sh.tftpl` | First boot: swap file, k3s, generated cluster secrets, `/opt/shopsphere/deploy.sh`, first deployment of `k8s/overlays/aws` |
| `outputs.tf` | `app_url`, `instance_id`, `github_deploy_role_arn`, admin password, start/stop/shell commands |

Nothing here creates a NAT gateway, load balancer, EKS, RDS or ElastiCache.

## Before you apply

1. Install Terraform 1.6+ and the AWS CLI, and sign in (`aws configure` or `aws sso login`).
2. Make sure the container images exist on GHCR and are public: merge to `master` (or run the **CD** workflow manually), then for each `shopsphere-*` package open GitHub, Packages, Package settings, and change visibility to Public. The node pulls images anonymously.
3. `cp terraform.tfvars.example terraform.tfvars` and set `budget_email`. If you want to deploy a branch that is not merged yet, also set `git_branch`.

## Apply

```bash
terraform init
terraform plan
terraform apply
terraform output app_url                 # public link, ready about 8 minutes after the first apply
terraform output -raw admin_password     # admin@shopsphere.local
```

Watch the first boot with `$(terraform output -raw shell_command)` and then `sudo kubectl -n shopsphere get pods`.

## Connect the CD pipeline (optional)

Set these repository variables in GitHub (Settings, Secrets and variables, Actions, Variables):

| Variable | Value |
| --- | --- |
| `AWS_DEPLOY_ROLE_ARN` | `terraform output -raw github_deploy_role_arn` |
| `EC2_INSTANCE_ID` | `terraform output -raw instance_id` |
| `AWS_REGION` | only if you changed `aws_region` |

From then on every push to `master` builds, scans and pushes the images, and then runs `/opt/shopsphere/deploy.sh` on the node through SSM. No AWS keys are stored in GitHub. If the account already has GitHub's OIDC provider, set `create_github_oidc_provider = false`.

## Save credits

```bash
$(terraform output -raw stop_command)    # stop after a demo
$(terraform output -raw start_command)   # start before the next one; the link stays the same
terraform destroy                        # when the assignment is graded
```
