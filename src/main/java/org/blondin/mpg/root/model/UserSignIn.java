package org.blondin.mpg.root.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSignIn {

    private String token;
    private String userId;

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }
}
