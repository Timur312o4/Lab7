package Commands;

import Exceptions.WrongArgumentsException;
import Managers.CollectionManager;
import Managers.Console;
import City.City;
import Managers.DBManager;
import Network.Request;
import Network.Response;

/**
 * Команда clear - очистить коллекцию
 *
 * @author Timur
 */
public class Clear extends Command {
    /**
     * Менеджер коллекции
     */
    private final DBManager dbManager;
    private final CollectionManager collectionManager;

    /**
     * Конструктор команды Clear
     * @param console консоль
     * @param collectionManager менеджер коллекции
     */
    public Clear(Console console, CollectionManager collectionManager, DBManager dbManager) {
        super("clear", "очистить коллекцию");
        this.console = console;
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
    }

    /**
     * Очистить коллекцию
     */
    @Override
    public Response execute(Request request){
        try{
            if (!request.getArgs().isEmpty()) throw new WrongArgumentsException();
            boolean clearElements = dbManager.deleteAllElement(request.getUser());
            if (!clearElements) {
                return new Response("ERROR","не удалось удалить элементы из коллекции,так как нет элементов в коллекции принадлежащих данному пользователю.");
            }
            boolean statusCommand=collectionManager.clearCollection(request.getUser());
            if (statusCommand){
                return new Response("Complete","Команда успешно выполнилась");
            }else {
                return new Response("Error","Возникли ошибки! Команда не выполнилась");
            }
    }catch(WrongArgumentsException e){
            return new Response("Error","Возникли ошибки! Команда не выполнилась "+e.getMessage());
        }
    }
    @Override
    public String describe(){
        return this+": очищает коллекцию";
    }
    @Override
    public String toString(){
        return "clear";
    }
}
