package org.fogbow.federatednetwork.model;

import java.util.List;
import java.util.UUID;

public class FederatedNetwork {

	String id;
	String label;
	String cidr;
	List<FederationMember> members;

	public FederatedNetwork(String label, String cidr, List<FederationMember> members) {
		this.id = UUID.randomUUID().toString();
		this.label = label;
		this.cidr = cidr;
		this.members = members;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCidr() {
		return cidr;
	}

	public void setCidr(String cidr) {
		this.cidr = cidr;
	}

	public List<FederationMember> getMembers() {
		return members;
	}

	public void setMembers(List<FederationMember> members) {
		this.members = members;
	}
}
