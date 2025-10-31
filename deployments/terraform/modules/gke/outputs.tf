output "cluster_name" {
  description = "GKE Cluster Name"
  value       = google_container_cluster.load_test_cluster.name
}

output "cluster_endpoint" {
  description = "GKE Cluster Endpoint"
  value       = google_container_cluster.load_test_cluster.endpoint
}

output "cluster_ca_certificate" {
  description = "GKE Cluster CA Certificate"
  value       = google_container_cluster.load_test_cluster.master_auth[0].cluster_ca_certificate
  sensitive   = true
}

output "cluster_location" {
  description = "GKE Cluster Location"
  value       = google_container_cluster.load_test_cluster.location
}
