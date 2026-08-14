### Check Terraform Registry for documentation ###

### Provider :  Azure ###
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "5.0.1"
    }
  }
}

provider "azurerm" {
  features {}
}


### Resource : Resource Group : Logical container that organizes related Azure resources ###
resource "azurerm_resource_group" "stouchi" {
  name     = "stouchi-resources"
  location = "swedencentral"
}

### Resource : Network Security Group : Set of network traffic rules ###
resource "azurerm_network_security_group" "stouchi" {
  name                = "stouchi-nsg"
  location            = azurerm_resource_group.stouchi.location
  resource_group_name = azurerm_resource_group.stouchi.name
}

### Resource : Network Security Rule ###
resource "azurerm_network_security_rule" "stouchi" {
  name                        = "allow-ssh"
  priority                    = 100 ### highest priority ! ###
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "22"
  source_address_prefix       = "*" ### allow traffic coming from any source IP address ###
  destination_address_prefix  = "*"
  resource_group_name         = azurerm_resource_group.stouchi.name
  network_security_group_name = azurerm_network_security_group.stouchi.name
}

### Resource : Virtual Network : My Private Network inside Azure ###
resource "azurerm_virtual_network" "stouchi" {
  name                = "stouchi-vnet"
  location            = azurerm_resource_group.stouchi.location
  resource_group_name = azurerm_resource_group.stouchi.name
  address_space       = ["10.0.0.0/16"]

  tags = {
    environment = "Production"
  }
}

### Resource : Subnet inside Virtual Network ###
resource "azurerm_subnet" "stouchi" {
  name                 = "stouchi-subnet"
  resource_group_name  = azurerm_resource_group.stouchi.name
  virtual_network_name = azurerm_virtual_network.stouchi.name
  address_prefixes     = ["10.0.1.0/24"]
}

### Resource : Subnet NSG Association : To apply sec rules on the subnet ###
resource "azurerm_subnet_network_security_group_association" "stouchi" {
  subnet_id                 = azurerm_subnet.stouchi.id
  network_security_group_id = azurerm_network_security_group.stouchi.id
}

### Resource : Public IP given by Azure's public IP allocation system from Azure's available public address space ###
resource "azurerm_public_ip" "stouchi" {
  name                = "stouchi-public-ip"
  resource_group_name = azurerm_resource_group.stouchi.name
  location            = azurerm_resource_group.stouchi.location

  allocation_method = "Static"
  sku               = "Standard"
}

### Resource : NIC : To connect the VM to Azure VNet ###
### Azure handles the public-to-private connectivity : Associates the public IP with the NIC's IP configuration ###
resource "azurerm_network_interface" "stouchi" {
  name                = "stouchi-nic"
  location            = azurerm_resource_group.stouchi.location
  resource_group_name = azurerm_resource_group.stouchi.name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.stouchi.id
    private_ip_address_allocation = "Dynamic"

    public_ip_address_id = azurerm_public_ip.stouchi.id
  }
}

### Resource : Ubuntu VM ###
resource "azurerm_linux_virtual_machine" "stouchi" {
  name                = "stouchi-machine"
  resource_group_name = azurerm_resource_group.stouchi.name
  location            = azurerm_resource_group.stouchi.location
  size                = "Standard_B2as_v2"
  admin_username      = "deploy"
  network_interface_ids = [
    azurerm_network_interface.stouchi.id,
  ]

  admin_ssh_key {
    username   = "deploy"
    public_key = file("~/.ssh/stouchi_vm.pub")
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  source_image_reference {
    publisher = "Canonical"
    offer     = "0001-com-ubuntu-server-jammy"
    sku       = "22_04-lts"
    version   = "latest"
  }
}
