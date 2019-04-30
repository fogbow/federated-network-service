#!/bin/bash

#the IP address of the host (assigned by the DC)
host_ip=$1
#vlanID of the network federation
vlanID=$2

prefix="gre-vm"
#get progressive index
index=$(sudo python ../utils/get_progressive_index.py $prefix)
#add the gre tunnel
sudo ovs-vsctl add-port br-dc $prefix$index -- set interface $prefix$index \
type=gre options:remote_ip=$host_ip options:key=$vlanID
#tag the “gre-vm*” as a VLAN access port
sudo ovs-vsctl set port $prefix$index tag=$vlanID
