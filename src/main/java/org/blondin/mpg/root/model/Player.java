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
        return StringUtils.defaultIfBlank(firstName, "");
    }

    public String getLastName() {
        return StringUtils.defaultIfBlank(lastName, "");
    }

    public String getName() {
        return (getLastName() + " " + getFirstName()).trim();
    }

}
