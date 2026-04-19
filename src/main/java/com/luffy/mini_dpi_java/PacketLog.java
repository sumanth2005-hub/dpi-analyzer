package com.luffy.mini_dpi_java;

public class PacketLog {

    private String srcIp;
    private String dstIp;
    private int srcPort;
    private int dstPort;
    private String protocol;
    private long timestamp;

    public PacketLog(String srcIp, String dstIp, int srcPort, int dstPort, String protocol) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters only (important for JSON)
    public String getSrcIp() { return srcIp; }
    public String getDstIp() { return dstIp; }
    public int getSrcPort() { return srcPort; }
    public int getDstPort() { return dstPort; }
    public String getProtocol() { return protocol; }
    public long getTimestamp() { return timestamp; }
}
