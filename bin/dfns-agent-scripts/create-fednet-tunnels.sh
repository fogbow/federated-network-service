ipsec_psk="fogbow_ipsec_psk"

i=0
for ip in "$@"
do
	sudo ovs-vsctl add-port br-interdc gre-DC$i -- set interface gre-DC$i type=gre \
	options:remote_ip=$ip options:psk=$ipsec_psk
	((i++))
done
