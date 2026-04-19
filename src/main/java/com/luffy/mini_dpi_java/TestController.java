package com.luffy.mini_dpi_java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class TestController {

    @Autowired
    private PacketService service;

    @GetMapping("/")
    public String home() {
        return "DPI Project Running 🚀";
    }

    @GetMapping("/start")
    public String start() throws Exception {
        service.startCapture();
        return "Packet capture started";
    }
    @GetMapping("/packets")
    public List<PacketLog> getPackets() {
        return service.getPackets();
    }
}