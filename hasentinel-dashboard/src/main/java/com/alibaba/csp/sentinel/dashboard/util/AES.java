package com.alibaba.csp.sentinel.dashboard.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import lombok.extern.slf4j.Slf4j;

/**
 * 类AES.java的实现描述：TODO 类实现描述
 * 
 * @author fenglibin 2016年2月18日 下午3:32:51
 */
@Slf4j
public class AES {

	private static String iv = "0102030405060708";
	private static String defaultKey = "1234567809123456";

	public static String encrypt(Integer data) {
		return encrypt(String.valueOf(data), defaultKey);
	}

	public static String encrypt(Number data) {
		return encrypt(String.valueOf(data), defaultKey);
	}

	public static String encrypt(String data) {
		return encrypt(data, defaultKey);
	}

	@SuppressWarnings("restriction")
	public static String encrypt(String data, String key) {
		try {
			if (data == null || data.trim().length() == 0) {
				return "";
			}
			data = data.trim();
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			int blockSize = cipher.getBlockSize();

			byte[] dataBytes = data.getBytes();
			int plaintextLength = dataBytes.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}

			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(plaintext);

			return new sun.misc.BASE64Encoder().encode(encrypted);

		} catch (Exception e) {
			log.error("encrypt exception", e);
			return null;
		}
	}

	public static String decrypt(String data) {
		return decrypt(data, defaultKey);
	}

	public static String decrypt(String data, String key) {
		try {

			if (data == null || data.trim().length() == 0) {
				return "";
			}
			data = data.trim();

			byte[] encrypted1 = Base64.decodeBase64(data.getBytes());

			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original);
			return originalString.trim();

		} catch (Exception e) {
			log.error("desEncrypt exception", e);
			return null;
		}
	}
}
