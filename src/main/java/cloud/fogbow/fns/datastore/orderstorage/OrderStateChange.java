package cloud.fogbow.fns.datastore.orderstorage;

import cloud.fogbow.fns.model.FederatedNetworkOrder;
import cloud.fogbow.fns.model.OrderState;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "order_state_change")
public class OrderStateChange {
    @Id
    @GeneratedValue
    private String id;

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
