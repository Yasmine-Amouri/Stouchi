terraform {
  backend "azurerm" {
    resource_group_name  = "terraform-state"
    storage_account_name = "stouchitfstate2026"
    container_name       = "tfstate-container"
    key                  = "stouchi.terraform.tfstate"
  }
}
