package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.integrated;

import edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal.RubySuiteAuthorizationSourceFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import org.junit.Before;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * @author Rhett Sutphin
 */
public abstract class CtmsAuthRubyIntegratedTestCase {
    @Inject
    protected BundleContext bundleContext;

    private ConfigurationAdmin configurationAdmin;
    private IntegratedTestHelper helper;

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
        props.put("sourceScript", getSourceScriptPath());
        config.update(props);
    }

    protected abstract String getSourceScriptPath() throws IOException;

    protected SuiteAuthorizationSource actualSource() {
        return helper.getService(SuiteAuthorizationSource.class);
    }
}
