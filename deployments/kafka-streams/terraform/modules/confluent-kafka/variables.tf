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

variable "topic_partitions" {
  description = "Number of partitions for Kafka topics"
  type        = number
  default     = 3
}

variable "topic_replication_factor" {
  description = "Replication factor for Kafka topics"
  type        = number
  default     = 3
}

variable "topic_prefix" {
  description = "Prefix for Kafka topic names"
  type        = string
  default     = "prod"
}