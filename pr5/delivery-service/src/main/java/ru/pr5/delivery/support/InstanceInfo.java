package ru.pr5.delivery.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InstanceInfo {
    private final String instanceId;

    public InstanceInfo(@Value("${HOSTNAME:local-delivery-service}") String instanceId) {
        this.instanceId = instanceId;
    }

    public String id() {
        return instanceId;
    }
}
