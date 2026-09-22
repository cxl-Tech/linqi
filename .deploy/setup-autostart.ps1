# Register the two auto-start scheduled tasks for GoodsManage. Run ELEVATED.
#
# Why schtasks.exe + hand-written XML instead of Register-ScheduledTask:
#   - Register-ScheduledTask hung indefinitely in this environment (the process
#     sat idle at 0.75s CPU with no further output).
#   - schtasks.exe cannot express "no execution time limit" via flags, and its
#     built-in defaults impose PT72H, which would silently kill the JVM after
#     three days. Supplying our own XML sets <ExecutionTimeLimit>PT0S</ExecutionTimeLimit>.
#
# Why SYSTEM (S-1-5-18) instead of the logged-in user:
#   - no password stored, runs at boot with nobody logged on, no console window,
#     and it avoids the 0x80070520 credential-storage failure that Windows 11
#     Home cannot repair (Home has no secpol.msc).

$root = 'D:\tool\IDEA\linqi\GoodsManage'
$logDir = "$root\.deploy\logs"
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }
$log = "$logDir\setup-autostart.log"

# Add-Content flushes on every call, so the log shows exactly which step blocks.
# Start-Transcript was avoided precisely because it buffers.
function Log($m) {
    Add-Content -Path $log -Value ("{0}  {1}" -f (Get-Date -Format 'HH:mm:ss'), $m)
}

Set-Content -Path $log -Value "=== setup-autostart $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ==="

$tpl = @'
<?xml version="1.0" encoding="UTF-16"?>
<Task version="1.2" xmlns="http://schemas.microsoft.com/windows/2004/02/mit/task">
  <RegistrationInfo>
    <Description>__DESC__</Description>
  </RegistrationInfo>
  <Triggers>
    <BootTrigger>
      <Enabled>true</Enabled>
      <Delay>PT30S</Delay>
    </BootTrigger>
  </Triggers>
  <Principals>
    <Principal id="Author">
      <UserId>S-1-5-18</UserId>
      <RunLevel>HighestAvailable</RunLevel>
    </Principal>
  </Principals>
  <Settings>
    <MultipleInstancesPolicy>IgnoreNew</MultipleInstancesPolicy>
    <DisallowStartIfOnBatteries>false</DisallowStartIfOnBatteries>
    <StopIfGoingOnBatteries>false</StopIfGoingOnBatteries>
    <AllowHardTerminate>true</AllowHardTerminate>
    <StartWhenAvailable>true</StartWhenAvailable>
    <RunOnlyIfNetworkAvailable>false</RunOnlyIfNetworkAvailable>
    <IdleSettings>
      <StopOnIdleEnd>false</StopOnIdleEnd>
      <RestartOnIdle>false</RestartOnIdle>
    </IdleSettings>
    <AllowStartOnDemand>true</AllowStartOnDemand>
    <Enabled>true</Enabled>
    <Hidden>false</Hidden>
    <RunOnlyIfIdle>false</RunOnlyIfIdle>
    <WakeToRun>false</WakeToRun>
    <ExecutionTimeLimit>PT0S</ExecutionTimeLimit>
    <Priority>7</Priority>
    <RestartOnFailure>
      <Interval>PT1M</Interval>
      <Count>5</Count>
    </RestartOnFailure>
  </Settings>
  <Actions Context="Author">
    <Exec>
      <Command>cmd.exe</Command>
      <Arguments>/c "__CMD__"</Arguments>
      <WorkingDirectory>__WD__</WorkingDirectory>
    </Exec>
  </Actions>
</Task>
'@

$tasks = @(
    @{ Name = 'GoodsManage-Backend';
       Desc = 'GoodsManage Spring Boot backend on :8080';
       Cmd  = "$root\.deploy\run-backend.cmd";
       Wd   = "$root\backend" },
    @{ Name = 'GoodsManage-Gateway';
       Desc = 'GoodsManage Node gateway on :9000';
       Cmd  = "$root\.deploy\run-gateway.cmd";
       Wd   = "$root\.deploy" }
)

Log '--- registering ---'
foreach ($t in $tasks) {
    Log "register $($t.Name)"
    try {
        $xml = $tpl.Replace('__DESC__', $t.Desc).Replace('__CMD__', $t.Cmd).Replace('__WD__', $t.Wd)
        $xmlFile = Join-Path $env:TEMP "$($t.Name).xml"
        # schtasks requires the XML file to be UTF-16
        [System.IO.File]::WriteAllText($xmlFile, $xml, [System.Text.Encoding]::Unicode)
        Log "  xml -> $xmlFile"

        $out = & schtasks.exe /Create /TN $t.Name /XML $xmlFile /F 2>&1
        Log "  exit=$LASTEXITCODE  $out"
    }
    catch {
        Log "  FAILED: $($_.Exception.Message)"
    }
}

Log '--- starting ---'
foreach ($t in $tasks) {
    $out = & schtasks.exe /Run /TN $t.Name 2>&1
    Log "run $($t.Name) exit=$LASTEXITCODE  $out"
}

Start-Sleep -Seconds 6

Log '--- final state ---'
foreach ($t in $tasks) {
    $out = & schtasks.exe /Query /TN $t.Name /FO LIST 2>&1
    Log (($out | Select-String -Pattern 'TaskName|Status|Last Result|任务名|状态|上次运行结果') -join ' | ')
}

Log '=== done ==='
Write-Host "Log written to $log"
