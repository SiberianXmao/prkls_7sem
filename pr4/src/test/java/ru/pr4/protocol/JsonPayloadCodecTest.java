package ru.pr4.protocol;

import org.junit.jupiter.api.Test;
import ru.pr4.model.ProductRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonPayloadCodecTest {
    @Test
    void encodesAndDecodesRecord() {
        JsonPayloadCodec codec = new JsonPayloadCodec();

        ProductRequest decoded = codec.decodeAndRelease(
                codec.encode(new ProductRequest(27)), ProductRequest.class);

        assertEquals(new ProductRequest(27), decoded);
    }
}
