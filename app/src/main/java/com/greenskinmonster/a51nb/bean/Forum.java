package com.greenskinmonster.a51nb.bean;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.typeface.IIcon;

/**
 * Created by GreenSkinMonster on 2016-07-21.
 */
public class Forum {

    public final static int GROUP = 0;
    public final static int FORUM = 1;
    public final static int SUB_FORUM = 2;

    private String mName;
    private int mId;
    private int mLevel;

    public Forum(int id, int level, String name) {
        mId = id;
        mName = name;
        mLevel = level;
    }

    public IIcon getIcon() {
        return FontAwesome.Icon.faw_circle;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    @Override
    public String toString() {
        return "Forum{" +
                "mName='" + mName + '\'' +
                ", mId=" + mId +
                ", mLevel=" + mLevel +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Forum forum = (Forum) o;

        return mId == forum.mId;

    }

    @Override
    public int hashCode() {
        return mId;
    }
}
