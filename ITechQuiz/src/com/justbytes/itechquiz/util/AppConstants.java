package com.justbytes.itechquiz.util;

public class AppConstants {
	public static final String EMAIL_TO = "TO";
	public static final String EMAIL_SUBJECT = "SUBJECT";
	public static final String EMAIL_BODY_KEY = "BODY";
	public static final String ADMIN = "Admin";
	
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";
	public static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";
	/**
	 * Default lifespan (7 days) of a reservation until it is considered
	 * expired.
	 */
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;
}
