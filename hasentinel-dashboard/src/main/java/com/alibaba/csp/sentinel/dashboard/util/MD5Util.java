package com.alibaba.csp.sentinel.dashboard.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Static functions to simplifiy common {@link java.security.MessageDigest}
 * tasks. This class is thread safe.
 * 
 * @author 99bill
 */
public abstract class MD5Util {

	private MD5Util() {
	}

	/**
	 * Returns a MessageDigest for the given <code>algorithm</code>.
	 * 
	 * @param algorithm The MessageDigest algorithm name.
	 * @return An MD5 digest instance.
	 * @throws RuntimeException when a
	 *                          {@link java.security.NoSuchAlgorithmException} is
	 *                          caught
	 */

	static MessageDigest getDigest() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculates the MD5 digest and returns the value as a 16 element
	 * <code>byte[]</code>.
	 * 
	 * @param data Data to digest
	 * @return MD5 digest
	 */
	public static byte[] md5(byte[] data) {
		if (data == null) {
			return null;
		}
		return getDigest().digest(data);
	}

	/**
	 * Calculates the MD5 digest and returns the value as a 16 element
	 * <code>byte[]</code>.
	 * 
	 * @param data Data to digest
	 * @return MD5 digest
	 */
	public static byte[] md5(String data) {
		if (data == null) {
			return null;
		}
		return md5(data.getBytes());
	}

	/**
	 * Calculates the MD5 digest and returns the value as a 32 character hex string.
	 * 
	 * @param data Data to digest
	 * @return MD5 digest as a hex string
	 */
	public static String md5Hex(byte[] data) {
		if (data == null) {
			return null;
		}
		return HexUtil.toHexString(md5(data));
	}

	/**
	 * Calculates the MD5 digest and returns the value as a 32 character hex string.
	 * 
	 * @param data Data to digest
	 * @return MD5 digest as a hex string
	 */
	public static String md5Hex(String data) {
		if (data == null) {
			return null;
		}
		return HexUtil.toHexString(md5(data));
	}

	/**
	 * 32位
	 * 
	 * @param plainText
	 * @return
	 */
	public static String md5Of32(String plainText) {
		if (plainText == null) {
			return null;
		}
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
}
