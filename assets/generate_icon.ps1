# PowerShell script to generate a high quality PetCare icon (PNG and multi-resolution ICO)
Add-Type -AssemblyName System.Drawing

$assetsDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not (Test-Path $assetsDir)) {
    New-Item -ItemType Directory -Path $assetsDir -Force | Out-Null
}

$pngPath = Join-Path $assetsDir "app_icon.png"
$icoPath = Join-Path $assetsDir "app_icon.ico"

function Draw-PetCareIcon([int]$size) {
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)

    # 1. Background Rounded Circle / Squircle with modern Teal/Emerald gradient
    $rect = New-Object System.Drawing.RectangleF(1.0, 1.0, [float]($size - 2), [float]($size - 2))
    $cTop = [System.Drawing.Color]::FromArgb(255, 13, 148, 136)    # Teal 600
    $cBottom = [System.Drawing.Color]::FromArgb(255, 15, 118, 110) # Teal 700
    $brushBg = New-Object System.Drawing.Drawing2D.LinearGradientBrush($rect, $cTop, $cBottom, [float]45.0)
    
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $radius = [float]($size * 0.22)
    $d = [float]($radius * 2)
    $path.AddArc($rect.X, $rect.Y, $d, $d, [float]180.0, [float]90.0)
    $path.AddArc([float]($rect.Right - $d), $rect.Y, $d, $d, [float]270.0, [float]90.0)
    $path.AddArc([float]($rect.Right - $d), [float]($rect.Bottom - $d), $d, $d, [float]0.0, [float]90.0)
    $path.AddArc($rect.X, [float]($rect.Bottom - $d), $d, $d, [float]90.0, [float]90.0)
    $path.CloseFigure()
    
    $g.FillPath($brushBg, $path)

    # Inner subtle border
    $penBorder = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(90, 255, 255, 255), [float]([Math]::Max(1.0, $size * 0.02)))
    $g.DrawPath($penBorder, $path)

    # 2. Draw Cute Pet Paw & Medical Cross in clean White
    $whiteBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $scale = [float]($size / 100.0)

    # Paw Main Pad (bottom center, rounded bean shape)
    $padRect = New-Object System.Drawing.RectangleF([float](27 * $scale), [float](48 * $scale), [float](46 * $scale), [float](34 * $scale))
    $g.FillEllipse($whiteBrush, $padRect)

    # Medical Cross inside Main Pad (Teal color)
    $crossBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 13, 148, 136))
    $cw = [float](5.0 * $scale)
    $ch = [float](16.0 * $scale)
    $cx = [float](50.0 * $scale)
    $cy = [float](65.0 * $scale)
    $g.FillRectangle($crossBrush, [float]($cx - $cw/2), [float]($cy - $ch/2), $cw, $ch)
    $g.FillRectangle($crossBrush, [float]($cx - $ch/2), [float]($cy - $cw/2), $ch, $cw)

    # Paw 4 Toes (ovals rotated around top of pad)
    # Toe 1 (leftmost)
    $g.TranslateTransform([float](25 * $scale), [float](38 * $scale))
    $g.RotateTransform([float]-25.0)
    $toe1 = New-Object System.Drawing.RectangleF([float](-7 * $scale), [float](-10 * $scale), [float](14 * $scale), [float](20 * $scale))
    $g.FillEllipse($whiteBrush, $toe1)
    $g.ResetTransform()

    # Toe 2 (middle left)
    $g.TranslateTransform([float](41 * $scale), [float](30 * $scale))
    $g.RotateTransform([float]-8.0)
    $toe2 = New-Object System.Drawing.RectangleF([float](-7.5 * $scale), [float](-11 * $scale), [float](15 * $scale), [float](22 * $scale))
    $g.FillEllipse($whiteBrush, $toe2)
    $g.ResetTransform()

    # Toe 3 (middle right)
    $g.TranslateTransform([float](59 * $scale), [float](30 * $scale))
    $g.RotateTransform([float]8.0)
    $toe3 = New-Object System.Drawing.RectangleF([float](-7.5 * $scale), [float](-11 * $scale), [float](15 * $scale), [float](22 * $scale))
    $g.FillEllipse($whiteBrush, $toe3)
    $g.ResetTransform()

    # Toe 4 (rightmost)
    $g.TranslateTransform([float](75 * $scale), [float](38 * $scale))
    $g.RotateTransform([float]25.0)
    $toe4 = New-Object System.Drawing.RectangleF([float](-7 * $scale), [float](-10 * $scale), [float](14 * $scale), [float](20 * $scale))
    $g.FillEllipse($whiteBrush, $toe4)
    $g.ResetTransform()

    $g.Dispose()
    return $bmp
}

# Generate 256x256 PNG
$bmp256 = Draw-PetCareIcon 256
$bmp256.Save($pngPath, [System.Drawing.Imaging.ImageFormat]::Png)

# Generate multi-size ICO file (256, 128, 64, 48, 32, 16)
$sizes = @(256, 128, 64, 48, 32, 16)
$pngStreams = @()

foreach ($s in $sizes) {
    $img = Draw-PetCareIcon $s
    $ms = New-Object System.IO.MemoryStream
    $img.Save($ms, [System.Drawing.Imaging.ImageFormat]::Png)
    $pngStreams += $ms
}

$fs = [System.IO.File]::Create($icoPath)
$bw = New-Object System.IO.BinaryWriter($fs)

# ICONDIR header
$bw.Write([UInt16]0) # Reserved
$bw.Write([UInt16]1) # Resource Type: 1 = Icon
$bw.Write([UInt16]$sizes.Count) # Image count

$offset = 6 + ($sizes.Count * 16)

for ($i = 0; $i -lt $sizes.Count; $i++) {
    $s = $sizes[$i]
    $dataLen = $pngStreams[$i].Length
    
    # ICONDIRENTRY
    $bw.Write([Byte]($(if ($s -ge 256) { 0 } else { $s }))) # Width
    $bw.Write([Byte]($(if ($s -ge 256) { 0 } else { $s }))) # Height
    $bw.Write([Byte]0) # Color count
    $bw.Write([Byte]0) # Reserved
    $bw.Write([UInt16]1) # Color planes
    $bw.Write([UInt16]32) # Bit count
    $bw.Write([UInt32]$dataLen) # Bytes in resource
    $bw.Write([UInt32]$offset) # Image offset
    
    $offset += $dataLen
}

# Write PNG data for each entry
for ($i = 0; $i -lt $sizes.Count; $i++) {
    $bytes = $pngStreams[$i].ToArray()
    $bw.Write($bytes, 0, $bytes.Length)
    $pngStreams[$i].Dispose()
}

$bw.Flush()
$bw.Close()
$fs.Close()

Write-Host "Created $pngPath and $icoPath successfully."
