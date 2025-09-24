import java.util.*;

public class Projecttester {
    public static void main(String[] args) throws Exception {
        // Polymorphism with superclass LoginSystem
        List<LoginSystem> users = Arrays.asList(
            new Applicant("applicant@email.com", "pass123", "Mico", "555-1111", "AliceResume.pdf"),
            new Employer("hr@acme.com", "secret", "MERO Corp", "Manufacturing", "NYC"),
            new RecruiterHRManager("recruit@globex.com", "recruit!", "Eric", "Talent Acquisition")
        );

        for (LoginSystem user : users) {
            System.out.println("Role: " + user.getRole());
            user.showDashboard(); 
        }

        
        ApplicationOperations appOps = new JobApplication("Submitted");
        appOps.submit();
        appOps.updateStatus("Under Review");
        appOps.withdraw();

        
        for (LoginSystem user : users) {
            if (user instanceof Applicant) {
                ((Applicant) user).applyForJob();
            } else if (user instanceof Employer) {
                ((Employer) user).postJob("Software Engineer");
                ((Employer) user).viewApplicants();
            } else if (user instanceof RecruiterHRManager) {
                ((RecruiterHRManager) user).screenResumes();
            }
        }
    }
}
