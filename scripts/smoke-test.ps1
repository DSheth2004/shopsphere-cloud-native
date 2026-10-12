param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$suffix = [Guid]::NewGuid().ToString("N").Substring(0, 8)
$headers = @{ "Content-Type" = "application/json" }

function Invoke-ShopSpherePost([string]$Path, [object]$Body) {
    Invoke-RestMethod -Method Post -Uri "$BaseUrl$Path" -Headers $headers `
        -Body ($Body | ConvertTo-Json -Compress)
}

try {
    $health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
    if ($health.status -ne "UP") { throw "Gateway health is $($health.status), not UP." }

    $user = Invoke-ShopSpherePost "/api/auth/register" @{ email = "demo-$suffix@shopsphere.local"; password = "Password123!" }
    $product = Invoke-ShopSpherePost "/api/products" @{ name = "Smoke product $suffix"; price = 1499.00; category = "demo" }
    $cart = Invoke-ShopSpherePost "/api/carts/$suffix/items" @{ productId = $product.id; quantity = 1 }
    $order = Invoke-ShopSpherePost "/api/orders" @{ userId = $suffix; total = $product.price }
    $finalOrder = $order
    1..10 | ForEach-Object {
        Start-Sleep -Milliseconds 500
        $finalOrder = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/orders/$($order.id)"
        if ($finalOrder.status -in @("CONFIRMED", "CANCELLED")) { return }
    }
    if ($finalOrder.status -notin @("CONFIRMED", "CANCELLED")) { throw "Order $($order.id) did not reach a final saga state." }

    Write-Host "Smoke test passed." -ForegroundColor Green
    Write-Host "User: $($user.email)"
    Write-Host "Product: $($product.id)"
    Write-Host "Cart items: $($cart.Count)"
    Write-Host "Order: $($finalOrder.id), status: $($finalOrder.status)"
}
catch {
    Write-Error "ShopSphere smoke test failed: $($_.Exception.Message)"
    exit 1
}
