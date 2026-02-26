public class Doctor {
    private int doctor_id;
    private String name;
    private String specialization;
    private String availability_hours;

    // Default constructor
    public Doctor() {}

    // Constructor
    public Doctor(String name, String specialization, String availability_hours) {
        this.name = name;
        this.specialization = specialization;
        this.availability_hours = availability_hours;
    }

    // Getters and setters
    public int getDoctor_id() { return doctor_id; }
    public void setDoctor_id(int doctor_id) { this.doctor_id = doctor_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getAvailability_hours() { return availability_hours; }
    public void setAvailability_hours(String availability_hours) { this.availability_hours = availability_hours; }
}
