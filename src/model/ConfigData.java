package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by assistne on 2017/5/15.
 */
public class ConfigData {
    public String firstDomain;
    public String secondDomain;
    public String urlPath;

    public List<KeyTypeItem> getParams = new ArrayList<>();
    public List<KeyTypeItem> postParams = new ArrayList<>();

    public String respJson;

    public boolean isPlain;

    public void addGetParam(String fieldName, int typeIndex) {
        addParam(getParams, fieldName, typeIndex);
    }

    public void addPostParam(String fieldName, int typeIndex) {
        addParam(postParams, fieldName, typeIndex);
    }

    private void addParam(List<KeyTypeItem> params, String fieldName, int typeIndex) {
        KeyTypeItem item = new KeyTypeItem();
        item.key = fieldName;
        item.typeIndex = typeIndex;
        params.add(item);
    }

    @Override
    public String toString() {
        return "ConfigData{" +
                "firstDomain='" + firstDomain + '\'' +
                ", secondDomain='" + secondDomain + '\'' +
                ", urlPath='" + urlPath + '\'' +
                ", getParams=" + getParams +
                ", postParams=" + postParams +
                ", respJson='" + respJson + '\'' +
                ", isPlain=" + isPlain +
                '}';
    }
}
