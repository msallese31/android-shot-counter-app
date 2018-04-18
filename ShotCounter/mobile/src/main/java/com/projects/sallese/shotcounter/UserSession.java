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

        private static String name;
        public static void SetName(String name) { UserSession.name = name; }
        public static String GetName() { return UserSession.name; }

        private static Integer count;
        public static void SetCount(Integer count) { UserSession.count = count; }
        public static Integer GetCount() { return UserSession.count; }
        public synchronized static void IncrementCount(Integer count) { UserSession.count = UserSession.count + count; }

        private static Boolean bluetooth = false;
        public static Boolean GetBluetoothStatus() { return UserSession.bluetooth; }
        public static void SetBluetoothStatus(Boolean b) { UserSession.bluetooth = b; }

        private static Boolean authenticated = false;
        public static Boolean GetAuthenticatedStatus() { return UserSession.authenticated; }
        public static void SetAuthenticated(Boolean a) { UserSession.authenticated = a; }

    }
