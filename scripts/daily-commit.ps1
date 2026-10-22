# Daily Commit & Activity Runner for Windows PowerShell
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$date = Get-Date -Format "yyyy-MM-dd"
$timestamp = Get-Date -Format "yyyy-MM-ddTHH:mm:ssZ"
$statsDir = Join-Path $repoRoot "stats"

if (-not (Test-Path $statsDir)) {
    New-Item -ItemType Directory -Path $statsDir | Out-Null
}

# Run SysPulse from target/allday.jar if available
$jdk = "$env:LOCALAPPDATA\Programs\jdk-21\bin\java.exe"
$jarPath = Join-Path $repoRoot "target\allday.jar"

if ((Test-Path $jdk) -and (Test-Path $jarPath)) {
    & $jdk -jar $jarPath syspulse --json | Out-File -FilePath "$statsDir\syspulse-$date.json" -Encoding utf8
}

$ledgerLine = "{""date"":""$date"",""timestamp"":""$timestamp"",""source"":""local-agent""}"
Add-Content -Path "$statsDir\activity-ledger.jsonl" -Value $ledgerLine

# Update README badge
$readmePath = Join-Path $repoRoot "README.md"
if (Test-Path $readmePath) {
    $content = Get-Content $readmePath -Raw
    $updated = $content -replace "<!-- LAST_PULSE_START -->.*<!-- LAST_PULSE_END -->", "<!-- LAST_PULSE_START -->⭐ Last Active Pulse: ``$date $timestamp`` <!-- LAST_PULSE_END -->"
    Set-Content -Path $readmePath -Value $updated -NoNewline
}

git add stats README.md
$status = git status --porcelain
if ($status) {
    git commit -m "chore(pulse): daily developer activity sync [$date]"
    Write-Host "Committed daily pulse for $date"
    
    # Push if remote exists
    $remotes = git remote
    if ($remotes) {
        git push
        Write-Host "Pushed to GitHub!"
    } else {
        Write-Host "No git remote configured yet. Run 'git remote add origin <url>' to push automatically."
    }
} else {
    Write-Host "No changes to commit today."
}
