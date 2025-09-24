
import java.util.Date;

interface ApplicationOperations {
    void submit();
    void withdraw();
    void updateStatus(String newStatus);
}

public class JobApplication implements ApplicationOperations {
    private String status;
    private Date appliedDate;

    
    public JobApplication(String status, Date appliedDate) {
        this.status = status;
        this.appliedDate = appliedDate;
    }

    
    public JobApplication(String status) {
        this(status, new Date());
    }

    @Override
    public void submit() {
        System.out.println("Application submitted on " + appliedDate);
    }

    @Override
    public void withdraw() {
        System.out.println("Application withdrawn.");
    }

    @Override
    public void updateStatus(String newStatus) {
        this.status = newStatus;
        System.out.println("Application status updated to: " + status);
    }
}
