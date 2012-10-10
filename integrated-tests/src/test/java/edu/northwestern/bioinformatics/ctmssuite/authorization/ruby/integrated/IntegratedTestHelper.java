package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.integrated;

import gov.nih.nci.cabig.ctms.CommonsSystemException;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class IntegratedTestHelper {
    private static final Logger log = LoggerFactory.getLogger(IntegratedTestHelper.class);

    private static final long SERVICE_WAIT_TIMEOUT = 30 * 1000L;

    private static File projectRoot;
    private BundleContext bundleContext;

    public IntegratedTestHelper(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private BundleContext getBundleContext() {
        return bundleContext;
    }

    public static File getModuleRelativeFile(String moduleName, String directory) throws IOException {
        File dir = new File(findProjectRootDirectory(), moduleName.replaceAll(":", "/"));
        dir = new File(dir, directory);
        if (dir.exists()) return dir;

        throw new FileNotFoundException(
            String.format("Could not find directory %s relative to module %s from project directory %s",
                directory, moduleName, findProjectRootDirectory().getCanonicalPath()));
    }

    private synchronized static File findProjectRootDirectory() throws FileNotFoundException {
        if (projectRoot == null) {
            File buildfile;
            projectRoot = new File(".");
            do {
                buildfile = new File(projectRoot, "buildfile");
                if (buildfile.exists()) {
                    return projectRoot;
                }
                projectRoot = new File(projectRoot, "..");
            } while (projectRoot.exists() && projectRoot.isDirectory());

            projectRoot = null;
            throw new FileNotFoundException(
                String.format("Could not find project directory.  Started from %s and walked up to %s.",
                    new File("."), projectRoot));
        }
        return projectRoot;
    }

    public Bundle findBundle(String bundleName) throws IOException {
        for (Bundle candidate : getBundleContext().getBundles()) {
            if (candidate.getSymbolicName() == null) {
                throw new NullPointerException(
                    String.format("Bundle %s (%s) has no symbolic name",
                        candidate, candidate.getBundleId()));
            }
            if (candidate.getSymbolicName().equals(bundleName)) {
                return candidate;
            }
        }

        List<String> bundles = new ArrayList<String>();
        for (Bundle bundle : getBundleContext().getBundles()) {
            bundles.add(bundle.getSymbolicName());
        }

        throw new CommonsSystemException("No bundle %s in the testing context:\n- %s",
            bundleName, StringUtils.join(bundles.iterator(), ("\n- ")));
    }

    @SuppressWarnings({ "unchecked" })
    public <T> T getService(Class<T> anInterface) {

        ServiceReference ref = null;
        long start = System.currentTimeMillis();
        while (ref == null && System.currentTimeMillis() - start < SERVICE_WAIT_TIMEOUT) {
            ref = bundleContext.getServiceReference(anInterface.getName());
            try { Thread.sleep(500); } catch (InterruptedException ie) { /* DC */ }
        }
        if (ref == null) {
            throw new IllegalStateException(
                "After " + SERVICE_WAIT_TIMEOUT + "ms, no service registered for " + anInterface.getName());
        }
        return (T) bundleContext.getService(ref);
    }
}
