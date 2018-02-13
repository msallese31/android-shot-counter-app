package com.projects.sallese.shotcounter;

import android.provider.ContactsContract;

/**
 * Created by sallese on 2/11/18.
 */

public class UserSession {
        private static String idToken;
        public static void SetIdToken(String idToken) { UserSession.idToken = idToken; }
        public static String GetIdToken() { return UserSession.idToken; }

        private static String email;
        public static void SetEmail(String email) { UserSession.email = email; }
        public static String GetEmail() { return UserSession.email; }
    }
