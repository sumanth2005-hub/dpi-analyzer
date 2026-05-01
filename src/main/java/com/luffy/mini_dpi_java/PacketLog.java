package com.luffy.mini_dpi_java;

import jakarta.persistence.*;

@Entity
@Table(name = "packets")
public class PacketLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String srcIp;
    private String dstIp;
    private int srcPort;
    private int dstPort;
    private String protocol;
    private long timestamp;

    public PacketLog() {}

    public PacketLog(String srcIp, String dstIp, int srcPort, int dstPort, String protocol) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return srcIp + ":" + srcPort + " → " + dstIp + ":" + dstPort + " [" + protocol + "]";
    }

    public Long getId() { return id; }
    public String getSrcIp() { return srcIp; }
    public String getDstIp() { return dstIp; }
    public int getSrcPort() { return srcPort; }
    public int getDstPort() { return dstPort; }
    public String getProtocol() { return protocol; }
    public long getTimestamp() { return timestamp; }
}