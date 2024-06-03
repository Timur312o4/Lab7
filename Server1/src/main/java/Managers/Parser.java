package Managers;

import City.*;
import Exceptions.EmptyFileException;
import Exceptions.IncorrectValueEntryException;
import Exceptions.SameIdInFileException;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс Parser - который парсит данные из файла и на основе этих данных создает коллекцию
 * @author Timur
 */
public class Parser {
    /**
     * Парсер, который принимает на вход данные из базы данных и каждую строку преобразует в элемент коллекции
     * @return коллекцию
     */
    public CopyOnWriteArrayList<City> getCollection(ResultSet rs) {
        CopyOnWriteArrayList<City> cities = new CopyOnWriteArrayList<>();
        try {
            if (!rs.isBeforeFirst()) throw new EmptyFileException();
            while (rs.next()){
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        Coordinates coordinates = new Coordinates(rs.getInt("x"),rs.getDouble("y"));
                        Date creationDate = rs.getTimestamp("creationDate");
                        Long area = rs.getLong("area");
                        Long population = rs.getLong("population");
                        Long telephoneCode = rs.getLong("telephoneCode");
                        Long metersAboveSeaLevel = rs.getLong("metersAboveSeaLevel");
                        Goverment goverment = null;
                        StandartOfLiving standartOfLiving = null;
                        String govermentStr = rs.getString("goverment");
                        if (govermentStr != null) {
                            goverment = Goverment.valueOf(rs.getString("goverment"));
                        }
                        String standartOfLivingstr = rs.getString("standartOfLiving");
                        if (standartOfLivingstr != null) {
                            standartOfLiving = StandartOfLiving.valueOf(rs.getString("StandartOfLiving"));
                        }
                        Human governor = null;
                        boolean ExistGovernor = rs.getBoolean("existgovernor");
                        if (ExistGovernor) {
                            governor = new Human(rs.getString("nameGovernor"),rs.getLong("age"),rs.getInt("height"));
                        }
                        String username = rs.getString("username");
                        City city = new City(id, name, coordinates, creationDate, area, population, metersAboveSeaLevel, telephoneCode, goverment, standartOfLiving, governor,username);
                        rs.getInt("userId");
                        int index = rs.getInt("indexcollection");
                        if (ValidatorParse.validateCity(city)) {
                            cities.add(city);
                        } else {
                            System.out.println(city);
                            Console.printError("Ошибка в создании объекта город, в файле введены значения полей не удовлетворяющие ограничениям");
                        }
            }
        }catch(SQLException e){
            Console.printError(e.getMessage());
        } catch (EmptyFileException e) {
            Console.printError("База данных не должна быть пустой!");
        }
        return cities;
    }
}

