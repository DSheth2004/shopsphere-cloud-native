# Runs read-only checks on the ShopSphere AWS node through SSM and prints the result:
# pod status, an admin password consistency check, a login through the public route, and recent logs.
# Usage (from the repository root):  powershell -ExecutionPolicy Bypass -File scripts\aws-diagnose.ps1
param(
    [string]$InstanceId = "",
    [string]$Region = "ap-south-1"
)
$ErrorActionPreference = "Stop"
if (-not $InstanceId) {
    $InstanceId = terraform -chdir="$PSScriptRoot\..\infra\terraform" output -raw instance_id
}

$script = @'
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
echo "== pods"; kubectl -n shopsphere get pods
P=$(aws ssm get-parameter --region REGION --name /shopsphere/admin-password --with-decryption --query Parameter.Value --output text)
echo "== admin password: SSM vs auth-service (hashes must match)"
printf %s "$P" | sha256sum
kubectl -n shopsphere exec deploy/auth-service -- printenv ADMIN_PASSWORD | tr -d '\n' | sha256sum
echo "== admin login through the public route (expect 200)"
curl -s -o /dev/null -w "%{http_code}\n" -H "Content-Type: application/json" -d "{\"email\":\"admin@shopsphere.local\",\"password\":\"$P\"}" http://localhost/api/auth/login
echo "== auth-service log"; kubectl -n shopsphere logs deploy/auth-service --tail=200 | grep -iE "admin|error|exception|started" | tail -15
echo "== gateway log"; kubectl -n shopsphere logs deploy/api-gateway --tail=200 | grep -iE "error|exception|started" | tail -10
'@
$script = $script.Replace("REGION", $Region)
$file = Join-Path $env:TEMP "shopsphere-diag.json"
[IO.File]::WriteAllText($file, (@{ commands = @($script) } | ConvertTo-Json -Compress))

$id = aws ssm send-command --region $Region --instance-ids $InstanceId --document-name AWS-RunShellScript `
    --parameters "file://$file" --query Command.CommandId --output text
Write-Host "Waiting for the node to run the checks..." -ForegroundColor Cyan
do {
    Start-Sleep 3
    $status = aws ssm get-command-invocation --region $Region --instance-id $InstanceId --command-id $id --query Status --output text 2>$null
} while ($status -in @("Pending", "InProgress", "Delayed", $null, ""))
aws ssm get-command-invocation --region $Region --instance-id $InstanceId --command-id $id --query StandardOutputContent --output text
aws ssm get-command-invocation --region $Region --instance-id $InstanceId --command-id $id --query StandardErrorContent --output text
