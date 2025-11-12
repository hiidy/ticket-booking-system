output "cluster_name" {
  description = "GKE Cluster Name"
  value       = google_container_cluster.booking_cluster.name
}

output "cluster_endpoint" {
  description = "GKE Cluster Endpoint"
  value       = google_container_cluster.booking_cluster.endpoint
}

output "cluster_ca_certificate" {
  description = "GKE Cluster CA Certificate"
  value       = google_container_cluster.booking_cluster.master_auth[0].cluster_ca_certificate
  sensitive   = true
}

output "cluster_location" {
  description = "GKE Cluster Location"
  value       = google_container_cluster.booking_cluster.location
}

output "gcp_project_id" {
  description = "GCP Project ID"
  value       = var.gcp_project_id
}

output "gcp_region" {
  description = "GCP Region"
  value       = var.gcp_region
}

output "kubectl_config_command" {
  description = "Command to configure kubectl"
  value       = "gcloud container clusters get-credentials ${google_container_cluster.booking_cluster.name} --region ${var.gcp_region} --project ${var.gcp_project_id}"
}