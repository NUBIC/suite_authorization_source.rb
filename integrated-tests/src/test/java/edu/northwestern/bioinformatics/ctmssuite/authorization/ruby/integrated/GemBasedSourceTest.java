package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.integrated;

import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import java.io.IOException;

import static edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.integrated.IntegratedTestHelper.getModuleRelativeFile;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 * Uses the sample <code>AkerCtmsAuthorizationSource</code> script to demonstrate
 * that a ctms-auth-ruby deployment can use properly bundled rubygems. Look at the
 * integrated-test-gems module in the buildfile to see how the gems are packaged.
 *
 * @author Rhett Sutphin
 */
@RunWith(JUnit4TestRunner.class)
public class GemBasedSourceTest extends CtmsAuthRubyIntegratedTestCase {
    @Override
    protected String getSourceScriptPath() throws IOException {
        return getModuleRelativeFile("integrated-tests",
            "src/test/resources/aker_ctms_auth_source.rb").getCanonicalPath();
    }

    @Test
    public void itCanFindAUserByUsername() throws Exception {
        assertThat(actualSource().getUser("zelda", SuiteUserRoleLevel.NONE).getId(), is(729));
    }

    @Test
    public void itCanFindAUserById() throws Exception {
        assertThat(actualSource().getUser(625, SuiteUserRoleLevel.ROLES_AND_SCOPES).getUsername(),
            is("xia"));
    }
}
