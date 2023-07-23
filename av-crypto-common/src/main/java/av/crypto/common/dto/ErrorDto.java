package av.crypto.common.dto;

import org.json.JSONObject;

public class ErrorDto {
    public static String serialize(String error) {
        JSONObject rootObj = new JSONObject();
        return serialize(rootObj, error);
    }

    public static String serialize(JSONObject rootObj, String error) {
        rootObj.put("error", error);
        return rootObj.toString();
    }

    public static String deserialize(String jsonStr) {
        JSONObject rootObj = new JSONObject(jsonStr);
        String error = rootObj.optString("error");
        return error;
    }
}
