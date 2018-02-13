package com.projects.sallese.shotcounter;

/**
 * Created by sallese on 2/11/18.
 */

public class UserInfo {
    private static String accountId;
    private static final UserInfo ourInstance = new UserInfo();

    public static UserInfo getInstance() {
        return ourInstance;
    }

    public static void setAccountId(String accountId){
        UserInfo.accountId = accountId;
    }
    public static String getAccountId(String accountId){
        return accountId;
    }

    private UserInfo() {
    }
}
