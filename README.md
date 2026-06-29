🌐 Network Utility Tool

A Java desktop application built with Swing GUI that provides essential networking features in one place — domain lookup, real-time geolocation, port scanning, and local system info.


📌 About

Network Utility Tool is a Java Swing desktop application that allows users to perform common networking tasks without using the command line. It fetches real-time geolocation data using the ip-api.com REST API, scans common ports using Socket programming, and resolves domain names to IP addresses — all from a clean and simple GUI.


✨ Features


🔍 Domain to IP Resolution — Convert any domain name to its IP address
🏠 Hostname Lookup — Get the hostname of any domain or IP
📡 Ping Check — Check if a server is reachable and measure response time
📍 Real-Time Geolocation — Fetch country, city, ISP, latitude and longitude via ip-api.com
🔌 Port Scanner — Scan 12 common ports (HTTP, HTTPS, SSH, FTP, MySQL, etc.)
💻 Local System Info — Display your own machine's IP address and hostname
⚡ Multi-threaded — UI stays responsive during all network operations



🛠️ Tech Stack

TechnologyPurposeJavaCore programming languageJava SwingDesktop GUIInetAddressDomain resolution and pingSocketPort scanningHttpURLConnectionREST API callsorg.jsonJSON parsingMulti-threadingNon-blocking UI


📡 API Used

ip-api.com — Free geolocation REST API (no API key required)

GET http://ip-api.com/json/{ip}

Returns: country, city, ISP, organisation, latitude, longitude
