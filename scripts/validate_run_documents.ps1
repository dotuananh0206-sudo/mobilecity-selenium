param(
    [Parameter(Mandatory = $true)]
    [string]$Directory
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$patterns = @(
    '\u00C3[\u0080-\u00FF]',
    '\u00C2[\u0080-\u00FF]',
    '\u00C4[\u0080-\u00FF]',
    '\u00E1[\u00BA\u00BB][\u0080-\u00FF]',
    '\uFFFD',
    '\p{L}\?\p{L}'
)

if (-not (Test-Path -LiteralPath $Directory -PathType Container)) {
    throw "Run directory does not exist: $Directory"
}

$files = Get-ChildItem -LiteralPath $Directory -File -Recurse |
    Where-Object { $_.Extension -in @(".md", ".txt", ".csv", ".json", ".xml", ".log", ".docx", ".xlsx") }
$findings = @()

foreach ($file in $files) {
    $texts = @()
    if ($file.Extension -in @(".docx", ".xlsx")) {
        $archive = [System.IO.Compression.ZipFile]::OpenRead($file.FullName)
        try {
            foreach ($entry in $archive.Entries | Where-Object { $_.FullName -match '\.xml$' }) {
                $stream = $entry.Open()
                $reader = $null
                try {
                    $reader = [System.IO.StreamReader]::new(
                        $stream, [System.Text.Encoding]::UTF8, $true)
                    $xmlText = $reader.ReadToEnd()
                    [xml]$xml = $xmlText
                    $namespace = [System.Xml.XmlNamespaceManager]::new($xml.NameTable)
                    $namespace.AddNamespace(
                        "w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
                    $namespace.AddNamespace(
                        "s", "http://schemas.openxmlformats.org/spreadsheetml/2006/main")

                    if ($file.Extension -eq ".docx") {
                        $nodes = $xml.SelectNodes("//w:t", $namespace)
                    } else {
                        $nodes = $xml.SelectNodes("//s:t|//s:v", $namespace)
                    }
                    foreach ($node in $nodes) {
                        $texts += $node.InnerText
                    }
                } finally {
                    if ($reader) { $reader.Dispose() } else { $stream.Dispose() }
                }
            }
        } finally {
            $archive.Dispose()
        }
    } else {
        $texts += [System.IO.File]::ReadAllText(
            $file.FullName, [System.Text.Encoding]::UTF8)
    }

    $combinedText = $texts -join " "
    $combinedText = [regex]::Replace(
        $combinedText, '\?[A-Za-z][A-Za-z0-9_]*(?==)', '')
    foreach ($pattern in $patterns) {
        if ($combinedText -match $pattern) {
            $findings += [pscustomobject]@{
                File    = $file.FullName
                Pattern = $pattern
            }
        }
    }
}

if ($findings.Count -gt 0) {
    $findings | Format-Table -AutoSize | Out-String | Write-Error
    exit 1
}

Write-Output "UTF-8 text validation passed for $($files.Count) files under $Directory"
