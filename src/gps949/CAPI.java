package gps949;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.widgets.Display;

import gps949.block.P10pack;
import gps949.block.P12pack;
import gps949.block.P7Sig;
import gps949.dlg.DlgChannel;
import gps949.dlg.pwdDlg;
import gps949.dlg.waitDlg;
import sun.security.mscapi.SunMSCAPI;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.pkcs10.PKCS10;
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
		sig.Signature = Base64.getEncoder().encodeToString(encodedPKCS7).replaceAll("\\s+", "");
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

	private static boolean _RSAP10Gen(String _keyLen, String _DN, P10pack _P10) throws Exception {
		KeyPairGenerator keyPairGenerator = null;
		keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(Integer.parseInt(_keyLen));
		KeyPair kp = keyPairGenerator.generateKeyPair();

		// Gen the P10 with the private key
		X500Name x500Name = new X500Name(_DN);
		Signature sig = Signature.getInstance("SHA256WithRSA");
		sig.initSign(kp.getPrivate());
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(outStream);
		byte[] csrBytes = null;
		try {
			PKCS10 pkcs10 = new PKCS10(kp.getPublic());
			// pkcs10.encodeAndSign(new X500Signer(sig, x500Name)); // For Java 6
			pkcs10.encodeAndSign(x500Name, sig); // For Java 7 and Java 8
			pkcs10.print(printStream);
			csrBytes = outStream.toByteArray();
		} catch (Exception e) {
		} finally {
			if (null != outStream) {
				try {
					outStream.close();
				} catch (IOException e) {
				}
			}
			if (null != printStream) {
				printStream.close();
			}
		}

		// Process after the P10 generated: save the key pair and return the P10
		if (csrBytes == null) {
			return false;
		} else {
			_P10.P10 = new String(csrBytes).replaceAll("---.*---\r\n", "").replaceAll("\r\n", "");

			StringBuilder randki = new StringBuilder();
			Random rand = new Random();
			Random randdata = new Random();
			int data = 0;
			for (int i = 0; i < 16; i++) {
				int index = rand.nextInt(2);
				switch (index) {
				case 0:
					data = randdata.nextInt(10);// 生成0~9
					randki.append(data);
					break;
				case 1:
					data = randdata.nextInt(26) + 65;
					randki.append((char) data);// 产生A~Z
					break;
				}
			}
			_P10.keyIndex = randki.toString();

			File file = new File(System.getProperty("user.dir") + "\\tempKP");
			// 如果文件夹不存在则创建
			if (!file.exists() && !file.isDirectory())
				file.mkdir();

			ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(System.getProperty("user.dir") + "\\tempKP\\" + _P10.keyIndex + ".kp")));
			oout.writeObject(_P10.pwd);
			oout.writeObject(kp.getPrivate());
			oout.close();

			return true;
		}

	}

	public static P10pack RSAP10Gen(String keyLen, String DN) throws Exception {
		P10pack P10p = new P10pack();
		P10p.pwd = null;
		P10p.keyIndex = "";
		P10p.P10 = "ERROR";
		if (!keyLen.equals("1024") && !keyLen.equals("2048") && !keyLen.equals("4096"))
			return P10p;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				pwdDlg pd = new pwdDlg(Display.getDefault().getActiveShell(), P10p);
				pd.setBlockOnOpen(true);
				pd.open();
			}
		});

		while (P10p.pwd == null) {
			Thread.sleep(200);
		}
		_RSAP10Gen(keyLen, DN, P10p);
		return P10p;
	}

	public static P12pack RSAP12Gen(String _keyIndex, String _cert) throws Exception {
		P12pack P12p = new P12pack();
		P12p.pwd = null;
		P12p.keyIndex = _keyIndex;
		P12p.cert = _cert;
		P12p.P12 = "ERROR";

		File file = new File(System.getProperty("user.dir") + "\\tempKP");
		if (!file.exists() && !file.isDirectory()) {
			// No tempKP directory found ERROR
			return P12p;
		}
		file = new File(System.getProperty("user.dir") + "\\tempKP\\" + P12p.keyIndex + ".kp");
		if (!file.exists()) {
			// No kp file found ERROR
			return P12p;
		}

		InputStream in = new FileInputStream(System.getProperty("user.dir") + "\\tempKP\\" + P12p.keyIndex + ".kp");
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
		String pwd = (String) oin.readObject();
		PrivateKey privKey = (PrivateKey) oin.readObject();
		oin.close();
		P12p.pwd = pwd;
		file.delete();

		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(null, null);
		byte encodedCert[] = Base64.getDecoder()
				.decode(_cert.replaceAll("---.*---", "").replace("\n\r", "").replace(" ", "").replace("\t", ""));
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encodedCert);
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);
		X509Certificate[] chain = new X509Certificate[1];
		chain[0] = cert;
		ks.setKeyEntry("LocalSignKeyPair", privKey, pwd.toCharArray(), chain);
		FileOutputStream fos = new FileOutputStream(
				System.getProperty("user.dir") + "\\tempKP\\" + P12p.keyIndex + ".pfx");
		ks.store(fos, P12p.pwd.toCharArray());
		fos.close();

		file = new File(System.getProperty("user.dir") + "\\tempKP\\" + P12p.keyIndex + ".pfx");
		FileInputStream fin = null;
		fin = new FileInputStream(file);
		byte[] fileContent = new byte[(int) file.length()];
		fin.read(fileContent);
		P12p.P12 = Base64.getEncoder().encodeToString(fileContent);
		fin.close();
		file.delete();

		return P12p;
	}

}
