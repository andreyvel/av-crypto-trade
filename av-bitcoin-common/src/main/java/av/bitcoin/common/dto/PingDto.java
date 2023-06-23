package av.bitcoin.common.dto;

import org.json.JSONObject;
import av.bitcoin.common.Utils;
import java.time.LocalDateTime;

public class PingDto {
    private LocalDateTime created = LocalDateTime.now();
    private LocalDateTime received = null;
    private int status;
    public long delayMs() {
        if (received == null) {
            return 0;
        }

        return Utils.delayMs(created, received);
    }

    public PingDto(int status, LocalDateTime created, LocalDateTime received) {
        this.status = status;
        this.created = created;
        this.received = received;
    }

    public PingDto(JSONObject obj) {
        status = obj.optInt("status");
        String created2 = obj.optString("created");
        created = LocalDateTime.parse(created2);

        String received2 = obj.optString("received");
        if (received2 != null) {
            received = LocalDateTime.parse(received2);
        }
    }

    public JSONObject serialize() {
        JSONObject obj = new JSONObject();
        obj.put("status", status);
        obj.put("created", created);

        if (received != null) {
            obj.put("received", received);
        }
        return obj;
    }
}
