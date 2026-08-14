output "vm_public_ip" {
  description = "Public IP address of the Stouchi VM"
  value       = azurerm_public_ip.stouchi.ip_address
}
