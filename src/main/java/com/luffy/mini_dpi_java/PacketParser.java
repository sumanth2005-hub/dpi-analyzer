package com.luffy.mini_dpi_java;

import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

public class PacketParser {

    public PacketLog parse(Packet packet) {
        IpV4Packet ipPacket = packet.get(IpV4Packet.class);
        if (ipPacket == null) return null;

        String srcIp = ipPacket.getHeader().getSrcAddr().getHostAddress();
        String dstIp = ipPacket.getHeader().getDstAddr().getHostAddress();

        int srcPort = -1, dstPort = -1;

        TcpPacket tcp = packet.get(TcpPacket.class);
        UdpPacket udp = packet.get(UdpPacket.class);

        if (tcp != null) {
            srcPort = tcp.getHeader().getSrcPort().valueAsInt();
            dstPort = tcp.getHeader().getDstPort().valueAsInt();
        } else if (udp != null) {
            srcPort = udp.getHeader().getSrcPort().valueAsInt();
            dstPort = udp.getHeader().getDstPort().valueAsInt();
        }

        String protocol = detectProtocol(srcPort, dstPort);
        return new PacketLog(srcIp, dstIp, srcPort, dstPort, protocol);
    }

    private String detectProtocol(int src, int dst) {
        if (hits(src, dst, 80))   return "HTTP";
        if (hits(src, dst, 443))  return "HTTPS";
        if (hits(src, dst, 53))   return "DNS";
        if (hits(src, dst, 22))   return "SSH";
        if (hits(src, dst, 3306)) return "MySQL";
        return "OTHER";
    }

    private boolean hits(int src, int dst, int port) {
        return src == port || dst == port;
    }
}