package av.bitcoin.common.dto;

import org.json.JSONArray;
import org.json.JSONObject;
import av.bitcoin.common.Enums.ValueUnit;
import av.bitcoin.common.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChartLineDto {
    private LocalDateTime date0;
    private LocalDateTime date1;
    private double value0;
    private double value1;
    private ValueUnit unit; // absolute or percent
    private String color;

    public ChartLineDto() {
    }

    public ChartLineDto(LocalDateTime date0, LocalDateTime date1, String color, ValueUnit unit) {
        this.date0 = date0;
        this.date1 = date1;
        this.color = color;
        this.unit = unit;
    }

    public LocalDateTime date0() {
        return date0;
    }
    public LocalDateTime date1() {
        return date1;
    }

    public double value0() {
        return value0;
    }
    public double calcValue0(double valueBase) {
        if (unit == ValueUnit.ABS) {
            return value0;
        }
        return valueBase * (1.0d + value0 / 100d);
    }

    public double calcValue1(double valueBase) {
        if (unit == ValueUnit.ABS) {
            return value1;
        }
        return valueBase * (1.0d + value1 / 100d);
    }
    public double value1() {
        return value1;
    }

    public String color() {
        return color;
    }

    public void value0(double value0) {
        this.value0 = value0;
    }

    public void value1(double value1) {
        this.value1 = value1;
    }

    public JSONObject serialize() {
        JSONObject rootObj = new JSONObject();
        rootObj.put("date0", date0);
        rootObj.put("date1", date1);
        rootObj.put("color", color);
        rootObj.put("unit", unit);

        rootObj.put("value0", Utils.num4(value0));
        rootObj.put("value1", Utils.num4(value1));
        return rootObj;
    }

    public static JSONArray serialize(Collection<ChartLineDto> list) {
        JSONArray jsonArr = new JSONArray();
        for(ChartLineDto item : list) {
            JSONObject jsonObj = item.serialize();
            jsonArr.put(jsonObj);
        }
        return jsonArr;
    }

    public static List<ChartLineDto> deserialize(JSONArray jsonArr) {
        List<ChartLineDto> listRet = new ArrayList<>();
        for (int ind = 0; ind < jsonArr.length(); ind++) {
            JSONObject obj = jsonArr.getJSONObject(ind);
            ChartLineDto dto = new ChartLineDto();

            String unit = obj.optString("unit");
            dto.unit = ("PCT".equals(unit)) ? ValueUnit.PCT : ValueUnit.ABS;

            dto.color = obj.optString("color");
            dto.value0 = obj.optDouble("value0");
            dto.value1 = obj.optDouble("value1");

            String date0 = obj.optString("date0");
            String date1 = obj.optString("date1");
            dto.date0 = LocalDateTime.parse(date0);
            dto.date1 = LocalDateTime.parse(date1);
            listRet.add(dto);
        }
        return listRet;
    }
}
