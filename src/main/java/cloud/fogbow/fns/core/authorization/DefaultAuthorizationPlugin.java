package cloud.fogbow.fns.core.authorization;

import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.fns.core.model.FnsOperation;

public class DefaultAuthorizationPlugin implements AuthorizationPlugin<FnsOperation> {

    @Override
    public boolean isAuthorized(SystemUser systemUser, FnsOperation operation) {
        return true;
    }

    @Override
    public void setPolicy(String policy) {
        // Ignore
    }

    @Override
    public void updatePolicy(String policy) {
        // Ignore
    }
}
