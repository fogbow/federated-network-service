package cloud.fogbow.fns.core.drivers;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.BashScriptRunner;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.exceptions.AgentCommunicationException;
import org.apache.log4j.Logger;

import java.nio.file.Paths;
import java.util.UUID;

public abstract class CommonServiceDriver implements ServiceDriver {
    private static final Logger LOGGER = Logger.getLogger(CommonServiceDriver.class);

    private final String GEN_KEY_PAIR_SCRIPT_PATH_FROM_BIN = "/bin/agent-scripts/generateSshKeyPair";
    private final String GEN_KEY_PAIR_SCRIPT_WHOLE_PATH = Paths.get("").toAbsolutePath().toString() + GEN_KEY_PAIR_SCRIPT_PATH_FROM_BIN;
    private final String KEY_PAIR_SEPARATOR = "KEY SEPARATOR";
    private final String KEY_SIZE = "1024";
    protected static final int PUBLIC_KEY_INDEX = 0;
    protected static final int PRIVATE_KEY_INDEX = 1;

    protected String[] generateSshKeyPair() throws UnexpectedException {
        BashScriptRunner runner = new BashScriptRunner();
        String keyName = String.valueOf(UUID.randomUUID());

        // The key's size is passed as parameter and set to 1024 to keep the key small.
        String[] genCommand = {"bash", GEN_KEY_PAIR_SCRIPT_WHOLE_PATH, keyName, KEY_SIZE};
        BashScriptRunner.Output createCommandResult = runner.runtimeRun(genCommand);

        return new String[]{createCommandResult.getContent().split(KEY_PAIR_SEPARATOR)[PUBLIC_KEY_INDEX],
                createCommandResult.getContent().split(KEY_PAIR_SEPARATOR)[PRIVATE_KEY_INDEX]};
    }

    public String pasteScript(String scriptFilePath, String hostIp, String hostScriptPath, String permissionFile, String remoteUser) throws FogbowException {
        String randomScriptSuffix = UUID.randomUUID().toString();
        String remoteFilePath = hostScriptPath + randomScriptSuffix;
        ProcessBuilder builder = new ProcessBuilder("scp", "-o", "UserKnownHostsFile=/dev/null", "-o", "StrictHostKeyChecking=no", "-i", permissionFile,
                scriptFilePath, remoteUser + "@" + hostIp + ":" + remoteFilePath);

        int resultCode = 0;
        try {
            Process process = builder.start();
            resultCode = process.waitFor();
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.Error.UNABLE_TO_COPY_FILE_REMOTLY, resultCode), e);
        }

        if(resultCode != 0) {
            throw new AgentCommunicationException(String.format(Messages.Error.UNABLE_TO_COPY_FILE_REMOTLY, resultCode));
        }

        return remoteFilePath;
    }
}
