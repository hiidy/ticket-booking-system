terraform {
  required_providers {
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
