package org.fogbow.federatednetwork.controllers;

import org.fogbowcloud.manager.core.models.orders.ComputeOrder;

public class FederatedComputeController {

	public void activateCompute(ComputeOrder computeOrder, String federatedNetworkId){
		// if network not full
		// get unused ip
		// add an User Data (script, CloudInitUserDataBuilder.FileType.SHELL_SCRIPT)
		// send to Core
	}

	public void getCompute(String computeOrderId){
		// get compute from Core
		// if isFederated
		// add federatedIp as a new attribute
	}

	public void deleteCompute(String computeOrderId){
		// release ip from network
		// delete on Core
	}
}
