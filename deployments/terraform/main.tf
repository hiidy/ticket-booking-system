
module "confluent_cloud" {
  source = "./modules/confluent-kafka"

  confluent_cloud_api_key    = var.confluent_cloud_api_key
  confluent_cloud_api_secret = var.confluent_cloud_api_secret
  gcp_project_id             = var.gcp_project_id
  gcp_region                 = var.gcp_region
  gcp_vpc_name               = google_compute_network.load_test_vpc.name # GKE VPC 사용

  providers = {
    confluent = confluent
  }
}

module "gke_autopilot" {
  source = "./modules/gke"

  gcp_project_id = var.gcp_project_id
  gcp_region     = var.gcp_region
  gcp_vpc_name   = google_compute_network.load_test_vpc.name
  gcp_subnet_name = google_compute_subnetwork.load_test_subnet.name

  kafka_bootstrap_endpoint = module.confluent_cloud.kafka_bootstrap_endpoint
  kafka_api_key            = module.confluent_cloud.kafka_api_key
  kafka_api_secret         = module.confluent_cloud.kafka_api_secret
  schema_registry_url      = module.confluent_cloud.schema_registry_endpoint_url
  schema_registry_api_key  = module.confluent_cloud.schema_registry_api_key
  schema_registry_secret   = module.confluent_cloud.schema_registry_api_secret

  depends_on = [module.confluent_cloud]
}

resource "google_compute_network" "load_test_vpc" {
  name                    = "load-test-vpc"
  auto_create_subnetworks = false
  description             = "Shared VPC for Confluent Cloud and GKE"
}

resource "google_compute_subnetwork" "load_test_subnet" {
  name          = "load-test-subnet"
  ip_cidr_range = "10.10.0.0/20"
  region        = var.gcp_region
  network       = google_compute_network.load_test_vpc.id

  secondary_ip_range {
    range_name    = "pods-range"
    ip_cidr_range = "10.11.0.0/16"
  }

  secondary_ip_range {
    range_name    = "services-range"
    ip_cidr_range = "10.12.0.0/20"
  }
}

resource "google_compute_router" "router" {
  name    = "load-test-router"
  network = google_compute_network.load_test_vpc.id
  region  = var.gcp_region
}

resource "google_compute_router_nat" "nat" {
  name                               = "load-test-nat"
  router                             = google_compute_router.router.name
  region                             = var.gcp_region
  nat_ip_allocate_option             = "AUTO_ONLY"
  source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

  log_config {
    enable = true
    filter = "ERRORS_ONLY"
  }
}
