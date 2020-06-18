package cloud.fogbow.fns.core.drivers;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnavailableProviderException;
import cloud.fogbow.fns.constants.Messages;
import org.apache.log4j.Logger;

import java.util.UUID;

public abstract class CommonServiceDriver implements ServiceDriver {
    private static final Logger LOGGER = Logger.getLogger(CommonServiceDriver.class);

    public String pasteScript(String scriptFilePath, String hostIp, String hostScriptPath, String permissionFile, String remoteUser) throws FogbowException {
        String randomScriptSuffix = UUID.randomUUID().toString();
        String remoteFilePath = hostScriptPath + randomScriptSuffix;
        ProcessBuilder builder = new ProcessBuilder("scp", "-o", "UserKnownHostsFile=/dev/null", "-o", "StrictHostKeyChecking=no", "-i", permissionFile,
                scriptFilePath, remoteUser + "@" + hostIp + ":" + remoteFilePath);
        LOGGER.info("Trying to copy script (" + remoteFilePath + "): " + builder.command());

        int resultCode = 0;
        try {
            Process process = builder.start();
            resultCode = process.waitFor();
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.Log.UNABLE_TO_COPY_FILE_REMOTELY, resultCode), e);
        }

        if(resultCode != 0) {
            throw new UnavailableProviderException(String.format(Messages.Exception.UNABLE_TO_COPY_FILE_REMOTELY, resultCode));
        }

        return remoteFilePath;
    }
}
