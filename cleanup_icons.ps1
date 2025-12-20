
$baseDir = "app\src\main\res"
$densities = @("mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi")

foreach ($density in $densities) {
    $file = Join-Path $baseDir "$density\ic_launcher_round.png"
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "Removed $file"
    }
}
