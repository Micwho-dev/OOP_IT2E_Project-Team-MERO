
public abstract class LoginSystem {
    protected String email;
    protected String password;

    public LoginSystem(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean authenticate(String inputEmail, String inputPassword) {
        return this.email.equals(inputEmail) && this.password.equals(inputPassword);
    }

    // Polymorphic API to be implemented by all users of the system
    public abstract String getRole();

    public abstract void showDashboard();

    // Getters & Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
