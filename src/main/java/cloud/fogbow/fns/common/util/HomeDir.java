package cloud.fogbow.fns.common.util;

public class HomeDir {
    public static final String PRIVATE_DIRECTORY = "private/";

    public static String getPath() {
        return Thread.currentThread().getContextClassLoader().getResource("").getPath() + PRIVATE_DIRECTORY;
    }
}
