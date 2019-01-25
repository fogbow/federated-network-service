package cloud.fogbow.fns.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import cloud.fogbow.fns.api.parameters.Compute;

import org.fogbowcloud.ras.core.models.UserData;
import org.fogbowcloud.ras.core.plugins.interoperability.util.CloudInitUserDataBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FederatedComputeUtil {

    private static final String IPSEC_INSTALLATION_PATH = "bin/ipsec-configuration";
    public static final String LEFT_SOURCE_IP_KEY = "#LEFT_SOURCE_IP#";
    public static final String RIGHT_IP = "#RIGHT_IP#";
    public static final String RIGHT_SUBNET_KEY = "#RIGHT_SUBNET#";
    public static final String IS_FEDERATED_VM_KEY = "#IS_FEDERATED_VM#";
    public static final String PRE_SHARED_KEY = "#PRE_SHARED_KEY#";
    public static final String FEDERATED_NETWORK_USER_DATA_TAG = "FNS_SCRIPT";

    public static void addUserData(Compute fnsCompute, String federatedComputeIp, String agentPublicIp,
                                           String cidr, String preSharedKey) throws IOException {
        InputStream inputStream = new FileInputStream(IPSEC_INSTALLATION_PATH);
        String cloudInitScript = IOUtils.toString(inputStream);
        String newScript = replaceScriptValues(cloudInitScript, federatedComputeIp, agentPublicIp, cidr, preSharedKey);
        byte[] scriptBytes = newScript.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedScriptBytes = Base64.encodeBase64(scriptBytes);
        String encryptedScript = new String(encryptedScriptBytes, StandardCharsets.UTF_8);

        UserData newUserData = new UserData(encryptedScript,
                CloudInitUserDataBuilder.FileType.SHELL_SCRIPT, FEDERATED_NETWORK_USER_DATA_TAG);
        org.fogbowcloud.ras.api.parameters.Compute rasCompute = fnsCompute.getCompute();
        List<UserData> userDataList = rasCompute.getUserData();
        if (userDataList == null) {
            userDataList = new ArrayList<UserData>();
            rasCompute.setUserData((ArrayList<UserData>) userDataList);
        }
        userDataList.add(newUserData);
    }

    private static String replaceScriptValues(String script, String federatedComputeIp, String agentPublicIp,
                                              String cidr, String preSharedKey) {
        String isFederatedVM = "true";
        String scriptReplaced = script.replace(IS_FEDERATED_VM_KEY, isFederatedVM);
        scriptReplaced = scriptReplaced.replace(LEFT_SOURCE_IP_KEY, federatedComputeIp);
        scriptReplaced = scriptReplaced.replace(RIGHT_IP, agentPublicIp);
        scriptReplaced = scriptReplaced.replace(RIGHT_SUBNET_KEY, cidr);
        scriptReplaced = scriptReplaced.replace(PRE_SHARED_KEY, preSharedKey);
        scriptReplaced = scriptReplaced.replace("\n", "[[\\n]]");
        scriptReplaced = scriptReplaced.replace("\r", "");
        return scriptReplaced;
    }
}

