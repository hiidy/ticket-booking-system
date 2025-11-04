resource "google_container_cluster" "load_test_cluster" {
  name     = "load-test-autopilot-cluster"
  location = var.gcp_region

  enable_autopilot = true

  network    = var.gcp_vpc_name
  subnetwork = var.gcp_subnet_name

  release_channel {
    channel = "REGULAR"
  }

  gateway_api_config {
    channel = "CHANNEL_STANDARD"
  }

  ip_allocation_policy {
    cluster_secondary_range_name  = "pods-range"
    services_secondary_range_name = "services-range"
  }

  private_cluster_config {
    enable_private_nodes = true
    enable_private_endpoint = false
  }

  deletion_protection = false
}
