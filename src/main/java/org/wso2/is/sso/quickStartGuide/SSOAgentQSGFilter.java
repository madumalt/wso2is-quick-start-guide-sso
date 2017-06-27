package org.wso2.is.sso.quickStartGuide;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.XMLObject;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.sso.agent.SSOAgentConstants;
import org.wso2.carbon.identity.sso.agent.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.SSOAgentFilter;
import org.wso2.carbon.identity.sso.agent.SSOAgentRequestResolver;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Created by thilina on 6/22/17.
 */
public class SSOAgentQSGFilter extends SSOAgentFilter {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CHARACTER_ENCODING = "UTF-8";

    private static Properties properties = null;
    protected FilterConfig filterConfig = null;

    static {
        properties = QSGContextEventListener.getProperties();
    }

    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        this.filterConfig = fConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        //get the configs initialized at servlet context initialization
        SSOAgentConfig config = (SSOAgentConfig)filterConfig.getServletContext().
                getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);

        //use http-post biding for SAML2 binding
        String httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
        config.getSAML2().setHttpBinding(httpBinding);

        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        //if request is a passive saml2 response, and authentication is a failure redirect to login page
        if(shouldDirectToLoginPage(request, response, config)) {
            response.sendRedirect(config.getSAML2SSOURL());
            return;
        }

        servletRequest.setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);
        super.doFilter(servletRequest, servletResponse, chain);

        if(isLogoutResponse(request, response, config)){
            response.sendRedirect(config.getSAML2SSOURL());
        }
    }

    /**
     * Refine this method, just a draft, have lot of inefficiencies
     * @param request
     * @param response
     * @param config
     * @return Boolean
     */
    private Boolean shouldDirectToLoginPage (HttpServletRequest request,
                                      HttpServletResponse response,
                                      SSOAgentConfig config) {
        try {
            SSOAgentRequestResolver resolver = new SSOAgentRequestResolver(request, response, config);
            if(!resolver.isSLORequest() && resolver.isSAML2SSOResponse()){
                String saml2ResponseString = new String(org.opensaml.xml.util.Base64.decode(
                        request.getParameter("SAMLResponse")), Charset.forName("UTF-8"));

                XMLObject SAML2response = SSOAgentUtils.unmarshall(saml2ResponseString);
                NodeList list = SAML2response.getDOM().getElementsByTagNameNS(
                        "urn:oasis:names:tc:SAML:2.0:protocol", "Response");

                // if a logout response allow normal flow
                if(SAML2response instanceof LogoutResponse) {
                    return  false;
                }

                if(list.getLength() > 0) {
                    System.out.println("Invalid schema for the SAML2 response. Multiple Response elements found.");
                    //throw new SSOAgentException("Error occurred while processing SAML2 response.");
                    return true;
                } else {
                    NodeList assertionList = SAML2response.getDOM().getElementsByTagNameNS(
                            "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");
                    if(assertionList.getLength() > 1) {
                        System.out.println(("Invalid schema for the SAML2 response. Multiple Assertion elements found" +
                                "."));
                        //throw new SSOAgentException("Error occurred while processing SAML2 response.");
                        return true;
                    } else {
                        Response saml2Response = (Response) SAML2response;
                        if(saml2Response.getAssertions() == null || saml2Response.getAssertions().isEmpty()){
                            //no SAML2 assertion found, redirect to login page
                            if (this.isNoPassive(saml2Response)) {
                                //"No Passive", SAML2 response
                                return true;
                            } else {
                                //throw new SSOAgentException("SAML2 Assertion not found in the Response");
                                return true;
                            }
                        } else {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
        } catch (SSOAgentException e) {
            System.out.println("cannot unmarshal " + e);
            return false;
        }
    }

    private Boolean isLogoutResponse (HttpServletRequest request,
                              HttpServletResponse response,
                              SSOAgentConfig config){
        try {
            SSOAgentRequestResolver resolver = new SSOAgentRequestResolver(request, response, config);
            if (!resolver.isSLORequest() && resolver.isSAML2SSOResponse()) {
                String saml2ResponseString = new String(org.opensaml.xml.util.Base64.decode(
                        request.getParameter("SAMLResponse")), Charset.forName("UTF-8"));

                XMLObject SAML2response = SSOAgentUtils.unmarshall(saml2ResponseString);

                // if a logout response allow normal flow
                if (SAML2response instanceof LogoutResponse) {
                    return true;
                }
            }
            return false;
        } catch (SSOAgentException e) {
            System.out.println("cannot unmarshal " + e);
            return false;
        }
    }

    /**
     * Checks whether a given saml2 response is a NoPassive response
     * @param response
     * @return boolean
     */
    protected boolean isNoPassive(Response response) {
        return response.getStatus() != null
                && response.getStatus().getStatusCode() != null
                && response.getStatus().getStatusCode().getValue().equals("urn:oasis:names:tc:SAML:2.0:status:Responder")
                && response.getStatus().getStatusCode().getStatusCode() != null
                && response.getStatus().getStatusCode().getStatusCode().getValue().equals("urn:oasis:names:tc:SAML:2.0:status:NoPassive");
    }
}
