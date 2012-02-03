package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby;

import edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal.RubySuiteAuthorizationSourceFactory;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;

import java.util.Collections;

public class Activator implements BundleActivator {
    private ServiceRegistration factoryRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        RubySuiteAuthorizationSourceFactory factory =
            new RubySuiteAuthorizationSourceFactory(context.getBundle());
        factoryRegistration = context.registerService(ManagedServiceFactory.class.getName(), factory,
            new MapBasedDictionary<String, String>(Collections.singletonMap(
                Constants.SERVICE_PID,
                RubySuiteAuthorizationSourceFactory.FACTORY_PID)));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        factoryRegistration.unregister();
    }
}