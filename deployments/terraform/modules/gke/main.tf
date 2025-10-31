resource "google_container_cluster" "load_test_cluster" {
  name     = "load-test-autopilot-cluster"
  location = var.gcp_region

  enable_autopilot = true

  network    = var.gcp_vpc_name
  subnetwork = var.gcp_subnet_name

  ip_allocation_policy {
    cluster_secondary_range_name  = "pods-range"
    services_secondary_range_name = "services-range"
  }

  deletion_protection = false
}
