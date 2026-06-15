param(
    [Parameter(Mandatory = $true)]
    [string]$Owner,

    [Parameter(Mandatory = $true)]
    [string]$Repo,

    [string]$Branch = "main",

    [string]$RequiredCheckContext = "PR Check / gitleaks-pr-check"
)

$token = $env:GITHUB_TOKEN
if ([string]::IsNullOrWhiteSpace($token)) {
    throw "GITHUB_TOKEN is not set. Create a fine-grained token with repository administration write permission and set it in the environment."
}

$headers = @{
    Authorization = "Bearer $token"
    Accept        = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
}

$protection = @{
    required_status_checks = @{
        strict   = $true
        contexts = @($RequiredCheckContext)
    }
    enforce_admins = $true
    required_pull_request_reviews = @{
        dismiss_stale_reviews           = $true
        require_code_owner_reviews      = $false
        required_approving_review_count = 2
        require_last_push_approval      = $true
    }
    restrictions = $null
    allow_force_pushes = $false
    allow_deletions = $false
    block_creations = $false
    required_conversation_resolution = $true
    lock_branch = $false
    allow_fork_syncing = $false
}

$body = $protection | ConvertTo-Json -Depth 20
$uri = "https://api.github.com/repos/$Owner/$Repo/branches/$Branch/protection"

Write-Host "Applying branch protection for $Owner/$Repo on branch '$Branch'..."
Invoke-RestMethod -Method Put -Uri $uri -Headers $headers -Body $body -ContentType "application/json"
Write-Host "Done. Branch protection is active with: no direct push to $Branch, 2 approvals, and required CI check '$RequiredCheckContext'."
