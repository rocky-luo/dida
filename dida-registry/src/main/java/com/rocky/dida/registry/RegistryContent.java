package com.rocky.dida.registry;

/**
 * Created by rocky on 18/2/24.
 */
public class RegistryContent {

    private final static String ADMIN_URL_PREFIX = "/dida/admin";
    private final static String CLIENT_RUL_PREFIX_FORMATE = "/dida/client/%s/%s/%s";

    public static String adminUrlPrefix() {
        return ADMIN_URL_PREFIX;
    }

    public static String clientUrl(String group, String name, String id){
        return String.format(CLIENT_RUL_PREFIX_FORMATE, group, name, id);
    }

}
