package org.blondin.mpg.root.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Club {

    private Map<String, String> name;

    public String getName() {
        // All club have same name
        return this.name.get("fr-FR");
    }
}
