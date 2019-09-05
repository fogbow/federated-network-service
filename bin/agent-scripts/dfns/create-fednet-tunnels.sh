# In the current version, the tunnels among sites are created at deploy time,
# so commenting this script for now and writing a test file to certify the call
# is being made.
#ipsec_psk="fogbow_ipsec_psk"
#
#i=0
#for ip in "$@"
#do
#	sudo ovs-vsctl add-port br-interdc gre-DC$i -- set interface gre-DC$i type=gre \
#	options:remote_ip=$ip options:psk=$ipsec_psk
#	((i++))
#done
echo 'run create-fednet-tunnels script' > /tmp/create-fednet-script-run
