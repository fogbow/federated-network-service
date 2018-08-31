package org.fogbow.federatednetwork;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.datastore.order_storage.RecoveryService;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.fogbowcloud.manager.core.AaController;
import org.fogbowcloud.manager.core.BehaviorPluginsHolder;
import org.fogbowcloud.manager.core.PluginInstantiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class Main implements ApplicationRunner {

    @Autowired
    RecoveryService recoveryService;


    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    @Override
    public void run(ApplicationArguments args) throws SubnetAddressesCapacityReachedException, InvalidCidrException {
        DatabaseManager.getInstance().setRecoveryService(recoveryService);
        Properties properties = PropertiesUtil.readProperties();

        PluginInstantiator pluginInstantiator = PluginInstantiator.getInstance();
        BehaviorPluginsHolder behaviorPluginsHolder = new BehaviorPluginsHolder(pluginInstantiator);
        AaController aaController = new AaController(behaviorPluginsHolder);
        OrderController orderController = new OrderController(properties);

        applicationFacade.setOrderController(orderController);
        applicationFacade.setAaController(aaController);
    }
}