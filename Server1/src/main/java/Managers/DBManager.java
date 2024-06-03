package Managers;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import City.*;
import Exceptions.NotExistUser;
import Network.User;

public class DBManager {
    private Connection conn; //
    private MessageDigest md;
    private final String dburl;
    private final String dbUrlHelios;
    private final String dbPath;
    public DBManager(String dbUrl,String dbUrlHelios, String dbPath){
        this.dburl = dbUrl;
        this.dbUrlHelios = dbUrlHelios;
        this.dbPath = dbPath;
    }
    public void start(){
        try{
            this.connection();
            this.createDateBase();
        }catch(SQLException e){
            System.err.println("Таблицы уже созданы.");
        }
    }
    public void connection(){
        Properties info =null;
        try{
            info = new Properties();
            System.out.println(dbPath);
            info.load(new FileInputStream(this.dbPath));
            String user = info.getProperty("user");
            String password = info.getProperty("password");
            this.conn = DriverManager.getConnection(this.dburl,user,password);
            System.out.println("Подключение успешно!");
    }catch(IOException| SQLException e){
        try{
            this.conn = DriverManager.getConnection(this.dbUrlHelios,info);
        }catch(SQLException e2){
            System.out.println(e2.getMessage());
            System.exit(1);
        }
        }
    }
    public void createDateBase() throws SQLException{
        String sql ="""
                DROP TYPE IF EXISTS StandartOfLiving;
                DROP TYPE IF EXISTS Goverment;
                CREATE TYPE StandartOfLiving AS ENUM ('ULTRA_HIGH', 'VERY_HIGH', 'LOW', 'ULTRA_LOW','NIGHTMARE');
                CREATE TYPE Goverment AS ENUM ('DESPOTISM', 'CORPORATOCRACY', 'TOTALITARIANISM');
                CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY,username VARCHAR(80),password VARCHAR(80)); 
                CREATE TABLE IF NOT EXISTS city (id SERIAL PRIMARY KEY, name VARCHAR(60),x INT, y FLOAT,creationDate TIMESTAMP,
                area BIGINT, population BIGINT,metersAboveSeaLevel BIGINT, telephoneCode BIGINT,goverment Goverment,
                standartOfLiving StandartOfLiving,existgovernor boolean,nameGovernor VARCHAR(60), height INT,
                age BIGINT, userId INT REFERENCES users(id) ON DELETE CASCADE, indexCollection INT);
                """;
        this.conn.prepareStatement(sql).execute();
    }
    public CopyOnWriteArrayList<City> loadCollection(){
        Parser parser = new Parser();
        try{
            String sql = "SELECT * FROM city JOIN users ON users.id=city.userid ORDER BY indexcollection;";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();
            return parser.getCollection(resultSet);
        }catch(SQLException e){
            Console.printError("Возникли ошибки");
            return new CopyOnWriteArrayList<City>();
        }
    }
    public boolean addUser(User user){
        try {
            String sql = """
                    INSERT INTO users(username,password) VALUES (?,?);
                """;
            PreparedStatement ps = this.conn.prepareStatement(sql);
            if(this.checkExistUser(user.getName())) throw new SQLException();
            ps.setString(1, user.getName());
            ps.setString(2, HashPassword.hashPassword(user.getPassword()));
            int result = ps.executeUpdate();
            if (result > 0) return true;
            else return false;
        }catch (SQLException e){
            System.err.println("Пользователь с таким именем уже существует.");
            return false;
        }catch (NoSuchAlgorithmException e){
            System.err.println(e.getMessage());
            return false;
        }
    }
    public boolean checkExistUser(String user){
        try{
            String sql= """
                    SELECT * FROM users WHERE username=?;
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            boolean result = rs.next();
            return result;
        }catch(SQLException e){
            Console.printError(e.getMessage());
            return false;
        }
    }
    public boolean autorizationUser(User user){
        try{
            String username = user.getName();
            String sql = """
                    SELECT * FROM users where username=?;
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet resultSet = ps.executeQuery();
            if(resultSet.next()){
                String password = resultSet.getString("password");
                return password.equals(HashPassword.hashPassword(user.getPassword()));
            }
            else return false;
        }catch(SQLException e){
            Console.printError(e.getMessage());
            return false;
        }catch(NoSuchAlgorithmException e){
            Console.printError(e.getMessage());
            return false;
        }
    }
    public int getUserId(User user){
        int id = -1;
        try{
            String sql1 = """
                    SELECT id FROM users WHERE username=?;
                    """;
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,user.getName());
            ResultSet rs1 = ps1.executeQuery();
            if (!rs1.next()) return -1;
            id = rs1.getInt(1);
            return id;
        }catch(SQLException e){
            return id;
        }
    }
    public int addElement(City city, User user){
        int id=-1;
        try{
            String sql = """
                    INSERT INTO city (name,x,y,creationDate,area, population, metersabovesealevel, telephonecode,
                    goverment, standartofliving,existgovernor, namegovernor, height, age, userid, indexcollection) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?,?)
                    RETURNING id;        
            """;
            id = this.getUserId(user);
            int len = this.lenCollection();
            if (id <= 0 ) throw new NotExistUser();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, city.getName());
            ps.setInt(2,city.getCoordinates().getX());
            ps.setDouble(3,city.getCoordinates().getY());
            ps.setDate(4,new java.sql.Date(city.getCreationDate().getTime()));
            ps.setLong(5,city.getArea());
            ps.setLong(6,city.getPopulation());
            ps.setLong(7,city.getMetersAboveSeaLevel());
            ps.setLong(8,city.getTelephoneCode());
            ps.setObject(9, city.getGoverment());
            ps.setObject(10,city.getStandartOfLiving());
            if (city.getGovernor() != null) {
                ps.setBoolean(11,true);
                ps.setString(12, city.getGovernor().getName());
                ps.setInt(13, city.getGovernor().getHeight());
                ps.setLong(14, city.getGovernor().getAge());
            }else {
                ps.setBoolean(11,false);
                ps.setString(12, null);
                ps.setNull(13, java.sql.Types.INTEGER);
                ps.setNull(14, java.sql.Types.INTEGER);
            }
            ps.setInt(15, id);
            if (len != -100){
                ps.setInt(16, len);}
            else throw new SQLException();
            System.out.println("Дошел до исполнения запроса");
            ResultSet rs = ps.executeQuery();
            System.out.println("Вроде выполнил запрос");
            if(!rs.next()){
                System.err.println("Объект не был добавлен в базу данных");
                return -1;
            }else {
                return rs.getInt(1);
            }
        }catch(SQLException e){
            Console.printError(e.getMessage());
            return -1;
        }catch(NotExistUser e){
            Console.printError("Пользователя с таким id не существует.");
            return -100;
        }
    }
    public boolean deleteElement(int idElement, User user){
        int id=-1;
        try{
            id = this.getUserId(user);// мб проверку на удаление лучше сделать в командах
            if (id == -1){
                System.err.println("Авторизованным пользователям нельзя удалять чужие элементы!");
                return false;
            }
            String sql = """
                    DELETE FROM city WHERE city.id = ? AND city.userid = ? RETURNING city.indexcollection;
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,idElement);
            ps.setInt(2,getUserId(user));
            ResultSet result = ps.executeQuery();
            if (result.next()){
                int indexCollection = result.getInt(1);
                this.changeIndexCollection(-1000,indexCollection,false);
                return true;
            }else{
                return false;
            }
        }catch(SQLException e){
            Console.printError(e.getMessage());
            return false;
        }
    }
    public void changeIndexCollection(int idCity,int indexCollection, boolean addOrDelete){ //Если добавляем элемент, то true, иначе false
        try{
            String sql;
            if (addOrDelete){
                    sql = """
                    UPDATE city SET indexcollection=indexcollection+1 WHERE indexcollection>=? and id != ?;
                    """;
            }else{
                    sql = """
                    UPDATE city SET indexcollection=indexcollection-1 WHERE indexcollection>?;
                    """;}
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,indexCollection);
            if(addOrDelete){
                ps.setInt(2,idCity);}
            int res = ps.executeUpdate();
        }catch(SQLException e){
            Console.printError(e.getMessage());
        }
    }
    public boolean deleteLowerByElement(City city, User user){
        int id = -1;
        try {
            id = this.getUserId(user);
            String sql = """
                    DELETE FROM city WHERE city.userid = ? & city.population < ? RETURNING city.indexcollection;
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,id);
            ps.setLong(2,city.getPopulation());
            ResultSet result = ps.executeQuery();
            if (!result.next()){
                return false;}
            while (result.next()){
                int indexCollection = result.getInt(1);
                this.changeIndexCollection(-1000,indexCollection,false);
            }
            return true;
        } catch (SQLException e) {
            Console.printError(e.getMessage());
            return false;
        }
    }
    public boolean deleteAllElement(User user) {
        int id = -1;
        try {
            id = this.getUserId(user);
            String sql = """
                    DELETE FROM city WHERE city.userid = ? Returning city.indexcollection;
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,id);
            ResultSet result = ps.executeQuery();
            if (!result.next()){
                return false;}
            while (result.next()){
                int indexCollection = result.getInt(1);
                this.changeIndexCollection(-1000,indexCollection,false);
            }
            return true;
        } catch (SQLException e) {
            Console.printError(e.getMessage());
            return false;
        }
    }
    public int lenCollection(){
        try{
            String sql= """
                SELECT COUNT(*) FROM city ;
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                return 0;
            }
            return rs.getInt(1);
        }catch(SQLException e){
            System.out.println(e.getMessage());
            return -100;
        }
    }
    public boolean updateElement(City city, User user){
        int id=-1;
        try{
            id = this.getUserId(user);
            String sql = """
                    UPDATE city SET (name,x,y,creationDate,area, population, metersabovesealevel, telephonecode,
                    goverment, standartofliving,existgovernor, namegovernor, height, age, userid) = (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)
                    WHERE city.id = ? AND city.userid = ?;
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,city.getName());
            ps.setInt(2,city.getCoordinates().getX());
            ps.setDouble(3,city.getCoordinates().getY());
            ps.setDate(4,new java.sql.Date(city.getCreationDate().getTime()));;
            ps.setLong(5,city.getArea());
            ps.setLong(6,city.getPopulation());
            ps.setLong(7,city.getMetersAboveSeaLevel());
            ps.setLong(8,city.getTelephoneCode());
            ps.setObject(9, city.getGoverment(), Types.OTHER);
            ps.setObject(10,city.getStandartOfLiving(), Types.OTHER);
            if (city.getGovernor() != null) {
                ps.setBoolean(11,true);
                ps.setString(12, city.getGovernor().getName());
                ps.setInt(13, city.getGovernor().getHeight());
                ps.setLong(14, city.getGovernor().getAge());
            }else {
                ps.setBoolean(11,false);
                ps.setString(12, null);
                ps.setNull(13, java.sql.Types.INTEGER);
                ps.setNull(14, java.sql.Types.BIGINT);
            }
            ps.setInt(15, id);
            ps.setInt(16,city.getId());
            ps.setInt(17,getUserId(user));
            int rs = ps.executeUpdate();
            return rs>0;
        }catch(SQLException e){
            Console.printError(e.getMessage());
            return false;
        }
    }
    public int insertAtElement(City city, User user,int position){
        int id=-1;
        try{
            String sql = """
                    INSERT INTO city (name,x,y,creationDate,area, population, metersabovesealevel, telephonecode,
                    goverment, standartofliving,existgovernor, namegovernor, height, age, userid, indexcollection) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?,?)
                    RETURNING city.id;
                    """;
            id = this.getUserId(user);
            int len = this.lenCollection();
            if (id <= 0 ) throw new NotExistUser();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, city.getName());
            ps.setInt(2,city.getCoordinates().getX());
            ps.setDouble(3,city.getCoordinates().getY());
            ps.setDate(4,new java.sql.Date(city.getCreationDate().getTime()));
            ps.setLong(5,city.getArea());
            ps.setLong(6,city.getPopulation());
            ps.setLong(7,city.getMetersAboveSeaLevel());
            ps.setLong(8,city.getTelephoneCode());
            ps.setObject(9, city.getGoverment());
            ps.setObject(10,city.getStandartOfLiving());
            if (city.getGovernor() != null) {
                ps.setBoolean(11,true);
                ps.setString(12, city.getGovernor().getName());
                ps.setInt(13, city.getGovernor().getHeight());
                ps.setLong(14, city.getGovernor().getAge());
            }else {
                ps.setBoolean(11,false);
                ps.setString(12, null);
                ps.setNull(13, java.sql.Types.INTEGER);
                ps.setNull(14, java.sql.Types.INTEGER);
            }
            ps.setInt(15, id);
            ps.setInt(16, position);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                System.err.println("Объект не был добавлен в базу данных");
                return id; // если не сможет добавить вернет -1
            }else{
                int newId =rs.getInt(1);
                changeIndexCollection(newId,position,true);
                return newId; // вернет что-то большее -1
            }
        }catch(SQLException e){
            Console.printError(e.getMessage());
            return id;
        }catch(NotExistUser e){
            Console.printError("Пользователя с таким id не существует.");
            return -100;
        }
    }
    public void close(){
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Соединение закрыто");
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
            }
        }
    }
}
