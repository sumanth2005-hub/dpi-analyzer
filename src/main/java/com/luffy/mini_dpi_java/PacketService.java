package com.luffy.mini_dpi_java;

import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PacketService {

    // Thread-safe packet storage
    private final List<PacketLog> packetList = new CopyOnWriteArrayList<>();

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

        System.out.println(" SELECTED: " + nif.getName() + " -> " + nif.getDescription());

        // STEP 3: Open handle
        PcapHandle handle = nif.openLive(
                65536,
                PcapNetworkInterface.PromiscuousMode.PROMISCUOUS,
                10
        );

        // STEP 4: Apply filter
        handle.setFilter("ip", BpfProgram.BpfCompileMode.OPTIMIZE);

        System.out.println("🚀 Capture started...");

        //  STEP 5: LISTENER (THIS IS STEP 3 LOGIC AREA)
        PacketListener listener = packet -> {
            try {

                if (!packet.contains(IpV4Packet.class)) return;

                IpV4Packet ip = packet.get(IpV4Packet.class);

                String srcIp = ip.getHeader().getSrcAddr().getHostAddress();
                String dstIp = ip.getHeader().getDstAddr().getHostAddress();
                String protocol = ip.getHeader().getProtocol().name();

                int srcPort = -1;
                int dstPort = -1;

                // TCP
                if (packet.contains(TcpPacket.class)) {
                    TcpPacket tcp = packet.get(TcpPacket.class);
                    srcPort = tcp.getHeader().getSrcPort().valueAsInt();
                    dstPort = tcp.getHeader().getDstPort().valueAsInt();
                }

                // UDP
                else if (packet.contains(UdpPacket.class)) {
                    UdpPacket udp = packet.get(UdpPacket.class);
                    srcPort = udp.getHeader().getSrcPort().valueAsInt();
                    dstPort = udp.getHeader().getDstPort().valueAsInt();
                }

                // CREATE OBJECT
                PacketLog log = new PacketLog(
                        srcIp,
                        dstIp,
                        srcPort,
                        dstPort,
                        protocol
                );

                //  STORE PACKET
                packetList.add(log);

                // LIMIT SIZE (prevents memory overflow)
                if (packetList.size() > 1000) {
                    packetList.remove(0);
                }

                // DEBUG (optional)
                System.out.println(
                        "Captured: " + srcIp +
                                " → " + dstIp +
                                " | " + protocol +
                                " | " + srcPort + " → " + dstPort
                );

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

    //  API access
    public List<PacketLog> getPackets() {
        return packetList;
    }
}