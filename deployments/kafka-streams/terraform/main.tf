
module "confluent_cloud" {
  source = "modules/confluent-kafka"

  confluent_cloud_api_key    = var.confluent_cloud_api_key
  confluent_cloud_api_secret = var.confluent_cloud_api_secret
  gcp_project_id             = var.gcp_project_id
  gcp_region                 = var.gcp_region
  gcp_vpc_name               = google_compute_network.load_test_vpc.name

  providers = {
    confluent = confluent
  }
}

module "gke_autopilot" {
  source = "modules/gke"

  gcp_project_id  = var.gcp_project_id
  gcp_region      = var.gcp_region
  gcp_vpc_name    = google_compute_network.load_test_vpc.name
  gcp_subnet_name = google_compute_subnetwork.load_test_subnet.name

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

resource "google_compute_subnetwork" "proxy_only_subnet" {
  name          = "load-test-proxy-subnet"
  ip_cidr_range = "10.13.0.0/24"
  region        = var.gcp_region
  network       = google_compute_network.load_test_vpc.id

  purpose       = "REGIONAL_MANAGED_PROXY"
  role          = "ACTIVE"

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
