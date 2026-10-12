# End-to-end smoke test through the storefront (or the gateway directly with -BaseUrl http://localhost:8080).
param(
    [string]$BaseUrl = "http://localhost:3000",
    [string]$AdminEmail = "admin@shopsphere.local",
    [string]$AdminPassword = "Admin@12345"
)

$ErrorActionPreference = "Stop"
$suffix = [Guid]::NewGuid().ToString("N").Substring(0, 8)

function Invoke-Api([string]$Method, [string]$Path, [object]$Body = $null, [string]$Token = $null) {
    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $params = @{ Method = $Method; Uri = "$BaseUrl$Path"; Headers = $headers }
    if ($null -ne $Body) { $params["Body"] = ($Body | ConvertTo-Json -Compress -Depth 5) }
    Invoke-RestMethod @params
}

function Wait-Order([string]$Id, [string]$Token) {
    for ($i = 0; $i -lt 20; $i++) {
        Start-Sleep -Milliseconds 500
        $o = Invoke-Api Get "/api/orders/$Id" -Token $Token
        if ($o.status -in @("CONFIRMED", "CANCELLED")) { return $o }
    }
    throw "Order $Id did not reach a final saga state."
}

try {
    $health = Invoke-Api Get "/actuator/health"
    if ($health.status -ne "UP") { throw "Gateway health is $($health.status), not UP." }
    foreach ($svc in "auth", "product", "inventory", "cart", "order", "payment", "notification") {
        $s = Invoke-Api Get "/status/$svc"
        if ($s.status -ne "UP") { throw "$svc-service is $($s.status)." }
    }

    $products = Invoke-Api Get "/api/products"
    if ($products.Count -lt 1) { throw "Catalogue is empty." }
    $product = $products[0]

    $session = Invoke-Api Post "/api/auth/register" @{ name = "Smoke $suffix"; email = "smoke-$suffix@shopsphere.local"; password = "Password123!" }
    $token = $session.accessToken

    $cart = Invoke-Api Post "/api/carts/items" @{ productId = $product.id; name = $product.name; price = $product.price; image = $product.image; quantity = 1 } -Token $token
    $order = Invoke-Api Post "/api/orders" @{ items = @(@{ productId = $product.id; quantity = 1 }) } -Token $token
    $final = Wait-Order $order.id $token

    $admin = Invoke-Api Post "/api/auth/login" @{ email = $AdminEmail; password = $AdminPassword }
    $created = Invoke-Api Post "/api/products" @{ name = "Smoke product $suffix"; price = 1499.00; category = "Demo"; image = "🧪" } -Token $admin.accessToken

    Write-Host "Smoke test passed." -ForegroundColor Green
    Write-Host "User:          $($session.user.email)"
    Write-Host "Cart items:    $($cart.itemCount) (subtotal $($cart.subtotal))"
    Write-Host "Order:         $($final.id) -> $($final.status)"
    Write-Host "Admin product: $($created.id)"
}
catch {
    Write-Error "ShopSphere smoke test failed: $($_.Exception.Message)"
    exit 1
}
