package com.luffy.mini_dpi_java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    private PacketService service;

    @GetMapping("/packets")
    public List<PacketLog> getPackets() {
        return service.getPackets();
    }

    @GetMapping(value = "/packets/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPackets() {
        return service.addEmitter();
    }
}