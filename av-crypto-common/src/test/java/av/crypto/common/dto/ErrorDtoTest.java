package av.crypto.common.dto;

import org.junit.Assert;
import org.junit.Test;
import java.util.UUID;

public class ErrorDtoTest {
    @Test
    public void serializeTest() {
        String error = UUID.randomUUID().toString();
        String content = ErrorDto.serialize(error);
        String error2 = ErrorDto.deserialize(content);
        Assert.assertEquals(error, error2);
    }
}
