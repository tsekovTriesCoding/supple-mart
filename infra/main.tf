# Azure Infrastructure for SuppleMart
# Simplified version with containerized MySQL (no Azure MySQL Flexible Server)

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.100"
    }
    azurecaf = {
      source  = "aztfmod/azurecaf"
      version = "~> 1.2"
    }
  }
}

provider "azurerm" {
  features {}
}

provider "azurecaf" {}

# Variables

variable "environmentName" {
  type        = string
  description = "Name of the environment (from azd)"
}

variable "location" {
  type        = string
  description = "Azure region for resources"
}

variable "resourceGroupName" {
  type        = string
  description = "Name of the resource group"
  default     = ""
}

variable "db_admin_username" {
  type        = string
  description = "MySQL admin username"
  default     = "supplemart_admin"
}

variable "db_admin_password" {
  type        = string
  description = "MySQL admin password"
  sensitive   = true
}

variable "jwt_secret" {
  type        = string
  description = "JWT secret for authentication"
  sensitive   = true
}

variable "jwt_expiration" {
  type        = string
  description = "JWT token expiration in milliseconds"
  default     = "86400000"
}

variable "jwt_refresh_expiration" {
  type        = string
  description = "JWT refresh token expiration in milliseconds"
  default     = "604800000"
}

variable "google_client_id" {
  type        = string
  description = "Google OAuth2 client ID"
  default     = ""
}

variable "google_client_secret" {
  type        = string
  description = "Google OAuth2 client secret"
  sensitive   = true
  default     = ""
}

variable "github_oauth_client_id" {
  type        = string
  description = "GitHub OAuth2 client ID"
  default     = ""
}

variable "github_oauth_client_secret" {
  type        = string
  description = "GitHub OAuth2 client secret"
  sensitive   = true
  default     = ""
}

variable "cloudinary_cloud_name" {
  type        = string
  description = "Cloudinary cloud name"
  default     = ""
}

variable "cloudinary_api_key" {
  type        = string
  description = "Cloudinary API key"
  default     = ""
}

variable "cloudinary_api_secret" {
  type        = string
  description = "Cloudinary API secret"
  sensitive   = true
  default     = ""
}

variable "stripe_secret_key" {
  type        = string
  description = "Stripe secret key"
  sensitive   = true
  default     = ""
}

variable "stripe_webhook_secret" {
  type        = string
  description = "Stripe webhook secret"
  sensitive   = true
  default     = ""
}

variable "stripe_publishable_key" {
  type        = string
  description = "Stripe publishable key for frontend"
  default     = ""
}

variable "mail_host" {
  type        = string
  description = "SMTP mail host"
  default     = "smtp.gmail.com"
}

variable "mail_port" {
  type        = string
  description = "SMTP mail port"
  default     = "587"
}

variable "mail_username" {
  type        = string
  description = "SMTP mail username"
  default     = ""
}

variable "mail_password" {
  type        = string
  description = "SMTP mail password"
  sensitive   = true
  default     = ""
}

# Data Sources

data "azurerm_client_config" "current" {}

# Resource Group

resource "azurerm_resource_group" "main" {
  name     = var.resourceGroupName != "" ? var.resourceGroupName : "rg-${var.environmentName}"
  location = var.location

  tags = {
    "azd-env-name" = var.environmentName
  }
}

# CAF Naming

resource "azurecaf_name" "container_registry" {
  name          = var.environmentName
  resource_type = "azurerm_container_registry"
  clean_input   = true
}

resource "azurecaf_name" "log_analytics" {
  name          = var.environmentName
  resource_type = "azurerm_log_analytics_workspace"
  clean_input   = true
}

resource "azurecaf_name" "container_app_env" {
  name          = var.environmentName
  resource_type = "azurerm_container_app_environment"
  clean_input   = true
}

resource "azurecaf_name" "managed_identity" {
  name          = var.environmentName
  resource_type = "azurerm_user_assigned_identity"
  clean_input   = true
}

# User-Assigned Managed Identity

resource "azurerm_user_assigned_identity" "main" {
  name                = azurecaf_name.managed_identity.result
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
}

# Log Analytics Workspace

resource "azurerm_log_analytics_workspace" "main" {
  name                = azurecaf_name.log_analytics.result
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "PerGB2018"
  retention_in_days   = 30
}

# Application Insights

resource "azurerm_application_insights" "main" {
  name                = "appi-${var.environmentName}"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  workspace_id        = azurerm_log_analytics_workspace.main.id
  application_type    = "web"
}

# Azure Container Registry

resource "azurerm_container_registry" "main" {
  name                = azurecaf_name.container_registry.result
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Basic"
  admin_enabled       = false
}

# AcrPull role assignment for managed identity
resource "azurerm_role_assignment" "acr_pull" {
  scope                            = azurerm_container_registry.main.id
  role_definition_id               = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/providers/Microsoft.Authorization/roleDefinitions/7f951dda-4ed3-4680-a7ca-43fe172d538d"
  principal_id                     = azurerm_user_assigned_identity.main.principal_id
  principal_type                   = "ServicePrincipal"
  skip_service_principal_aad_check = true
}

# Container Apps Environment

resource "azurerm_container_app_environment" "main" {
  name                       = azurecaf_name.container_app_env.result
  resource_group_name        = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
}

# MySQL Container App (ephemeral - data not persisted across restarts)
# Note: Azure Files has permission issues with MySQL. For production, use Azure Database for MySQL.

resource "azurerm_container_app" "mysql" {
  name                         = "ca-mysql-${var.environmentName}"
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode                = "Single"

  template {
    min_replicas = 1
    max_replicas = 1

    container {
      name   = "mysql"
      image  = "mysql:8.0"
      cpu    = 1.0
      memory = "2Gi"

      env {
        name  = "MYSQL_ROOT_PASSWORD"
        value = var.db_admin_password
      }

      env {
        name  = "MYSQL_DATABASE"
        value = "supplemart_db"
      }

      env {
        name  = "MYSQL_USER"
        value = var.db_admin_username
      }

      env {
        name  = "MYSQL_PASSWORD"
        value = var.db_admin_password
      }
    }
  }

  ingress {
    external_enabled = false
    target_port      = 3306
    transport        = "tcp"
    exposed_port     = 3306

    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }
}

# Frontend Container App (created first for backend URL reference)

resource "azurerm_container_app" "frontend" {
  name                         = "ca-frontend-${var.environmentName}"
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode                = "Single"

  tags = {
    "azd-service-name" = "frontend"
  }

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.main.id]
  }

  registry {
    server   = azurerm_container_registry.main.login_server
    identity = azurerm_user_assigned_identity.main.id
  }

  ingress {
    external_enabled = true
    target_port      = 80
    transport        = "auto"

    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }

  template {
    min_replicas = 0
    max_replicas = 10

    container {
      name   = "frontend"
      image  = "mcr.microsoft.com/azuredocs/containerapps-helloworld:latest"
      cpu    = 0.5
      memory = "1Gi"
    }
  }

  depends_on = [azurerm_role_assignment.acr_pull]

  lifecycle {
    ignore_changes = [template[0].container[0].image]
  }
}

# Backend Container App

resource "azurerm_container_app" "backend" {
  name                         = "ca-backend-${var.environmentName}"
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode                = "Single"

  tags = {
    "azd-service-name" = "backend"
  }

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.main.id]
  }

  registry {
    server   = azurerm_container_registry.main.login_server
    identity = azurerm_user_assigned_identity.main.id
  }

  ingress {
    external_enabled = true
    target_port      = 8080
    transport        = "auto"

    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }

  template {
    min_replicas = 1
    max_replicas = 10

    container {
      name   = "backend"
      image  = "mcr.microsoft.com/azuredocs/containerapps-helloworld:latest"
      cpu    = 1.0
      memory = "2Gi"

      env {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:mysql://ca-mysql-${var.environmentName}:3306/supplemart_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
      }

      env {
        name  = "DB_USERNAME"
        value = var.db_admin_username
      }

      env {
        name  = "DB_PASSWORD"
        value = var.db_admin_password
      }

      env {
        name  = "JWT_SECRET"
        value = var.jwt_secret
      }

      env {
        name  = "JWT_EXPIRATION"
        value = var.jwt_expiration
      }

      env {
        name  = "JWT_REFRESH_EXPIRATION"
        value = var.jwt_refresh_expiration
      }

      env {
        name  = "GOOGLE_CLIENT_ID"
        value = var.google_client_id
      }

      env {
        name  = "GOOGLE_CLIENT_SECRET"
        value = var.google_client_secret
      }

      env {
        name  = "GITHUB_CLIENT_ID"
        value = var.github_oauth_client_id
      }

      env {
        name  = "GITHUB_CLIENT_SECRET"
        value = var.github_oauth_client_secret
      }

      env {
        name  = "CLOUDINARY_CLOUD_NAME"
        value = var.cloudinary_cloud_name
      }

      env {
        name  = "CLOUDINARY_API_KEY"
        value = var.cloudinary_api_key
      }

      env {
        name  = "CLOUDINARY_API_SECRET"
        value = var.cloudinary_api_secret
      }

      env {
        name  = "STRIPE_SECRET_KEY"
        value = var.stripe_secret_key
      }

      env {
        name  = "STRIPE_WEBHOOK_SECRET"
        value = var.stripe_webhook_secret
      }

      env {
        name  = "MAIL_HOST"
        value = var.mail_host
      }

      env {
        name  = "MAIL_PORT"
        value = var.mail_port
      }

      env {
        name  = "MAIL_USERNAME"
        value = var.mail_username
      }

      env {
        name  = "MAIL_PASSWORD"
        value = var.mail_password
      }

      env {
        name  = "FRONTEND_URL"
        value = "https://${azurerm_container_app.frontend.ingress[0].fqdn}"
      }

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "prod"
      }

      env {
        name  = "APPLICATIONINSIGHTS_CONNECTION_STRING"
        value = azurerm_application_insights.main.connection_string
      }
    }
  }

  depends_on = [
    azurerm_role_assignment.acr_pull,
    azurerm_container_app.mysql,
    azurerm_container_app.frontend
  ]

  lifecycle {
    ignore_changes = [template[0].container[0].image]
  }
}
