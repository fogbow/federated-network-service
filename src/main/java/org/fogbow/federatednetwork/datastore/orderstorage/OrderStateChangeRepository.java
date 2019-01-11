package org.fogbow.federatednetwork.datastore.orderstorage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStateChangeRepository extends JpaRepository<OrderStateChange, String> {
}
