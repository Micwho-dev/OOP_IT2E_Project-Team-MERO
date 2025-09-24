
public class RecruiterHRManager extends LoginSystem {
    private String name;
    private String department;

    public RecruiterHRManager(String email, String password, String name, String department) {
        super(email, password);
        this.name = name;
        this.department = department;
    }

    public void screenResumes() {
        System.out.println(name + " is screening resumes in " + department + " department.");
    }

    public void manageApplication() {
        System.out.println(name + " is managing applications.");
    }

    public void makeOffer() {
        System.out.println(name + " made a job offer.");
    }

    @Override
    public String getRole() {
        return "Recruiter/HR Manager";
    }

    @Override
    public void showDashboard() {
        System.out.println("Welcome, " + name + " (" + getRole() + ") - showing recruiter dashboard.");
    }
}
