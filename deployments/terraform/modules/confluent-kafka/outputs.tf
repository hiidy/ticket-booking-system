output "kafka_bootstrap_endpoint" {
  description = "Kafka cluster bootstrap endpoint"
  value       = confluent_kafka_cluster.basic.bootstrap_endpoint
}

output "kafka_api_key" {
  description = "Kafka API Key"
  value       = confluent_api_key.kafka_key.id
}

output "kafka_api_secret" {
  description = "Kafka API Secret"
  value       = confluent_api_key.kafka_key.secret
  sensitive   = true
}

output "schema_registry_endpoint_url" {
  description = "Schema Registry endpoint URL"
  value       = data.confluent_schema_registry_cluster.load_test_sr.rest_endpoint
}

output "schema_registry_api_key" {
  description = "Schema Registry API Key"
  value       = confluent_api_key.sr_key.id
}

output "schema_registry_api_secret" {
  description = "Schema Registry API Secret"
  value       = confluent_api_key.sr_key.secret
  sensitive   = true
}