import java.sql.Date;
import java.sql.Time;

public class Appointment {
    private int appointment_id;
    private int patient_id;
    private int doctor_id;
    private Date appointment_date;
    private Time appointment_time;
    private String status;
    private String patient_name;
    private String doctor_name;
    private String phone;
    private String specialization;
    private double fees;  // NEW FIELD

    // Default constructor
    public Appointment() {}

    // Existing getters and setters...
    public int getAppointment_id() { return appointment_id; }
    public void setAppointment_id(int appointment_id) { this.appointment_id = appointment_id; }

    public int getPatient_id() { return patient_id; }
    public void setPatient_id(int patient_id) { this.patient_id = patient_id; }

    public int getDoctor_id() { return doctor_id; }
    public void setDoctor_id(int doctor_id) { this.doctor_id = doctor_id; }

    public Date getAppointment_date() { return appointment_date; }
    public void setAppointment_date(Date appointment_date) { this.appointment_date = appointment_date; }

    public Time getAppointment_time() { return appointment_time; }
    public void setAppointment_time(Time appointment_time) { this.appointment_time = appointment_time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPatient_name() { return patient_name; }
    public void setPatient_name(String patient_name) { this.patient_name = patient_name; }

    public String getDoctor_name() { return doctor_name; }
    public void setDoctor_name(String doctor_name) { this.doctor_name = doctor_name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    // NEW GETTER AND SETTER FOR FEES
    public double getFees() { return fees; }
    public void setFees(double fees) { this.fees = fees; }
}
