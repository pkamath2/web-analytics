package org.pk.web_analytics.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Analytics {

    private String name;

    @JsonProperty(value = "user-agent")
    private String user_agent;
    private String preferred_animal;
    private String gender;

    public Analytics() {
    }

    public Analytics(String name, String user_agent, String preferred_animal, String gender) {
        this.name = name;
        this.user_agent = user_agent;
        this.preferred_animal = preferred_animal;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public String getUser_agent() {
        return user_agent;
    }

    public void setUser_agent(String user_agent) {
        this.user_agent = user_agent;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPreferred_animal() {
        return preferred_animal;
    }

    public void setPreferred_animal(String preferred_animal) {
        this.preferred_animal = preferred_animal;
    }

}
