package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.integrated;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import java.io.IOException;
import java.util.Collection;

import static edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.integrated.IntegratedTestHelper.*;
import static edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.integrated.SuiteUserCollectionMatcher.suiteUsers;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(JUnit4TestRunner.class)
public class SourceUsableTest extends CtmsAuthRubyIntegratedTestCase {
    @Override
    protected String getSourceScriptPath() throws IOException {
        return getModuleRelativeFile("source", "src/test/resources/test_source.rb").getCanonicalPath();
    }

    @Test
    public void itExportsASuiteAuthorizationSource() throws Exception {
        assertThat("No source exported",
            bundleContext.getServiceReference("gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource"),
            is(not(nullValue()))
        );
    }

    @Test
    public void itCanGetAUserByUsername() throws Exception {
        SuiteUser alice = actualSource().getUser("alice", SuiteUserRoleLevel.ROLES_AND_SCOPES);
        assertEquals("Anderson", alice.getLastName());
    }

    @Test
    public void itCanGetAUserById() throws Exception {
        SuiteUser alice = actualSource().getUser(4, SuiteUserRoleLevel.ROLES_AND_SCOPES);
        assertEquals("alice", alice.getUsername());
    }

    @Test
    public void itCanGetUsersByRole() throws Exception {
        Collection<SuiteUser> actual = actualSource().getUsersByRole(SuiteRole.STUDY_CREATOR);
        assertThat(actual, is(suiteUsers("alice", "cat")));
    }

    @Test
    public void itCanSearchUsers() throws Exception {
        Collection<SuiteUser> actual = actualSource().searchUsers(SuiteUserSearchOptions.forAllNames("cath"));
        assertThat(actual, is(suiteUsers("cat")));
    }

}