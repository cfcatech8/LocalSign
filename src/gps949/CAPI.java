package gps949;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import sun.security.mscapi.SunMSCAPI;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.util.DerOutputStream;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

public class CAPI {
	private static P7Sig _RSAP7DetachSign(KeyStore ks, String _src, AliasCan ac) throws Exception {
		P7Sig sig = new P7Sig();
		// compute signature:
		Signature signature = Signature.getInstance("SHA256WithRSA", "SunMSCAPI");
		signature.initSign((PrivateKey) ks.getKey(ac.alias, null));
		signature.update(_src.getBytes("UTF-8"));
		byte[] signedData = null;
		try {
			signedData = signature.sign();
		} catch (Exception e) {
			sig.Signature = "ERROR";
			return sig;
		}

		X509Certificate cert = (X509Certificate) ks.getCertificate(ac.alias);
		// load X500Name
		X500Name xName = X500Name.asX500Name(cert.getIssuerX500Principal());
		// load serial number
		BigInteger serial = cert.getSerialNumber();
		// laod digest algorithm
		AlgorithmId digestAlgorithmId = new AlgorithmId(AlgorithmId.SHA256_oid);
		// load signing algorithm
		AlgorithmId signAlgorithmId = new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid);
		// Create SignerInfo:
		SignerInfo sInfo = new SignerInfo(xName, serial, digestAlgorithmId, signAlgorithmId, signedData);
		// Create ContentInfo:
		ContentInfo cInfo = new ContentInfo(ContentInfo.DATA_OID, null);
		// Create PKCS7 Signed data
		PKCS7 p7 = new PKCS7(new AlgorithmId[] { digestAlgorithmId }, cInfo,
				new java.security.cert.X509Certificate[] { cert }, new SignerInfo[] { sInfo });
		// Write PKCS7 to bYteArray
		ByteArrayOutputStream bOut = new DerOutputStream();
		p7.encodeSignedData(bOut);
		byte[] encodedPKCS7 = bOut.toByteArray();
		// System.out.println(Base64.encode(encodedPKCS7));
		sig.DN = cert.getSubjectDN().toString();
		sig.SN = cert.getSerialNumber().toString(16);
		sig.Signature = Base64.encode(encodedPKCS7).replaceAll("\\s+", "");
		ac.alias = null;
		return sig;
	}

	public static P7Sig RSAP7DetachSign(String _src, String _dnFilter) throws Exception {
		DlgChannel dc = new DlgChannel();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				waitDlg wd = new waitDlg(Display.getDefault().getActiveShell(), "正在筛选证书，请稍候。。。", dc);
				wd.setBlockOnOpen(true);
				wd.open();
			}
		});

		SunMSCAPI ms = new SunMSCAPI();
		Security.addProvider(ms);
		KeyStore ks = KeyStore.getInstance("Windows-MY");
		ks.load(null, null);
		Enumeration<String> aliases = ks.aliases();
		AliasCan ac = new AliasCan();
		List<String> aliasesAfterFilter = new ArrayList<String>();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			X509Certificate tempcert = (X509Certificate) ks.getCertificate(alias);
			if (tempcert.getSubjectDN().toString().contains(_dnFilter) && tempcert.getKeyUsage() != null
					&& tempcert.getKeyUsage()[0]) {
				aliasesAfterFilter.add(alias);
			}
		}

		dc.flag = true;
		P7Sig sig = new P7Sig();
		sig.Signature = "ERROR";
		switch (aliasesAfterFilter.size()) {
		case 0:
			return sig;
		case 1:
			ac.alias = aliasesAfterFilter.get(0);
			return _RSAP7DetachSign(ks, _src, ac);
		default:
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					new CertSelector(ks, aliasesAfterFilter, ac);
				}
			});
			while (ac.alias == null) {
				Thread.sleep(200);
			}
			if (ac.alias == "ERROR")
				return sig;
			else
				return _RSAP7DetachSign(ks, _src, ac);
		}

	}
}
