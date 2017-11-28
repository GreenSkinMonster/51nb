package com.greenskinmonster.a51nb.bean;

/**
 * Created by GreenSkinMonster on 2017-08-17.
 */

public class UserBean {

    private String mUid = "";
    private String mUsername = "";

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserBean userBean = (UserBean) o;

        return mUid != null ? mUid.equals(userBean.mUid) : userBean.mUid == null;

    }

    @Override
    public int hashCode() {
        return mUid != null ? mUid.hashCode() : 0;
    }
}
