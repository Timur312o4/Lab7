package Commands;

// тут переделать
import Exceptions.BuildObjectException;
import Exceptions.IncorrectValueEntryException;
import Exceptions.MustBeNotEmptyException;
import Exceptions.WrongArgumentsException;
import Managers.CollectionManager;
import Managers.Console;
import Managers.DBManager;
import Network.Request;
import Network.Response;
import City.City;
/**
 * Команда removeLower - удаляет элементы, которые меньше заданного элемента
 */
public class RemoveLower extends Command{
    /**
     * Менеджер коллекции
     */
    private final CollectionManager collectionManager;
    private final DBManager dbManager;
    /**
     * Конструктор класса RemoveLower
     * @param console консоль
     * @param collectionManager коллекции менеджер
     */
    public RemoveLower(Console console, CollectionManager collectionManager,DBManager dbManager){
        super("RemoveLower","");
        this.console=console;
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
    }

    /**
     * удаляет элементы, которые меньше заданного элемента
     */
    // реализовать данную команду
    @Override
    public Response execute(Request request) {
        boolean statusCommand;
        try {
            if (!request.getArgs().isEmpty()) throw new WrongArgumentsException();
            City cityreq = (City) request.getCommandCity();
            boolean result = dbManager.deleteLowerByElement(cityreq,request.getUser());
            if (!result) return new Response("Error","Не удалось удалить элементы меньшие чем заданный! У данного пользователя нет таких элементов.");
            statusCommand=collectionManager.removeFromCollectionByLower(cityreq);
            if (statusCommand){
                return new Response("Complete","Команда успешно выполнилась");
            }else{
                return new Response("Error","Возникли ошибки! Команда не выполнилась. Не нужно удалять элементы из пустой коллекции.");
            }
        } catch (NumberFormatException| WrongArgumentsException e) {
            return new Response("Error","Возникли ошибки! Команда не выполнилась." +e.getMessage());

        }
    }

    @Override
    public String describe(){
        return this+": Удалить из коллекции все элементы, меньше, чем заданный";
    }
    @Override
    public String toString(){
        return "removeLower";
    }
}
