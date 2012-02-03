package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal;

import org.osgi.framework.Bundle;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class RubySuiteAuthorizationSourceFactory implements ManagedServiceFactory {
    public static final String FACTORY_PID = "ctmssuite.authorization.ruby";

    private Map<String, RubySuiteAuthorizationSourceManager> managers;
    private final Bundle homeBundle;

    public RubySuiteAuthorizationSourceFactory(Bundle homeBundle) {
        this.homeBundle = homeBundle;
        this.managers = new HashMap<String, RubySuiteAuthorizationSourceManager>();
    }

    @Override
    public String getName() {
        return "Factory for ruby-implemented SuiteAuthorizationSources";
    }

    @Override
    public void updated(String pid, Dictionary properties) throws ConfigurationException {
        String path = (String) properties.get("sourceScript");
        if (managers.containsKey(pid)) {
            managers.get(pid).updateSourceScript(path);
        } else {
            managers.put(pid, new RubySuiteAuthorizationSourceManager(homeBundle, path));
        }
    }

    @Override
    public void deleted(String pid) {
        if (managers.containsKey(pid)) {
            managers.remove(pid).stop();
        }
    }
}
