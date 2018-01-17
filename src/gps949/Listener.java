package gps949;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import gps949.txrx.rxRSAP7DetachSign;
import gps949.txrx.txRSAP7DetachSign;

public class Listener {
	HttpsServer httpsServer;

	public static class RSAP7DetachSign implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			Gson gson = new Gson();
			txRSAP7DetachSign res = new txRSAP7DetachSign();
			res.Signature = "ERROR";
			HttpsExchange httpsExchange = (HttpsExchange) t;
			String reqContent = "";
			if (httpsExchange.getRequestMethod().equals("POST")) {
				reqContent = IOUtils.toString(httpsExchange.getRequestBody(), "UTF-8");

				rxRSAP7DetachSign req = gson.fromJson(reqContent, rxRSAP7DetachSign.class);
				if (req != null) {
					try {
						P7Sig sig = CAPI.RSAP7DetachSign(req.Src, req.FilterDN);
						res.Signature = sig.Signature;
						res.DN = sig.DN;
						res.SN = sig.SN;
					} catch (Exception e) {
					}
				}
			}
			String response = gson.toJson(res);
			String originSite = t.getRequestHeaders().get("Origin").get(0);
			if (originSite.equals("http://api.gps949.com") || originSite.equals("http://www.gps949.com")
					|| originSite.equals("https://api.gps949.com") || originSite.equals("https://www.gps949.com")
					|| originSite.equals("http://rae.gps949.com") || originSite.equals("https://rae.gps949.com")) {
				t.getResponseHeaders().add("Access-Control-Allow-Origin", originSite);
			}
			t.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
			t.getResponseHeaders().add("Access-Control-Allow-Headers", "content-type");
			t.getResponseHeaders().add("contentType", "application/json; charset=utf-8");
			t.sendResponseHeaders(200, response.getBytes("UTF-8").length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes("UTF-8"));
			os.close();
		}
	}

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
			httpsServer.setExecutor(
					new ThreadPoolExecutor(2, 4, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100)));
			httpsServer.start();

		} catch (Exception exception) {
		}
	}
}