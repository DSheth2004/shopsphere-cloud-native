terraform { required_version = ">= 1.6.0" required_providers { aws = { source = "hashicorp/aws", version = "~> 5.0" } } }
provider "aws" { region = var.aws_region }
variable "aws_region" { type = string default = "ap-south-1" }
variable "budget_email" { type = string }
resource "aws_s3_bucket" "reports" { bucket_prefix = "shopsphere-reports-" }
resource "aws_budgets_budget" "cap" { name = "shopsphere-capstone" budget_type = "COST" limit_amount = "80" limit_unit = "USD" time_unit = "MONTHLY" notification { comparison_operator = "GREATER_THAN" threshold = 80 threshold_type = "PERCENTAGE" notification_type = "ACTUAL" subscriber_email_addresses = [var.budget_email] } }
