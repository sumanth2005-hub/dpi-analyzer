package com.luffy.mini_dpi_java;

import jakarta.annotation.PostConstruct;
import org.pcap4j.core.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class PacketService {

    private final LinkedBlockingDeque<PacketLog> packetList = new LinkedBlockingDeque<>(500);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final PacketParser parser = new PacketParser();
    private final PacketRepository repository;

    public PacketService(PacketRepository repository) {
        this.repository = repository;
    }

    public SseEmitter addEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    @PostConstruct
    public void startCapture() {
        try {
            System.out.println("===== AVAILABLE INTERFACES =====");
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();

            if (allDevs == null || allDevs.isEmpty()) {
                throw new RuntimeException("No network interfaces found. Is Npcap installed?");
            }

            for (PcapNetworkInterface dev : allDevs) {
                System.out.println(dev.getName() + " -> " + dev.getDescription());
            }

            PcapNetworkInterface nif = allDevs.stream()
                    .filter(dev -> {
                        try {
                            String desc = dev.getDescription() == null ? "" : dev.getDescription().toLowerCase();
                            return dev.isRunning()
                                    && !dev.isLoopBack()
                                    && dev.getAddresses().size() > 0
                                    && (
                                    desc.contains("wi-fi") ||
                                            desc.contains("wireless") ||
                                            desc.contains("realtek") ||
                                            desc.contains("ethernet")
                            )
                                    && !desc.contains("bluetooth");
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No suitable internet interface found"));

            System.out.println("SELECTED: " + nif.getName() + " -> " + nif.getDescription());

            PcapHandle handle = nif.openLive(
                    65536,
                    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS,
                    10
            );

            handle.setFilter("ip", BpfProgram.BpfCompileMode.OPTIMIZE);

            System.out.println("Capture started...");

            PacketListener listener = packet -> {
                try {
                    PacketLog log = parser.parse(packet);
                    if (log == null) return;

                    if (!packetList.offerLast(log)) {
                        packetList.pollFirst();
                        packetList.offerLast(log);
                    }

                    for (SseEmitter emitter : emitters) {
                        try {
                            emitter.send(log);
                        } catch (Exception e) {
                            emitters.remove(emitter);
                        }
                    }

                    System.out.println("Captured: " + log);
                    repository.save(log);

                } catch (Exception e) {
                    System.err.println("Error processing packet: " + e.getMessage());
                }
            };

            new Thread(() -> {
                try {
                    handle.loop(-1, listener);
                } catch (InterruptedException e) {
                    System.out.println("Capture stopped.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "packet-capture-thread").start();

        } catch (Exception e) {
            System.err.println("Failed to start capture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<PacketLog> getPackets() {
        return new ArrayList<>(packetList);
    }
}