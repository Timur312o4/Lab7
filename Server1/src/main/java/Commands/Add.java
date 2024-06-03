package Commands;

//тут переделать
import Exceptions.BuildObjectException;
import Exceptions.IncorrectValueEntryException;
import Exceptions.MustBeNotEmptyException;
import Exceptions.WrongArgumentsException;
import Managers.CollectionManager;
import Managers.Console;
import City.City;
import Managers.DBManager;
import Network.Request;
import Network.Response;

/**
 * Команда add - добавить новый элемент в коллекцию
 *
 * @author Timur
 */
public class Add extends Command {
    /**
     * Менеджер коллекции
     */
    private final CollectionManager collectionManager;
    private final DBManager dbManager;
    /**
     * Конструктор класса Add
     * @param console консоль
     * @param collectionManager менеджер коллекции
     */
    public Add(Console console, CollectionManager collectionManager, DBManager dbManager){
        super("add","добавить элемент в коллекцию");
        this.console = console;
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
    }

    /**
     * Добавить новый элемент в коллекцию
     * @param request аргумент команды*
     */
    @Override
    public Response execute(Request request) {
        try{
            if (!request.getArgs().isEmpty()) throw new WrongArgumentsException();
            City city = (City) request.getCommandCity();
            int addElid = dbManager.addElement(city,request.getUser());
            if (addElid == -1){
                return new Response("ERROR","Объект не удалось добавить.");
            }
            city.setId(addElid);
            boolean statusCommand = collectionManager.addToCollection(city);
            if (statusCommand) {
                return new Response("Complete", "Город был успешно создан и добавлен в коллекцию. Команда успешно выполнилась");
            }
            else{
                return new Response("Error","Возникли ошибки! Команда не выполнилась");
            }
    }catch (WrongArgumentsException  | NumberFormatException  e){
            return new Response("Error",e.getMessage());
        }}
    @Override
    public String describe(){
        return this+ ": добавляет элемент в коллекцию";
    }
    @Override
    public String toString(){
        return "add";
    }
}
