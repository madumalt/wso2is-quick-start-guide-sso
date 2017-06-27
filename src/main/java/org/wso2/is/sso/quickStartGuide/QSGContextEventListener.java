package org.wso2.is.sso.quickStartGuide;

import org.wso2.carbon.identity.sso.agent.SSOAgentConstants;
import org.wso2.carbon.identity.sso.agent.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;
import org.wso2.carbon.identity.sso.agent.saml.SSOAgentX509Credential;
import org.wso2.carbon.identity.sso.agent.saml.SSOAgentX509KeyStoreCredential;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by thilina on 6/22/17.
 */
public class QSGContextEventListener implements ServletContextListener {

    private static Properties properties = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        properties = new Properties();
        try {

            // if swift.com load the swift property file, otherwise load default dispatch property file
            if(servletContextEvent.getServletContext().getContextPath().contains("swift.com")) {
                properties.load(servletContextEvent.getServletContext().getResourceAsStream(
                        "/WEB-INF/classes/swift.properties"));
            } else {
                properties.load(servletContextEvent.getServletContext().
                        getResourceAsStream("/WEB-INF/classes/dispatch.properties"));
            }

            InputStream keyStoreInputStream = servletContextEvent.getServletContext().
                    getResourceAsStream("/WEB-INF/classes/wso2carbon.jks");
            SSOAgentX509Credential credential = new SSOAgentX509KeyStoreCredential(keyStoreInputStream,
                            properties.getProperty("KeyStorePassword").toCharArray(),
                            properties.getProperty("IdPPublicCertAlias"),
                            properties.getProperty("PrivateKeyAlias"),
                            properties.getProperty("PrivateKeyPassword").toCharArray());

            SSOAgentConfig config = new SSOAgentConfig();
            config.initConfig(properties);
            config.getSAML2().setSSOAgentX509Credential(credential);
            servletContextEvent.getServletContext().
                    setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);

        } catch (IOException e) {
            System.out.println("IOException in reading .properties: " + e.getStackTrace());
        } catch (SSOAgentException e){
            System.out.println("SSOAgentException in contextInitialized handler: " + e.getStackTrace());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    public static Properties getProperties() {
        return properties;
    }
}
