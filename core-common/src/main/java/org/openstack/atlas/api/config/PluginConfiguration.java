package org.openstack.atlas.api.config;

import org.apache.log4j.Logger;
import org.openstack.atlas.common.config.Configuration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PluginConfiguration {
    private static final Logger LOG = Logger.getLogger(PluginConfiguration.class);

    private static final String CLASSPATH = "classpath:";

    /**
     * Returns the adapter name (eg. null, zeus, netscaler etc) specified in the configuration file.
     */
    public static String getAdapterPrefix() {
        Configuration configuration = new RestApiConfiguration();
        return configuration.getString(PublicApiServiceConfigurationKeys.adapter);
    }

    /**
     *  Returns the extension prefix name (eg. rax, citrix) if any  specified in the configuration file.
     */
    public static String getExtensionPrefix() {
        Configuration configuration = new RestApiConfiguration();
        String extensions = configuration.getString(PublicApiServiceConfigurationKeys.extensions);
        return extensions;
    }

    /**
     *  Returns a list of spring configuration files required by both core and extension deployment.
     */
    public static List<String> getCommonContexts() {
        List<String> contexts = new ArrayList<String>();
        contexts.add("api-context.xml");
        return contexts;
    }

    /**
     * Returns a list of all spring configurations files required by the core.
     */
    public static List<String> getCoreContexts(String adapterName) {
        List<String> core_contexts = new ArrayList<String>();
        core_contexts.add("api-context.xml");
        core_contexts.add("persistence-context.xml");
        core_contexts.add("data-model-context.xml");
        //contexts.add("dozer-context.xml");
        core_contexts.add(adapterName + "-adapter-context.xml");
        validateCoreContexts(core_contexts);

        return core_contexts;
    }


    /**
     * Returns a list of all spring configurations files (including optional ones) required by the core.
     */
    public static List<String> getCoreAndOptionalContexts(String adapterName) {

        List<String> coreContexts = getCoreContexts(adapterName);

        // The adapter layer may have a persistence layer or not.
        List<String> optional_contexts = new ArrayList<String>();
        optional_contexts.add("adapter-persistence-context.xml");
        validateOptionalContexts(optional_contexts);
        List<String> allContexts = new ArrayList<String>(coreContexts);
        allContexts.addAll(optional_contexts);

        return allContexts;
    }
    /**
     * Prepends each of the core configuration file names with the extension name to derive the extension configuration
     * file name as per the naming convention. Eg. persistence-context.xml will become rax-persistence-context.xml
     */
    public static List<String> getExtensionContexts(String extensionName, String adapterName) {
        List<String> extensionContexts = new ArrayList<String>();
        List<String> coreContexts = getCoreContexts(adapterName);
        for (String context : coreContexts) {
            extensionContexts.add(extensionName + "-" + context);
        }

        // The adapter layer may have a persistence layer or not.
        extensionContexts.add("adapter-persistence-context.xml");

        return extensionContexts;
    }

    /**
     * Checks if all of the given core configuration files exist in classpath, else throws runtime error. And this will
     * prevent the war from being deployed as all configuration files are required.
     */
    public static void validateCoreContexts(List<String> contexts) {
        for (String context : contexts) {
            if (!doesContextExist(context)) {
                LOG.warn("All of the core configuration files must exist.");
                throw new RuntimeException("Missing a required Configuration file: " + context);
            }
        }
    }

    /**
     *
     */
    public static void validateOptionalContexts(List<String> contexts) {
        for (String context : contexts) {
            if (!doesContextExist(context)) {
                LOG.warn("Could not find the configuration file " + context + ". Quietly ignoring it .....");
            }
        }
    }

    /**
     * Prepends all of the configuration file names with classpath: coz thats what the spring application context likes.
     */
    public static List<String> classpathify(List<String> contexts) {
        List<String> classpathContexts = new ArrayList<String>();
        for(String context: contexts) {
            classpathContexts.add(CLASSPATH + context);
        }
        return classpathContexts;
    }

    /**
     *  Checks if the configuration file is in the classloader. This means that the file exists.
     */
    private static boolean doesContextExist(String context) {
        URL url = PluginConfiguration.class.getClassLoader().getResource(context);
        if (url != null) {
            return true;
        }
        return false;
    }
}
