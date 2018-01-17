package gps949.endpoints;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsExchange;

import gps949.CAPI;
import gps949.block.P10pack;
import gps949.txrx.rxRSAP10Gen;
import gps949.txrx.txRSAP10Gen;

public class RSAP10Gen implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		Gson gson = new Gson();
		txRSAP10Gen res = new txRSAP10Gen();
		res.P10 = "ERROR";
		HttpsExchange httpsExchange = (HttpsExchange) t;
		String reqContent = "";
		if (httpsExchange.getRequestMethod().equals("POST")) {
			reqContent = IOUtils.toString(httpsExchange.getRequestBody(), "UTF-8");

			rxRSAP10Gen req = gson.fromJson(reqContent, rxRSAP10Gen.class);
			if (req != null) {
				try {
					P10pack P10p = CAPI.RSAP10Gen(req.keyLen, req.DN);
					res.keyIndex = P10p.keyIndex;
					res.P10 = P10p.P10;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// Common part for send response
		String response = gson.toJson(res);
		String originSite = httpsExchange.getRequestHeaders().get("Origin").get(0);
		if (originSite.equals("http://api.gps949.com") || originSite.equals("http://www.gps949.com")
				|| originSite.equals("https://api.gps949.com") || originSite.equals("https://www.gps949.com")
				|| originSite.equals("http://rae.gps949.com") || originSite.equals("https://rae.gps949.com")) {
			httpsExchange.getResponseHeaders().add("Access-Control-Allow-Origin", originSite);
		}
		httpsExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
		httpsExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "content-type");
		httpsExchange.getResponseHeaders().add("contentType", "application/json; charset=utf-8");
		httpsExchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
		OutputStream os = httpsExchange.getResponseBody();
		os.write(response.getBytes("UTF-8"));
		os.close();

	}

}