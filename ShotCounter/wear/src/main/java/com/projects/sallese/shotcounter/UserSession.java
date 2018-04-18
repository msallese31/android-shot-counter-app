package com.projects.sallese.shotcounter;

/**
 * Created by sallese on 4/17/18.
 */

public class UserSession {

    private static Boolean authenticated = false;
    public static Boolean GetAuthenticatedStatus() { return UserSession.authenticated; }
    public static void SetAuthenticated(Boolean a) { UserSession.authenticated = a; }


}
