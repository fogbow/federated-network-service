#!/bin/bash

#install grepcidr to discover host ip
sudo apt-get update
sudo apt-get install grepcidr

#create file .iplog with all valid local ip addresses
cidr=#CIDR#
ifconfig | awk -F "[: ]+" '/inet addr:/ { if ($4 != "127.0.0.1") print $4 }' >> .iplog

#the IP address of the host (assigned by the DC)
local_ip=$(grepcidr $cidr .iplog)
rm .iplog

#public IP address of the local gateway
gateway_ip=#GATEWAY_IP#
#vlanID of the network federation
vlanID=#VLAN_ID#
#the private address for the host in the federated network
host_fn_ip=#FEDERATED_IP#

#get interface name given the local ip
if=$(ifconfig | grep -B1 $local_ip | grep -o "^\w*")
sudo ip link add tun$vlanID type gretap local $local_ip remote $gateway_ip key $vlanID dev $if
sudo ip addr add $host_fn_ip/24 dev tun$vlanID
sudo ip link set tun$vlanID up

#create key files
echo #PUBLIC_KEY# >> ~/.ssh/access-agent-key.pub
echo #PRIVATE_KEY# >> ~/.ssh/access-agent-key
chmod 644 ~/.ssh/access-agent-key.pub
chmod 600 ~/.ssh/access-agent-key

#ssh to agent in order to execute the create tunnel from agent to compute script
agent_user=#AGENT_USER#
agent_public_ip=#AGENT_PUBLIC_IP#

ssh $agent_user@$agent_public_ip -i ~/.ssh/access-agent-key -T "bash /tmp/create_tunnel_from_agent_to_compute.sh $local_ip $vlan_id"

