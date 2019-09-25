package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FatalErrorException;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.ras.constants.Messages;
import cloud.fogbow.ras.core.PropertiesHolder;

import java.util.ArrayList;
import java.util.List;

public class ServiceListController {

    private List<String> serviceNames;

    public ServiceListController() {
        this.serviceNames = new ArrayList<>();
        String serviceNamesList = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.SERVICE_NAMES_KEY);

        if (serviceNamesList == null || serviceNamesList.isEmpty()) {
            throw new FatalErrorException(Messages.Fatal.NO_CLOUD_SPECIFIED);
        }

        for (String serviceName : serviceNamesList.split(",")) {
            serviceName = serviceName.trim();
            // Here we populate the list of clouds configured and, at the same time, check if all
            // clouds have been correctly configured. If not, the RAS won't even start, and will throw a
            // fatal exception.
            new ServiceDriverConnector(serviceName);
            this.serviceNames.add(serviceName);
        }
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }
}
