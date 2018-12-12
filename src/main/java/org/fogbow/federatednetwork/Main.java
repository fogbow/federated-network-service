package org.fogbow.federatednetwork;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.constants.SystemConstants;
import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.datastore.order_storage.RecoveryService;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.fogbowcloud.ras.core.AaaController;
import org.fogbowcloud.ras.core.AaaPluginInstantiator;
import org.fogbowcloud.ras.core.AaaPluginsHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Properties;

@Component
public class Main implements ApplicationRunner {

    @Autowired
    RecoveryService recoveryService;


    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    @Override
    public void run(ApplicationArguments args) throws SubnetAddressesCapacityReachedException, InvalidCidrException,
            SQLException {
        DatabaseManager.getInstance().setRecoveryService(recoveryService);
        Properties properties = PropertiesUtil.readProperties(SystemConstants.CONF_FILE_NAME);

        AaaPluginInstantiator instantiationInitService = AaaPluginInstantiator.getInstance();
        AaaPluginsHolder aaaPluginsHolder = new AaaPluginsHolder(instantiationInitService);

        AaaController aaController = new AaaController(aaaPluginsHolder);
        OrderController orderController = new OrderController(properties);

        applicationFacade.setOrderController(orderController);
        applicationFacade.setAaController(aaController);
    }
}