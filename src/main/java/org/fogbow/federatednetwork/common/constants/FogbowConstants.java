package org.fogbow.federatednetwork.common.constants;

public class FogbowConstants {
    // Services' key files
    public static final String PUBLIC_KEY_FILE_PATH = "public_key_file_path";
    public static final String PRIVATE_KEY_FILE_PATH = "private_key_file_path";

    // Token  separators
    public static final String TOKEN_STRING_SEPARATOR = "!^!";
    public static final String TOKEN_SEPARATOR = "!%!";
    public static final String PAYLOAD_SEPARATOR  = "!$!";
    public static final String ATTRIBUTE_SEPARATOR  = "!#!";
    public static final String KEY_VALUE_SEPARATOR = "!@!";

    // Token mandatory fields
    public static final String PROVIDER_ID_KEY =  "provider";
    public static final String USER_ID_KEY  = "id";

    // Token optional fields
    public static final String USER_NAME_KEY = "name";
    public static final String TOKEN_VALUE_KEY = "token";
}
