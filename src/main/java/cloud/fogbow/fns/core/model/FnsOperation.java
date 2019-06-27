package cloud.fogbow.fns.core.model;

import cloud.fogbow.common.models.FogbowOperation;

import java.util.Objects;

public class FnsOperation extends FogbowOperation {

    private Operation operationType;
    private ResourceType resourceType;
    private FederatedNetworkOrder order;

    public FnsOperation(Operation operationType, ResourceType resourceType, FederatedNetworkOrder order) {
        this.operationType = operationType;
        this.resourceType = resourceType;
        this.order = order;
    }

    public FnsOperation(Operation operationType, ResourceType resourceType) {
        this.operationType = operationType;
        this.resourceType = resourceType;
    }

    public Operation getOperationType() {
        return operationType;
    }

    public void setOperationType(Operation operationType) {
        this.operationType = operationType;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public FederatedNetworkOrder getOrder() {
        return order;
    }

    public void setOrder(FederatedNetworkOrder order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FnsOperation operation = (FnsOperation) o;
        return operationType == operation.operationType &&
                resourceType == operation.resourceType &&
                Objects.equals(order, operation.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationType, resourceType, order);
    }
}
