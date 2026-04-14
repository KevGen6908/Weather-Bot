terraform {
  required_version = ">= 1.5.0"

  required_providers {
    yandex = {
      source  = "yandex-cloud/yandex"
      version = ">= 0.138.0"
    }
  }
}

provider "yandex" {
  cloud_id = var.cloud_id
  folder_id = var.folder_id
  zone = var.zone
  token = var.token
}

locals {
  user_data = templatefile("${path.module}/cloud-init.yaml.tftpl", {
    vm_user = var.vm_user
    ssh_public_key = trimspace(file(var.ssh_public_key_path))
  })
}

resource "yandex_vpc_network" "weather_bot" {
  name = var.network_name
}

resource "yandex_vpc_subnet" "weather_bot" {
  name = var.subnet_name
  zone = var.zone
  v4_cidr_blocks = [var.subnet_cidr]
  network_id = yandex_vpc_network.weather_bot.id
}

resource "yandex_vpc_security_group" "weather_bot_vm" {
  name = "${var.vm_name}-sg"
  network_id = yandex_vpc_network.weather_bot.id

  egress {
    description = "allow all outbound"
    protocol = "ANY"
    v4_cidr_blocks = ["0.0.0.0/0"]
    from_port = 0
    to_port = 65535
  }

  ingress {
    description = "ssh"
    protocol= "TCP"
    v4_cidr_blocks = ["0.0.0.0/0"]
    port = 22
  }

  ingress {
    description = "bot-port"
    protocol = "TCP"
    v4_cidr_blocks = ["0.0.0.0/0"]
    port = var.bot_port
  }

  ingress {
    description = "self"
    protocol = "ANY"
    from_port = 0
    to_port = 65535
    predefined_target = "self_security_group"
  }
}

resource "yandex_vpc_address" "weather_bot" {
  name = "${var.vm_name}-public-ip"

  external_ipv4_address {
    zone_id = var.zone
  }
}

data "yandex_compute_image" "ubuntu" {
  family = var.image_family
}

resource "yandex_compute_instance" "weather_bot" {
  name = var.vm_name
  hostname = var.vm_name
  zone = var.zone
  platform_id = var.platform_id
  allow_stopping_for_update = true

  resources {
    cores = var.vm_cores
    memory = var.vm_memory
    core_fraction = var.core_fraction
  }

  boot_disk {
    initialize_params {
      image_id = data.yandex_compute_image.ubuntu.id
      size = var.disk_size_gb
      type = var.disk_type
    }
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.weather_bot.id
    nat = true
    nat_ip_address = yandex_vpc_address.weather_bot.external_ipv4_address[0].address
    security_group_ids = [yandex_vpc_security_group.weather_bot_vm.id]
  }

  metadata = {
    user-data = local.user_data
  }
}
