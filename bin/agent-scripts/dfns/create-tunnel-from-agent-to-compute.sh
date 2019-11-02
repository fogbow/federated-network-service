#!/bin/bash

#the IP address of the host (assigned by the DC)
host_ip=$1
#vlanID of the network federation
vlanID=$2
#publicKey to be removed from the authorized keys
keyToBeRemoved=$3

prefix="gre-vm-"
#get progressive index
index="${host_ip}-vlan-${vlanID}"
#add the gre tunnel
sudo ovs-vsctl add-port br-dc $prefix$index -- set interface $prefix$index \
type=gre options:remote_ip=$host_ip options:key=$vlanID
#tag the gre-vm as a VLAN access port
sudo ovs-vsctl set port $prefix$index tag=$vlanID

#deleting the public key from the authorized keys
#sed -i "\:$keyToBeRemoved:d" ~/.ssh/authorized_keys

#rm $0
