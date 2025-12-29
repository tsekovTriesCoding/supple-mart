output "RESOURCE_GROUP_ID" {
  value       = azurerm_resource_group.main.id
  description = "Resource Group ID"
}

output "RESOURCE_GROUP_NAME" {
  value       = azurerm_resource_group.main.name
  description = "Resource Group name"
}

output "AZURE_CONTAINER_REGISTRY_ENDPOINT" {
  value       = azurerm_container_registry.main.login_server
  description = "Azure Container Registry login server"
}

output "AZURE_CONTAINER_REGISTRY_NAME" {
  value       = azurerm_container_registry.main.name
  description = "Azure Container Registry name"
}

output "MYSQL_CONTAINER_APP_NAME" {
  value       = azurerm_container_app.mysql.name
  description = "MySQL Container App name"
}

output "MYSQL_INTERNAL_FQDN" {
  value       = azurerm_container_app.mysql.ingress[0].fqdn
  description = "MySQL Container App internal FQDN"
}

output "BACKEND_URL" {
  value       = "https://${azurerm_container_app.backend.ingress[0].fqdn}"
  description = "Backend Container App URL"
}

output "FRONTEND_URL" {
  value       = "https://${azurerm_container_app.frontend.ingress[0].fqdn}"
  description = "Frontend Container App URL"
}

output "BACKEND_CONTAINER_APP_NAME" {
  value       = azurerm_container_app.backend.name
  description = "Backend Container App name"
}

output "FRONTEND_CONTAINER_APP_NAME" {
  value       = azurerm_container_app.frontend.name
  description = "Frontend Container App name"
}

output "CONTAINER_APP_ENVIRONMENT_NAME" {
  value       = azurerm_container_app_environment.main.name
  description = "Container App Environment name"
}

output "APPLICATION_INSIGHTS_CONNECTION_STRING" {
  value       = azurerm_application_insights.main.connection_string
  description = "Application Insights connection string"
  sensitive   = true
}

output "LOG_ANALYTICS_WORKSPACE_ID" {
  value       = azurerm_log_analytics_workspace.main.id
  description = "Log Analytics Workspace ID"
}

output "MANAGED_IDENTITY_CLIENT_ID" {
  value       = azurerm_user_assigned_identity.main.client_id
  description = "User-Assigned Managed Identity client ID"
}

output "MANAGED_IDENTITY_PRINCIPAL_ID" {
  value       = azurerm_user_assigned_identity.main.principal_id
  description = "User-Assigned Managed Identity principal ID"
}
