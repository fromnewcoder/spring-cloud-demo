package com.gonwan.springcloud.license.event;

import com.gonwan.springcloud.license.client.OrganizationWebClient;
import com.gonwan.springcloud.license.model.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.SubscribableChannel;

interface OrganizationChannel {

    @Input("inputOrganization")
    SubscribableChannel inputOrganization();

}

@EnableBinding(OrganizationChannel.class)
public class OrganizationMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationMessageHandler.class);

    @Autowired
    private OrganizationRepository repository;

    /* @CacheEvict only available in proxy mode, so define it in another bean. */
    @Autowired
    private OrganizationWebClient organizationClient;

    @StreamListener("inputOrganization")
    public void loggerSink(OrganizationMessage msg) {
        switch (msg.getAction()) {
            case "GET":
                logger.debug("Received GET from organization service for id: {}", msg.getOrganizationId());
                break;
            case "SAVE":
                logger.debug("Received SAVE from organization service for id: {}", msg.getOrganizationId());
                break;
            case "UPDATE":
                logger.debug("Received UPDATE from organization service for id: {}", msg.getOrganizationId());
                //repository.delete(msg.getOrganizationId());
                organizationClient.evictOrganization(msg.getOrganizationId());
                break;
            case "DELETE":
                logger.debug("Received DELETE from organization service for id: {}", msg.getOrganizationId());
                //repository.delete(msg.getOrganizationId());
                organizationClient.evictOrganization(msg.getOrganizationId());
                break;
            default:
                logger.error("Received UNKNOWN action from organization service: {}", msg.toString());
                break;
        }
    }

}
