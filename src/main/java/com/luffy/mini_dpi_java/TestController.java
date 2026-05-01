package com.luffy.mini_dpi_java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    private PacketService service;

    /*@GetMapping("/packets")
    public List<PacketLog> getPackets() {
        return service.getPackets();
    }*/
    @GetMapping("/packets")
    public List<PacketLog> getPackets(
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) String ip) {

        return service.getPackets().stream()
                .filter(p -> protocol == null || p.getProtocol().equalsIgnoreCase(protocol))
                .filter(p -> ip == null || p.getSrcIp().contains(ip) || p.getDstIp().contains(ip))
                .collect(java.util.stream.Collectors.toList());
    }


    @GetMapping(value = "/packets/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPackets() {
        return service.addEmitter();
    }

    @GetMapping("/packets/stats")
    public Map<String, Long> getStats() {
        return service.getPackets().stream()
                .collect(Collectors.groupingBy(PacketLog::getProtocol, Collectors.counting()));
    }
}