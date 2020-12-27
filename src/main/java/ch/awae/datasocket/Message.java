package ch.awae.datasocket;

import java.io.Serializable;
import java.util.UUID;

class Message {

    private final MessageType type;
    private final UUID uuid;
    private final Serializable payload;

    Message(MessageType type, UUID uuid, Serializable payload) {
        this.type = type;
        this.uuid = uuid;
        this.payload = payload;
    }

    MessageType getType() {
        return type;
    }

    Serializable getPayload() {
        return payload;
    }

    UUID getUuid() {
        return uuid;
    }

}
