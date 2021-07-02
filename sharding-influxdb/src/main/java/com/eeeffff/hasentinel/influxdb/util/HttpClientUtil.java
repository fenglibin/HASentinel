package com.eeeffff.hasentinel.influxdb.util;

import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.csp.sentinel.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fenglibin
 *
 */
@Slf4j
public class HttpClientUtil {
	
	private static CloseableHttpClient httpClient;

	public static void setHttpClient(CloseableHttpClient httpClient) {
		HttpClientUtil.httpClient = httpClient;
	}

	/**
	 * 获取对所有Https请求都信任的SSLConnectionSocketFactory
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static SSLConnectionSocketFactory getSelfTrustSSLConnectionSocketFactory()
			throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext ctx = SSLContext.getInstance("TLS");
		X509TrustManager tm = new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}
		};
		ctx.init(null, new TrustManager[] { tm }, null);
		return new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
	}

	/**
	 * 通过Influxdb的ＵＲＬ查询数据
	 * 
	 * @param url
	 * @return
	 */
	public static String doGet(String url) {
		log.info("url is:" + url);
		Charset charset = null;
		final HttpGet httpGet = new HttpGet(url);
		try {
			httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			String contentTypeStr = response.getFirstHeader("Content-type").getValue();
			if (StringUtil.isNotEmpty(contentTypeStr)) {
				ContentType contentType = ContentType.parse(contentTypeStr);
				charset = contentType.getCharset();
			}
			return EntityUtils.toString(response.getEntity(), charset != null ? charset : CharsetUtil.DEFAULT_CHARSET);
		} catch (Exception e) {
			log.error("执行查询出错，查询的URL：" + url + "，异常：" + e.getMessage(), e);
			httpGet.abort();
		}
		return StringUtils.EMPTY_STRING;
	}
}
