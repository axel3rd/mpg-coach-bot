package org.blondin.mpg.out.model;

import org.apache.commons.lang3.StringUtils;

public class Player {

    private String fullNameWithPosition;
    private OutType outType;
    private String description;
    private String length;
    private String team;

    public String getFullNameWithPosition() {
        return fullNameWithPosition;
    }

    public Position getPosition() {
        if (getFullNameWithPosition().matches(".* \\([A-Z]{2}\\)")) { // NOSONAR : Secure regexp here
            return Position.getNameByValue(getFullNameWithPosition().substring(getFullNameWithPosition().lastIndexOf('(')));
        }
        return Position.UNDEFINED;
    }

    public void setFullNameWithPosition(String name) {
        this.fullNameWithPosition = name;
    }

    public OutType getOutType() {
        return outType;
    }

    public void setOutType(OutType outType) {
        this.outType = outType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getTeam() {
        if (StringUtils.isBlank(this.team)) {
            throw new UnsupportedOperationException("The team can't be null here, implement problem (should be filled before)");
        }
        return this.team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
