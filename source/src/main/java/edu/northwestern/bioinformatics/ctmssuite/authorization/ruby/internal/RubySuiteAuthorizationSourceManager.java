package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal;

import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Rhett Sutphin
 */
public class RubySuiteAuthorizationSourceManager {
    private ServiceRegistration sourceReg;
    private String currentSourceScript;
    private Bundle bundle;

    public RubySuiteAuthorizationSourceManager(Bundle bundle, String sourceScript) {
        this.bundle = bundle;
        updateSourceScript(sourceScript);
    }

    public void updateSourceScript(String sourceScript) {
        if (!sourceScript.equals(currentSourceScript)) {
            stop();
            sourceReg = bundle.getBundleContext().registerService(
                SuiteAuthorizationSource.class.getName(), buildNewSource(sourceScript), null);
            currentSourceScript = sourceScript;
        }
    }

    private RubySuiteAuthorizationSource buildNewSource(String sourceScript) {
        ScriptingContainer container = new ScriptingContainer(
            LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        EmbedEvalUnit evalUnit = container.parse(PathType.ABSOLUTE, sourceScript);
        evalUnit.run();
        return new RubySuiteAuthorizationSource(container);
    }

    public void stop() {
        if (sourceReg != null) {
            sourceReg.unregister();
            sourceReg = null;
            currentSourceScript = null;
        }
    }
}
