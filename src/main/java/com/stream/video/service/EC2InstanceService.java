package com.stream.video.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EC2InstanceService {
    private final RestTemplate restTemplate;

    @Autowired
    public EC2InstanceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getIpV4Instance() {
        return restTemplate.getForEntity("http://checkip.amazonaws.com/", String.class).getBody();
    }
}
