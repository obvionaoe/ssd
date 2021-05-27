package pt.up.fc.dcc.ssd.p2p.security;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;

public class Ssl {
    private static final File CA_CERT_FILE = new File("cert/ca/cert.pem");
    private static final File SERVER_CERT_FILE = new File("cert/server/cert.pem");
    private static final File SERVER_KEY_FILE = new File("cert/server/key.pem");
    private static final File CLIENT_CERT_FILE = new File("cert/client/cert.pem");
    private static final File CLIENT_KEY_FILE = new File("cert/client/key.pem");

    public static SslContext loadServerTlsCredentials() throws SSLException {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(SERVER_CERT_FILE, SERVER_KEY_FILE).clientAuth(ClientAuth.REQUIRE).trustManager(CA_CERT_FILE);
        return GrpcSslContexts.configure(sslContextBuilder).build();
    }

    public static SslContext loadClientTlsCredentials() throws SSLException {
        return GrpcSslContexts.forClient().keyManager(CLIENT_CERT_FILE, CLIENT_KEY_FILE).trustManager(CA_CERT_FILE).build();
    }
}
