terraform {
  required_version = ">= 1.0"
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.50.0"
    }
    google = {
      source  = "hashicorp/google"
      version = "~> 7.9"
    }
    google-beta = {
      source  = "hashicorp/google-beta"
      version = "~> 7.9"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
  }
}

# Confluent Provider
provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
}

# GCP Provider
provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

provider "google-beta" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

# Kubernetes Provider (GKE 클러스터 생성 후 설정)
provider "kubernetes" {
  config_path = "~/.kube/config"  # 또는 동적으로 설정
}
