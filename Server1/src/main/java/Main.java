import Exceptions.NotEnoughRightException;
import Managers.CollectionManager;
import Managers.CommandManager;
import Managers.*;
import Managers.Server;
import City.City;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {

        /*
        String dbUrl = "jdbc:postgresql://localhost:5432/studs";
        String dbUrlHelios = "jdbc:postgresql://pg:5432/studs";
        String dbConfigPATH = "C:\\Users\\Тимур\\IdeaProjects\\Lab7_vers1\\DBconfig.Properties";
        */
        Logger logger = Logger.getLogger(Main.class.getName());
        String dbUrl = System.getenv("Lab7_DB_URL");
        String dbUrlHelios = System.getenv("Lab7_DB_URL_HELIOS");
        String dbConfigPATH = System.getenv("Lab7_DB_CONFIG_PATH");
        if (dbUrl == null || dbUrl.isEmpty() || dbUrlHelios == null || dbUrlHelios.isEmpty() ||dbConfigPATH==null || dbConfigPATH.isEmpty()) {
            System.err.println("Database configuration is missing.");
            return;
        }
        int port = 46086;
        String host = "localhost";
        DBManager dbManager = new DBManager(dbUrl,dbUrlHelios,dbConfigPATH);
        dbManager.start();
        CopyOnWriteArrayList<City> cities = dbManager.loadCollection();
        CollectionManager collectionManager = new CollectionManager(cities);
        DBSaveManager dataBaseSaveManager = new DBSaveManager(collectionManager);
        CommandManager commandManager = new CommandManager(collectionManager,dbManager);
        Server serv1 = new Server(port, commandManager, dataBaseSaveManager,dbManager);
        serv1.start();
    }
}
