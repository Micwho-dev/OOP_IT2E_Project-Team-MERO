
public class Employer extends LoginSystem {
    private String companyName;
    private String industry;
    private String location;


    public Employer(String email, String password, String companyName, String industry, String location) {
        super(email, password);
        this.companyName = companyName;
        this.industry = industry;
        this.location = location;
    }

    
    public Employer(String companyName, String industry, String location) {
        super("", ""); 
        this.companyName = companyName;
        this.industry = industry;
        this.location = location;
    }

    public void postJob() {
        System.out.println(companyName + " posted a new job in the " + industry + " industry.");
    }

    public void postJob(String jobTitle) {
        System.out.println(companyName + " posted a new job: " + jobTitle + " in the " + industry + " industry.");
    }

    public void viewApplicants() {
        System.out.println(companyName + " is viewing applicants for positions in " + location + ".");
    }

    public void getApplicationData() {
        System.out.println(companyName + " retrieved job application data.");
    }
}
