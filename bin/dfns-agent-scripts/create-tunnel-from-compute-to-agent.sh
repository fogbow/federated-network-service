#!/bin/bash

# inputs
cidr=#CIDR# # cidr of the default network
gateway_ip=#GATEWAY_IP# # public IP address of the local gateway
vlanID=#VLAN_ID# # vlanID of the network federation
host_fn_ip=#FEDERATED_IP# # the private address for the host in the federated network
agent_user=#AGENT_USER# # the user to ssh into the agent machine
private_key=#PRIVATE_KEY#
public_key=#PUBLIC_KEY#

# install grepcidr to discover host ip
sudo apt-get update
sudo apt-get install grepcidr

# create file .iplog with all valid local ip addresses
ifconfig | awk -F "[: ]+" '/inet addr:/ { if ($4 != "127.0.0.1") print $4 }' >> .iplog

# the IP address of the host (assigned by the DC)
local_ip=$(grepcidr $cidr .iplog | head -n1)
rm .iplog

if [ -z\ "$local_ip" ]; then
    echo "Unable to retrieve local IP"
    return 1
fi

# get interface name given the local ip
if=$(ifconfig | grep -B1 $local_ip | grep -o "^\w*")
sudo ip link add tun$vlanID type gretap local $local_ip remote $gateway_ip key $vlanID dev $if
sudo ip addr add $host_fn_ip/24 dev tun$vlanID
sudo ip link set tun$vlanID up

#create key files
echo $public_key >> ~/.ssh/access-agent-key.pub
echo $private_key >> ~/.ssh/access-agent-key
chmod 644 ~/.ssh/access-agent-key.pub
chmod 600 ~/.ssh/access-agent-key

#ssh to agent in order to execute the create tunnel from agent to compute script
ssh $agent_user@$gateway_ip -i ~/.ssh/access-agent-key -T "bash /tmp/create_tunnel_from_agent_to_compute.sh $local_ip $vlan_id"

#deleting the public key from the authorized keys
sed -i '/'"$public_key"'/d' ~/.ssh/authorized_keys