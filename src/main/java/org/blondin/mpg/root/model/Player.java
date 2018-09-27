package org.blondin.mpg.root.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    @JsonProperty("firstname")
    private String firstName;
    @JsonProperty("lastname")
    private String lastName;

    public String getFirstName() {
        if (StringUtils.isBlank(firstName)) {
            return "";
        }
        return firstName;
    }

    public String getLastName() {
        if (StringUtils.isBlank(lastName)) {
            return "";
        }
        return lastName;
    }

    public String getName() {
        return getLastName() + " " + getFirstName();
    }

}
