package Managers;

import City.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Менеджер сохранения коллекции в файл
 * @author Timur
 */
// для 7 лабы этот класс точно не пригодится
public class DBSaveManager {
    private final CollectionManager collectionManager;
    public DBSaveManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }
    public void writeToBD(String fileName) {
        String path;
        CopyOnWriteArrayList<City> cities = collectionManager.getCityCollection();
        if (!fileName.substring(Math.max(0,fileName.length()-4)).equals(".csv")){
            path = fileName+".csv"; // переделать, хотя мне кажется этот класс вообще уйдет, потому что там все динамически в базе данных будет обновляться
        }else {
            path = fileName;
        }
        File file = new File(path);
        if (file.canWrite() || !file.exists()) {
            try(PrintWriter writer = new PrintWriter(file)){
                for (City city : cities) {
                    String csvString = toCSVstring(city);
                    writer.println(csvString);
                }
            }catch(IOException e){
                System.err.println(e.getMessage());}
        }else{
            System.err.println("Недостаточно прав для записи в файл!");
        }
    }

    /**
     * Преобразование каждого элемента коллекции в строковое представление, где каждые поля разделены запятой, для удобного преобразования в csv формат
     * @param city элемент из коллекции
     * @return строковое представление
     */
    public static String toCSVstring(City city){
        if (city.getGovernor() != null){
            return city.getId()+", "+city.getName()+","+city.getCoordinates().getX()+','+city.getCoordinates().getY()+","+
                city.getCreationDate()+","+ city.getArea()+","+city.getPopulation()+','+city.getMetersAboveSeaLevel()+","+
                city.getTelephoneCode()+","+city.getGoverment()+", "+city.getStandartOfLiving()+", "+","+city.getGovernor().getName()+","+
                city.getGovernor().getAge()+","+city.getGovernor().getHeight();
        }else{
            return city.getId()+", "+city.getName()+","+city.getCoordinates().getX()+','+city.getCoordinates().getY()+","+
                    city.getCreationDate()+","+ city.getArea()+","+city.getPopulation()+','+city.getMetersAboveSeaLevel()+","+
                    city.getTelephoneCode()+","+city.getGoverment()+", "+city.getStandartOfLiving()+",null"+",1"+",1"+",1";
        }
    }
}
