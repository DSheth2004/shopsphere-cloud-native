# Create a kind cluster, build all images, load them and deploy the local overlay.
# Storefront: http://localhost:30080
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
$cluster = "shopsphere"

if (-not (kind get clusters | Select-String -Quiet "^$cluster$")) {
    kind create cluster --name $cluster --config scripts/kind-config.yaml
}
kubectl config use-context "kind-$cluster"

Write-Host "Building images with docker compose..." -ForegroundColor Cyan
docker compose build

& (Join-Path $PSScriptRoot "deploy.ps1") -Overlay local
Write-Host "ShopSphere is up at http://localhost:30080" -ForegroundColor Green
