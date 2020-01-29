package com.xsolla.android.login.social;

public enum Social {

    GOOGLE("google"),
    FACEBOOK("facebook"),
    TWITTER("twitter"),
    NAVER("naver"),
    LINKEDIN("linkedin"),
    BAIDU("baidu");

    public final String providerName;

    Social(String providerName) {
        this.providerName = providerName;
    }
}