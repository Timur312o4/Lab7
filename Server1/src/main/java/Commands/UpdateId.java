package Commands;

// тут тоже переделать
import City.*;
import Exceptions.*;
import Managers.CollectionManager;
import Managers.Console;
import Managers.DBManager;
import Network.Request;
import Network.Response;

/**
 * Команда updateId - обновить значение элемента коллекции, id которого равен заданному
 */

public class UpdateId extends Command {
    /**
     * Менеджер коллекции
     */
    private final CollectionManager collectionManager;
    private final DBManager dbManager;
    /**
     * Конструктор класса UpdateId
     * @param console консоль
     * @param collectionManager менеджер коллекци
     */
    public UpdateId(Console console, CollectionManager collectionManager, DBManager dbManager){
        super("UpdateId","");
        this.console = console;
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
    }

    /**
     * Обновляет элемент, у которого id равен заданному
     */
    // вроде как реализовал
    @Override
    public Response execute(Request request){
        boolean statusCommand;
        try{
            if (request.getArgs().isEmpty()) throw new WrongArgumentsException();
            int id = Integer.parseInt(request.getArgs());
            if (id<=0) {
                Console.printError("Значение id должно быть больше 0! ");
                throw new IncorrectValueEntryException();
            }
            if (collectionManager.getCityCollection().isEmpty()){
                throw new CollectionMustBeNotEmptyException();
            }
            if (!collectionManager.getIdCities().contains(id)){
                throw new ClientDeleteObjectException();
            }
            City city = (City) request.getCommandCity();
            boolean updateElement = dbManager.updateElement(city,request.getUser());
            if (!updateElement){
                return new Response("Error","При обновлении элемента в базу данных возникли ошибки! Нельзя обновлять чужие элементы!");
            }
            statusCommand=collectionManager.updateByIdFromCollection(city,id);
            if (statusCommand){
                return new Response("Complete","Команда успешно выполнилась!");
            }
            else{
                return new Response("Error","Возникли ошибки! Команда не выполнилась. Элемента с таким id нет в коллекции!");
            }
    }catch(IncorrectValueEntryException | WrongArgumentsException | NumberFormatException  e){
            return new Response("Error","Возникли ошибки! Команда не выполнилась");
        }catch (CollectionMustBeNotEmptyException e){
            return new Response("Error","Возникли ошибки! Команда не выполнилась"+e.getMessage());
        }catch (ClientDeleteObjectException e){
            return new Response("Error", "Объекта с данным id нет в коллекции! Вероятно его удалил другое устройство, сидящее на вашем аккаунте.");
        }
    }

    @Override
    public String describe(){
        return this +": обновить значение элемента коллекции по id.";
    }
    @Override
    public String toString(){
        return "updateId";
    }
}
