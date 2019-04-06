package cloud.fogbow.fns;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ApplicationFacadeTest extends MockedFederatedNetworkUnitTests {
    private final String FEDERATED_NETWORK_ID = "fake-network-id";
    private final String USER_ID = "fake-user-id";
    private final String USER_NAME = "fake-user-name";
    private final String TOKEN_PROVIDER = "token-provider";

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
