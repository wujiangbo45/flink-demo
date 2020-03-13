package com.navinfo.opentsp.flink.pojo;

public class TboxDataPojo {
    private long tId;

    private long gpsTime;

    private int lat;

    private int lon;

    public long gettId() {
        return tId;
    }

    public void settId(long tId) {
        this.tId = tId;
    }

    public long getGpsTime() {
        return gpsTime;
    }

    public void setGpsTime(long gpsTime) {
        this.gpsTime = gpsTime;
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getLon() {
        return lon;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "TboxDataPojo{" +
                "tId=" + tId +
                ", gpsTime=" + gpsTime +
                ", lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
