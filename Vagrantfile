# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  config.vm.box = "ubuntu/bionic64"

  config.vm.provider "virtualbox" do |vb|
     vb.memory = "4096"
  end
  
  config.vm.provision "shell", privileged: true, path: 'provision-root-priviledged.sh'
  config.vm.provision "shell", privileged: false, path: 'provision-user-priviledged.sh'

  config.vm.provision "Copy user's git config", type:'file', source: '~/.gitconfig', destination: '.gitconfig'

  config.vm.network "forwarded_port", guest: 8080, host: 8088, host_ip: "0.0.0.0", id: "spring_boot"
  config.vm.network "forwarded_port", guest: 8081, host: 8089, host_ip: "0.0.0.0", id: "spring_boot_2"
  config.vm.network "forwarded_port", guest: 8500, host: 8501, host_ip: "0.0.0.0", id: "consul_http"
  config.vm.network "forwarded_port", guest: 8600, host: 8601, host_ip: "0.0.0.0", id: "consul_dns"
  config.vm.network "forwarded_port", guest: 9090, host: 9091, host_ip: "0.0.0.0", id: "prometheus"
  config.vm.network "forwarded_port", guest: 27017, host: 27018, host_ip: "0.0.0.0", id: "mongo_db"
end
