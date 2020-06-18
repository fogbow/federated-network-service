package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FatalErrorException;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;

import java.util.ArrayList;
import java.util.List;

public class ServiceListController {

    private List<String> serviceNames;
    private final int FIRST_SERVICE_POSITION = 0;

    public ServiceListController() {
        this.serviceNames = new ArrayList<>();
        String serviceNamesList = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.SERVICE_NAMES_KEY);

        if (serviceNamesList == null || serviceNamesList.isEmpty()) {
            throw new FatalErrorException(Messages.Exception.NO_SERVICE_SPECIFIED);
        }

        for (String serviceName : serviceNamesList.split(",")) {
            serviceName = serviceName.trim();
            // Here we populate the list of services configured and, at the same time, check if all
            // services have been correctly configured. If not, FNS won't even start, and will throw a
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

    public String getDefaultService() { return serviceNames.get(FIRST_SERVICE_POSITION);}
}
