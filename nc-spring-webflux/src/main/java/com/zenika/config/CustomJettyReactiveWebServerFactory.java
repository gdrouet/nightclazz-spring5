package com.zenika.config;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyReactiveWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.server.reactive.JettyHttpHandlerAdapter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;

/**
 * A custom {@code org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory} that configures SSL connector,
 * which is currently not supported by spring webflux.
 */
public class CustomJettyReactiveWebServerFactory extends JettyReactiveWebServerFactory {

    private final ServerProperties serverProperties;

    /**
     * Builds a new instance with the specified server properties.
     *
     * @param serverProperties the server properties indicating the configuration to apply
     */
    public CustomJettyReactiveWebServerFactory(final ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    private AbstractConnector createSslConnector(final Server server,
                                                 final SslContextFactory sslContextFactory,
                                                 final int port) {
        final HttpConnectionFactory connectionFactory = createConnectionFactory();
        final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());
        final ServerConnector serverConnector = new ServerConnector(server, sslConnectionFactory, connectionFactory);
        return setPortToServerConnector(port, serverConnector);
    }

    private HttpConnectionFactory createConnectionFactory() {
        return new HttpConnectionFactory(createConfiguration());
    }

    private HttpConfiguration createConfiguration() {
        final HttpConfiguration config = new HttpConfiguration();
        config.setSendServerVersion(false);
        config.addCustomizer(new SecureRequestCustomizer());
        return config;
    }

    private ServerConnector setPortToServerConnector(final int port, final ServerConnector serverConnector) {
        serverConnector.setPort(port);
        return serverConnector;
    }

    protected Server createJettyServer(final JettyHttpHandlerAdapter servlet) {
        final Server server = super.createJettyServer(servlet);
        setSsl(serverProperties.getSsl());

        if (getSsl() != null && getSsl().isEnabled()) {
            SslContextFactory sslContextFactory = new SslContextFactory();
            configureSsl(sslContextFactory, getSsl());
            AbstractConnector connector = createSslConnector(server, sslContextFactory, serverProperties.getPort());
            server.setConnectors(new Connector[] { connector });
        }

        return server;
    }

    /**
     * Configure the SSL connection.
     *
     * @param factory the Jetty {@link SslContextFactory}.
     * @param ssl the ssl details.
     */
    protected void configureSsl(SslContextFactory factory, Ssl ssl) {
        factory.setProtocol(ssl.getProtocol());
        configureSslClientAuth(factory, ssl);
        configureSslPasswords(factory, ssl);
        factory.setCertAlias(ssl.getKeyAlias());

        if (!ObjectUtils.isEmpty(ssl.getCiphers())) {
            factory.setIncludeCipherSuites(ssl.getCiphers());
            factory.setExcludeCipherSuites();
        }

        if (ssl.getEnabledProtocols() != null) {
            factory.setIncludeProtocols(ssl.getEnabledProtocols());
        }

        if (getSslStoreProvider() != null) {
            try {
                factory.setKeyStore(getSslStoreProvider().getKeyStore());
                factory.setTrustStore(getSslStoreProvider().getTrustStore());
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to set SSL store", ex);
            }
        } else {
            configureSslKeyStore(factory, ssl);
            configureSslTrustStore(factory, ssl);
        }
    }

    private void configureSslClientAuth(SslContextFactory factory, Ssl ssl) {
        if (ssl.getClientAuth() == Ssl.ClientAuth.NEED) {
            factory.setNeedClientAuth(true);
            factory.setWantClientAuth(true);
        } else if (ssl.getClientAuth() == Ssl.ClientAuth.WANT) {
            factory.setWantClientAuth(true);
        }
    }

    private void configureSslPasswords(SslContextFactory factory, Ssl ssl) {
        if (ssl.getKeyStorePassword() != null) {
            factory.setKeyStorePassword(ssl.getKeyStorePassword());
        }

        if (ssl.getKeyPassword() != null) {
            factory.setKeyManagerPassword(ssl.getKeyPassword());
        }
    }

    private void configureSslKeyStore(SslContextFactory factory, Ssl ssl) {
        try {
            URL url = ResourceUtils.getURL(ssl.getKeyStore());
            factory.setKeyStoreResource(Resource.newResource(url));
        } catch (IOException ex) {
            throw new WebServerException(
                    "Could not find key store '" + ssl.getKeyStore() + "'", ex);
        }

        if (ssl.getKeyStoreType() != null) {
            factory.setKeyStoreType(ssl.getKeyStoreType());
        }

        if (ssl.getKeyStoreProvider() != null) {
            factory.setKeyStoreProvider(ssl.getKeyStoreProvider());
        }
    }

    private void configureSslTrustStore(final SslContextFactory factory, final Ssl ssl) {
        if (ssl.getTrustStorePassword() != null) {
            factory.setTrustStorePassword(ssl.getTrustStorePassword());
        }

        if (ssl.getTrustStore() != null) {
            try {
                URL url = ResourceUtils.getURL(ssl.getTrustStore());
                factory.setTrustStoreResource(Resource.newResource(url));
            } catch (IOException ex) {
                throw new WebServerException(
                        "Could not find trust store '" + ssl.getTrustStore() + "'", ex);
            }
        }

        if (ssl.getTrustStoreType() != null) {
            factory.setTrustStoreType(ssl.getTrustStoreType());
        }

        if (ssl.getTrustStoreProvider() != null) {
            factory.setTrustStoreProvider(ssl.getTrustStoreProvider());
        }
    }
}
