
public class LoginSystem {
    protected String email;
    protected String password;

    public LoginSystem(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean authenticate(String inputEmail, String inputPassword) {
        return this.email.equals(inputEmail) && this.password.equals(inputPassword);
    }

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
