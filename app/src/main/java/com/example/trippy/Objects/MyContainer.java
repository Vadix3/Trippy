package com.example.trippy.Objects;

import java.io.Serializable;
import java.util.ArrayList;

public class MyContainer implements Serializable {

    /**
     * My container for trip and user
     * TODO: Maybe trips array?
     */
    private MyUser myUser;
    private MyTrip myTrip;

    public MyContainer() {
    }

    public MyContainer(MyUser myUser, MyTrip myTrip) {
        this.myTrip = myTrip;
        this.myUser = myUser;
    }

    public MyTrip getMyTrip() {
        return myTrip;
    }

    public void setMyTrip(MyTrip myTrip) {
        this.myTrip = myTrip;
    }

    public MyUser getMyUser() {
        return myUser;
    }

    public void setMyUser(MyUser myUser) {
        this.myUser = myUser;
    }
}
