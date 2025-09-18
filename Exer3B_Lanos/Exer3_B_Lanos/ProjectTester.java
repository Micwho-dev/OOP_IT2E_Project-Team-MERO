
public class ProjectTester {
    public static void main(String[] args) {
        
        LoginSystem login = new LoginSystem("test@email.com", "12345");

        if (login.authenticate("test@email.com", "12345")) {
            System.out.println("Login successful!\n");

            
            Applicant applicant = new Applicant("Noel Mico Regulada ", "Noel@email.com", "09123456789", "My Resume");
            applicant.updateProfile("Noel Mico R.", "NoelMico.Regulada@email.com", "09987654321");
            applicant.viewJobs();
            applicant.applyForJob();

            
            Employer employer = new Employer("Tech Corp", "Software", "Cebu");
            employer.postJob("Software Engineer");
            employer.viewApplicants();

           
            RecruiterHRManager hr = new RecruiterHRManager("hr@company.com", "hrPass", "Eric HR", "Recruitment");
            hr.screenResumes();
            hr.manageApplication();
            hr.makeOffer();

            
            JobApplication jobApp = new JobApplication("Pending");
            jobApp.submit();
            jobApp.updateStatus("Reviewed");
            jobApp.withdraw();

        } else {
            System.out.println("Invalid credentials!");
        }
    }
}
