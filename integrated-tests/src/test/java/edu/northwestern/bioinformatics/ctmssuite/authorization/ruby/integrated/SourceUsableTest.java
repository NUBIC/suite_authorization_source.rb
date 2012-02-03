package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.integrated;

import edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal.RubySuiteAuthorizationSourceFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.inject.Inject;
import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(JUnit4TestRunner.class)
public class SourceUsableTest {
    @Inject
    private BundleContext bundleContext;

    private ConfigurationAdmin configurationAdmin;

    @org.ops4j.pax.exam.junit.Configuration
    public Option[] config() {
        return options(
            composite(buildrArtifactBundles()),
            junitBundles()
        );
    }

    private Option[] buildrArtifactBundles() {
        String[] startBundleArtifacts = System.getProperty("startBundleArtifacts").split(",");
        String[] noStartBundleArtifacts = System.getProperty("noStartBundleArtifacts").split(",");
        Option[] bundleOptions = new Option[startBundleArtifacts.length + noStartBundleArtifacts.length];
        for (int i = 0; i < startBundleArtifacts.length; i++) {
            String bundleFilename = startBundleArtifacts[i];
            System.out.println("Selecting to install & start " + bundleFilename);
            bundleOptions[i] = bundle(new File(bundleFilename).toURI().toString()).start();
        }
        for (int i = 0; i < noStartBundleArtifacts.length; i++) {
            String bundleFilename = noStartBundleArtifacts[i];
            System.out.println("Selecting to install only " + bundleFilename);
            bundleOptions[i + startBundleArtifacts.length] =
                bundle(new File(bundleFilename).toURI().toString()).noStart();
        }
        return bundleOptions;
    }

    private IntegratedTestHelper helper;

    @Before
    public void before() throws Exception {
        helper = new IntegratedTestHelper(bundleContext);

        // service injection ungets the service after injecting it, which is pretty damn useless
        configurationAdmin = helper.getService(ConfigurationAdmin.class);

        Bundle rubyAuthBundle = helper.findBundle("edu.northwestern.bioinformatics.ctms-auth-ruby.source");

        Configuration config = configurationAdmin.createFactoryConfiguration(
            RubySuiteAuthorizationSourceFactory.FACTORY_PID, rubyAuthBundle.getLocation());
        if (config == null) {
            fail("Factory " + RubySuiteAuthorizationSourceFactory.FACTORY_PID + " not available.");
        }
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("sourceScript",
            IntegratedTestHelper.getModuleRelativeFile("source", "src/test/resources/test_source.rb").getCanonicalPath());
        config.update(props);

        // TODO: is there an event to listen for instead?
        Thread.sleep(5000);
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

//    @Test @Ignore
    public void itCanGetAUserById() throws Exception {
        SuiteUser alice = actualSource().getUser(4, SuiteUserRoleLevel.ROLES_AND_SCOPES);
        assertEquals("alice", alice.getUsername());
    }

    private SuiteAuthorizationSource actualSource() {
        return helper.getService(SuiteAuthorizationSource.class);
    }
}