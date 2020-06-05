package com.example.checkoutdevices.Bean;

public class Devices {
    private String zkId;//网关id
    private String pin;//网关ping值
    private String zkVersion;//网关版本号
    private String time;//记录创建时间
    private String operator;//记录创建者，操作员
    private String zkType;//网关类型
    private String zkFactory;//网关厂商
    private String issynchr;//是否被同步
    private String qrcode;//关联二维码
    private String isqrcode;//是否关联了二维码

    public String getZkId() {
        return zkId;
    }

    @Override
    public String toString() {
        return "Devices{" +
                "zkId='" + zkId + '\'' +
                ", pin='" + pin + '\'' +
                ", zkVersion='" + zkVersion + '\'' +
                ", time='" + time + '\'' +
                ", operator='" + operator + '\'' +
                ", zkType='" + zkType + '\'' +
                ", zkFactory='" + zkFactory + '\'' +
                ", issynchr='" + issynchr + '\'' +
                ", qrcode='" + qrcode + '\'' +
                ", isqrcode='" + isqrcode + '\'' +
                '}';
    }

    public void setZkId(String zkId) {
        this.zkId = zkId;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getZkVersion() {
        return zkVersion;
    }

    public void setZkVersion(String zkVersion) {
        this.zkVersion = zkVersion;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getZkType() {
        return zkType;
    }

    public void setZkType(String zkType) {
        this.zkType = zkType;
    }

    public String getZkFactory() {
        return zkFactory;
    }

    public void setZkFactory(String zkFactory) {
        this.zkFactory = zkFactory;
    }

    public String getIssynchr() {
        return issynchr;
    }

    public void setIssynchr(String issynchr) {
        this.issynchr = issynchr;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getIsqrcode() {
        return isqrcode;
    }

    public void setIsqrcode(String isqrcode) {
        this.isqrcode = isqrcode;
    }

    public Devices(String zkId, String pin, String zkVersion, String time, String operator, String zkType, String zkFactory, String issynchr, String qrcode, String isqrcode) {
        this.zkId = zkId;
        this.pin = pin;
        this.zkVersion = zkVersion;
        this.time = time;
        this.operator = operator;
        this.zkType = zkType;
        this.zkFactory = zkFactory;
        this.issynchr = issynchr;
        this.qrcode = qrcode;
        this.isqrcode = isqrcode;
    }

    public Devices() {

    }
}
