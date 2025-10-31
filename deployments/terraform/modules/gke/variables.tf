variable "gcp_project_id" {
  description = "GCP Project ID"
  type        = string
}

variable "gcp_region" {
  description = "GCP Region"
  type        = string
}

variable "gcp_vpc_name" {
  description = "GCP VPC Name"
  type        = string
}

variable "gcp_subnet_name" {
  description = "GCP Subnet Name"
  type        = string
}

variable "kafka_bootstrap_endpoint" {
  description = "Kafka bootstrap endpoint"
  type        = string
}

variable "kafka_api_key" {
  description = "Kafka API key"
  type        = string
  sensitive   = true
}

variable "kafka_api_secret" {
  description = "Kafka API secret"
  type        = string
  sensitive   = true
}

variable "schema_registry_url" {
  description = "Schema Registry URL"
  type        = string
}

variable "schema_registry_api_key" {
  description = "Schema Registry API key"
  type        = string
  sensitive   = true
}

variable "schema_registry_secret" {
  description = "Schema Registry API secret"
  type        = string
  sensitive   = true
}