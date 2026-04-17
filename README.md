# 🛰️ DPI Analyzer (Java)

A simple **Deep Packet Inspection (DPI) Analyzer** built using Java and Spring Boot.
This project captures live network packets and extracts key information such as **source IP** and **destination IP**.

---

## 🚀 Features

* 📡 Capture real-time network packets
* 🌐 Extract IPv4 packet details
* 🔍 Display source and destination IP addresses
* ⚙️ Built using Spring Boot (REST API based)
* 🧩 Uses Pcap4J for packet capturing

---

## 🛠️ Tech Stack

* Java 17
* Spring Boot
* Maven
* Pcap4J
* Npcap (for packet capture)

---

## 📂 Project Structure

```
mini-dpi-java/
│
├── src/main/java/com/luffy/mini_dpi_java/
│   ├── MiniDpiJavaApplication.java   # Main Spring Boot Application
│   ├── TestController.java           # REST Controller
│   └── PacketService.java            # Packet Capture Logic
│
├── src/main/resources/
│   └── application.properties
│
├── pom.xml
└── README.md
```

---

## ⚙️ Setup Instructions

### 1️⃣ Clone the Repository

```
git clone https://github.com/sumanth2005-hub/dpi-analyzer.git
cd dpi-analyzer
```

---

### 2️⃣ Install Dependencies

Make sure you have:

* Java 17+
* Maven installed

---

### 3️⃣ Install Npcap

Download and install from:
https://npcap.com

👉 أثناء installation:

* Enable **WinPcap API-compatible mode**

---

### 4️⃣ Run the Application

```
mvn spring-boot:run
```

OR run `MiniDpiJavaApplication.java` from IntelliJ.

---

### 5️⃣ Start Packet Capture

Open browser:

```
http://localhost:8080/start
```

---

## 📊 Sample Output

```
SRC: 192.168.0.5 → DST: 142.250.183.78
SRC: 192.168.0.5 → DST: 8.8.8.8
```

---

## ⚠️ Important Notes

* Run IDE as **Administrator**
* Ensure internet traffic is active
* Select correct network interface (Wi-Fi/Ethernet)

---

## 🔮 Future Improvements

* Packet filtering (TCP/UDP/HTTP)
* Store packets in database (MySQL)
* UI dashboard for visualization
* Protocol-level analysis
* Threat detection

---

## 🧠 What I Learned

* Networking fundamentals (IP packets)
* Packet capture using Pcap4J
* Spring Boot architecture
* REST API design
* Real-time data processing

---

## 👨‍💻 Author

**Sumanth**
GitHub: https://github.com/sumanth2005-hub

---

## ⭐ Give a Star

If you like this project, consider giving it a ⭐ on GitHub!
