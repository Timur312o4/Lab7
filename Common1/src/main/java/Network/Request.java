package Network;
import java.io.Serializable;
import City.City;
public class Request implements Serializable {
    private String commandName;
    private String args = ""; // аргументы, которые передаются в команде
    private City commandCity = null;
    private User user;
    public Request(User user,String commandName,String args, City commandCity){
        this.user = user;
        this.commandName = commandName;
        this.args = args;
        this.commandCity = commandCity;
    }
    public Request(User user,String commandName,String args){
        this.user = user;
        this.commandName=commandName;
        this.args=args;
    }
    public Request(User user,String commandName, City commandCity){
        this.user = user;
        this.commandName=commandName;
        this.commandCity=commandCity;
    }
    /*public Request(String commandName, City commandCity, String status){
        this.commandName=commandName;
        this.commandCity=commandCity;
        this.status=status;
    }*/

    public String getCommandName() {
        return commandName;
    }
    public String getArgs() {
        return args;
    }
    public Serializable getCommandCity() {
        return commandCity;
    }
    public boolean isEmpty(){
        return commandName.isEmpty() && args.isEmpty() && commandCity == null;
    }
    public User getUser() {
        return user;
    }
    @Override
    public String toString() {
        return this.getUser().getName()+" "+this.getUser().getPassword()+" "+this.getCommandName();
    }
}
