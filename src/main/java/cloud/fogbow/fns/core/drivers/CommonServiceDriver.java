package cloud.fogbow.fns.core.drivers;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.BashScriptRunner;

import java.nio.file.Paths;
import java.util.UUID;

public abstract class CommonServiceDriver implements ServiceDriver {
    private final String GEN_KEY_PAIR_SCRIPT_PATH_FROM_BIN = "/bin/agent-scripts/dfns/generateSshKeyPair";
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


}
