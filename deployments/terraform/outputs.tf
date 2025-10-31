output "kafka_bootstrap_endpoint" {
  description = "Kafka cluster bootstrap endpoint"
  value       = module.confluent_cloud.kafka_bootstrap_endpoint
}

output "kafka_api_key" {
  description = "Kafka API Key"
  value       = module.confluent_cloud.kafka_api_key
  sensitive   = true
}

output "kafka_api_secret" {
  description = "Kafka API Secret"
  value       = module.confluent_cloud.kafka_api_secret
  sensitive   = true
}

output "schema_registry_endpoint_url" {
  description = "Schema Registry endpoint URL"
  value       = module.confluent_cloud.schema_registry_endpoint_url
}

output "schema_registry_api_key" {
  description = "Schema Registry API Key"
  value       = module.confluent_cloud.schema_registry_api_key
  sensitive   = true
}

output "schema_registry_api_secret" {
  description = "Schema Registry API Secret"
  value       = module.confluent_cloud.schema_registry_api_secret
  sensitive   = true
}

# GKE outputs
output "gke_cluster_name" {
  description = "GKE Cluster Name"
  value       = module.gke_autopilot.cluster_name
}

output "gke_cluster_endpoint" {
  description = "GKE Cluster Endpoint"
  value       = module.gke_autopilot.cluster_endpoint
}

output "gke_cluster_location" {
  description = "GKE Cluster Location"
  value       = module.gke_autopilot.cluster_location
}
