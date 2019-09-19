package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.constants.HttpConstants;
import cloud.fogbow.common.constants.HttpMethod;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.BashScriptRunner;
import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.common.util.connectivity.HttpRequestClient;
import cloud.fogbow.common.util.connectivity.HttpResponse;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

public class DefaultServiceConnector implements ServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(DefaultServiceConnector.class);

    public static final String VLAN_ID_SERVICE_URL = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.VLAN_ID_SERVICE_URL_KEY);
    public static final String VLAN_ID_ENDPOINT = "/vlanId";
    private final String GEN_KEY_PAIR_SCRIPT_PATH_FROM_BIN = "/bin/agent-scripts/dfns/generateSshKeyPair";
    private final String GEN_KEY_PAIR_SCRIPT_WHOLE_PATH = Paths.get("").toAbsolutePath().toString() + GEN_KEY_PAIR_SCRIPT_PATH_FROM_BIN;
    private final String KEY_PAIR_SEPARATOR = "KEY SEPARATOR";
    private final String KEY_SIZE = "1024";
    protected static final int PUBLIC_KEY_INDEX = 0;
    protected static final int PRIVATE_KEY_INDEX = 1;

    @Override
    public int acquireVlanId() throws FogbowException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.CONTENT_TYPE_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        headers.put(HttpConstants.ACCEPT_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        String acquireVlanIdEndpoint = VLAN_ID_SERVICE_URL + VLAN_ID_ENDPOINT;

        HttpResponse response = HttpRequestClient.doGenericRequest(HttpMethod.GET, acquireVlanIdEndpoint, headers, new HashMap<>());

        if (response.getHttpCode() == HttpStatus.NOT_ACCEPTABLE.value()) {
            throw new NoVlanIdsLeftException();
        }

        DefaultServiceConnector.VlanId vlanId = GsonHolder.getInstance().fromJson(response.getContent(), DefaultServiceConnector.VlanId.class);
        return vlanId.vlanId;
    }

    @Override
    public void releaseVlanId(int vlanId) throws FogbowException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.CONTENT_TYPE_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        headers.put(HttpConstants.ACCEPT_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);

        String jsonBody = GsonHolder.getInstance().toJson(new DefaultServiceConnector.VlanId(vlanId));
        HashMap<String, String> body = GsonHolder.getInstance().fromJson(jsonBody, HashMap.class);

        String releaseVlanIdEndpoint = VLAN_ID_SERVICE_URL + VLAN_ID_ENDPOINT;

        HttpResponse response = HttpRequestClient.doGenericRequest(HttpMethod.POST, releaseVlanIdEndpoint, headers, body);

        if (response.getHttpCode() == HttpStatus.NOT_FOUND.value()) {
            LOGGER.warn(String.format(Messages.Warn.UNABLE_TO_RELEASE_VLAN_ID, vlanId));
            throw new UnexpectedException(String.format(Messages.Warn.UNABLE_TO_RELEASE_VLAN_ID, vlanId));
        }
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) throws UnexpectedException {
        return null;
    }

    protected String[] generateSshKeyPair() throws UnexpectedException {
        BashScriptRunner runner = new BashScriptRunner();
        String keyName = String.valueOf(UUID.randomUUID());

        // The key's size is passed as parameter and set to 1024 to keep the key small.
        String[] genCommand = {"bash", GEN_KEY_PAIR_SCRIPT_WHOLE_PATH, keyName, KEY_SIZE};
        BashScriptRunner.Output createCommandResult = runner.runtimeRun(genCommand);

        return new String[]{createCommandResult.getContent().split(KEY_PAIR_SEPARATOR)[PUBLIC_KEY_INDEX],
                createCommandResult.getContent().split(KEY_PAIR_SEPARATOR)[PRIVATE_KEY_INDEX]};
    }

    private class VlanId {

        private int vlanId;

        public VlanId(int vlanId) {
            this.vlanId = vlanId;
        }
    }
}
