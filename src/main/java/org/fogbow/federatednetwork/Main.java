package org.fogbow.federatednetwork;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.common.constants.FogbowConstants;
import org.fogbow.federatednetwork.common.exceptions.FatalErrorException;
import org.fogbow.federatednetwork.common.plugins.authorization.AuthorizationPlugin;
import org.fogbow.federatednetwork.common.plugins.authorization.AuthorizationController;
import org.fogbow.federatednetwork.common.util.ServiceAsymmetricKeysHolder;
import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.datastore.orderstorage.AuditService;
import org.fogbow.federatednetwork.datastore.orderstorage.RecoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Main implements ApplicationRunner {

    @Autowired
    RecoveryService recoveryService;

    @Autowired
    AuditService auditService;

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    @Override
    public void run(ApplicationArguments args) {
        try {
            DatabaseManager.getInstance().setRecoveryService(recoveryService);
            DatabaseManager.getInstance().setAuditService(auditService);

            // Setting up asymmetric cryptography
            String publicKeyFilePath = PropertiesHolder.getInstance().getProperty(FogbowConstants.PUBLIC_KEY_FILE_PATH);
            String privateKeyFilePath = PropertiesHolder.getInstance().getProperty(FogbowConstants.PRIVATE_KEY_FILE_PATH);
            ServiceAsymmetricKeysHolder.getInstance().setPublicKeyFilePath(publicKeyFilePath);
            ServiceAsymmetricKeysHolder.getInstance().setPrivateKeyFilePath(privateKeyFilePath);

            // Setting up controllers and application facade
            FederatedNetworkOrderController federatedNetworkOrderController = new FederatedNetworkOrderController();
            ComputeRequestsController computeRequestsController = new ComputeRequestsController();
            AuthorizationPlugin authorizationPlugin = AuthorizationPluginInstantiator.getAuthorizationPlugin();
            AuthorizationController authorizationController =  new AuthorizationController(authorizationPlugin);

            this.applicationFacade.setFederatedNetworkOrderController(federatedNetworkOrderController);
            this.applicationFacade.setComputeRequestsController(computeRequestsController);
            this.applicationFacade.setAuthorizationController(authorizationController);
        } catch (FatalErrorException e) {
            LOGGER.fatal(e.getMessage(), e);
            tryExit();
        }
    }

    private void tryExit() {
        if (!Boolean.parseBoolean(System.getenv("SKIP_TEST_ON_TRAVIS")))
            System.exit(1);
    }
}