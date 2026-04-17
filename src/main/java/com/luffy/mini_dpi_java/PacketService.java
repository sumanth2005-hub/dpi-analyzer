package com.luffy.mini_dpi_java;

import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacketService {

    public void startCapture() throws Exception {

        // STEP 1: List all interfaces
        System.out.println("===== AVAILABLE INTERFACES =====");
        List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();

        if (allDevs == null || allDevs.isEmpty()) {
            throw new RuntimeException("No network interfaces found. Is Npcap installed?");
        }

        for (PcapNetworkInterface dev : allDevs) {
            System.out.println(dev.getName() + " -> " + dev.getDescription());
        }

        // STEP 2: Select active interface (non-loopback, running, has addresses)
        PcapNetworkInterface nif = allDevs.stream()
                .filter(dev -> {
                    try {
                        return dev.isRunning()
                                && !dev.isLoopBack()
                                && dev.getAddresses().size() > 0;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active network interface found"));

        System.out.println("✅ SELECTED: " + nif.getName() + " -> " + nif.getDescription());
        System.out.println("IS RUNNING: " + nif.isRunning());
        System.out.println("IS LOOPBACK: " + nif.isLoopBack());

        // STEP 3: Open the handle
        PcapHandle handle = nif.openLive(
                65536,                                          // snaplen: max bytes per packet
                PcapNetworkInterface.PromiscuousMode.PROMISCUOUS,
                10                                              // timeout in ms
        );

        // STEP 4: Apply BPF filter — only capture IPv4 packets
        handle.setFilter("ip", BpfProgram.BpfCompileMode.OPTIMIZE);

        System.out.println("🚀 Capture started...");

        // STEP 5: Define the packet listener
        PacketListener listener = packet -> {
            try {
                // Now works because pcap4j-packetfactory-static is present
                if (packet.contains(IpV4Packet.class)) {
                    IpV4Packet ip = packet.get(IpV4Packet.class);
                    String src = ip.getHeader().getSrcAddr().getHostAddress();
                    String dst = ip.getHeader().getDstAddr().getHostAddress();
                    String protocol = ip.getHeader().getProtocol().name();

                    // Detect TCP vs UDP for richer output
                    if (packet.contains(TcpPacket.class)) {
                        TcpPacket tcp = packet.get(TcpPacket.class);
                        System.out.printf("[TCP] %s:%d → %s:%d%n",
                                src, tcp.getHeader().getSrcPort().valueAsInt(),
                                dst, tcp.getHeader().getDstPort().valueAsInt());

                    } else if (packet.contains(UdpPacket.class)) {
                        UdpPacket udp = packet.get(UdpPacket.class);
                        System.out.printf("[UDP] %s:%d → %s:%d%n",
                                src, udp.getHeader().getSrcPort().valueAsInt(),
                                dst, udp.getHeader().getDstPort().valueAsInt());

                    } else {
                        System.out.printf("[%s] SRC: %s → DST: %s%n", protocol, src, dst);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing packet: " + e.getMessage());
            }
        };

        // STEP 6: Run capture in background thread (so Spring Boot doesn't block)
        new Thread(() -> {
            try {
                handle.loop(-1, listener); // -1 = infinite loop
            } catch (InterruptedException e) {
                System.out.println("Capture stopped.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "packet-capture-thread").start(); // give thread a name — good practice
    }
}