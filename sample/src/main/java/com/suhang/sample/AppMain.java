package com.suhang.sample;

import java.util.List;

/**
 * Created by 苏杭 on 2017/4/24 16:19.
 */

public class AppMain extends ErrorBean{
    public static final String URL = "/api/other/indexApiForApp.php";
    public static final String URL1 = "/api/other/indexApiForApp1.php";
    public static final String METHOD = "getAppMain";
    private String total;

//    @Override
//    public String toString() {
//        return "AppMain{" +
//                "total='" + total + '\'' +
//                ", list=" + list +
//                '}';
//    }

    private List<ListEntity> list;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<ListEntity> getList() {
        return list;
    }

    public void setList(List<ListEntity> list) {
        this.list = list;
    }

    public static class ListEntity {
        private String lvid;
        private String uid;
        private String nick;
        private String head;
        private String gameID;
        private String poster;
        private String title;
        private String stime;
        private String orientation;
        private String vtype;
        private String gameName;
        private String userCount;

        @Override
        public String toString() {
            return "ListEntity{" +
                    "lvid='" + lvid + '\'' +
                    ", uid='" + uid + '\'' +
                    ", nick='" + nick + '\'' +
                    ", head='" + head + '\'' +
                    ", gameID='" + gameID + '\'' +
                    ", poster='" + poster + '\'' +
                    ", title='" + title + '\'' +
                    ", stime='" + stime + '\'' +
                    ", orientation='" + orientation + '\'' +
                    ", vtype='" + vtype + '\'' +
                    ", gameName='" + gameName + '\'' +
                    ", userCount='" + userCount + '\'' +
                    '}';
        }

        public String getLvid() {
            return lvid;
        }

        public void setLvid(String lvid) {
            this.lvid = lvid;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public String getHead() {
            return head;
        }

        public void setHead(String head) {
            this.head = head;
        }

        public String getGameID() {
            return gameID;
        }

        public void setGameID(String gameID) {
            this.gameID = gameID;
        }

        public String getPoster() {
            return poster;
        }

        public void setPoster(String poster) {
            this.poster = poster;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getStime() {
            return stime;
        }

        public void setStime(String stime) {
            this.stime = stime;
        }

        public String getOrientation() {
            return orientation;
        }

        public void setOrientation(String orientation) {
            this.orientation = orientation;
        }

        public String getVtype() {
            return vtype;
        }

        public void setVtype(String vtype) {
            this.vtype = vtype;
        }

        public String getGameName() {
            return gameName;
        }

        public void setGameName(String gameName) {
            this.gameName = gameName;
        }

        public String getUserCount() {
            return userCount;
        }

        public void setUserCount(String userCount) {
            this.userCount = userCount;
        }
    }
}
