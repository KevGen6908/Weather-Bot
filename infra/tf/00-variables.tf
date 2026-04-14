variable "cloud_id" {
  description = "Yandex Cloud ID"
  type        = string
}

variable "folder_id" {
  description = "Yandex Cloud folder ID"
  type        = string
}

variable "zone" {
  description = "Availability zone"
  type        = string
  default     = "ru-central1-b"
}

variable "token" {
  description = "Yandex Cloud IAM token (use either token or service_account_key_file)"
  type        = string
  default     = null
  sensitive   = true
}

variable "service_account_key_file" {
  description = "Path to Yandex Cloud service account key JSON (use either this or token)"
  type        = string
  default     = null
}

variable "ssh_public_key_path" {
  description = "Path to the public SSH key used for VM access"
  type        = string
}

variable "vm_user" {
  description = "Linux user created on the VM"
  type        = string
  default     = "yc-user"
}

variable "vm_name" {
  description = "Virtual machine name"
  type        = string
  default     = "weather-bot-vm"
}

variable "image_family" {
  description = "Public image family for the VM"
  type        = string
  default     = "ubuntu-2404-lts-oslogin"
}

variable "network_name" {
  description = "Cloud network name"
  type        = string
  default     = "weather-bot-network"
}

variable "subnet_name" {
  description = "Subnet name"
  type        = string
  default     = "weather-bot-subnet"
}

variable "subnet_cidr" {
  description = "Subnet IPv4 CIDR"
  type        = string
  default     = "10.10.0.0/24"
}

variable "platform_id" {
  description = "VM platform"
  type        = string
  default     = "standard-v3"
}

variable "vm_cores" {
  description = "Number of vCPUs"
  type        = number
  default     = 2
}

variable "vm_memory" {
  description = "RAM in GB"
  type        = number
  default     = 4
}

variable "core_fraction" {
  description = "Guaranteed CPU percentage"
  type        = number
  default     = 100
}

variable "disk_type" {
  description = "Boot disk type"
  type        = string
  default     = "network-hdd"
}

variable "disk_size_gb" {
  description = "Boot disk size in GB"
  type        = number
  default     = 20
}

variable "bot_port" {
  description = "Optional application port to expose"
  type        = number
  default     = 8080
}
