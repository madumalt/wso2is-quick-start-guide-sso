/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.is.sso.quickStartGuide;

import org.opensaml.saml2.core.*;
import org.opensaml.xml.XMLObject;
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
import java.util.Properties;

/**
 * Filter class for filtering servlet requests.
 */
public class SSOAgentQSGFilter extends SSOAgentFilter {

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

        servletRequest.setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);
        super.doFilter(servletRequest, servletResponse, chain);

        if(shouldForwardToLoginPage(request, response, config)) {
            forwardToLoginPage(request, response);
        }
    }

    /**
     * Forward to login page
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    private void forwardToLoginPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    /**
     * Checks whether the request should be forward to the login page.
     * If the request is a SAML SLO || SAML LogoutResponse || SAML NoPassive
     * will return true, else false
     * @param request
     * @param response
     * @param config
     * @return Boolean
     */
    private Boolean shouldForwardToLoginPage (HttpServletRequest request,
                                      HttpServletResponse response,
                                      SSOAgentConfig config) {
        try {
            SSOAgentRequestResolver resolver = new SSOAgentRequestResolver(request, response, config);

            // if the request is a SLO request from the IS server should direct to login page
            if(resolver.isSLORequest()){
                return true;
            }

            if(resolver.isSAML2SSOResponse()){
                String saml2ResponseString = new String(org.opensaml.xml.util.Base64.decode(
                        request.getParameter("SAMLResponse")), Charset.forName("UTF-8"));
                XMLObject SAML2response = SSOAgentUtils.unmarshall(saml2ResponseString);

                // if a logout response, should direct to login page
                if (SAML2response instanceof LogoutResponse) {
                    return true;
                }

                //if cannot authenticate using passive, should direct to login page
                if(SAML2response instanceof Response){
                    Response saml2Response = (Response) SAML2response;
                    if(saml2Response.getAssertions() == null || saml2Response.getAssertions().isEmpty()){
                        return isNoPassive(saml2Response);
                    }
                }

            }

            return false;

        } catch (SSOAgentException e) {
            return false;
        }
    }

    /**
     * Checks whether a given saml2 response is a NoPassive response.
     * If saml2 response status code is set to 'NoPassive' means, that the
     * user cannot be authenticated with a passive authentication request.
     * @param response
     * @return boolean
     */
    private boolean isNoPassive(Response response) {
        return response.getStatus() != null
                && response.getStatus().getStatusCode() != null
                && response.getStatus().getStatusCode().getValue().equals(StatusCode.RESPONDER_URI)
                && response.getStatus().getStatusCode().getStatusCode() != null
                && response.getStatus().getStatusCode().getStatusCode().getValue().equals(StatusCode.NO_PASSIVE_URI);
    }
}
