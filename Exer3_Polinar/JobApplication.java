
import java.util.Date;

public class JobApplication {
    private String status;
    private Date appliedDate;

    
    public JobApplication(String status, Date appliedDate) {
        this.status = status;
        this.appliedDate = appliedDate;
    }

    
    public JobApplication(String status) {
        this(status, new Date());
    }

    public void submit() {
        System.out.println("Application submitted on " + appliedDate);
    }

    public void withdraw() {
        System.out.println("Application withdrawn.");
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
        System.out.println("Application status updated to: " + status);
    }
}
