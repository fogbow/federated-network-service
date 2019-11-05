#!/bin/bash

#the IP in the fednet
fednet_ip=$1
#the IP address of the host (assigned by the DC)
host_ip=$2
#vlanID of the network federation
vlanID=$3

prefix="gre-vm-"
#get progressive index
index="${fednet_ip}-vlan-${vlanID}"
#add the gre tunnel
sudo ovs-vsctl add-port br-dc $prefix$index -- set interface $prefix$index \
type=gre options:remote_ip=$host_ip options:key=$vlanID
#tag the gre-vm as a VLAN access port
sudo ovs-vsctl set port $prefix$index tag=$vlanID

#deleting the public key from the authorized keys
sed -i "/FNS-script-key/d" ~/.ssh/authorized_keys

rm $0
