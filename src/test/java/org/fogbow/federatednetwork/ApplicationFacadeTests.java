package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.constants.SystemConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

public class ApplicationFacadeTests {
    private String VALID_PATH_CONF = "src/test/resources/private/fns-with-build.conf";
    private String VALID_PATH_CONF_WITHOUT_BUILD_PROPERTY = "src/test/resources/private/fns.conf";
    private String INVALID_PATH_CONF = "invalid.conf";
    private ApplicationFacade applicationFacade;

    @Before
    public void setUp() {
        this.applicationFacade = ApplicationFacade.getInstance();
    }

    @Test
    public void testVersion() throws Exception {
        String build = this.applicationFacade.getVersionNumber(this.VALID_PATH_CONF);

        Assert.assertEquals(SystemConstants.API_VERSION_NUMBER + "-" + "abcd", build);
    }

    @Test
    public void testVersionWithoutBuildProperty() throws Exception {
        String build = this.applicationFacade.getVersionNumber(this.VALID_PATH_CONF_WITHOUT_BUILD_PROPERTY);

        Assert.assertTrue(build.equals(SystemConstants.API_VERSION_NUMBER + "-" + "[testing mode]"));
    }

    @Test(expected = FileNotFoundException.class)
    public void testListMembersWithInvalidConfPath() throws FileNotFoundException {
        this.applicationFacade.getVersionNumber(this.INVALID_PATH_CONF);
    }
}
