output "external_ip" {
  value = yandex_compute_instance.weather_bot.network_interface[0].nat_ip_address
}

output "internal_ip" {
  value = yandex_compute_instance.weather_bot.network_interface[0].ip_address
}

output "ssh_command" {
  value = "ssh ${var.vm_user}@${yandex_compute_instance.weather_bot.network_interface[0].nat_ip_address}"
}
