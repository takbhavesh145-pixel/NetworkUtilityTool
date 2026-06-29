import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import org.json.*;

public class NetworkUtilityTool extends JFrame {

    // ── UI Components ──────────────────────────────────────────
    private JTextField txtDomain;
    private JTextArea  txtResult;
    private JLabel     lblStatus;

    // Geolocation fields
    private JLabel lblIP, lblCountry, lblCity, lblISP, lblLat, lblLon, lblOrg;

    // Port scanner table
    private DefaultTableModel portTableModel;

    // Local system fields
    private JLabel lblLocalIP, lblHostname;

    // ── Entry Point ────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NetworkUtilityTool());
    }

    // ── Constructor: Build UI ──────────────────────────────────
    public NetworkUtilityTool() {
        setTitle("Network Utility Tool");
        setSize(700, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Color.WHITE);

        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Add all sections
        mainPanel.add(buildHeaderPanel());
        mainPanel.add(Box.createVerticalStrut(16));
        mainPanel.add(buildLookupPanel());
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(buildGeolocationPanel());
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(buildPortScannerPanel());
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(buildLocalSystemPanel());

        // Scroll wrapper
        JScrollPane scroll = new JScrollPane(mainPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll);

        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════
    //  SECTION 1 — Header
    // ══════════════════════════════════════════════════════════════
    private JPanel buildHeaderPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(235, 245, 255));
        p.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 220, 245), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel title = new JLabel("🌐  Network Utility Tool");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(30, 80, 160));

        JLabel subtitle = new JLabel("Domain lookup  •  Geolocation  •  Port scanner");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 120, 160));

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
    //  SECTION 2 — Domain Lookup
    // ══════════════════════════════════════════════════════════════
    private JPanel buildLookupPanel() {
        JPanel card = createCard("🔍  Domain Lookup");

        // Input row
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setBackground(Color.WHITE);
        txtDomain = new JTextField();
        txtDomain.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtDomain.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 210, 220), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        txtDomain.setToolTipText("Enter a domain name or IP address");

        JButton btnLookup = createButton("Lookup", new Color(40, 100, 200));
        btnLookup.addActionListener(e -> performFullLookup());

        inputRow.add(txtDomain, BorderLayout.CENTER);
        inputRow.add(btnLookup, BorderLayout.EAST);
        card.add(inputRow);
        card.add(Box.createVerticalStrut(10));

        // Button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(Color.WHITE);

        JButton btnIP   = createButton("Get IP",       new Color(60, 130, 60));
        JButton btnHost = createButton("Get Hostname",  new Color(130, 80, 180));
        JButton btnPing = createButton("Ping",          new Color(180, 100, 20));
        JButton btnGeo  = createButton("Geolocate IP",  new Color(20, 140, 140));
        JButton btnClear= createButton("Clear",         new Color(150, 150, 150));

        btnIP.addActionListener(e   -> getIPAddress());
        btnHost.addActionListener(e -> getHostname());
        btnPing.addActionListener(e -> pingHost());
        btnGeo.addActionListener(e  -> geolocateIP());
        btnClear.addActionListener(e-> clearAll());

        btnRow.add(btnIP);
        btnRow.add(btnHost);
        btnRow.add(btnPing);
        btnRow.add(btnGeo);
        btnRow.add(btnClear);
        card.add(btnRow);
        card.add(Box.createVerticalStrut(10));

        // Result area
        txtResult = new JTextArea(4, 40);
        txtResult.setEditable(false);
        txtResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResult.setBackground(new Color(245, 248, 252));
        txtResult.setBorder(new EmptyBorder(8, 10, 8, 10));
        txtResult.setText("Results will appear here...");
        txtResult.setForeground(new Color(130, 140, 160));

        JScrollPane resultScroll = new JScrollPane(txtResult);
        resultScroll.setBorder(new LineBorder(new Color(210, 220, 235), 1, true));
        card.add(resultScroll);

        // Status bar
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(100, 150, 100));
        card.add(Box.createVerticalStrut(4));
        card.add(lblStatus);

        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  SECTION 3 — Geolocation Result
    // ══════════════════════════════════════════════════════════════
    private JPanel buildGeolocationPanel() {
        JPanel card = createCard("📍  Geolocation Result");

        JPanel grid = new JPanel(new GridLayout(4, 2, 12, 6));
        grid.setBackground(Color.WHITE);

        lblIP      = makeInfoLabel("—");
        lblCountry = makeInfoLabel("—");
        lblCity    = makeInfoLabel("—");
        lblISP     = makeInfoLabel("—");
        lblLat     = makeInfoLabel("—");
        lblLon     = makeInfoLabel("—");
        lblOrg     = makeInfoLabel("—");

        grid.add(makeFieldRow("IP Address",  lblIP));
        grid.add(makeFieldRow("Country",     lblCountry));
        grid.add(makeFieldRow("City",        lblCity));
        grid.add(makeFieldRow("ISP",         lblISP));
        grid.add(makeFieldRow("Latitude",    lblLat));
        grid.add(makeFieldRow("Longitude",   lblLon));
        grid.add(makeFieldRow("Organisation",lblOrg));
        grid.add(new JLabel()); // spacer

        card.add(grid);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  SECTION 4 — Port Scanner
    // ══════════════════════════════════════════════════════════════
    private JPanel buildPortScannerPanel() {
        JPanel card = createCard("🔌  Port Scanner");

        // Predefined common ports
        int[] commonPorts = {21,22,23,25,53,80,110,143,443,3306,5432,8080};

        String[] cols = {"Port", "Service", "Status"};
        portTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (int port : commonPorts) {
            portTableModel.addRow(new Object[]{port, portName(port), "—"});
        }

        JTable portTable = new JTable(portTableModel);
        portTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        portTable.setRowHeight(24);
        portTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        portTable.setBackground(new Color(248, 250, 255));
        portTable.setGridColor(new Color(220, 228, 240));

        // Color rows by status
        portTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String status = (String) t.getModel().getValueAt(row, 2);
                if ("Open".equals(status))   setForeground(new Color(30, 130, 30));
                else if ("Closed".equals(status)) setForeground(new Color(180, 40, 40));
                else                         setForeground(new Color(100, 100, 100));
                setBackground(sel ? new Color(210, 230, 255) : new Color(248, 250, 255));
                return this;
            }
        });

        JScrollPane tableScroll = new JScrollPane(portTable);
        tableScroll.setPreferredSize(new Dimension(600, 220));
        tableScroll.setBorder(new LineBorder(new Color(210, 220, 235), 1, true));
        card.add(tableScroll);
        card.add(Box.createVerticalStrut(10));

        JButton btnScan = createButton("Scan Ports", new Color(40, 100, 200));
        btnScan.addActionListener(e -> scanPorts());
        card.add(btnScan);

        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  SECTION 5 — Local System Info
    // ══════════════════════════════════════════════════════════════
    private JPanel buildLocalSystemPanel() {
        JPanel card = createCard("💻  Your Local System");

        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));
        grid.setBackground(Color.WHITE);

        lblLocalIP  = makeInfoLabel("—");
        lblHostname = makeInfoLabel("—");

        grid.add(makeFieldRow("Local IP",  lblLocalIP));
        grid.add(makeFieldRow("Hostname",  lblHostname));

        card.add(grid);
        card.add(Box.createVerticalStrut(10));

        JButton btnMyIP = createButton("Get My Info", new Color(60, 130, 60));
        btnMyIP.addActionListener(e -> getLocalInfo());
        card.add(btnMyIP);

        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC — Domain Lookup Actions
    // ══════════════════════════════════════════════════════════════

    /** Get IP address from domain */
    private void getIPAddress() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) { showError("Please enter a domain or IP."); return; }
        try {
            InetAddress ia = InetAddress.getByName(domain);
            setResult("IP Address of " + domain + ":\n" + ia.getHostAddress());
            setStatus("✔ IP resolved successfully");
        } catch (UnknownHostException e) {
            showError("Cannot resolve: " + domain);
        }
    }

    /** Get hostname from domain/IP */
    private void getHostname() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) { showError("Please enter a domain or IP."); return; }
        try {
            InetAddress ia = InetAddress.getByName(domain);
            setResult("Hostname: " + ia.getHostName() + "\nIP:       " + ia.getHostAddress());
            setStatus("✔ Hostname resolved");
        } catch (UnknownHostException e) {
            showError("Cannot resolve: " + domain);
        }
    }

    /** Ping the host */
    private void pingHost() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) { showError("Please enter a domain or IP."); return; }
        setStatus("⏳ Pinging " + domain + "...");
        new Thread(() -> {
            try {
                InetAddress ia = InetAddress.getByName(domain);
                long start  = System.currentTimeMillis();
                boolean ok  = ia.isReachable(5000);
                long elapsed = System.currentTimeMillis() - start;
                String msg = ok
                        ? "✅ " + domain + " is REACHABLE\nResponse time: " + elapsed + " ms"
                        : "❌ " + domain + " is NOT REACHABLE (timeout)";
                SwingUtilities.invokeLater(() -> {
                    setResult(msg);
                    setStatus(ok ? "✔ Host reachable" : "✖ Host not reachable");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showError("Ping failed: " + e.getMessage()));
            }
        }).start();
    }

    /** Perform full lookup: IP + hostname + ping */
    private void performFullLookup() {
        getIPAddress();
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC — Geolocation via ip-api.com (FREE, no key needed)
    // ══════════════════════════════════════════════════════════════
    private void geolocateIP() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) { showError("Please enter a domain or IP."); return; }
        setStatus("⏳ Fetching geolocation...");

        new Thread(() -> {
            try {
                // Resolve domain to IP first
                InetAddress ia = InetAddress.getByName(domain);
                String ip = ia.getHostAddress();

                // Call ip-api.com
                String apiUrl = "http://ip-api.com/json/" + ip;
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // Read response
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                // Parse JSON
                JSONObject json = new JSONObject(sb.toString());

                if ("success".equals(json.optString("status"))) {
                    String country = json.optString("country",  "N/A");
                    String city    = json.optString("city",     "N/A");
                    String isp     = json.optString("isp",      "N/A");
                    String org     = json.optString("org",      "N/A");
                    double lat     = json.optDouble("lat",      0.0);
                    double lon     = json.optDouble("lon",      0.0);

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
                    SwingUtilities.invokeLater(() ->
                            showError("Geolocation failed: " + json.optString("message", "unknown error"))
                    );
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        showError("Geolocation error: " + e.getMessage())
                );
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC — Port Scanner
    // ══════════════════════════════════════════════════════════════
    private void scanPorts() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) { showError("Please enter a domain or IP first."); return; }
        setStatus("⏳ Scanning ports on " + domain + "...");

        // Reset all to "Scanning..."
        for (int i = 0; i < portTableModel.getRowCount(); i++) {
            portTableModel.setValueAt("Scanning...", i, 2);
        }

        new Thread(() -> {
            try {
                InetAddress ia = InetAddress.getByName(domain);
                String host = ia.getHostAddress();
                int[] ports = {21,22,23,25,53,80,110,143,443,3306,5432,8080};

                for (int i = 0; i < ports.length; i++) {
                    final int idx = i;
                    final int port = ports[i];
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(host, port), 1000);
                        socket.close();
                        SwingUtilities.invokeLater(() ->
                                portTableModel.setValueAt("Open", idx, 2)
                        );
                    } catch (Exception e) {
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
    //  LOGIC — Local System Info
    // ══════════════════════════════════════════════════════════════
    private void getLocalInfo() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            lblLocalIP.setText(local.getHostAddress());
            lblHostname.setText(local.getHostName());
            setStatus("✔ Local system info loaded");
        } catch (UnknownHostException e) {
            showError("Cannot get local info: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LOGIC — Clear
    // ══════════════════════════════════════════════════════════════
    private void clearAll() {
        txtDomain.setText("");
        txtResult.setText("Results will appear here...");
        txtResult.setForeground(new Color(130, 140, 160));
        lblStatus.setText(" ");
        lblIP.setText("—");      lblCountry.setText("—");
        lblCity.setText("—");   lblISP.setText("—");
        lblLat.setText("—");    lblLon.setText("—");
        lblOrg.setText("—");
        for (int i = 0; i < portTableModel.getRowCount(); i++)
            portTableModel.setValueAt("—", i, 2);
        lblLocalIP.setText("—");
        lblHostname.setText("—");
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════

    private void setResult(String text) {
        txtResult.setForeground(new Color(30, 50, 80));
        txtResult.setText(text);
    }

    private void showError(String msg) {
        txtResult.setForeground(new Color(180, 40, 40));
        txtResult.setText("⚠ " + msg);
        lblStatus.setText("✖ " + msg);
        lblStatus.setForeground(new Color(180, 40, 40));
    }

    private void setStatus(String msg) {
        lblStatus.setForeground(new Color(30, 130, 60));
        lblStatus.setText(msg);
    }

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

    /** Create a styled card panel with a title */
    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 220, 235), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(new Color(50, 80, 130));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(12));
        return card;
    }

    /** Create a styled button */
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

    /** Create a muted info label */
    private JLabel makeInfoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lbl.setForeground(new Color(40, 60, 100));
        return lbl;
    }

    /** Create a field row: label + value */
    private JPanel makeFieldRow(String name, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(Color.WHITE);
        JLabel key = new JLabel(name + ":");
        key.setFont(new Font("SansSerif", Font.PLAIN, 12));
        key.setForeground(new Color(100, 110, 130));
        key.setPreferredSize(new Dimension(110, 24));
        row.add(key,   BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }
}
