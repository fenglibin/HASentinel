package com.eeefff.hasentinel.influxdb.util;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * 封装的支持Https连接的Okhttp客户端
 */
@Slf4j
public class HttpsUtils {

	private static MyTrustManager mMyTrustManager;

	private static SSLSocketFactory createSSLSocketFactory() {
		SSLSocketFactory ssfFactory = null;
		try {
			mMyTrustManager = new MyTrustManager();
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { mMyTrustManager }, new SecureRandom());
			ssfFactory = sc.getSocketFactory();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return ssfFactory;
	}

	// 实现X509TrustManager接口
	public static class MyTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}

	// 实现HostnameVerifier接口
	private static class TrustAllHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	@SuppressWarnings("synthetic-access")
	public static OkHttpClient.Builder getTrustAllClientBuilder() {
		OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
		mBuilder.sslSocketFactory(createSSLSocketFactory(), mMyTrustManager)
				.hostnameVerifier(new TrustAllHostnameVerifier());
		return mBuilder;
	}

	/**
	 * 对外提供的获取支持自签名的okhttp客户端
	 *
	 * @param certificate 自签名证书的输入流
	 * @return 支持自签名的客户端
	 */
	public static OkHttpClient getTrusClient(InputStream certificate) {
		X509TrustManager trustManager = null;
		SSLSocketFactory sslSocketFactory = null;
		try {
			trustManager = trustManagerForCertificates(certificate);
			if (trustManager == null) {
				new OkHttpClient.Builder();
			}
			SSLContext sslContext = SSLContext.getInstance("TLS");
			// 使用构建出的trustManger初始化SSLContext对象
			sslContext.init(null, new TrustManager[] { trustManager }, null);
			// 获得sslSocketFactory对象
			sslSocketFactory = sslContext.getSocketFactory();
		} catch (GeneralSecurityException e) {
			log.error(e.getMessage(), e);
		}
		return new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, trustManager).build();
	}

	/**
	 * 获去信任自签证书的trustManager
	 *
	 * @param in 自签证书输入流
	 * @return 信任自签证书的trustManager
	 * @throws GeneralSecurityException
	 */
	private static X509TrustManager trustManagerForCertificates(InputStream in) throws GeneralSecurityException {
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		// 通过证书工厂得到自签证书对象集合
		Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
		if (certificates.isEmpty()) {
			throw new IllegalArgumentException("expected non-empty set of trusted certificates");
		}
		// 为证书设置一个keyStore
		char[] password = "password".toCharArray(); // Any password will work.
		KeyStore keyStore = newEmptyKeyStore(password);
		if (keyStore == null) {
			return null;
		}
		int index = 0;
		// 将证书放入keystore中
		for (Certificate certificate : certificates) {
			String certificateAlias = Integer.toString(index++);
			keyStore.setCertificateEntry(certificateAlias, certificate);
		}
		// Use it to build an X509 trust manager.
		// 使用包含自签证书信息的keyStore去构建一个X509TrustManager
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, password);
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
			throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
		}
		return (X509TrustManager) trustManagers[0];
	}

	private static KeyStore newEmptyKeyStore(char[] password) {
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			InputStream in = null; // By convention, 'null' creates an empty key store.
			keyStore.load(in, password);
			return keyStore;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
}
