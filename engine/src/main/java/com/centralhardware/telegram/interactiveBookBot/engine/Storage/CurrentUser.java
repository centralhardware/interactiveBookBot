package com.centralhardware.telegram.interactiveBookBot.engine.Storage;

import org.springframework.stereotype.Component;

@Component
public class CurrentUser extends Storage {

    private static final String READING_SPEED_KEY = "reading_speed";
    private static final String USERNAME_KEY = "username";
    private static final String FIRST_NAME_KEY = "first_name";
    private static final String LAST_NAME_KEY = "last_name";
    private static final String IS_PREMIUM_KEY = "is_premium";

    public void init(String username, String firstName, String lastName, Boolean isPremium){
        set(USERNAME_KEY, username);
        set(FIRST_NAME_KEY, firstName);
        set(LAST_NAME_KEY, lastName);
        set(IS_PREMIUM_KEY, isPremium);
    }

    public Integer getReadingSpeed(){
        return get(READING_SPEED_KEY);
    }

    public void setReadingSpeed(Integer readingSpeed){
        set(READING_SPEED_KEY, readingSpeed);
    }

    public String getUsername(){
        return get(USERNAME_KEY);
    }

    public String getFirstName(){
        return get(FIRST_NAME_KEY);
    }

    public String getLastName(){
        return get(LAST_NAME_KEY);
    }

    public Boolean getIsPremium(){
        return get(IS_PREMIUM_KEY);
    }


}
