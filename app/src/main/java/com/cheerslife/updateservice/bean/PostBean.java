package com.cheerslife.updateservice.bean;

import com.google.gson.Gson;

/**
 * <pre>
 *     author : fupp-
 *     time   : 2020/10/20
 *     desc   :
 * </pre>
 */
public class PostBean {

    private String id;
    private String macAddress;
    private String productType;
    private String factoryName;
    private String officeName;
    private String wardName;
    private String bedNo;
    private String apkType;
    private String currentVersion;
    private String newVersion;
    private String updateStatus;
    private String updateLog;

    public PostBean(Builder b) {
        this.macAddress = b.macAddress;
        this.productType = b.productType;
        this.factoryName = b.factoryName;
        this.officeName = b.officeName;
        this.wardName = b.wardName;
        this.bedNo = b.bedNo;
        this.apkType = b.apkType;
        this.currentVersion = b.currentVersion;
        this.newVersion = b.newVersion;
        this.updateStatus = b.updateStatus;
        this.updateLog = b.updateLog;
        this.id = b.id;
    }

    public static class Builder{

        private String id;
        private String macAddress;
        private String productType;
        private String factoryName;
        private String officeName;
        private String wardName;
        private String bedNo;
        private String apkType;
        private String currentVersion;
        private String newVersion;
        private String updateStatus;
        private String updateLog;

        public PostBean build(){
            return new PostBean(this);
        }

        public Builder setMacAddress(String macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setProductType(String productType) {
            this.productType = productType;
            return this;
        }

        public Builder setFactoryName(String factoryName) {
            this.factoryName = factoryName;
            return this;
        }

        public Builder setOfficeName(String officeName) {
            this.officeName = officeName;
            return this;
        }

        public Builder setWardName(String wardName) {
            this.wardName = wardName;
            return this;
        }

        public Builder setBedNo(String bedNo) {
            this.bedNo = bedNo;
            return this;
        }

        public Builder setApkType(String apkType) {
            this.apkType = apkType;
            return this;
        }

        public Builder setCurrentVersion(String currentVersion) {
            this.currentVersion = currentVersion;
            return this;
        }

        public Builder setNewVersion(String newVersion) {
            this.newVersion = newVersion;
            return this;
        }

        public Builder setUpdateStatus(String updateStatus) {
            this.updateStatus = updateStatus;
            return this;
        }

        public Builder setUpdateLog(String updateLog) {
            this.updateLog = updateLog;
            return this;
        }
    }

    public String toJson(){
       return new Gson().toJson(this);
    }

}
