package av.bitcoin.common.dto;

import org.json.JSONArray;
import org.json.JSONObject;
import av.bitcoin.common.Enums.ValueUnit;
import av.bitcoin.common.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChartItemDto {
    private LocalDateTime date;
    private String shape;
    private double value;
    private ValueUnit unit; // absolute or percent
    private String color;
    private int radius;

    public ChartItemDto() {
    }

    public ChartItemDto(LocalDateTime date, String shape, String color, ValueUnit unit, int radius) {
        this.date = date;
        this.shape = shape;
        this.color = color;

        this.unit = unit;
        this.radius = radius;
    }

    public LocalDateTime date() {
        return date;
    }


    public double value() {
        return value;
    }

    public double calcValue(double valueBase) {
        if (unit == ValueUnit.ABS) {
            return value;
        }
        return valueBase * (1.0d + value / 100d);
    }
    public ValueUnit unit() {
        return unit;
    }

    public String color() {
        return color;
    }

    public int radius() {
        return radius;
    }

    public void value(double value) {
        this.value = value;
    }

    public JSONObject serialize() {
        JSONObject rootObj = new JSONObject();
        rootObj.put("date", date);
        rootObj.put("shape", shape);
        rootObj.put("color", color);
        rootObj.put("radius", radius);

        rootObj.put("unit", unit);
        rootObj.put("value", Utils.num4(value));
        return rootObj;
    }

    public static JSONArray serialize(Collection<ChartItemDto> list) {
        JSONArray jsonArr = new JSONArray();
        for(ChartItemDto item : list) {
            JSONObject jsonObj = item.serialize();
            jsonArr.put(jsonObj);
        }
        return jsonArr;
    }

    public static List<ChartItemDto> deserialize(JSONArray jsonArr) {
        List<ChartItemDto> listRet = new ArrayList<>();
        for (int ind = 0; ind < jsonArr.length(); ind++) {
            JSONObject obj = jsonArr.getJSONObject(ind);

            ChartItemDto dto = new ChartItemDto();
            dto.shape = obj.optString("shape");
            dto.color = obj.optString("color");
            dto.radius = obj.optInt("radius");
            dto.value = obj.optDouble("value");

            String unit = obj.optString("unit");
            dto.unit = ("PCT".equals(unit)) ? ValueUnit.PCT : ValueUnit.ABS;

            String date = obj.optString("date");
            dto.date = LocalDateTime.parse(date);
            listRet.add(dto);
        }
        return listRet;
    }
}
