package org.fogbow.federatednetwork;

import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;

public class CoreRequester {

	public boolean createCompute(ComputeOrder order){
		throw new UnsupportedOperationException();
	}

	public ComputeInstance getCompute(String orderId){
		throw new UnsupportedOperationException();
	}

	public void deleteCompute(String id){
		throw new UnsupportedOperationException();
	}
}
