
public class Applicant extends LoginSystem {
    private String name;
    private String phone;
    private String resume;

    
    public Applicant(String email, String password, String name, String phone, String resume) {
        super(email, password);
        this.name = name;
        this.phone = phone;
        this.resume = resume;
    }

    
    public Applicant(String name, String email, String phone, String resume) {
        super(email, ""); 
        this.name = name;
        this.phone = phone;
        this.resume = resume;
    }

    public void applyForJob() {
        System.out.println(name + " has applied for a job with resume: " + resume + ".");
    }

   
    public void updateProfile(String newResume) {
        this.resume = newResume;
        System.out.println(name + " updated their resume.");
    }

   
    public void updateProfile(String newName, String newEmail, String newPhone) {
        this.name = newName;
        this.email = newEmail;
        this.phone = newPhone;
        System.out.println("Profile updated: " + this.name + ", " + this.email + ", " + this.phone);
    }

    public void viewJobs() {
        System.out.println(name + " is viewing available jobs.");
    }

    @Override
    public String getRole() {
        return "Applicant";
    }

    @Override
    public void showDashboard() {
        System.out.println("Welcome, " + name + " (" + getRole() + ") - showing applicant dashboard.");
    }
}
