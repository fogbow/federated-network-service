package org.fogbow.federatednetwork.model;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class FederatedNetwork {

	private String id;
	private String cidrNotation;
	private String label;
	private Set<String> allowedMembers;

	private int ipsServed = 1;
	private Queue<String> freedIps;

	// TODO: This class should be just a model. Create a new class to know the mapping of computeId and ip.
	private Map<String, String> computeIpMap;

	public static final String NO_FREE_IPS_MESSAGE = "Subnet Addresses Capacity Reached, there isn't free IPs to attach";

	public FederatedNetwork() { }

	public FederatedNetwork(String cidrNotation, String label, Set<String> allowedMembers) {
		// the reason for this to start at '1' is because the first ip is allocated
		// to the virtual ip address
		this.ipsServed = 1;
		this.freedIps = new LinkedList<String>();
		this.computeIpMap = new HashMap<String, String>();

		this.cidrNotation = cidrNotation;
		this.label = label;
		this.allowedMembers = allowedMembers;
	}

	public boolean isIpAddressFree(String address) {
		SubnetUtils.SubnetInfo subnetInfo = this.getSubnetInfo();
		if (subnetInfo.isInRange(address)) {
			if (freedIps.contains(address)) {
				return true;
			} else {
				int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
				if (subnetInfo.asInteger(address) >= lowAddress + ipsServed) {
					return true;
				}
			}
		}

		return false;
	}

	public void freeIp(String ipAddress, String computeId) {
		if (isIpAddressFree(ipAddress)) {
			// TODO Signal the caller that it tried to free an already free address
			return;
		}
		computeIpMap.remove(computeId);
		freedIps.add(ipAddress);
	}

	public void addFederationNetworkMember(String member) {
		this.allowedMembers.add(member);
	}

	public String nextFreeIp(String orderId) throws SubnetAddressesCapacityReachedException {
		if (this.computeIpMap.containsKey(orderId)) {
			return this.computeIpMap.get(orderId);
		}
		String freeIp = null;
		if (freedIps.isEmpty()) {
			SubnetUtils.SubnetInfo subnetInfo = this.getSubnetInfo();
			int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
			int candidateIpAddress = lowAddress + ipsServed;
			if (!subnetInfo.isInRange(candidateIpAddress)) {
				throw new SubnetAddressesCapacityReachedException(
						FederatedNetwork.NO_FREE_IPS_MESSAGE);
			} else {
				ipsServed++;
				freeIp = toIpAddress(candidateIpAddress);
			}
		} else {
			freeIp = freedIps.poll();
		}
		this.computeIpMap.put(orderId, freeIp);
		return freeIp;
	}

	@Override
	public String toString() {
		return "FederatedNetwork [id=" + id + ", cidrNotation=" + cidrNotation + ", label=" + label
				+ ", allowedMembers=" + allowedMembers + ", ipsServed=" + ipsServed + ", freedIps="
				+ freedIps + ", computeIpMap=" + computeIpMap + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FederatedNetwork other = (FederatedNetwork) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	private SubnetUtils.SubnetInfo getSubnetInfo() {
		return new SubnetUtils(cidrNotation).getInfo();
	}

	private String toIpAddress(int value) {
		byte[] bytes = BigInteger.valueOf(value).toByteArray();
		try {
			InetAddress address = InetAddress.getByAddress(bytes);
			return address.toString().replaceAll("/", "");
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCidrNotation() {
		return cidrNotation;
	}

	public void setCidrNotation(String cidrNotation) {
		this.cidrNotation = cidrNotation;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Set<String> getAllowedMembers() {
		return allowedMembers;
	}

	public void setAllowedMembers(Set<String> allowedMembers) {
		this.allowedMembers = allowedMembers;
	}

	public int getIpsServed() {
		return ipsServed;
	}

	public void setIpsServed(int ipsServed) {
		this.ipsServed = ipsServed;
	}

	public Queue<String> getFreedIps() {
		return freedIps;
	}

	public void setFreedIps(Queue<String> freedIps) {
		this.freedIps = freedIps;
	}

	public Map<String, String> getComputeIpMap() {
		return computeIpMap;
	}

	public void setComputeIpMap(Map<String, String> computeIpMap) {
		this.computeIpMap = computeIpMap;
	}
}