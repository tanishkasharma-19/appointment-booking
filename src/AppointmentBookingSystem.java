import java.sql.*;
import java.util.Scanner;

public class AppointmentBookingSystem {

    public static void main(String[] args) {
        AppointmentBookingSystem system = new AppointmentBookingSystem();
        system.run();
    }

    public void run() {
        // Test database connection
        DatabaseConnection.testConnection();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMenu();

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        addPatient(scanner);
                        break;
                    case 2:
                        viewAllPatients();
                        break;
                    case 3:
                        bookAppointment(scanner);
                        break;
                    case 4:
                        viewAllAppointments();
                        break;
                    case 5:
                        updateAppointmentStatus(scanner);
                        break;
                    case 6:
                        cancelAppointment(scanner);
                        break;
                    case 7:
                        searchPatientAppointments(scanner);
                        break;
                    case 8:
                        System.out.println("\n[SUCCESS] Thank you for using the Appointment Booking System!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("\n[ERROR] Invalid choice! Please select 1-8.");
                }

                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();

            } catch (Exception e) {
                System.out.println("\n[ERROR] Invalid input! Please enter a number.");
                scanner.nextLine(); // clear invalid input
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n========================================");
        System.out.println("  APPOINTMENT BOOKING SYSTEM - MENU");
        System.out.println("========================================");
        System.out.println("1. Add New Patient");
        System.out.println("2. View All Patients");
        System.out.println("3. Book New Appointment");
        System.out.println("4. View All Appointments");
        System.out.println("5. Update Appointment Status");
        System.out.println("6. Cancel Appointment");
        System.out.println("7. Search Patient Appointments");
        System.out.println("8. Exit");
        System.out.println("----------------------------------------");
        System.out.print("Choose option (1-8): ");
    }

    private void addPatient(Scanner scanner) {
        System.out.println("\n=== ADD NEW PATIENT ===");

        System.out.print("Enter patient name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();

        System.out.print("Enter address: ");
        String address = scanner.nextLine();

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
                    System.out.println("\n[SUCCESS] Patient added successfully with ID: " + id);
                }
            } else {
                System.out.println("\n[ERROR] Failed to add patient!");
            }

        } catch (SQLException e) {
            System.out.println("\n[ERROR] Database error: " + e.getMessage());
        }
    }

    private void viewAllPatients() {
        System.out.println("\n=== ALL PATIENTS ===");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM patients ORDER BY patient_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (!rs.next()) {
                System.out.println("No patients found in database.");
                return;
            }

            System.out.println("\nID | Name                | Email                    | Phone        | Address");
            System.out.println("---|---------------------|--------------------------|--------------|------------------");

            do {
                System.out.printf("%-3d| %-20s| %-25s| %-13s| %-20s\n",
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"));
            } while (rs.next());

        } catch (SQLException e) {
            System.out.println("\n[ERROR] Database error: " + e.getMessage());
        }
    }

    private void bookAppointment(Scanner scanner) {
        System.out.println("\n=== BOOK NEW APPOINTMENT ===");

        // Show available patients
        System.out.println("\nAvailable Patients:");
        viewAllPatients();

        System.out.print("\nEnter Patient ID: ");
        int patientId = scanner.nextInt();

        // Show available doctors
        System.out.println("\nAvailable Doctors:");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM doctors ORDER BY doctor_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\nID | Name         | Specialization    | Available Hours");
            System.out.println("---|--------------|-------------------|------------------");

            while (rs.next()) {
                System.out.printf("%-3d| %-13s| %-18s| %-20s\n",
                        rs.getInt("doctor_id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("availability_hours"));
            }
        } catch (SQLException e) {
            System.out.println("\n[ERROR] Database error: " + e.getMessage());
            return;
        }

        System.out.print("\nEnter Doctor ID: ");
        int doctorId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter appointment date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        System.out.print("Enter appointment time (HH:MM): ");
        String time = scanner.nextLine();

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
                    System.out.println("\n[SUCCESS] Appointment booked successfully with ID: " + id);
                    System.out.println("[INFO] Consultation Fees: ₹" + String.format("%.2f", fees));
                    if (fees == 300.00) {
                        System.out.println("[INFO] Returning patient discount applied! (₹200 off)");
                    }
                }
            } else {
                System.out.println("\n[ERROR] Failed to book appointment!");
            }

        } catch (SQLException e) {
            System.out.println("\n[ERROR] Database error: " + e.getMessage());
        }
    }

    // NEW HELPER METHOD: Calculate fees based on patient visit history
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

    private void viewAllAppointments() {
        System.out.println("\n=== ALL APPOINTMENTS ===");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT a.appointment_id, a.appointment_date, a.appointment_time, a.status, a.fees,
                       p.name as patient_name, p.phone,
                       d.name as doctor_name, d.specialization
                FROM appointments a 
                JOIN patients p ON a.patient_id = p.patient_id 
                JOIN doctors d ON a.doctor_id = d.doctor_id
                ORDER BY a.appointment_date, a.appointment_time
                """;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (!rs.next()) {
                System.out.println("No appointments found in database.");
                return;
            }

            System.out.println("\nID | Patient Name     | Doctor Name   | Specialization | Date       | Time     | Fees   | Status");
            System.out.println("---|------------------|---------------|----------------|------------|----------|--------|----------");

            do {
                System.out.printf("%-3d| %-17s| %-14s| %-15s| %-11s| %-9s| ₹%-6.2f| %-10s\n",
                        rs.getInt("appointment_id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("specialization"),
                        rs.getDate("appointment_date"),
                        rs.getTime("appointment_time"),
                        rs.getDouble("fees"),
                        rs.getString("status"));
            } while (rs.next());

        } catch (SQLException e) {
            System.out.println("\n[ERROR] Database error: " + e.getMessage());
        }
    }

    private void updateAppointmentStatus(Scanner scanner) {
        System.out.println("\n=== UPDATE APPOINTMENT STATUS ===");

        // Show current appointments
        viewAllAppointments();

        System.out.print("\nEnter Appointment ID to update: ");
        int appointmentId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.println("\nStatus Options:");
        System.out.println("1. Pending");
        System.out.println("2. Confirmed");
        System.out.println("3. Completed");
        System.out.println("4. Cancelled");

        System.out.print("Choose new status (1-4): ");
        int statusChoice = scanner.nextInt();

        String status;
        switch (statusChoice) {
            case 1: status = "Pending"; break;
            case 2: status = "Confirmed"; break;
            case 3: status = "Completed"; break;
            case 4: status = "Cancelled"; break;
            default:
                System.out.println("\n[ERROR] Invalid status choice!");
                return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, appointmentId);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("\n[SUCCESS] Appointment status updated to: " + status);
            } else {
                System.out.println("\n[ERROR] Appointment ID not found!");
            }

        } catch (SQLException e) {
            System.out.println("\n[ERROR] Database error: " + e.getMessage());
        }
    }

    private void cancelAppointment(Scanner scanner) {
        System.out.println("\n=== CANCEL APPOINTMENT ===");

        // Show current appointments
        viewAllAppointments();

        System.out.print("\nEnter Appointment ID to cancel: ");
        int appointmentId = scanner.nextInt();

        System.out.print("Are you sure you want to cancel this appointment? (y/n): ");
        scanner.nextLine(); // consume newline
        String confirm = scanner.nextLine();

        if (!confirm.toLowerCase().equals("y")) {
            System.out.println("\n[INFO] Appointment cancellation aborted.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM appointments WHERE appointment_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, appointmentId);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("\n[SUCCESS] Appointment cancelled successfully!");
            } else {
                System.out.println("\n[ERROR] Appointment ID not found!");
            }

        } catch (SQLException e) {
            System.out.println("\n[ERROR] Database error: " + e.getMessage());
        }
    }

    private void searchPatientAppointments(Scanner scanner) {
        System.out.println("\n=== SEARCH PATIENT APPOINTMENTS ===");

        System.out.print("Enter patient name to search: ");
        String searchName = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT a.appointment_id, a.appointment_date, a.appointment_time, a.status, a.fees,
                       p.name as patient_name, p.phone,
                       d.name as doctor_name, d.specialization
                FROM appointments a 
                JOIN patients p ON a.patient_id = p.patient_id 
                JOIN doctors d ON a.doctor_id = d.doctor_id
                WHERE p.name LIKE ?
                ORDER BY a.appointment_date, a.appointment_time
                """;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + searchName + "%");
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("\nNo appointments found for patient name containing: " + searchName);
                return;
            }

            System.out.println("\nSearch Results:");
            System.out.println("ID | Patient Name     | Doctor Name   | Specialization | Date       | Time     | Fees   | Status");
            System.out.println("---|------------------|---------------|----------------|------------|----------|--------|----------");

            do {
                System.out.printf("%-3d| %-17s| %-14s| %-15s| %-11s| %-9s| ₹%-6.2f| %-10s\n",
                        rs.getInt("appointment_id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("specialization"),
                        rs.getDate("appointment_date"),
                        rs.getTime("appointment_time"),
                        rs.getDouble("fees"),
                        rs.getString("status"));
            } while (rs.next());

        } catch (SQLException e) {
            System.out.println("\n[ERROR] Database error: " + e.getMessage());
        }
    }
}
