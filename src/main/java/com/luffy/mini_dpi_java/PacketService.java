package com.luffy.mini_dpi_java;

import org.pcap4j.core.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PacketService {

    private final List<PacketLog> packetList = new CopyOnWriteArrayList<>();
    private final PacketParser parser = new PacketParser();

    public void startCapture() throws Exception {

        // STEP 1: List interfaces
        System.out.println("===== AVAILABLE INTERFACES =====");
        List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();

        if (allDevs == null || allDevs.isEmpty()) {
            throw new RuntimeException("No network interfaces found. Is Npcap installed?");
        }

        for (PcapNetworkInterface dev : allDevs) {
            System.out.println(dev.getName() + " -> " + dev.getDescription());
        }

        // STEP 2: Select active interface
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

        // STEP 3: Open handle
        PcapHandle handle = nif.openLive(
                65536,
                PcapNetworkInterface.PromiscuousMode.PROMISCUOUS,
                10
        );

        // STEP 4: Apply filter
        handle.setFilter("ip", BpfProgram.BpfCompileMode.OPTIMIZE);

        System.out.println("Capture started...");

        // STEP 5: Listener — clean, parser does the work
        PacketListener listener = packet -> {
            try {
                PacketLog log = parser.parse(packet);
                if (log == null) return;

                packetList.add(log);

                if (packetList.size() > 1000) {
                    packetList.remove(0);
                }

                System.out.println("Captured: " + log);

            } catch (Exception e) {
                System.err.println("Error processing packet: " + e.getMessage());
            }
        };

        // STEP 6: Background thread
        new Thread(() -> {
            try {
                handle.loop(-1, listener);
            } catch (InterruptedException e) {
                System.out.println("Capture stopped.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "packet-capture-thread").start();
    }

    public List<PacketLog> getPackets() {
        return packetList;
    }
}