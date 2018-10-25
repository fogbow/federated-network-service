package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.constants.SystemConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ApplicationFacadeTests {
    private String VALID_PATH_CONF = "fns.conf";
    private String VALID_PATH_CONF_WITHOUT_BUILD_PROPERTY = "fns-without-build-number.conf";
    private ApplicationFacade applicationFacade;

    @Before
    public void setUp() {
        this.applicationFacade = ApplicationFacade.getInstance();
    }

    @Test
    public void testVersion() throws Exception {
        // Setup
        this.applicationFacade.setBuildNumber(this.VALID_PATH_CONF);

        // Exercise
        String build = this.applicationFacade.getVersionNumber();

        // Test
        Assert.assertEquals(SystemConstants.API_VERSION_NUMBER + "-" + "abcd", build);
    }

    @Test
    public void testVersionWithoutBuildProperty() throws Exception {
        // Setup
        this.applicationFacade.setBuildNumber(this.VALID_PATH_CONF_WITHOUT_BUILD_PROPERTY);

        // Exercise
        String build = this.applicationFacade.getVersionNumber();

        // Test
        Assert.assertTrue(build.equals(SystemConstants.API_VERSION_NUMBER + "-" + "[testing mode]"));
    }
}
