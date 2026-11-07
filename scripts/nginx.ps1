param(
    [ValidateSet('start', 'reload', 'stop', 'test', 'status')]
    [string]$Action = 'start',
    [string]$NginxPath = $env:NGINX_EXE
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$prefix = $projectRoot.Replace('\', '/') + '/'
$runtimeDir = Join-Path $projectRoot '.runtime/nginx'
$pidFile = Join-Path $runtimeDir 'nginx.pid'

if (-not $NginxPath) {
    $command = Get-Command nginx.exe -ErrorAction SilentlyContinue
    if ($command) { $NginxPath = $command.Source }
    elseif (Test-Path -LiteralPath 'D:/nginx/hmdp/nginx-1.18.0/nginx.exe') {
        $NginxPath = 'D:/nginx/hmdp/nginx-1.18.0/nginx.exe'
    }
    else { throw '找不到 nginx.exe。请先设置 $env:NGINX_EXE 为 nginx.exe 的绝对路径。' }
}
$NginxPath = (Resolve-Path -LiteralPath $NginxPath).Path
$running = $null
if ((Test-Path -LiteralPath $pidFile) -and (Get-Item -LiteralPath $pidFile).Length -gt 0) {
    $nginxProcessId = [int](Get-Content -LiteralPath $pidFile -Raw).Trim()
    $candidate = Get-CimInstance Win32_Process -Filter "ProcessId = $nginxProcessId"
    if ($candidate) {
        if ($candidate.Name -ne 'nginx.exe' -or
            $candidate.CommandLine -notlike "*$prefix*" -or
            $candidate.CommandLine -notlike '*nginx/nginx.conf*') {
            throw 'PID 文件对应的进程不是本项目 Nginx，停止操作以避免影响其他服务。'
        }
        $running = $candidate
    }
}
if ($Action -eq 'status') {
    if ($running) { Write-Output "本项目 Nginx 正在运行，PID $($running.ProcessId)，http://127.0.0.1:8080" }
    else { Write-Output '本项目 Nginx 未运行' }
    exit 0
}
if ($Action -eq 'stop' -and -not $running) { Write-Output '本项目 Nginx 已停止'; exit 0 }
if ($Action -in @('start', 'reload') -and -not (Test-Path -LiteralPath (Join-Path $projectRoot 'dist/index.html'))) {
    throw '缺少前端构建文件，请先执行 npm run build。'
}
foreach ($directory in @($runtimeDir, "$runtimeDir/client_temp", "$runtimeDir/proxy_temp", "$runtimeDir/fastcgi_temp", "$runtimeDir/uwsgi_temp", "$runtimeDir/scgi_temp", "$projectRoot/logs")) {
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
}

Push-Location $projectRoot
try {
    & $NginxPath -p $prefix -c nginx/nginx.conf -t
    if ($LASTEXITCODE -ne 0) { throw 'Nginx 配置校验失败，未执行启动或重载。' }
    if ($Action -eq 'test') { exit 0 }
    if ($Action -eq 'stop') {
        & $NginxPath -p $prefix -c nginx/nginx.conf -s quit
        if ($LASTEXITCODE -ne 0) { throw 'Nginx 停止失败。' }
        $runningProcess = Get-Process -Id $running.ProcessId -ErrorAction SilentlyContinue
        if ($runningProcess) { $runningProcess.WaitForExit(10000) | Out-Null }
        if (Get-Process -Id $running.ProcessId -ErrorAction SilentlyContinue) { throw 'Nginx 尚未退出，请检查 error.log。' }
        Write-Output '本项目 Nginx 已停止'
        exit 0
    }
    if ($running) {
        & $NginxPath -p $prefix -c nginx/nginx.conf -s reload
        if ($LASTEXITCODE -ne 0) { throw 'Nginx 重载失败。' }
    }
    elseif ($Action -eq 'reload') { throw '本项目 Nginx 未运行，请执行 npm start。' }
    else {
        $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 8080)
        try { $listener.Start() }
        catch { throw '127.0.0.1:8080 已被占用，请先确认占用服务。' }
        finally { $listener.Stop() }
        Start-Process -FilePath $NginxPath -WorkingDirectory $projectRoot -WindowStyle Hidden `
            -ArgumentList @('-p', "`"$prefix`"", '-c', 'nginx/nginx.conf')
    }
    $healthy = $false
    for ($attempt = 0; $attempt -lt 20; $attempt++) {
        Start-Sleep -Milliseconds 250
        if (Test-Path -LiteralPath $pidFile) {
            try {
                $response = Invoke-WebRequest 'http://127.0.0.1:8080/healthz' -UseBasicParsing -TimeoutSec 2
                if ($response.StatusCode -eq 200 -and $response.Content.Trim() -eq 'nginx ok') { $healthy = $true; break }
            } catch { }
        }
    }
    if (-not $healthy) { throw 'Nginx 未通过健康检查，请查看 .runtime/nginx/error.log。' }
    Write-Output 'Nginx 已就绪：http://127.0.0.1:8080（Vue 静态文件 + 后端反向代理）'
}
finally { Pop-Location }
