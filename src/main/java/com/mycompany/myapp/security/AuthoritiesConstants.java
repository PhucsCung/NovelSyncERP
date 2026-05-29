package com.mycompany.myapp.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String SALES = "ROLE_SALES";
    public static final String PURCHASER = "ROLE_PURCHASER";
    public static final String WAREHOUSE = "ROLE_WAREHOUSE";
    public static final String ACCOUNTANT = "ROLE_ACCOUNTANT";
    public static final String MANAGER = "ROLE_MANAGER";

    private AuthoritiesConstants() {}
}
