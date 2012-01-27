package edu.northwestern.bioinformatics.aker.ctmssuite.internal;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class AkerSuiteAuthorizationSource implements SuiteAuthorizationSource {
    @Override
    public SuiteUser getUser(String username, SuiteUserRoleLevel suiteUserRoleLevel) {
        throw new UnsupportedOperationException("getUser not implemented");
    }

    @Override
    public SuiteUser getUser(long id, SuiteUserRoleLevel suiteUserRoleLevel) {
        throw new UnsupportedOperationException("getUser not implemented");
    }

    @Override
    public Collection<SuiteUser> getUsersByRole(SuiteRole suiteRole) {
        throw new UnsupportedOperationException("getUsersByRole not implemented");
    }

    @Override
    public Collection<SuiteUser> searchUsers(SuiteUserSearchOptions suiteUserSearchOptions) {
        throw new UnsupportedOperationException("searchUsers not implemented");
    }
}
