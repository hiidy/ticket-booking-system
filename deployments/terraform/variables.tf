variable "confluent_cloud_api_key" {
  description = "Confluent Cloud API Key"
  type        = string
  sensitive   = true
}

variable "confluent_cloud_api_secret" {
  description = "Confluent Cloud API Secret"
  type        = string
  sensitive   = true
}

variable "gcp_project_id" {
  description = "Your GCP Project ID"
  type        = string
}

variable "gcp_region" {
  description = "The GCP region for your resources"
  type        = string
  default     = "asia-northeast3"
}

variable "gcp_credentials_file" {
  description = "Path to GCP credentials file"
  type        = string
  default     = "~/.config/gcloud/application_default_credentials.json"
}