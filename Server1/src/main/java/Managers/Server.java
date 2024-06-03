package Managers;
import Commands.*;
import Exceptions.OpeningServerException;
import Exceptions.UnknownCommandException;
import Network.Request;
import Network.Response;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private Selector selector;
    private int address;
    private Response responseToUser;
    private Request userRequest;
    private final Logger logger = Logger.getLogger(Server.class.getName());
    private ServerSocketChannel serverSocketChannel;
    private CommandManager commandManager;
    private ForkJoinPool forkJoinPool;
    private DBSaveManager dataBaseSaveManager;
    private DBManager dbManager;
    private static ExecutorService readingPool = Executors.newFixedThreadPool(100);
    private static ForkJoinPool processingPool = new ForkJoinPool();
    private static ForkJoinPool answerPool= new ForkJoinPool();
    public Server(int address, CommandManager commandManager, DBSaveManager dataBaseSaveManager,DBManager dbManager) {
        this.address = address;
        this.commandManager = commandManager;
        this.dataBaseSaveManager = dataBaseSaveManager;
        this.dbManager = dbManager;
        this.forkJoinPool = ForkJoinPool.commonPool();
    }

    public void start() {
        try {
            openServerSocket();
            new Thread(()-> {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(System.in)));
                    while(true) {
                        try{
                            String line = reader.readLine();
                            if (line.equals("save")) {
                                logger.log(Level.INFO, "Завершение работы сервера с помощью специальной команды.");
                                System.exit(0);
                            }
                        }catch(IOException e){
                        }catch (Exception e){
                            logger.log(Level.WARNING, "");
                        }
                    }
            }).start();
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    try{
                        if (key.isValid()) {
                            if (key.isAcceptable()) {
                                SocketChannel clientChannel = this.connectToClient(key);
                            }
                            if (key.isReadable()) {
                                SocketChannel clientChannel = (SocketChannel) key.channel();
                                clientChannel.configureBlocking(false);
                                Future<?> future = readingPool.submit(()-> {
                                    try{
                                        userRequest = this.getRequest(clientChannel);
                                        logger.info("Получение входящего запроса со стороны клиента");
                                    }catch(IOException e){

                                    }catch(ClassNotFoundException e){
                                    }});
                                try{
                                    future.get();
                                }catch(InterruptedException | ExecutionException e){
                                }
                                processingPool.submit(()->{
                                    responseToUser = executeCommand(userRequest);
                                    logger.info("Обработка запроса");
                                }).join();
                                    clientChannel.register(selector, SelectionKey.OP_WRITE);
                                }
                            if (key.isWritable()) {
                                    SocketChannel clientSocketChannel = (SocketChannel) key.channel();
                                    clientSocketChannel.configureBlocking(false);
                                answerPool.submit(() ->{
                                    try {
                                        this.sendResponse(clientSocketChannel, responseToUser);
                                        logger.info("Ответ отправлен клиенту");
                                    } catch (IOException e) {
                                        logger.log(Level.WARNING,"Возникла ошибка ввода-вывода при отправлении ответа");
                                    }
                                }).join();
                                clientSocketChannel.register(selector, SelectionKey.OP_READ);
                            }
                        }
                    } catch (IOException | CancelledKeyException e) {
                        logger.info("Клиент отключился");
                        key.cancel();
                    }
                        keys.remove();
                    }
            }
        }catch (OpeningServerException e) {
            Console.printError("Сервер не может быть запущен!");
        }catch(NoSuchElementException e){
            logger.info("Остановка сервера через консоль.");
            //dataBaseSaveManager.writeToBD("fileProgramm");
            System.exit(1);
        }catch(IOException e){
            logger.log(Level.WARNING, "Ошибка ввода-вывода");
        //}catch(ClassNotFoundException e){
          //   logger.log(Level.WARNING,"Несоответствующие классы");
        }
    }
    public Request getRequest(SocketChannel clientSocket) throws IOException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 10);
        while (true) {
            try {
                clientSocket.read(buffer);
                buffer.mark();
                byte[] buf = buffer.array();
                ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                ObjectInputStream objectInputStream = new ObjectInputStream(bais);
                return (Request) objectInputStream.readObject();
            } catch (StreamCorruptedException e) {
                // ответ будет позже
            }
        }
    }
    public void openServerSocket() throws OpeningServerException {
        try {
            selector = Selector.open();
            logger.info("Селектор открыт");
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(address));
            serverSocketChannel.configureBlocking(false);
            logger.info("Сервер готов к работе.");
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING,"Неправильные аргументы при старте работы сервера.");
            throw new OpeningServerException();
        } catch (IOException e) {
            logger.log(Level.WARNING,"Ошибка ввода-вывода при старте работы сервера.");
            Console.printError(e.getMessage());
            throw new OpeningServerException();
        }
    }
// отправить ответ, выполняется после исполнения запроса
    public void sendResponse(SocketChannel clientSocket, Response responseToUser) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(responseToUser);
        objectOutputStream.flush();
        clientSocket.write(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
        byteArrayOutputStream.reset();
        clientSocket.close();
        logger.info("Передача ответа клиенту");
    }
    private SocketChannel connectToClient(SelectionKey key) throws IOException{
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel inputSocket = serverSocketChannel.accept();
            inputSocket.configureBlocking(false);
            inputSocket.register(this.selector, SelectionKey.OP_READ);
            logger.info("Создание подключения клиента с сервером.");
            return inputSocket;
        } catch (SocketException e) {
            logger.log(Level.WARNING,"Ошибка с сокетами при подключении сервера и клиента!");
            throw new SocketException("Ошибка с сокетами!");
        } catch (IOException e) {
            logger.log(Level.WARNING,"Ошибка ввода-вывода при подключении сервера и клиента!");
            throw new IOException("Ошибка ввода-вывода");
        }
    }
    // Исполнение команды, которая была вложена в запрос, при этом все действия происходят с моей коллекцией
    public Response executeCommand(Request request) {
        try {
            if (request.getCommandName().equalsIgnoreCase("authorization")){
                if (!dbManager.checkExistUser(request.getUser().getName())){
                    return new Response("authenticationERROR","Пользователя с данным логином не существует! Пройдите регистрацию.");
                }
                if (dbManager.autorizationUser(request.getUser())) {
                    return new Response("Complete", "Вы успешно авторизовались.");
                }else {
                    return new Response("authenticationERROR","Неправильный ввод пароля!");
                    }
                }
            Command command = commandManager.getCommand(request.getCommandName());
            if (command == null) throw new UnknownCommandException();
            if (command instanceof Register){
                    boolean res = dbManager.addUser(request.getUser());
                    if (res) {
                        return new Response("Complete", "Пользователь успешно зарегистрирован и авторизован.");
                    } else
                        return new Response("authenticationError", "Возникли ошибки при регистрации, пользователь с таким именем уже существует, выберите другое.");
                }
            if (!dbManager.checkExistUser(request.getUser().getName())){
                    return new Response("authenticationERROR","Пользователя с данным логином не существует! Пройдите регистрацию.");
                }
            if ((command instanceof InsertAt ||command instanceof UpdateId) && !request.getArgs().isEmpty() && request.getCommandCity()==null) {
                return new Response("GENERATE_CITY", "Создайте объект");
            }else if((command instanceof Add || command instanceof RemoveLower) && request.getArgs().isEmpty() && request.getCommandCity()==null){
                return new Response("GENERATE_CITY","Создайте объект");
            }
            else{
                    commandManager.addToCommandHistory(request.getCommandName());
                    Response response = command.execute(request);//по идее должен
                    logger.info("Успешное выполнение команды.");
                    return response;
                }
        } catch (UnknownCommandException e) {
            logger.log(Level.WARNING,"Неизвестная команда");
            Console.printError(e.getMessage());
            return new Response("error","Неизвестная команда");
        }
    }
}
