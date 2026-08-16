### Store the tf-state for Github Actions ###

### Resource : Resource Group ###
resource "azurerm_resource_group" "terraform_state" {
  name     = "terraform-state"
  location = "swedencentral"
}

### Resource : Storage Account ###
resource "azurerm_storage_account" "terraform_state" {
  name                     = "stouchitfstate2026"
  resource_group_name      = azurerm_resource_group.terraform_state.name
  location                 = azurerm_resource_group.terraform_state.location
  account_tier             = "Standard"
  account_replication_type = "LRS" ### Locally Redundant Storage cheaper than GeoRS ###
}

### Resource : Storage Container : Remote Folder ###
resource "azurerm_storage_container" "tfstate_container" {
  name                  = "tfstate-container"
  storage_account_id    = azurerm_storage_account.terraform_state.id
  container_access_type = "private"
}