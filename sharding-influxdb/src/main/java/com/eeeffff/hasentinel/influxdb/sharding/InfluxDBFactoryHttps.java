package com.eeeffff.hasentinel.influxdb.sharding;

import java.util.Objects;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ResponseFormat;
import org.influxdb.impl.InfluxDBImpl;
import org.influxdb.impl.Preconditions;

import com.eeeffff.hasentinel.influxdb.util.HttpsUtils;

import okhttp3.OkHttpClient;

public enum InfluxDBFactoryHttps {
	 INSTANCE;

	  /**
	   * Create a connection to a InfluxDB.
	   *
	   * @param url
	   *            the url to connect to.
	   * @return a InfluxDB adapter suitable to access a InfluxDB.
	   */
	  public static InfluxDB connect(final String url) {
	    Preconditions.checkNonEmptyString(url, "url");
	    return new InfluxDBImpl(url, null, null, HttpsUtils.getTrustAllClientBuilder());
	  }

	  /**
	   * Create a connection to a InfluxDB.
	   *
	   * @param url
	   *            the url to connect to.
	   * @param username
	   *            the username which is used to authorize against the influxDB instance.
	   * @param password
	   *            the password for the username which is used to authorize against the influxDB
	   *            instance.
	   * @return a InfluxDB adapter suitable to access a InfluxDB.
	   */
	  public static InfluxDB connect(final String url, final String username, final String password) {
	    Preconditions.checkNonEmptyString(url, "url");
	    Preconditions.checkNonEmptyString(username, "username");
	    return new InfluxDBImpl(url, username, password, HttpsUtils.getTrustAllClientBuilder());
	  }

	  /**
	   * Create a connection to a InfluxDB.
	   *
	   * @param url
	   *            the url to connect to.
	   * @param client
	   *            the HTTP client to use
	   * @return a InfluxDB adapter suitable to access a InfluxDB.
	   */
	  public static InfluxDB connect(final String url, final OkHttpClient.Builder client) {
	    Preconditions.checkNonEmptyString(url, "url");
	    Objects.requireNonNull(client, "client");
	    return new InfluxDBImpl(url, null, null, client);
	  }

	  /**
	   * Create a connection to a InfluxDB.
	   *
	   * @param url
	   *            the url to connect to.
	   * @param username
	   *            the username which is used to authorize against the influxDB instance.
	   * @param password
	   *            the password for the username which is used to authorize against the influxDB
	   *            instance.
	   * @param client
	   *            the HTTP client to use
	   * @return a InfluxDB adapter suitable to access a InfluxDB.
	   */
	  public static InfluxDB connect(final String url, final String username, final String password,
	      final OkHttpClient.Builder client) {
	    return connect(url, username, password, client, ResponseFormat.JSON);
	  }

	  /**
	   * Create a connection to a InfluxDB.
	   *
	   * @param url
	   *            the url to connect to.
	   * @param username
	   *            the username which is used to authorize against the influxDB instance.
	   * @param password
	   *            the password for the username which is used to authorize against the influxDB
	   *            instance.
	   * @param client
	   *            the HTTP client to use
	   * @param responseFormat
	   *            The {@code ResponseFormat} to use for response from InfluxDB server
	   * @return a InfluxDB adapter suitable to access a InfluxDB.
	   */
	  public static InfluxDB connect(final String url, final String username, final String password,
	      final OkHttpClient.Builder client, final ResponseFormat responseFormat) {
	    Preconditions.checkNonEmptyString(url, "url");
	    Preconditions.checkNonEmptyString(username, "username");
	    Objects.requireNonNull(client, "client");
	    return new InfluxDBImpl(url, username, password, client, responseFormat);
	  }
}
