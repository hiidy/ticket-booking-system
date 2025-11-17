resource "google_container_cluster" "booking_cluster" {
  name     = "booking-app-cluster"
  location = var.gcp_region

  enable_autopilot = true

  network    = google_compute_network.booking_vpc.name
  subnetwork = google_compute_subnetwork.booking_subnet.name

  release_channel {
    channel = "REGULAR"
  }

  ip_allocation_policy {
    cluster_secondary_range_name  = "pods-range"
    services_secondary_range_name = "services-range"
  }

  private_cluster_config {
    enable_private_nodes    = true
    enable_private_endpoint = false
  }

  deletion_protection = false
}

resource "google_compute_network" "booking_vpc" {
  name                    = "booking-app-vpc"
  auto_create_subnetworks = false
  description             = "VPC for booking platform"
}

resource "google_compute_subnetwork" "booking_subnet" {
  name          = "booking-app-subnet"
  ip_cidr_range = "10.20.0.0/20"
  region        = var.gcp_region
  network       = google_compute_network.booking_vpc.id

  secondary_ip_range {
    range_name    = "pods-range"
    ip_cidr_range = "10.21.0.0/16"
  }

  secondary_ip_range {
    range_name    = "services-range"
    ip_cidr_range = "10.22.0.0/20"
  }
}

resource "google_compute_router" "booking_router" {
  name    = "booking-app-router"
  network = google_compute_network.booking_vpc.id
  region  = var.gcp_region
}

resource "google_compute_router_nat" "booking_nat" {
  name                               = "booking-app-nat"
  router                             = google_compute_router.booking_router.name
  region                             = var.gcp_region
  nat_ip_allocate_option             = "AUTO_ONLY"
  source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

  log_config {
    enable = true
    filter = "ERRORS_ONLY"
  }
}