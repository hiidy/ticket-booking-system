resource "confluent_environment" "load_test_env" {
  display_name = "LoadTestEnvironment"
  stream_governance {
    package = "ESSENTIALS"
  }
}

resource "confluent_kafka_cluster" "basic" {
  display_name = "load-test-cluster"
  availability = "SINGLE_ZONE"
  cloud        = "GCP"
  region       = var.gcp_region
  environment {
    id = confluent_environment.load_test_env.id
  }

  basic {}
}

data "confluent_schema_registry_cluster" "load_test_sr" {
  environment {
    id = confluent_environment.load_test_env.id
  }
  depends_on = [confluent_kafka_cluster.basic]
}

resource "confluent_service_account" "app-manager" {
  display_name = "app-manager"
  description  = "Service account for load testing Kafka cluster"
}

resource "confluent_role_binding" "app-manager-kafka-cluster-admin" {
  principal   = "User:${confluent_service_account.app-manager.id}"
  role_name   = "CloudClusterAdmin"
  crn_pattern = confluent_kafka_cluster.basic.rbac_crn
}

resource "confluent_role_binding" "app-manager-environment-admin" {
  principal   = "User:${confluent_service_account.app-manager.id}"
  role_name   = "EnvironmentAdmin"
  crn_pattern = confluent_environment.load_test_env.resource_name
}

resource "confluent_api_key" "kafka_key" {
  display_name = "kafka-load-test-key"
  description  = "API Key for Kafka load testing"

  owner {
    id          = confluent_service_account.app-manager.id
    api_version = confluent_service_account.app-manager.api_version
    kind        = confluent_service_account.app-manager.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.basic.id
    api_version = confluent_kafka_cluster.basic.api_version
    kind        = confluent_kafka_cluster.basic.kind
    environment {
      id = confluent_environment.load_test_env.id
    }
  }
  depends_on = [confluent_role_binding.app-manager-kafka-cluster-admin]
}

resource "confluent_api_key" "sr_key" {
  display_name = "sr-load-test-key"
  description  = "API Key for Schema Registry load testing"

  owner {
    id          = confluent_service_account.app-manager.id
    api_version = confluent_service_account.app-manager.api_version
    kind        = confluent_service_account.app-manager.kind
  }

  managed_resource {
    id          = data.confluent_schema_registry_cluster.load_test_sr.id
    api_version = data.confluent_schema_registry_cluster.load_test_sr.api_version
    kind        = data.confluent_schema_registry_cluster.load_test_sr.kind
    environment {
      id = confluent_environment.load_test_env.id
    }
  }
  depends_on = [confluent_role_binding.app-manager-environment-admin]
}

resource "confluent_kafka_topic" "booking_request" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name         = "${var.topic_prefix}.booking.request.v1"
  partitions_count   = var.topic_partitions
  rest_endpoint      = confluent_kafka_cluster.basic.rest_endpoint
  credentials {
    key    = confluent_api_key.kafka_key.id
    secret = confluent_api_key.kafka_key.secret
  }

  depends_on = [confluent_api_key.kafka_key]
}

resource "confluent_kafka_topic" "ticket_state" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name         = "${var.topic_prefix}.ticket.state.v1"
  partitions_count   = var.topic_partitions
  rest_endpoint      = confluent_kafka_cluster.basic.rest_endpoint
  credentials {
    key    = confluent_api_key.kafka_key.id
    secret = confluent_api_key.kafka_key.secret
  }

  depends_on = [confluent_api_key.kafka_key]
}

resource "confluent_kafka_topic" "booking_command" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name         = "${var.topic_prefix}.booking.command.v1"
  partitions_count   = var.topic_partitions
  rest_endpoint      = confluent_kafka_cluster.basic.rest_endpoint
  credentials {
    key    = confluent_api_key.kafka_key.id
    secret = confluent_api_key.kafka_key.secret
  }
  depends_on = [confluent_api_key.kafka_key]
}

resource "confluent_kafka_topic" "booking_result" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name         = "${var.topic_prefix}.booking.result.v1"
  partitions_count   = var.topic_partitions
  rest_endpoint      = confluent_kafka_cluster.basic.rest_endpoint
  credentials {
    key    = confluent_api_key.kafka_key.id
    secret = confluent_api_key.kafka_key.secret
  }
  depends_on = [confluent_api_key.kafka_key]
}

resource "confluent_kafka_topic" "booking_completed" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name         = "${var.topic_prefix}.booking.completed.v1"
  partitions_count   = var.topic_partitions
  rest_endpoint      = confluent_kafka_cluster.basic.rest_endpoint
  credentials {
    key    = confluent_api_key.kafka_key.id
    secret = confluent_api_key.kafka_key.secret
  }
  depends_on = [confluent_api_key.kafka_key]
}

resource "confluent_kafka_topic" "ticket_init" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name         = "${var.topic_prefix}.ticket.init.v1"
  partitions_count   = var.topic_partitions
  rest_endpoint      = confluent_kafka_cluster.basic.rest_endpoint
  credentials {
    key    = confluent_api_key.kafka_key.id
    secret = confluent_api_key.kafka_key.secret
  }

  depends_on = [confluent_api_key.kafka_key]
}

resource "confluent_role_binding" "app-manager-schema-registry" {
  principal   = "User:${confluent_service_account.app-manager.id}"
  role_name   = "ResourceOwner"
  crn_pattern = "${data.confluent_schema_registry_cluster.load_test_sr.resource_name}/subject=*"

  depends_on = [
    confluent_role_binding.app-manager-environment-admin,
    data.confluent_schema_registry_cluster.load_test_sr
  ]
}
