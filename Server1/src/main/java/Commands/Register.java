package Commands;

import Managers.Console;
import Managers.DBManager;
import Network.Request;
import Network.Response;

public class Register extends Command{
    private DBManager db;
    public Register(){
        super("register","");
    }
    @Override
    public Response execute(Request request) {
        return new Response("complete","");
    }
    @Override
    public String describe(){
        return "Регистрация.";
    }
}
