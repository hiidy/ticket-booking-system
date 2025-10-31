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

# Service Account (유지)
resource "confluent_service_account" "app-manager" {
  display_name = "app-manager"
  description  = "Service account for load testing Kafka cluster"
}

# Role Bindings (유지)
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

# API Keys (유지)
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
