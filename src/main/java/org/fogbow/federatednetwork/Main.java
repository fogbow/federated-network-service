package org.fogbow.federatednetwork;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.constants.ConfigurationConstants;
import org.fogbow.federatednetwork.constants.SystemConstants;
import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.datastore.orderstorage.AuditService;
import org.fogbow.federatednetwork.datastore.orderstorage.RecoveryService;
import org.fogbow.federatednetwork.exceptions.FatalErrorException;
import org.fogbow.federatednetwork.utils.HomeDir;
import org.fogbow.federatednetwork.utils.PropertiesHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import org.fogbowcloud.ras.core.AaaController;
import org.fogbowcloud.ras.core.AaaPluginInstantiator;
import org.fogbowcloud.ras.core.AaaPluginsHolder;
import org.fogbowcloud.ras.core.plugins.aaa.RASAuthenticationHolder;


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
            // Setting up controllers and application facade
            FederatedNetworkOrderController federatedNetworkOrderController = new FederatedNetworkOrderController();
            ComputeRequestsController computeRequestsController = new ComputeRequestsController();

            String localProviderId = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_NAME);
            String aaaConfFilePath = HomeDir.getPath() + SystemConstants.AAA_CONF_FILE_NAME;

            String privateKeyFile = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_PRIVATE_KEY_FILE_PATH);
            RASAuthenticationHolder.getInstance().setPrivateKeyFilePath(privateKeyFile);
            String publicKeyFile = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_PUBLIC_KEY_FILE_PATH);
            RASAuthenticationHolder.getInstance().setPublicKeyFilePath(publicKeyFile);

            AaaPluginsHolder aaaPluginsHolder = new AaaPluginsHolder();
            aaaPluginsHolder.setFederationIdentityPlugin(AaaPluginInstantiator.getFederationIdentityPlugin(aaaConfFilePath));
            aaaPluginsHolder.setAuthenticationPlugin(AaaPluginInstantiator.getAuthenticationPlugin(aaaConfFilePath, localProviderId));
            aaaPluginsHolder.setAuthorizationPlugin(AaaPluginInstantiator.getAuthorizationPlugin(aaaConfFilePath));

            AaaController aaaController = new AaaController(aaaPluginsHolder, localProviderId);

            this.applicationFacade.setFederatedNetworkOrderController(federatedNetworkOrderController);
            this.applicationFacade.setComputeRequestsController(computeRequestsController);
            this.applicationFacade.setAaaController(aaaController);
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