package cloud.fogbow.fns.core.drivers;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.drivers.dfns.AgentConfiguration;
import cloud.fogbow.ras.core.models.UserData;

public interface ServiceDriver {
    /**
     * The driver that implements this method must do
     * the operations needed to make the order able to transition
     * to the spawning state, according to each driver specifications.
     * If one of the needed operations can't be done, a FogbowException
     * must be thrown.
     * @param order
     * @throws FogbowException
     */
    void processOpen(FederatedNetworkOrder order) throws FogbowException;

    /**
     * The driver that implements this method must do
     * the operations needed to make the order able to transition
     * to the fulfilled state, according to each driver specifications.
     * If one of the needed operations can't be done, a FogbowException
     * must be thrown.
     * @param order
     * @throws FogbowException
     */
    void processSpawning(FederatedNetworkOrder order) throws FogbowException;

    /**
     * The driver that implements this method must do
     * the operations needed to make the order able to transition
     * to the deactivated state, according to each driver specifications.
     * If one of the needed operations can't be done, a FogbowException
     * must be thrown.
     * @param order
     * @throws FogbowException
     */
    void processClosed(FederatedNetworkOrder order) throws FogbowException;

    /**
     * The driver that implement this method must returns
     * an AgentConfiguration according to the agent's specification
     * for that driver's service. If any problem occurs during the process,
     * a FogbowException must be thrown
     * @return
     * @throws FogbowException
     */
    AgentConfiguration configureAgent(String provider) throws FogbowException;

    /**
     * The driver that implements this method must return
     * a UserData that is capable of setup the compute to be part
     * of the FederatedNetwork in that driver's service mode.
     * If it can't be done, a FogbowException must be thrown.
     * @param agentConfiguration
     * @param compute
     * @param order
     * @param instanceIp
     * @return
     * @throws FogbowException
     */
    UserData getComputeUserData(AgentConfiguration agentConfiguration, FederatedCompute compute, FederatedNetworkOrder order, String instanceIp) throws FogbowException;

    /**
     * The driver that implements this method must clean up the agent
     * according to the driver specification.
     * If it can't be done, a FogbowException must be thrown.
     * @param order
     * @param hostIp
     * @throws FogbowException
     */
    void cleanupAgent(FederatedNetworkOrder order, String hostIp) throws FogbowException;

    /**
     * This method is necessary because on remote call
     * the whole configureAgent can't be executed once the keys
     * must be kept. Thus, the remoteFacade will call doConfigureAgent
     * instead of configureAgent, avoiding a new and out of context key pair.
     * The driver that implements this method must setup the AgentConfiguration
     * or throw a FogbowException if it is not possible.
     * @param publicKey
     * @return
     */
    AgentConfiguration doConfigureAgent(String publicKey) throws FogbowException;

    /**
     * The driver that implements this method must return
     * the AgentIp that comes from its conf file.
     * @return
     * @throws FogbowException
     */
    String getAgentIp();
}
