package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.integrationtests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(JUnit4TestRunner.class)
public class SourceUsableTest {
    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] config() {
        return options(
            junitBundles()
        );
    }

    @Test
    public void itExportsASuiteAuthorizationSource() throws Exception {
        assertThat("No source exported",
            bundleContext.getServiceReference("gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource"),
            is(not(nullValue()))
        );
    }
}