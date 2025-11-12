output "nginx_ingress_namespace" {
  description = "Nginx Ingress Namespace"
  value       = helm_release.nginx_ingress.namespace
}

output "booking_platform_namespace" {
  description = "Booking Platform Namespace"
  value       = helm_release.booking_platform.namespace
}

output "nginx_ingress_ip_command" {
  description = "Command to get Nginx Ingress External IP"
  value       = "kubectl get svc nginx-ingress-ingress-nginx-controller -n ingress-nginx -o jsonpath='{.status.loadBalancer.ingress[0].ip}'"
}