
package org.blondin.mpg.config;

import org.apache.commons.lang3.StringUtils;

public class Proxy {

    private String uri;
    private String user;
    private String password;

    public Proxy(String uri, String user, String password) {
        this.uri = uri;
        this.user = user;
        this.password = password;
    }

    public boolean isConfigured() {
        return StringUtils.isNotBlank(getUri());
    }

    public String getUri() {
        return uri;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
