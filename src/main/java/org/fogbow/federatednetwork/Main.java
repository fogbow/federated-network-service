package org.fogbow.federatednetwork;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.fogbowcloud.manager.core.AaController;
import org.fogbowcloud.manager.core.BehaviorPluginsHolder;
import org.fogbowcloud.manager.core.PluginInstantiator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class Main implements ApplicationRunner {


    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    @Override
    public void run(ApplicationArguments args) {
        Properties properties = PropertiesUtil.readProperties();

        PluginInstantiator pluginInstantiator = PluginInstantiator.getInstance();
        BehaviorPluginsHolder behaviorPluginsHolder = new BehaviorPluginsHolder(pluginInstantiator);
        AaController aaController = new AaController(behaviorPluginsHolder);
        OrderController orderController = new OrderController(properties);

        applicationFacade.setOrderController(orderController);
        applicationFacade.setAaController(aaController);
    }
}