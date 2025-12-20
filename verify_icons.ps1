
$baseDir = "app\src\main\res"
$densities = @("mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi")

$allGood = $true

foreach ($density in $densities) {
    $folderPath = Join-Path $baseDir $density
    
    # 1. Check for round_ic_launcher.png (Accurate Name) - REQUIRED
    $roundFile = Join-Path $folderPath "round_ic_launcher.png"
    if (-not (Test-Path $roundFile)) {
        Write-Host "MISSING: $roundFile"
        $allGood = $false
    }

    # 2. Check for ic_launcher.png (Square Name) - REQUIRED
    $squareFile = Join-Path $folderPath "ic_launcher.png"
    if (-not (Test-Path $squareFile)) {
        Write-Host "MISSING: $squareFile"
        $allGood = $false
    }
    
    # 3. Check for ic_launcher_foreground.png (Adaptive) - REQUIRED
    $foreFile = Join-Path $folderPath "ic_launcher_foreground.png"
    if (-not (Test-Path $foreFile)) {
        Write-Host "MISSING: $foreFile"
        $allGood = $false
    }

    # 4. Check for ic_launcher_round.png (Old Name) - SHOULD NOT EXIST
    $oldFile = Join-Path $folderPath "ic_launcher_round.png"
    if (Test-Path $oldFile) {
        Write-Host "FOUND OLD FILE (Please Delete): $oldFile"
        $allGood = $false
    }
}

if ($allGood) {
    Write-Host "VERIFICATION SUCCESS: All folders contain [round_ic_launcher, ic_launcher, ic_launcher_foreground] and are free of old files."
}
else {
    Write-Host "VERIFICATION FAILED: See above errors."
}
