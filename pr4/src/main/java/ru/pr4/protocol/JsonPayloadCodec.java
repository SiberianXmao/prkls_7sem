package ru.pr4.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.Payload;
import io.rsocket.util.DefaultPayload;

import java.util.Objects;

public final class JsonPayloadCodec {
    private final ObjectMapper objectMapper;

    public JsonPayloadCodec() {
        this(new ObjectMapper());
    }

    public JsonPayloadCodec(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public Payload encode(Object value) {
        try {
            return DefaultPayload.create(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Не удалось сериализовать payload", exception);
        }
    }

    public <T> T decodeAndRelease(Payload payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload.getDataUtf8(), type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Некорректный JSON payload", exception);
        } finally {
            payload.release();
        }
    }
}
