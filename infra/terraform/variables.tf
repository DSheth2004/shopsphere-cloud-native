variable "aws_region" {
  description = "AWS region for every resource."
  type        = string
  default     = "ap-south-1"
}

variable "instance_type" {
  description = "Single k3s node. t3.medium (4 GiB) fits all 13 pods with the 2 GiB swap file added at boot."
  type        = string
  default     = "t3.medium"
}

variable "root_volume_gb" {
  description = "gp3 root volume size in GiB."
  type        = number
  default     = 20
}

variable "repo_url" {
  description = "Git repository the node clones its Kubernetes manifests from."
  type        = string
  default     = "https://github.com/DSheth2004/shopsphere-cloud-native.git"
}

variable "git_branch" {
  description = "Branch deployed to the node."
  type        = string
  default     = "master"
}

variable "github_repository" {
  description = "owner/repo allowed to deploy through GitHub Actions OIDC."
  type        = string
  default     = "DSheth2004/shopsphere-cloud-native"
}

variable "create_github_oidc_provider" {
  description = "Set to false if this AWS account already has the token.actions.githubusercontent.com OIDC provider."
  type        = bool
  default     = true
}

variable "ssh_cidr" {
  description = "Optional CIDR allowed to SSH (port 22). Leave empty and use SSM Session Manager instead."
  type        = string
  default     = ""
}

variable "budget_email" {
  description = "Email address that receives AWS budget alerts."
  type        = string
}

variable "budget_limit_usd" {
  description = "Monthly cost budget in USD."
  type        = number
  default     = 80
}

variable "nightly_stop_enabled" {
  description = "Stop the instance every night to save credits; start it again before a demo."
  type        = bool
  default     = true
}

variable "nightly_stop_cron" {
  description = "EventBridge Scheduler cron expression for the nightly stop."
  type        = string
  default     = "cron(0 23 * * ? *)"
}

variable "nightly_stop_timezone" {
  description = "Time zone for the nightly stop schedule."
  type        = string
  default     = "Asia/Kolkata"
}
