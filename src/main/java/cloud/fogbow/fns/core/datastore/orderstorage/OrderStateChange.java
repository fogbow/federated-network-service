package cloud.fogbow.fns.core.datastore.orderstorage;

import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "order_state_change")
public class OrderStateChange {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private FederatedNetworkOrder order;

    @Column
    private Timestamp timestamp;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderState newState;

    public OrderStateChange(Timestamp timestamp, FederatedNetworkOrder order, OrderState newState) {
        this.order = order;
        this.timestamp = timestamp;
        this.newState = newState;
    }

    public FederatedNetworkOrder getOrder() {
        return order;
    }

    public void setOrder(FederatedNetworkOrder order) {
        this.order = order;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
