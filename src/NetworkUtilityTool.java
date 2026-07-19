// Color, Font, Dimension, BorderLayout, FlowLayout, GridLayout
import java.awt.*;

// BufferedReader, InputStreamReader — reads API response text line by line
import java.io.*;

// InetAddress, Socket, URL, HttpURLConnection, UnknownHostException — all networking
import java.net.*;

// JFrame, JPanel, JButton, JTextField, JTextArea, JLabel, JTable, JScrollPane, SwingUtilities
import javax.swing.*;

// EmptyBorder (padding inside panels), LineBorder (border outline), CompoundBorder (both combined)
import javax.swing.border.*;

// DefaultTableModel (manages port table rows/columns), DefaultTableCellRenderer (colors rows green/red)
import javax.swing.table.*;

// JSONObject — parses ip-api.com JSON response string into usable Java variables
import org.json.*;

// Extends JFrame so this class IS the main window
public class NetworkUtilityTool extends JFrame {

    // Text field where user types domain or IP address
    private JTextField txtDomain;

    // Multi-line area that shows results of all operations
    private JTextArea txtResult;

    // Small label at bottom showing current status (success/error/loading)
    private JLabel lblStatus;

    // Labels that display geolocation data fetched from ip-api.com
    private JLabel lblIP, lblCountry, lblCity, lblISP, lblLat, lblLon, lblOrg;

    // Manages the rows and columns of the port scanner table
    private DefaultTableModel portTableModel;

    // Labels that show local machine IP and hostname
    private JLabel lblLocalIP, lblHostname;

    // ── Entry Point ────────────────────────────────────────────
    public static void main(String[] args) {
        // invokeLater ensures UI is created on the Swing Event Dispatch Thread (thread safety)
        SwingUtilities.invokeLater(() -> new NetworkUtilityTool());
    }

    // ── Constructor: Builds the entire UI ──────────────────────
    public NetworkUtilityTool() {
        // Set the window title shown in the title bar
        setTitle("Network Utility Tool");

        // Set window size: 700px wide, 750px tall
        setSize(700, 750);

        // Close the app when user clicks the X button
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Center the window on the screen
        setLocationRelativeTo(null);

        setBackground(Color.WHITE);

        // Main panel — BoxLayout stacks all sections top to bottom
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Add all 5 sections with small gaps between them
        mainPanel.add(buildHeaderPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buildLookupPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buildGeolocationPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buildPortScannerPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buildLocalSystemPanel());

        // Wrapper with BorderLayout NORTH — prevents sections from stretching vertically
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(mainPanel, BorderLayout.NORTH);

        // Scroll pane — horizontal scroll disabled so cards always fill full width
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll);

        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════
    //  SECTION 1 — Header Panel
    //  Blue banner showing app title and subtitle
    // ══════════════════════════════════════════════════════════════
    private JPanel buildHeaderPanel() {
        // BorderLayout so text panel sits in the center
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(235, 245, 255));

        // CompoundBorder: outer blue line + inner padding
        p.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 220, 245), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        // Force full width inside BoxLayout
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Main title label
        JLabel title = new JLabel("🌐  Network Utility Tool");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(30, 80, 160));

        // Subtitle below the title
        JLabel subtitle = new JLabel("Domain lookup  •  Geolocation  •  Port scanner");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 120, 160));

        // Stack title and subtitle vertically
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(new Color(235, 245, 255));
        text.add(title);
        text.add(Box.createVerticalStrut(3));
        text.add(subtitle);

        p.add(text, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════
    //  SECTION 2 — Domain Lookup Panel
    //  Input field + action buttons + result display
    // ══════════════════════════════════════════════════════════════
    private JPanel buildLookupPanel() {
        JPanel card = createCard("🔍  Domain Lookup");

        // ── Input Row: text field + Lookup button side by side ──
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setBackground(Color.WHITE);
        inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // Text field where user types domain e.g. google.com or 8.8.8.8
        txtDomain = new JTextField();
        txtDomain.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtDomain.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 210, 220), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        txtDomain.setToolTipText("Enter a domain name or IP address");

        // Lookup button triggers getIPAddress()
        JButton btnLookup = createButton("Lookup", new Color(40, 100, 200));
        btnLookup.addActionListener(e -> performFullLookup());

        // Text field fills space, button sits on the right
        inputRow.add(txtDomain, BorderLayout.CENTER);
        inputRow.add(btnLookup, BorderLayout.EAST);
        card.add(inputRow);
        card.add(Box.createVerticalStrut(8));

        // ── Button Row: 5 action buttons ──
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 5 colored buttons — each calls a different logic method
        JButton btnIP    = createButton("Get IP",       new Color(60, 130, 60));
        JButton btnHost  = createButton("Get Hostname",  new Color(130, 80, 180));
        JButton btnPing  = createButton("Ping",          new Color(180, 100, 20));
        JButton btnGeo   = createButton("Geolocate IP",  new Color(20, 140, 140));
        JButton btnClear = createButton("Clear",         new Color(150, 150, 150));

        // Connect each button to its logic method using lambda
        btnIP.addActionListener(e    -> getIPAddress());
        btnHost.addActionListener(e  -> getHostname());
        btnPing.addActionListener(e  -> pingHost());
        btnGeo.addActionListener(e   -> geolocateIP());
        btnClear.addActionListener(e -> clearAll());

        btnRow.add(btnIP);
        btnRow.add(btnHost);
        btnRow.add(btnPing);
        btnRow.add(btnGeo);
        btnRow.add(btnClear);
        card.add(btnRow);
        card.add(Box.createVerticalStrut(8));

        // ── Result Area: shows output of every operation ──
        txtResult = new JTextArea(5, 40);
        txtResult.setEditable(false);
        txtResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResult.setBackground(new Color(245, 248, 252));
        txtResult.setBorder(new EmptyBorder(8, 10, 8, 10));
        txtResult.setText("Results will appear here...");
        txtResult.setForeground(new Color(130, 140, 160));

        // JScrollPane wraps result area — adds border and handles overflow text
        JScrollPane resultScroll = new JScrollPane(txtResult);
        resultScroll.setBorder(new LineBorder(new Color(210, 220, 235), 1, true));
        resultScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.add(resultScroll);

        // ── Status Label: small text showing success or error ──
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(100, 150, 100));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(4));
        card.add(lblStatus);

        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  SECTION 3 — Geolocation Result Panel
    //  Shows country, city, ISP, lat, lon after API call
    // ══════════════════════════════════════════════════════════════
    private JPanel buildGeolocationPanel() {
        JPanel card = createCard("📍  Geolocation Result");

        // 4 rows, 2 columns grid for all geo fields
        JPanel grid = new JPanel(new GridLayout(4, 2, 12, 8));
        grid.setBackground(Color.WHITE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // All labels start with "—" and update after API call
        lblIP      = makeInfoLabel("—");
        lblCountry = makeInfoLabel("—");
        lblCity    = makeInfoLabel("—");
        lblISP     = makeInfoLabel("—");
        lblLat     = makeInfoLabel("—");
        lblLon     = makeInfoLabel("—");
        lblOrg     = makeInfoLabel("—");

        // makeFieldRow builds "IP Address:  —" style rows
        grid.add(makeFieldRow("IP Address",   lblIP));
        grid.add(makeFieldRow("Country",      lblCountry));
        grid.add(makeFieldRow("City",         lblCity));
        grid.add(makeFieldRow("ISP",          lblISP));
        grid.add(makeFieldRow("Latitude",     lblLat));
        grid.add(makeFieldRow("Longitude",    lblLon));
        grid.add(makeFieldRow("Organisation", lblOrg));
        grid.add(new JLabel()); // empty spacer for last grid cell

        card.add(grid);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  SECTION 4 — Port Scanner Panel
    //  Table of 12 common ports with Open/Closed status
    // ══════════════════════════════════════════════════════════════
    private JPanel buildPortScannerPanel() {
        JPanel card = createCard("🔌  Port Scanner");

        // 12 common ports to scan
        int[] commonPorts = {21, 22, 23, 25, 53, 80, 110, 143, 443, 3306, 5432, 8080};

        // Column headers for the table
        String[] cols = {"Port", "Service", "Status"};

        // DefaultTableModel holds table data — cells are not editable
        portTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // Add one row per port: port number, service name, default status "—"
        for (int port : commonPorts) {
            portTableModel.addRow(new Object[]{port, portName(port), "—"});
        }

        // Create JTable using the model
        //portTableModel → raw data in memory
        //JTable         → shows that data on screen
        JTable portTable = new JTable(portTableModel);
        portTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        portTable.setRowHeight(26);
        portTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        portTable.setBackground(new Color(248, 250, 255));
        portTable.setGridColor(new Color(220, 228, 240));

        // Custom renderer: Open = green text, Closed = red text, else gray
        portTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String status = (String) t.getModel().getValueAt(row, 2);
                if ("Open".equals(status))        setForeground(new Color(30, 130, 30));
                else if ("Closed".equals(status)) setForeground(new Color(180, 40, 40));
                else                             setForeground(new Color(100, 100, 100));
                setBackground(sel ? new Color(210, 230, 255) : new Color(248, 250, 255));
                return this;
            }
        });

        // Wrap table in scroll pane with fixed height
        JScrollPane tableScroll = new JScrollPane(portTable);
        tableScroll.setPreferredSize(new Dimension(600, 240));
        tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        tableScroll.setBorder(new LineBorder(new Color(210, 220, 235), 1, true));
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(tableScroll);
        card.add(Box.createVerticalStrut(10));

        // Scan Ports button — starts port scan on background thread
        JButton btnScan = createButton("Scan Ports", new Color(40, 100, 200));
        btnScan.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnScan.addActionListener(e -> scanPorts());
        card.add(btnScan);

        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  SECTION 5 — Local System Info Panel
    //  Shows this machine's own IP address and hostname
    // ══════════════════════════════════════════════════════════════
    private JPanel buildLocalSystemPanel() {
        JPanel card = createCard("💻  Your Local System");

        // 1 row, 2 columns for Local IP and Hostname side by side
        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));
        grid.setBackground(Color.WHITE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Labels start as "—" — updated when button is clicked
        lblLocalIP  = makeInfoLabel("—");
        lblHostname = makeInfoLabel("—");

        grid.add(makeFieldRow("Local IP",  lblLocalIP));
        grid.add(makeFieldRow("Hostname",  lblHostname));

        card.add(grid);
        card.add(Box.createVerticalStrut(10));

        // Button to fetch this machine's info
        JButton btnMyIP = createButton("Get My Info", new Color(60, 130, 60));
        btnMyIP.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnMyIP.addActionListener(e -> getLocalInfo());
        card.add(btnMyIP);

        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC 1 — Get IP Address
    //  InetAddress does DNS lookup: domain name → IP address
    // ══════════════════════════════════════════════════════════════
    private void getIPAddress() {
        // Read and trim whitespace from input field
        String domain = txtDomain.getText().trim();

        // Validate: stop if input is empty
        if (domain.isEmpty()) { showError("Please enter a domain or IP."); return; }

        try {
            // DNS lookup: converts domain → IP e.g. google.com → 142.250.183.14
            InetAddress ia = InetAddress.getByName(domain);
            setResult("IP Addreiss of " + domain + ":\n" + ia.getHostAddress());
            setStatus("✔ IP resolved successfully");

        } catch (UnknownHostException e) {
            // Thrown when domain cannot be found in DNS
            showError("Cannot resolve: " + domain);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC 2 — Get Hostname
    //  Reverse lookup: IP address → hostname
    // ══════════════════════════════════════════════════════════════
    private void getHostname() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) { showError("Please enter a domain or IP."); return; }

        try {
            InetAddress ia = InetAddress.getByName(domain);
            // getHostName() returns hostname e.g. "dns.google"
            setResult("Hostname: " + ia.getHostName() + "\nIP:       " + ia.getHostAddress());
            setStatus("✔ Hostname resolved");

        } catch (UnknownHostException e) {
            showError("Cannot resolve: " + domain);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC 3 — Ping Host
    //  Checks if server is online and measures response time
    //  Runs on background thread so UI stays responsive
    // ══════════════════════════════════════════════════════════════
    private void pingHost() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) { showError("Please enter a domain or IP."); return; }
        setStatus("⏳ Pinging " + domain + "...");

        // Run on background thread — ping can take up to 5 seconds
        new Thread(() -> {
            try {  //domain->ip
                InetAddress ia = InetAddress.getByName(domain);

                // Record time before ping starts
                long start   = System.currentTimeMillis();

                // isReachable() sends ping — waits max 5000ms
                boolean ok   = ia.isReachable(5000);

                // Calculate total time taken
                long elapsed = System.currentTimeMillis() - start;

                String msg = ok
                        ? "✅ " + domain + " is REACHABLE\nResponse time: " + elapsed + " ms"
                        : "❌ " + domain + " is NOT REACHABLE (timeout)";

                // invokeLater: safely update UI from background thread
                SwingUtilities.invokeLater(() -> {
                    setResult(msg);
                    setStatus(ok ? "✔ Host reachable" : "✖ Host not reachable");
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showError("Ping failed: " + e.getMessage()));
            }
        }).start();
    }

    private void performFullLookup() {
        getIPAddress();
        pingHost();
        geolocateIP();
        scanPorts();
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC 4 — Geolocate IP
    //  Calls ip-api.com REST API → reads JSON → updates labels
    //  Runs on background thread so UI stays responsive
    // ══════════════════════════════════════════════════════════════
    private void geolocateIP() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) { showError("Please enter a domain or IP."); return; }
        setStatus("⏳ Fetching geolocation...");

        // Network call must run on background thread
        new Thread(() -> {
            try {
                // Step 1: resolve domain to IP address
                InetAddress ia = InetAddress.getByName(domain);
                String ip = ia.getHostAddress();

                // Step 2: build API URL e.g. http://ip-api.com/json/142.250.183.14
                String apiUrl = "http://ip-api.com/json/" + ip;
                URL url = new URL(apiUrl);

                // Step 3: open HTTP connection and send GET request
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000); // 5 seconds to connect
                conn.setReadTimeout(5000);    // 5 seconds to read response

                // Step 4: read response text line by line into StringBuilder
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                // Step 5: parse full JSON string into JSONObject
                JSONObject json = new JSONObject(sb.toString());

                // Step 6: check API returned success status
                if ("success".equals(json.optString("status"))) {

                    // Extract each value — optString returns "N/A" if field missing
                    String country = json.optString("country", "N/A");
                    String city    = json.optString("city",    "N/A");
                    String isp     = json.optString("isp",     "N/A");
                    String org     = json.optString("org",     "N/A");
                    double lat     = json.optDouble("lat",     0.0);
                    double lon     = json.optDouble("lon",     0.0);

                    // Step 7: update all UI labels on Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        lblIP.setText(ip);
                        lblCountry.setText(country);
                        lblCity.setText(city);
                        lblISP.setText(isp);
                        lblLat.setText(String.valueOf(lat));
                        lblLon.setText(String.valueOf(lon));
                        lblOrg.setText(org);
                        setResult("Geolocation fetched for: " + domain +
                                "\nCountry: " + country + "  |  City: " + city +
                                "\nISP: " + isp + "\nCoords: " + lat + ", " + lon);
                        setStatus("✔ Geolocation loaded");
                    });

                } else {
                    // API returned fail status — show reason
                    SwingUtilities.invokeLater(() ->
                            showError("Geolocation failed: " + json.optString("message", "unknown error"))
                    );
                }

            } catch (Exception e) {
                // Network error, timeout, or JSON parse error
                SwingUtilities.invokeLater(() ->
                        showError("Geolocation error: " + e.getMessage())
                );
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC 5 — Port Scanner
    //  Tries Socket.connect() on each port
    //  Success = Open, Exception = Closed
    //  Runs on background thread so UI stays responsive
    // ══════════════════════════════════════════════════════════════
    private void scanPorts() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) { showError("Please enter a domain or IP first."); return; }
        setStatus("⏳ Scanning ports on " + domain + "...");

        // Reset all rows to "Scanning..." before starting
        for (int i = 0; i < portTableModel.getRowCount(); i++) {
            portTableModel.setValueAt("Scanning...", i, 2);
        }

        // Run scan on background thread
        new Thread(() -> {
            try {
                InetAddress ia = InetAddress.getByName(domain);
                String host = ia.getHostAddress();

                // All 12 ports to check
                int[] ports = {21, 22, 23, 25, 53, 80, 110, 143, 443, 3306, 5432, 8080};

                // Try connecting to each port one by one
                for (int i = 0; i < ports.length; i++) {
                    final int idx  = i;
                    final int port = ports[i];
                    try {
                        Socket socket = new Socket();
                        // Try to connect — timeout after 1 second
                        socket.connect(new InetSocketAddress(host, port), 1000);
                        socket.close(); // connected → port is Open
                        SwingUtilities.invokeLater(() ->
                                portTableModel.setValueAt("Open", idx, 2)
                        );
                    } catch (Exception e) {
                        // Failed to connect → port is Closed
                        SwingUtilities.invokeLater(() ->
                                portTableModel.setValueAt("Closed", idx, 2)
                        );
                    }
                }
                SwingUtilities.invokeLater(() -> setStatus("✔ Port scan complete"));

            } catch (UnknownHostException e) {
                SwingUtilities.invokeLater(() -> showError("Cannot resolve host for port scan"));
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC 6 — Get Local System Info
    //  Gets this machine's own IP and hostname
    // ══════════════════════════════════════════════════════════════
    private void getLocalInfo() {
        try {
            // getLocalHost() returns InetAddress of the current machine
            InetAddress local = InetAddress.getLocalHost();
            lblLocalIP.setText(local.getHostAddress()); // e.g. 192.168.1.5
            lblHostname.setText(local.getHostName());   // e.g. DESKTOP-XYZ
            setStatus("✔ Local system info loaded");
        } catch (UnknownHostException e) {
            showError("Cannot get local info: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC 7 — Clear All
    //  Resets every field and label back to default empty state
    // ══════════════════════════════════════════════════════════════
    private void clearAll() {
        txtDomain.setText("");
        txtResult.setText("Results will appear here...");
        txtResult.setForeground(new Color(130, 140, 160));
        lblStatus.setText(" ");
        lblIP.setText("—");      lblCountry.setText("—");
        lblCity.setText("—");    lblISP.setText("—");
        lblLat.setText("—");     lblLon.setText("—");
        lblOrg.setText("—");
        for (int i = 0; i < portTableModel.getRowCount(); i++)
            portTableModel.setValueAt("—", i, 2);
        lblLocalIP.setText("—");
        lblHostname.setText("—");
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ══════════════════════════════════════════════════════════════

    // Sets result area text in dark blue (normal result)
    private void setResult(String text) {
        txtResult.setForeground(new Color(30, 50, 80));
        txtResult.setText(text);
    }

    // Sets result area and status bar to red (error state)
    private void showError(String msg) {
        txtResult.setForeground(new Color(180, 40, 40));
        txtResult.setText("⚠ " + msg);
        lblStatus.setText("✖ " + msg);
        lblStatus.setForeground(new Color(180, 40, 40));
    }

    // Sets status bar text in green (success state)
    private void setStatus(String msg) {
        lblStatus.setForeground(new Color(30, 130, 60));
        lblStatus.setText(msg);
    }

    // Returns service name for a given port number
    private String portName(int port) {
        switch (port) {
            case 21:   return "FTP";
            case 22:   return "SSH";
            case 23:   return "Telnet";
            case 25:   return "SMTP";
            case 53:   return "DNS";
            case 80:   return "HTTP";
            case 110:  return "POP3";
            case 143:  return "IMAP";
            case 443:  return "HTTPS";
            case 3306: return "MySQL";
            case 5432: return "PostgreSQL";
            case 8080: return "HTTP-Alt";
            default:   return "Unknown";
        }
    }

    // Creates a white card panel with bold section title at the top
    // Every section uses this as its container
    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);

        // Outer border line + inner padding
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 220, 235), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // These two lines force card to stretch full width inside BoxLayout
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Section title label e.g. "🔍 Domain Lookup"
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(new Color(50, 80, 130));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(10));
        return card;
    }

    // Creates a colored button with white text and hand cursor
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Creates a monospaced label for displaying data values (IP, city etc.)
    private JLabel makeInfoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lbl.setForeground(new Color(40, 60, 100));
        return lbl;
    }

    // Creates a row with gray field name on left and value label on right
    // Used in geolocation panel and local system panel
    private JPanel makeFieldRow(String name, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(Color.WHITE);
        JLabel key = new JLabel(name + ":");
        key.setFont(new Font("SansSerif", Font.PLAIN, 12));
        key.setForeground(new Color(100, 110, 130));
        key.setPreferredSize(new Dimension(110, 24)); // fixed width so values align
        row.add(key,   BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }
}
