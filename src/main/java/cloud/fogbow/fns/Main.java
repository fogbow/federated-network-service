package cloud.fogbow.fns;

import cloud.fogbow.common.constants.FogbowConstants;
import cloud.fogbow.common.exceptions.FatalErrorException;
import cloud.fogbow.common.plugins.authorization.AuthorizationController;
import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.common.plugins.authorization.AuthorizationPluginInstantiator;
import cloud.fogbow.common.util.ServiceAsymmetricKeysHolder;
import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.fns.core.ComputeRequestsController;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.constants.ConfigurationConstants;
import cloud.fogbow.fns.core.datastore.DatabaseManager;
import cloud.fogbow.fns.core.datastore.orderstorage.RecoveryService;
import org.apache.log4j.Logger;
import cloud.fogbow.fns.core.datastore.orderstorage.AuditService;
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
            String className = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.AUTHORIZATION_PLUGIN_CLASS_KEY);
            AuthorizationPlugin authorizationPlugin = AuthorizationPluginInstantiator.getAuthorizationPlugin(className);
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