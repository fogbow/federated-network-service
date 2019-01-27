package cloud.fogbow.fns;

import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.fns.core.constants.SystemConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ApplicationFacadeTests {
    private ApplicationFacade applicationFacade;

    @Before
    public void setUp() {
        this.applicationFacade = ApplicationFacade.getInstance();
    }

    @Test
    public void testVersion() {
        // Exercise
        String build = this.applicationFacade.getVersionNumber();

        // Test
        Assert.assertEquals(SystemConstants.API_VERSION_NUMBER + "-" + "abcd", build);
    }
}
