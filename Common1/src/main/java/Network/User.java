package Network;

import java.io.Serializable;

public class User implements Serializable {
    private String userName;
    private String password;
    public User (String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
    public String getName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
}
