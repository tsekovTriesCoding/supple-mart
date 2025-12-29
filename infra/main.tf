# Azure Infrastructure for SuppleMart
# Deployed using Azure Developer CLI (azd) with Terraform

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
    azapi = {
      source  = "azure/azapi"
      version = "~> 2.0"
    }
  }
}

provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy = false
    }
  }
}

provider "azurecaf" {}
provider "azapi" {}

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

resource "azurecaf_name" "key_vault" {
  name          = var.environmentName
  resource_type = "azurerm_key_vault"
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

resource "azurecaf_name" "mysql_server" {
  name          = var.environmentName
  resource_type = "azurerm_mysql_flexible_server"
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

# Azure Key Vault

resource "azurerm_key_vault" "main" {
  name                       = azurecaf_name.key_vault.result
  resource_group_name        = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 7
  purge_protection_enabled   = false
  enable_rbac_authorization  = true
  public_network_access_enabled = true
}

# Key Vault Secrets Officer role for deployer (ServicePrincipal from GitHub Actions)
resource "azurerm_role_assignment" "kv_secrets_officer_deployer" {
  scope                = azurerm_key_vault.main.id
  role_definition_id   = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/providers/Microsoft.Authorization/roleDefinitions/b86a8fe4-44ce-4948-aee5-eccb2c155cd7"
  principal_id         = data.azurerm_client_config.current.object_id
}

# Key Vault Secrets User role for managed identity
resource "azurerm_role_assignment" "kv_secrets_user" {
  scope                = azurerm_key_vault.main.id
  role_definition_id   = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/providers/Microsoft.Authorization/roleDefinitions/4633458b-17de-408a-b874-0445c86b69e6"
  principal_id         = azurerm_user_assigned_identity.main.principal_id
  principal_type       = "ServicePrincipal"

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

# Key Vault Secrets

resource "azurerm_key_vault_secret" "db_password" {
  name         = "db-password"
  value        = var.db_admin_password
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

resource "azurerm_key_vault_secret" "jwt_secret" {
  name         = "jwt-secret"
  value        = var.jwt_secret
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

resource "azurerm_key_vault_secret" "google_client_secret" {
  count        = var.google_client_secret != "" ? 1 : 0
  name         = "google-client-secret"
  value        = var.google_client_secret
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

resource "azurerm_key_vault_secret" "github_oauth_client_secret" {
  count        = var.github_oauth_client_secret != "" ? 1 : 0
  name         = "github-oauth-client-secret"
  value        = var.github_oauth_client_secret
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

resource "azurerm_key_vault_secret" "cloudinary_api_secret" {
  count        = var.cloudinary_api_secret != "" ? 1 : 0
  name         = "cloudinary-api-secret"
  value        = var.cloudinary_api_secret
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

resource "azurerm_key_vault_secret" "stripe_secret_key" {
  count        = var.stripe_secret_key != "" ? 1 : 0
  name         = "stripe-secret-key"
  value        = var.stripe_secret_key
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

resource "azurerm_key_vault_secret" "stripe_webhook_secret" {
  count        = var.stripe_webhook_secret != "" ? 1 : 0
  name         = "stripe-webhook-secret"
  value        = var.stripe_webhook_secret
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

resource "azurerm_key_vault_secret" "mail_password" {
  count        = var.mail_password != "" ? 1 : 0
  name         = "mail-password"
  value        = var.mail_password
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

# Azure Database for MySQL Flexible Server

resource "azurerm_mysql_flexible_server" "main" {
  name                   = azurecaf_name.mysql_server.result
  resource_group_name    = azurerm_resource_group.main.name
  location               = azurerm_resource_group.main.location
  administrator_login    = var.db_admin_username
  administrator_password = var.db_admin_password
  sku_name               = "B_Standard_B1ms"
  version                = "8.0.21"
  zone                   = "1"

  storage {
    size_gb = 20
  }

  backup_retention_days = 7
}

# MySQL Firewall rule to allow Azure services
resource "azurerm_mysql_flexible_server_firewall_rule" "allow_azure" {
  name                = "AllowAzureServices"
  resource_group_name = azurerm_resource_group.main.name
  server_name         = azurerm_mysql_flexible_server.main.name
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "0.0.0.0"
}

# MySQL Database
resource "azurerm_mysql_flexible_database" "main" {
  name                = "supplemart_db"
  resource_group_name = azurerm_resource_group.main.name
  server_name         = azurerm_mysql_flexible_server.main.name
  charset             = "utf8mb4"
  collation           = "utf8mb4_unicode_ci"
}

# Store MySQL connection string in Key Vault
resource "azurerm_key_vault_secret" "mysql_connection_string" {
  name         = "mysql-connection-string"
  value        = "jdbc:mysql://${azurerm_mysql_flexible_server.main.fqdn}:3306/${azurerm_mysql_flexible_database.main.name}?useSSL=true&requireSSL=true&serverTimezone=UTC"
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_role_assignment.kv_secrets_officer_deployer]
}

# Container Apps Environment

resource "azurerm_container_app_environment" "main" {
  name                       = azurecaf_name.container_app_env.result
  resource_group_name        = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
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
        value = "jdbc:mysql://${azurerm_mysql_flexible_server.main.fqdn}:3306/${azurerm_mysql_flexible_database.main.name}?useSSL=true&requireSSL=true&serverTimezone=UTC"
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
    azurerm_mysql_flexible_server_firewall_rule.allow_azure,
    azurerm_container_app.frontend
  ]
}

# Backend CORS configuration
resource "azapi_resource_action" "backend_cors" {
  type        = "Microsoft.App/containerApps@2023-05-01"
  resource_id = azurerm_container_app.backend.id
  method      = "PATCH"

  body = {
    properties = {
      configuration = {
        ingress = {
          corsPolicy = {
            allowedOrigins = ["*"]
            allowedHeaders = ["*"]
            allowedMethods = ["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"]
            maxAge         = 86400
          }
        }
      }
    }
  }

  depends_on = [azurerm_container_app.backend]
}

# Frontend Container App

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
}

# Frontend CORS configuration
resource "azapi_resource_action" "frontend_cors" {
  type        = "Microsoft.App/containerApps@2023-05-01"
  resource_id = azurerm_container_app.frontend.id
  method      = "PATCH"

  body = {
    properties = {
      configuration = {
        ingress = {
          corsPolicy = {
            allowedOrigins = ["*"]
            allowedHeaders = ["*"]
            allowedMethods = ["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"]
            maxAge         = 86400
          }
        }
      }
    }
  }

  depends_on = [azurerm_container_app.frontend]
}
