
Add-Type -AssemblyName System.Drawing

$baseDir = "app\src\main\res"
$sourceDir = Join-Path $baseDir "mipmap-xxxhdpi"

# Source Files
$srcLauncher = Join-Path $sourceDir "ic_launcher.png"
$srcRound = Join-Path $sourceDir "round_ic_launcher.png"
$srcForeground = Join-Path $sourceDir "ic_launcher_foreground.png"

# Target Densities and Scales relative to xxxhdpi (which is 4x baseline)
# mdpi (1x): 0.25
# hdpi (1.5x): 0.375
# xhdpi (2x): 0.5
# xxhdpi (3x): 0.75

$targets = @{
    "mipmap-mdpi"   = 0.25
    "mipmap-hdpi"   = 0.375
    "mipmap-xhdpi"  = 0.5
    "mipmap-xxhdpi" = 0.75
}

function Resize-File {
    param(
        [string]$sourceFile,
        [string]$destFile,
        [double]$scaleFactor
    )

    if (!(Test-Path $sourceFile)) {
        Write-Warning "Source file not found: $sourceFile"
        return
    }

    $srcImage = [System.Drawing.Image]::FromFile($sourceFile)
    
    $newWidth = [int]($srcImage.Width * $scaleFactor)
    $newHeight = [int]($srcImage.Height * $scaleFactor)

    $destRect = New-Object System.Drawing.Rectangle(0, 0, $newWidth, $newHeight)
    $destImage = New-Object System.Drawing.Bitmap($newWidth, $newHeight)
    
    $destImage.SetResolution($srcImage.HorizontalResolution, $srcImage.VerticalResolution)

    $graphics = [System.Drawing.Graphics]::FromImage($destImage)
    $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $wrapMode = New-Object System.Drawing.Imaging.ImageAttributes
    $wrapMode.SetWrapMode([System.Drawing.Drawing2D.WrapMode]::TileFlipXY)

    $graphics.DrawImage($srcImage, $destRect, 0, 0, $srcImage.Width, $srcImage.Height, [System.Drawing.GraphicsUnit]::Pixel, $wrapMode)

    $destImage.Save($destFile, [System.Drawing.Imaging.ImageFormat]::Png)
    
    $graphics.Dispose()
    $destImage.Dispose()
    $srcImage.Dispose()
    
    Write-Host "Generated $destFile ($newWidth x $newHeight)"
}

foreach ($targetFolder in $targets.Keys) {
    $scale = $targets[$targetFolder]
    $destDir = Join-Path $baseDir $targetFolder

    if (!(Test-Path $destDir)) {
        New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    }

    # Resize all three files
    Resize-File -sourceFile $srcLauncher -destFile (Join-Path $destDir "ic_launcher.png") -scaleFactor $scale
    Resize-File -sourceFile $srcRound -destFile (Join-Path $destDir "round_ic_launcher.png") -scaleFactor $scale
    Resize-File -sourceFile $srcForeground -destFile (Join-Path $destDir "ic_launcher_foreground.png") -scaleFactor $scale
}

Write-Host "Downscaling complete."
