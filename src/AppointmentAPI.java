import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class AppointmentAPI {

    public static void main(String[] args) throws Exception {
        // Test database connection
        DatabaseConnection.testConnection();

        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Add CORS headers to all responses
        server.createContext("/api/patients", new PatientHandler());
        server.createContext("/api/doctors", new DoctorHandler());
        server.createContext("/api/appointments", new AppointmentHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("🚀 Server started on http://localhost:8080");
        System.out.println("📡 API endpoints available:");
        System.out.println("   GET  /api/doctors");
        System.out.println("   POST /api/patients");
        System.out.println("   GET  /api/patients");
        System.out.println("   POST /api/appointments");
        System.out.println("   GET  /api/appointments");
        System.out.println("   PUT  /api/appointments/{id}");
        System.out.println("   DELETE /api/appointments/{id}");
    }

    // CORS Helper Method
    private static void addCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    // Patient Handler
    static class PatientHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCORSHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }

            String method = exchange.getRequestMethod();
            String response = "";

            try {
                if ("GET".equals(method)) {
                    response = getAllPatients();
                } else if ("POST".equals(method)) {
                    String requestBody = readRequestBody(exchange);
                    response = createPatient(requestBody);
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (Exception e) {
                String error = "{\"error\":\"" + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, error.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
        }

        private String getAllPatients() throws SQLException {
            StringBuilder json = new StringBuilder("[");

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT * FROM patients ORDER BY patient_id";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append("{")
                            .append("\"patient_id\":").append(rs.getInt("patient_id")).append(",")
                            .append("\"name\":\"").append(rs.getString("name")).append("\",")
                            .append("\"email\":\"").append(rs.getString("email")).append("\",")
                            .append("\"phone\":\"").append(rs.getString("phone")).append("\",")
                            .append("\"address\":\"").append(rs.getString("address")).append("\"")
                            .append("}");
                    first = false;
                }
            }

            json.append("]");
            return json.toString();
        }

        private String createPatient(String requestBody) throws SQLException {
            // Parse JSON manually (simple approach)
            String name = extractJsonValue(requestBody, "name");
            String email = extractJsonValue(requestBody, "email");
            String phone = extractJsonValue(requestBody, "phone");
            String address = extractJsonValue(requestBody, "address");

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO patients (name, email, phone, address) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setString(4, address);

                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    ResultSet keys = pstmt.getGeneratedKeys();
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        return "{\"success\":true,\"patient_id\":" + id + ",\"message\":\"Patient created successfully\"}";
                    }
                }
            }

            return "{\"success\":false,\"message\":\"Failed to create patient\"}";
        }
    }

    // Doctor Handler
    static class DoctorHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCORSHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }

            String response = "";

            try {
                response = getAllDoctors();

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (Exception e) {
                String error = "{\"error\":\"" + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, error.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
        }

        private String getAllDoctors() throws SQLException {
            StringBuilder json = new StringBuilder("[");

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT * FROM doctors ORDER BY doctor_id";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append("{")
                            .append("\"doctor_id\":").append(rs.getInt("doctor_id")).append(",")
                            .append("\"name\":\"").append(rs.getString("name")).append("\",")
                            .append("\"specialization\":\"").append(rs.getString("specialization")).append("\",")
                            .append("\"availability_hours\":\"").append(rs.getString("availability_hours")).append("\"")
                            .append("}");
                    first = false;
                }
            }

            json.append("]");
            return json.toString();
        }
    }

    // Appointment Handler
    static class AppointmentHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCORSHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }

            String method = exchange.getRequestMethod();
            String response = "";

            try {
                if ("GET".equals(method)) {
                    response = getAllAppointments();
                } else if ("POST".equals(method)) {
                    String requestBody = readRequestBody(exchange);
                    response = createAppointment(requestBody);
                } else if ("PUT".equals(method)) {
                    String requestBody = readRequestBody(exchange);
                    String path = exchange.getRequestURI().getPath();
                    String[] parts = path.split("/");
                    int appointmentId = Integer.parseInt(parts[parts.length - 1]);
                    response = updateAppointment(appointmentId, requestBody);
                } else if ("DELETE".equals(method)) {
                    String path = exchange.getRequestURI().getPath();
                    String[] parts = path.split("/");
                    int appointmentId = Integer.parseInt(parts[parts.length - 1]);
                    response = deleteAppointment(appointmentId);
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (Exception e) {
                String error = "{\"error\":\"" + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, error.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
        }

        // NEW METHOD: Calculate fees based on patient visit history
        private double calculateFees(int patientId, Connection conn) throws SQLException {
            String sql = "SELECT COUNT(*) as visit_count FROM appointments WHERE patient_id = ? AND status != 'Cancelled'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int visitCount = rs.getInt("visit_count");
                // First visit: 500, Second visit onwards: 300
                return visitCount == 0 ? 500.00 : 300.00;
            }

            return 500.00; // Default first visit fee
        }

        private String getAllAppointments() throws SQLException {
            StringBuilder json = new StringBuilder("[");

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = """
                    SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, 
                           a.appointment_time, a.status, a.fees, p.name as patient_name, p.phone,
                           d.name as doctor_name, d.specialization
                    FROM appointments a 
                    JOIN patients p ON a.patient_id = p.patient_id 
                    JOIN doctors d ON a.doctor_id = d.doctor_id
                    ORDER BY a.appointment_date, a.appointment_time
                    """;
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append("{")
                            .append("\"appointment_id\":").append(rs.getInt("appointment_id")).append(",")
                            .append("\"patient_id\":").append(rs.getInt("patient_id")).append(",")
                            .append("\"doctor_id\":").append(rs.getInt("doctor_id")).append(",")
                            .append("\"appointment_date\":\"").append(rs.getDate("appointment_date")).append("\",")
                            .append("\"appointment_time\":\"").append(rs.getTime("appointment_time")).append("\",")
                            .append("\"status\":\"").append(rs.getString("status")).append("\",")
                            .append("\"fees\":").append(rs.getDouble("fees")).append(",")
                            .append("\"patient_name\":\"").append(rs.getString("patient_name")).append("\",")
                            .append("\"phone\":\"").append(rs.getString("phone")).append("\",")
                            .append("\"doctor_name\":\"").append(rs.getString("doctor_name")).append("\",")
                            .append("\"specialization\":\"").append(rs.getString("specialization")).append("\"")
                            .append("}");
                    first = false;
                }
            }

            json.append("]");
            return json.toString();
        }

        private String createAppointment(String requestBody) throws SQLException {
            int patientId = Integer.parseInt(extractJsonValue(requestBody, "patient_id"));
            int doctorId = Integer.parseInt(extractJsonValue(requestBody, "doctor_id"));
            String date = extractJsonValue(requestBody, "appointment_date");
            String time = extractJsonValue(requestBody, "appointment_time");

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Calculate fees based on patient visit history
                double fees = calculateFees(patientId, conn);

                String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, fees) VALUES (?, ?, ?, ?, 'Pending', ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, patientId);
                pstmt.setInt(2, doctorId);
                pstmt.setDate(3, java.sql.Date.valueOf(date));
                pstmt.setTime(4, java.sql.Time.valueOf(time + ":00"));
                pstmt.setDouble(5, fees);

                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    ResultSet keys = pstmt.getGeneratedKeys();
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        return "{\"success\":true,\"appointment_id\":" + id + ",\"fees\":" + fees + ",\"message\":\"Appointment created successfully with fees: ₹" + fees + "\"}";
                    }
                }
            }

            return "{\"success\":false,\"message\":\"Failed to create appointment\"}";
        }

        private String updateAppointment(int appointmentId, String requestBody) throws SQLException {
            String status = extractJsonValue(requestBody, "status");

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, status);
                pstmt.setInt(2, appointmentId);

                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    return "{\"success\":true,\"message\":\"Appointment updated successfully\"}";
                }
            }

            return "{\"success\":false,\"message\":\"Failed to update appointment\"}";
        }

        private String deleteAppointment(int appointmentId) throws SQLException {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM appointments WHERE appointment_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, appointmentId);

                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    return "{\"success\":true,\"message\":\"Appointment deleted successfully\"}";
                }
            }

            return "{\"success\":false,\"message\":\"Failed to delete appointment\"}";
        }
    }

    // Utility Methods
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf("\"", start);
        if (start > pattern.length() - 1 && end > start) {
            return json.substring(start, end);
        }

        // Try without quotes for numbers
        pattern = "\"" + key + "\":";
        start = json.indexOf(pattern) + pattern.length();
        end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        if (start > pattern.length() - 1 && end > start) {
            return json.substring(start, end).trim();
        }

        return "";
    }
}
