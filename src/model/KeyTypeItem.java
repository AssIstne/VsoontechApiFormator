package model;

/**
 * Created by assistne on 2017/5/15.
 */
public class KeyTypeItem {
    public static final String[] TYPE_VALUES = { "String", "int", "boolean", "float", "long" };
    public String key;
    public int typeIndex;

    @Override
    public String toString() {
        return "KeyTypeItem{" +
                "key='" + key + '\'' +
                ", type='" + TYPE_VALUES[typeIndex] + '\'' +
                '}';
    }
}
