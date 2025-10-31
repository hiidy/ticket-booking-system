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

output "kafka_topics" {
  description = "List of created Kafka topics"
  value = {
    booking_request  = confluent_kafka_topic.booking_request.topic_name
    ticket_state     = confluent_kafka_topic.ticket_state.topic_name
    booking_command  = confluent_kafka_topic.booking_command.topic_name
    booking_result   = confluent_kafka_topic.booking_result.topic_name
    booking_completed = confluent_kafka_topic.booking_completed.topic_name
    ticket_init      = confluent_kafka_topic.ticket_init.topic_name
  }
}