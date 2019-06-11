package cloud.fogbow.fns.utils;

import cloud.fogbow.common.util.CloudInitUserDataBuilder;
import cloud.fogbow.common.util.CryptoUtil;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.serviceconnector.DfnsAgentConfiguration;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import cloud.fogbow.fns.api.parameters.FederatedCompute;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FederatedComputeUtil {
    public static final String IPSEC_INSTALLATION_PATH = "bin/ipsec-configuration";
    public static final String LEFT_SOURCE_IP_KEY = "#LEFT_SOURCE_IP#";
    public static final String RIGHT_IP = "#RIGHT_IP#";
    public static final String RIGHT_SUBNET_KEY = "#RIGHT_SUBNET#";
    public static final String IS_FEDERATED_VM_KEY = "#IS_FEDERATED_VM#";
    public static final String PRE_SHARED_KEY_KEY = "#PRE_SHARED_KEY#";
    public static final String FEDERATED_NETWORK_USER_DATA_TAG = "FNS_SCRIPT";

    public static final String AGENT_PUBLIC_IP = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);
    public static final String PRE_SHARED_KEY = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_PRE_SHARED_KEY_KEY);

    // DFNS TOKENS
    public static final String CIDR_KEY = "#CIDR#";
    public static final String GATEWAY_IP_KEY = "#GATEWAY_IP#";
    public static final String VLAN_ID_KEY = "#VLAN_ID#";
    public static final String FEDERATED_IP_KEY = "#FEDERATED_IP#";
    public static final String AGENT_USER_KEY = "#AGENT_USER#";
    public static final String PRIVATE_KEY_KEY = "#PRIVATE_KEY#";
    public static final String PUBLIC_KEY_KEY = "#PUBLIC_KEY#";

    public static void addUserData(FederatedCompute fnsCompute, String federatedComputeIp,
                                           String cidr) throws IOException {
        UserData newUserData = getVanillaUserData(federatedComputeIp, cidr);
        cloud.fogbow.ras.api.parameters.Compute rasCompute = fnsCompute.getCompute();
        List<UserData> userDataList = rasCompute.getUserData();
        if (userDataList == null) {
            userDataList = new ArrayList<>();
            rasCompute.setUserData((ArrayList<UserData>) userDataList);
        }
        userDataList.add(newUserData);
    }

    @NotNull
    public static UserData getVanillaUserData(String federatedIp, String cidr) throws IOException {
        InputStream inputStream = new FileInputStream(IPSEC_INSTALLATION_PATH);
        String cloudInitScript = IOUtils.toString(inputStream);
        String newScript = replaceScriptValues(cloudInitScript, federatedIp, AGENT_PUBLIC_IP, cidr, PRE_SHARED_KEY);
        byte[] scriptBytes = newScript.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedScriptBytes = Base64.encodeBase64(scriptBytes);
        String encryptedScript = new String(encryptedScriptBytes, StandardCharsets.UTF_8);

        return new UserData(encryptedScript,
                CloudInitUserDataBuilder.FileType.SHELL_SCRIPT, FEDERATED_NETWORK_USER_DATA_TAG);
    }

    @NotNull
    public static UserData getDfnsUserData(DfnsAgentConfiguration configuration, String federatedIp, String agentIp, int vlanId, String accessKey) throws IOException, GeneralSecurityException {
        String scriptKey = ConfigurationPropertyKeys.CREATE_TUNNEL_FROM_COMPUTE_TO_AGENT_SCRIPT_PATH;
        String createTunnelScriptPath = PropertiesHolder.getInstance().getProperty(scriptKey);
        InputStream inputStream = new FileInputStream(createTunnelScriptPath);
        String templateScript = IOUtils.toString(inputStream);

        Map<String, String> scriptTokenValues = new HashMap<>();
        scriptTokenValues.put(CIDR_KEY, configuration.getDefaultNetworkCidr());
        scriptTokenValues.put(GATEWAY_IP_KEY, agentIp);
        scriptTokenValues.put(VLAN_ID_KEY, String.valueOf(vlanId));
        scriptTokenValues.put(FEDERATED_IP_KEY, federatedIp);
        scriptTokenValues.put(AGENT_USER_KEY, configuration.getAgentUser());
        scriptTokenValues.put(PRIVATE_KEY_KEY, accessKey);
        scriptTokenValues.put(PUBLIC_KEY_KEY, configuration.getPublicKey());

        String cloudInitScript = replaceScriptTokens(templateScript, scriptTokenValues);
        System.out.printf("here's the cloudinit script to run on compute ++++ " + cloudInitScript);

        byte[] scriptBytes = cloudInitScript.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedScriptBytes = Base64.encodeBase64(scriptBytes);
        String encryptedScript = new String(encryptedScriptBytes, StandardCharsets.UTF_8);

        return new UserData(encryptedScript,
                CloudInitUserDataBuilder.FileType.SHELL_SCRIPT, FEDERATED_NETWORK_USER_DATA_TAG);
    }

    private static String replaceScriptValues(String script, String federatedComputeIp, String agentPublicIp,
                                              String cidr, String preSharedKey) {
        String isFederatedVM = "true";
        String scriptReplaced = script.replace(IS_FEDERATED_VM_KEY, isFederatedVM);
        scriptReplaced = scriptReplaced.replace(LEFT_SOURCE_IP_KEY, federatedComputeIp);
        scriptReplaced = scriptReplaced.replace(RIGHT_IP, agentPublicIp);
        scriptReplaced = scriptReplaced.replace(RIGHT_SUBNET_KEY, cidr);
        scriptReplaced = scriptReplaced.replace(PRE_SHARED_KEY_KEY, preSharedKey);
        scriptReplaced = scriptReplaced.replace("\n", "[[\\n]]");
        scriptReplaced = scriptReplaced.replace("\r", "");
        return scriptReplaced;
    }

    private static String replaceScriptTokens(String scriptTemplate, Map<String, String> scriptTokenValues) {
        String result = scriptTemplate;
        for (String scriptToken : scriptTokenValues.keySet()) {
            result = result.replace(scriptToken, scriptTokenValues.get(scriptToken));
        }
        return result;
    }
}

