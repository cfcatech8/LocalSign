package gps949;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import gps949.endpoints.RSAP10Gen;
import gps949.endpoints.RSAP7DetachSign;

public class Listener {
	HttpsServer httpsServer;

	public void close() throws Exception {
		httpsServer.stop(0);
	}

	/**
	 * @param args
	 */
	public void run(String[] args) throws Exception {

		try {
			// setup the socket address
			InetSocketAddress address = new InetSocketAddress(11949);

			// initialize the HTTPS server
			httpsServer = HttpsServer.create(address, 0);
			SSLContext sslContext = SSLContext.getInstance("TLS");

			// initialize the keystore
			char[] pwd = "11111111".toCharArray();
			KeyStore ks = KeyStore.getInstance("JKS");
			InputStream fis = getClass().getClassLoader().getResourceAsStream("LocalSign_gps949_com.jks");
			ks.load(fis, pwd);

			// setup the key manager factory
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, pwd);

			// setup the trust manager factory
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);

			// setup the HTTPS context and parameters
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				public void configure(HttpsParameters params) {
					try {
						// initialize the SSL context
						SSLContext c = SSLContext.getDefault();
						SSLEngine engine = c.createSSLEngine();
						params.setNeedClientAuth(false);
						params.setCipherSuites(engine.getEnabledCipherSuites());
						params.setProtocols(engine.getEnabledProtocols());

						// get the default parameters
						SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
						params.setSSLParameters(defaultSSLParameters);
					} catch (Exception ex) {
					}
				}
			});
			httpsServer.createContext("/RSAP7DetachSign", new RSAP7DetachSign());
			httpsServer.createContext("/RSAP10Gen", new RSAP10Gen());
			httpsServer.setExecutor(
					new ThreadPoolExecutor(2, 4, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100)));
			httpsServer.start();

		} catch (Exception exception) {
		}
	}
}