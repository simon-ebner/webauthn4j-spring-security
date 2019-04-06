/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sharplab.springframework.security.webauthn.config.configurers;

import com.webauthn4j.converter.util.JsonConverter;
import net.sharplab.springframework.security.webauthn.WebAuthnProcessingFilter;
import net.sharplab.springframework.security.webauthn.challenge.ChallengeRepository;
import net.sharplab.springframework.security.webauthn.endpoint.OptionsEndpointFilter;
import net.sharplab.springframework.security.webauthn.options.OptionsProvider;
import net.sharplab.springframework.security.webauthn.server.ServerPropertyProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.MFATokenEvaluator;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * Adds webAuthnLogin authentication. All attributes have reasonable defaults making all
 * parameters are optional. If no {@link #loginPage(String)} is specified, a default login
 * page will be generated by the framework.
 *
 * <h2>Security Filters</h2>
 * <p>
 * The following Filters are populated
 *
 * <ul>
 * <li>{@link WebAuthnProcessingFilter}</li>
 * <li>{@link OptionsEndpointFilter}</li>
 * </ul>
 *
 * <h2>Shared Objects Created</h2>
 * <p>
 * The following shared objects are populated
 * <ul>
 * <li>{@link ChallengeRepository}</li>
 * <li>{@link OptionsProvider}</li>
 * <li>{@link ServerPropertyProvider}</li>
 * </ul>
 *
 * <h2>Shared Objects Used</h2>
 * <p>
 * The following shared objects are used:
 *
 * <ul>
 * <li>{@link org.springframework.security.authentication.AuthenticationManager}</li>
 * <li>{@link MFATokenEvaluator}</li>
 * </ul>
 */
public final class WebAuthnLoginConfigurer<H extends HttpSecurityBuilder<H>> extends
        AbstractAuthenticationFilterConfigurer<H, WebAuthnLoginConfigurer<H>, WebAuthnProcessingFilter> {

    private final WebAuthnLoginConfigurer<H>.OptionsEndpointConfig optionsEndpointConfig = new WebAuthnLoginConfigurer<H>.OptionsEndpointConfig();
    //~ Instance fields
    // ================================================================================================
    private OptionsProvider optionsProvider = null;
    private JsonConverter jsonConverter = null;
    private ServerPropertyProvider serverPropertyProvider = null;
    private String usernameParameter = null;
    private String passwordParameter = null;
    private String credentialIdParameter = null;
    private String clientDataJSONParameter = null;
    private String authenticatorDataParameter = null;
    private String signatureParameter = null;
    private String clientExtensionsJSONParameter = null;

    public WebAuthnLoginConfigurer() {
        super(new WebAuthnProcessingFilter(), null);
    }

    public static WebAuthnLoginConfigurer<HttpSecurity> webAuthnLogin() {
        return new WebAuthnLoginConfigurer<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(H http) throws Exception {
        super.configure(http);
        if (optionsProvider == null) {
            optionsProvider = WebAuthnConfigurerUtil.getOptionsProvider(http);
        }
        http.setSharedObject(OptionsProvider.class, optionsProvider);
        if (jsonConverter == null) {
            jsonConverter = WebAuthnConfigurerUtil.getJsonConverter(http);
        }
        http.setSharedObject(JsonConverter.class, jsonConverter);
        if (serverPropertyProvider == null) {
            serverPropertyProvider = WebAuthnConfigurerUtil.getServerPropertyProvider(http);
        }
        http.setSharedObject(ServerPropertyProvider.class, serverPropertyProvider);

        this.getAuthenticationFilter().setServerPropertyProvider(serverPropertyProvider);


        configureParameters();

        this.optionsEndpointConfig.configure(http);
    }

    private void configureParameters() {
        if (usernameParameter != null) {
            this.getAuthenticationFilter().setUsernameParameter(usernameParameter);
            this.optionsProvider.setUsernameParameter(usernameParameter);
        }
        if (passwordParameter != null) {
            this.getAuthenticationFilter().setPasswordParameter(passwordParameter);
            this.optionsProvider.setPasswordParameter(passwordParameter);
        }
        if (credentialIdParameter != null) {
            this.getAuthenticationFilter().setCredentialIdParameter(credentialIdParameter);
            this.optionsProvider.setCredentialIdParameter(credentialIdParameter);
        }
        if (clientDataJSONParameter != null) {
            this.getAuthenticationFilter().setClientDataJSONParameter(clientDataJSONParameter);
            this.optionsProvider.setClientDataJSONParameter(clientDataJSONParameter);
        }
        if (authenticatorDataParameter != null) {
            this.getAuthenticationFilter().setAuthenticatorDataParameter(authenticatorDataParameter);
            this.optionsProvider.setAuthenticatorDataParameter(authenticatorDataParameter);
        }
        if (signatureParameter != null) {
            this.getAuthenticationFilter().setSignatureParameter(signatureParameter);
            this.optionsProvider.setSignatureParameter(signatureParameter);
        }
        if (clientExtensionsJSONParameter != null) {
            this.getAuthenticationFilter().setClientExtensionsJSONParameter(clientExtensionsJSONParameter);
            this.optionsProvider.setClientExtensionsJSONParameter(clientExtensionsJSONParameter);
        }
    }

    /**
     * The HTTP parameter to look for the username when performing authentication. Default
     * is "username".
     *
     * @param usernameParameter the HTTP parameter to look for the username when
     *                          performing authentication
     * @return the {@link FormLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> usernameParameter(String usernameParameter) {
        this.usernameParameter = usernameParameter;
        return this;
    }

    /**
     * The HTTP parameter to look for the password when performing authentication. Default
     * is "password".
     *
     * @param passwordParameter the HTTP parameter to look for the password when
     *                          performing authentication
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> passwordParameter(String passwordParameter) {
        this.passwordParameter = passwordParameter;
        return this;
    }

    /**
     * The HTTP parameter to look for the credentialId when performing authentication. Default
     * is "credentialId".
     *
     * @param credentialIdParameter the HTTP parameter to look for the credentialId when
     *                              performing authentication
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> credentialIdParameter(String credentialIdParameter) {
        this.credentialIdParameter = credentialIdParameter;
        return this;
    }

    /**
     * The HTTP parameter to look for the clientData when performing authentication. Default
     * is "clientDataJSON".
     *
     * @param clientDataJSONParameter the HTTP parameter to look for the clientDataJSON when
     *                                performing authentication
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> clientDataJSONParameter(String clientDataJSONParameter) {
        this.clientDataJSONParameter = clientDataJSONParameter;
        return this;
    }

    /**
     * The HTTP parameter to look for the authenticatorData when performing authentication. Default
     * is "authenticatorData".
     *
     * @param authenticatorDataParameter the HTTP parameter to look for the authenticatorData when
     *                                   performing authentication
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> authenticatorDataParameter(String authenticatorDataParameter) {
        this.authenticatorDataParameter = authenticatorDataParameter;
        return this;
    }

    /**
     * The HTTP parameter to look for the signature when performing authentication. Default
     * is "signature".
     *
     * @param signatureParameter the HTTP parameter to look for the signature when
     *                           performing authentication
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> signatureParameter(String signatureParameter) {
        this.signatureParameter = signatureParameter;
        return this;
    }

    /**
     * The HTTP parameter to look for the clientExtensionsJSON when performing authentication. Default
     * is "clientExtensionsJSON".
     *
     * @param clientExtensionsJSONParameter the HTTP parameter to look for the clientExtensionsJSON when
     *                                      performing authentication
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> clientExtensionsJSONParameter(String clientExtensionsJSONParameter) {
        this.clientExtensionsJSONParameter = clientExtensionsJSONParameter;
        return this;
    }

    /**
     * Forward Authentication Success Handler
     *
     * @param forwardUrl the target URL in case of success
     * @return he {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> successForwardUrl(String forwardUrl) {
        successHandler(new ForwardAuthenticationSuccessHandler(forwardUrl));
        return this;
    }

    /**
     * Forward Authentication Failure Handler
     *
     * @param forwardUrl the target URL in case of failure
     * @return he {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> failureForwardUrl(String forwardUrl) {
        failureHandler(new ForwardAuthenticationFailureHandler(forwardUrl));
        return this;
    }

    /**
     * <p>
     * Specifies the URL to send users to if login is required. If used with
     * {@link WebSecurityConfigurerAdapter} a default login page will be generated when
     * this attribute is not specified.
     * </p>
     *
     * @param loginPage login page
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    @Override
    public WebAuthnLoginConfigurer<H> loginPage(String loginPage) {
        return super.loginPage(loginPage);
    }

    /**
     * Create the {@link RequestMatcher} given a loginProcessingUrl
     * @param loginProcessingUrl creates the {@link RequestMatcher} based upon the
     * loginProcessingUrl
     * @return the {@link RequestMatcher} to use based upon the loginProcessingUrl
     */
    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, "POST");
    }

    /**
     * Specifies the {@link OptionsProvider} to be used.
     * @param optionsProvider the {@link OptionsProvider}
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> optionsProvider(OptionsProvider optionsProvider) {
        Assert.notNull(optionsProvider, "optionsProvider must not be null");
        this.optionsProvider = optionsProvider;
        return this;
    }

    /**
     * Specifies the {@link JsonConverter} to be used.
     * @param jsonConverter the {@link JsonConverter}
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> jsonConverter(JsonConverter jsonConverter) {
        Assert.notNull(jsonConverter, "jsonConverter must not be null");
        this.jsonConverter = jsonConverter;
        return this;
    }

    /**
     * Specifies the {@link ServerPropertyProvider} to be used.
     * @param serverPropertyProvider the {@link ServerPropertyProvider}
     * @return the {@link WebAuthnLoginConfigurer} for additional customization
     */
    public WebAuthnLoginConfigurer<H> serverPropertyProvider(ServerPropertyProvider serverPropertyProvider) {
        Assert.notNull(serverPropertyProvider, "serverPropertyProvider must not be null");
        this.serverPropertyProvider = serverPropertyProvider;
        return this;
    }

    /**
     * Returns the {@link OptionsEndpointConfig} for configuring the {@link OptionsEndpointFilter}
     * @return the {@link OptionsEndpointConfig}
     */
    public WebAuthnLoginConfigurer<H>.OptionsEndpointConfig optionsEndpoint() {
        return optionsEndpointConfig;
    }


    /**
     * Configuration options for the {@link OptionsEndpointFilter}
     */
    public class OptionsEndpointConfig {

        private String processingUrl = OptionsEndpointFilter.FILTER_URL;

        private OptionsEndpointConfig() {
        }

        private void configure(H http) {
            OptionsEndpointFilter optionsEndpointFilter;
            ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
            String[] beanNames = applicationContext.getBeanNamesForType(OptionsEndpointFilter.class);
            if (beanNames.length == 0) {
                optionsEndpointFilter = new OptionsEndpointFilter(optionsProvider, jsonConverter);
                optionsEndpointFilter.setFilterProcessesUrl(processingUrl);
            } else {
                optionsEndpointFilter = applicationContext.getBean(OptionsEndpointFilter.class);
            }

            http.addFilterAfter(optionsEndpointFilter, SessionManagementFilter.class);
        }

        /**
         * Sets the URL for the options endpoint
         * @param processingUrl the URL for the options endpoint
         * @return the {@link OptionsEndpointConfig} for additional customization
         */
        public WebAuthnLoginConfigurer<H>.OptionsEndpointConfig processingUrl(String processingUrl) {
            this.processingUrl = processingUrl;
            return this;
        }

        /**
         * Returns the {@link WebAuthnLoginConfigurer} for further configuration.
         *
         * @return the {@link WebAuthnLoginConfigurer}
         */
        public WebAuthnLoginConfigurer<H> and() {
            return WebAuthnLoginConfigurer.this;
        }

    }


}
