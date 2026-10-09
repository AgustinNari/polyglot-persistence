# Import only the supported literal values; never execute the contents of .env.
$polyglotEnvPath = Join-Path $PSScriptRoot '.env'
if (-not (Test-Path -LiteralPath $polyglotEnvPath)) {
    throw 'Create infra/.env from infra/.env.example before loading credentials.'
}

$polyglotEnvValues = @{}
foreach ($polyglotEnvLine in Get-Content -LiteralPath $polyglotEnvPath) {
    if ($polyglotEnvLine -match '^\s*(#.*)?$') { continue }
    if ($polyglotEnvLine -notmatch "^(MSSQL_SA_PASSWORD|DEMO_USER_PASSWORD)='([^']*)'$") {
        throw 'Invalid infra/.env format: use the single-quoted assignments from .env.example.'
    }
    if ($polyglotEnvValues.ContainsKey($Matches[1])) {
        throw 'Duplicate variable in infra/.env.'
    }
    $polyglotEnvValues[$Matches[1]] = $Matches[2]
}
if ([string]::IsNullOrWhiteSpace($polyglotEnvValues['MSSQL_SA_PASSWORD'])) {
    throw 'Set a nonempty MSSQL_SA_PASSWORD in infra/.env.'
}
foreach ($polyglotEnvName in 'MSSQL_SA_PASSWORD', 'DEMO_USER_PASSWORD') {
    [Environment]::SetEnvironmentVariable($polyglotEnvName, $polyglotEnvValues[$polyglotEnvName], 'Process')
}
