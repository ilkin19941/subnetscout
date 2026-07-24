<#
    Сборка SubNet Scout в MSIX для Microsoft Store.
    Запускать на Windows. Требуется:
      - JDK 21+ (с jpackage и jlink в PATH)
      - Windows SDK (makeappx.exe, signtool.exe — обычно в
        C:\Program Files (x86)\Windows Kits\10\bin\<version>\x64)
      - Собранный jar: сначала выполните `gradlew win64` в корне проекта —
        это штатный таск проекта, он соберёт fat-jar с нужными Windows/SWT
        нативными библиотеками (см. build.gradle, packageTask('win64')).
        Результат появится в build/libs/subnetscout-<version>-win.jar

    Использование:
      .\build-msix.ps1 -Version 1.0.0.0 -JarPath ..\..\build\libs\subnetscout-1.0.0.0-win.jar
#>

param(
    [Parameter(Mandatory=$true)][string]$Version,
    [Parameter(Mandatory=$true)][string]$JarPath,
    [string]$WindowsKitBin = "C:\Program Files (x86)\Windows Kits\10\bin\10.0.22621.0\x64"
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$stage = Join-Path $root "stage"
$appDir = Join-Path $stage "app"

Write-Host "== 1. Очистка staging-директории ==" -ForegroundColor Cyan
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
New-Item -ItemType Directory -Path $appDir | Out-Null

Write-Host "== 2a. jlink: урезанный кастомный JRE (переиспользуем модули проекта) ==" -ForegroundColor Cyan
$jreOut = Join-Path $stage "custom-jre"
# Тот же набор модулей, что в build.gradle: java.base, java.prefs, java.logging, jdk.crypto.ec, java.desktop
jlink --output $jreOut --vm=client --compress=2 --no-header-files --no-man-pages --strip-debug `
    --add-modules java.base,java.prefs,java.logging,jdk.crypto.ec,java.desktop

Write-Host "== 2b. jpackage: app-image (jar + урезанный JRE) ==" -ForegroundColor Cyan
# --type app-image создаёт папку с exe и встроенным JRE, без инсталлятора —
# это именно то, что нам нужно как содержимое MSIX-пакета.
# --runtime-image указывает на jlink-рантайм выше вместо полного JDK по умолчанию.
jpackage `
    --type app-image `
    --runtime-image $jreOut `
    --input (Split-Path $JarPath) `
    --main-jar (Split-Path $JarPath -Leaf) `
    --main-class net.azib.ipscan.Main `
    --name subnetscout `
    --app-version $Version `
    --icon "$root\..\..\resources\images\icon.ico" `
    --dest $appDir

# jpackage создаёт подпапку app\subnetscout — приводим структуру к той,
# что ожидает Package.appxmanifest (app\subnetscout.exe)
Move-Item "$appDir\subnetscout\*" $appDir -Force
Remove-Item "$appDir\subnetscout" -Recurse -Force

Write-Host "== 3. Копируем манифест и ассеты в staging ==" -ForegroundColor Cyan
Copy-Item "$root\Package.appxmanifest" $stage
Copy-Item "$root\Assets" $stage -Recurse

# Подставляем реальный номер версии в манифест
(Get-Content "$stage\Package.appxmanifest") `
    -replace 'Version="1\.0\.0\.0"', "Version=`"$Version`"" |
    Set-Content "$stage\Package.appxmanifest"

Write-Host "== 4. Сборка .msix через makeappx ==" -ForegroundColor Cyan
$makeappx = Join-Path $WindowsKitBin "makeappx.exe"
$outMsix = Join-Path $root "SubNetScout-$Version.msix"
& $makeappx pack /d $stage /p $outMsix /overwrite

Write-Host "== Готово: $outMsix ==" -ForegroundColor Green
Write-Host "Для локального теста установки (нужен self-signed сертификат):" -ForegroundColor Yellow
Write-Host "  signtool sign /fd SHA256 /a /f yourcert.pfx /p password `"$outMsix`""
Write-Host "  Add-AppxPackage -Path `"$outMsix`""
