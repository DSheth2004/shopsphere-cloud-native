# Re-attaches resources that already exist in AWS to a fresh Terraform state, so `terraform plan` and
# `terraform destroy` manage them again. Run once from infra/terraform after `terraform init`, when the original
# terraform.tfstate has been lost. Already-imported resources are skipped.
$ErrorActionPreference = "Stop"

$account  = aws sts get-caller-identity --query Account --output text
$instance = aws ec2 describe-instances `
  --filters "Name=tag:Name,Values=shopsphere-k3s" "Name=instance-state-name,Values=pending,running,stopping,stopped" `
  --query "Reservations[0].Instances[0].InstanceId" --output text
$sg       = aws ec2 describe-security-groups --filters "Name=group-name,Values=shopsphere-node" `
  --query "SecurityGroups[0].GroupId" --output text
$eip      = aws ec2 describe-addresses --filters "Name=tag:Name,Values=shopsphere-k3s" `
  --query "Addresses[0].AllocationId" --output text

Write-Host "account=$account instance=$instance sg=$sg eip=$eip"

$imports = [ordered]@{
  "aws_ssm_parameter.admin_password"                    = "/shopsphere/admin-password"
  "aws_security_group.node"                             = $sg
  "aws_iam_role.node"                                   = "shopsphere-node"
  "aws_iam_role_policy_attachment.node_ssm"             = "shopsphere-node/arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
  "aws_iam_role_policy.node_params"                     = "shopsphere-node:read-shopsphere-parameters"
  "aws_iam_instance_profile.node"                       = "shopsphere-node"
  "aws_instance.node"                                   = $instance
  "aws_eip.node"                                        = $eip
  "aws_iam_openid_connect_provider.github[0]"           = "arn:aws:iam::${account}:oidc-provider/token.actions.githubusercontent.com"
  "aws_iam_role.github_deploy"                          = "shopsphere-github-deploy"
  "aws_iam_role_policy.github_deploy"                   = "shopsphere-github-deploy:run-deploy-on-node"
  "aws_budgets_budget.cap"                              = "${account}:shopsphere-capstone-budget"
  "aws_iam_role.scheduler[0]"                           = "shopsphere-nightly-stop"
  "aws_iam_role_policy.scheduler[0]"                    = "shopsphere-nightly-stop:stop-shopsphere-node"
  "aws_scheduler_schedule.nightly_stop[0]"              = "default/shopsphere-nightly-stop"
}

$known = @(terraform state list)
foreach ($address in $imports.Keys) {
  if ($known -contains $address) { Write-Host "skip $address (already in state)"; continue }
  terraform import -input=false $address $imports[$address]
  if ($LASTEXITCODE -ne 0) { throw "import failed for $address" }
}

Write-Host "Done. Run 'terraform plan' next: it should show no destroy or replace."
