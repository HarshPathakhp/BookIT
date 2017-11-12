import HelperClasses.BookITconstants;
import HelperClasses.Course;
import HelperClasses.Room;
import HelperClasses.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by nihesh on 12/11/17.
 */
public class server {
    public static void main(String[] args)throws IOException{
        ServerSocket s = new ServerSocket(BookITconstants.serverPort);
        while(true){
            Socket connection = s.accept();
            new Thread(new ConnectionHandler(connection)).start();
        }
    }
}

class ConnectionHandler implements Runnable{
    private Socket connection;
    public ConnectionHandler(Socket connection){
        this.connection = connection;
    }
    public Course deserializeCourse(String name){
        ObjectInputStream in = null;
        try{
            in = new ObjectInputStream(new FileInputStream("./src/AppData/Course/"+name+".dat"));
            return (Course)in.readObject();
        }
        catch (Exception e){
            System.out.println("Exception occured while deserialising Course");
            return null;
        }
        finally {
            try {
                if (in != null)
                    in.close();
            }
            catch(IOException f){
                ;
            }
        }
    }
    public ArrayList<String> getAllCourses(){
        File directory= new File("./src/AppData/Course");
        ArrayList<String> courses = new ArrayList<>();
        File[] courseFiles=directory.listFiles();
        for(int i=0;i<courseFiles.length;i++) {
            String courseName = courseFiles[i].getName().substring(0, courseFiles[i].getName().length() - 4);
            courses.add(courseName);
        }
        return courses;
    }
    public void serializeCourse(Course c){
        try{
            ObjectOutputStream out = null;
            try{
                out = new ObjectOutputStream(new FileOutputStream("./src/AppData/Course/"+c.getName()+".dat", false));
                out.writeObject(c);
            }
            finally {
                if(out!=null){
                    out.close();
                }
            }
        }
        catch (IOException e){
            ;
        }
    }
    public void serializeUser(User u){
        try{
            ObjectOutputStream out = null;
            try{
                out = new ObjectOutputStream(new FileOutputStream("./src/AppData/User/"+u.getEmail().getEmailID()+".txt", false));
                out.writeObject(u);
            }
            finally {
                if(out!=null){
                    out.close();
                }
            }

        }
        catch (IOException e){
            System.out.println("file not found");
        }
    }
    public User getUser(String email){
        ObjectInputStream in = null;
        try{
            in = new ObjectInputStream(new FileInputStream("./src/AppData/User/"+email+".txt"));
            return (User)in.readObject();
        }
        catch (Exception e){
            System.out.println("Exception occured while deserialising User");
            return null;
        }
        finally {
            try {
                if (in != null)
                    in.close();
            }
            catch(IOException f){
                ;
            }
        }
    }
    public Room deserializeRoom(String name){
        ObjectInputStream in = null;
        try{
            in = new ObjectInputStream(new FileInputStream("./src/AppData/Room/"+name+".dat"));
            return (Room)in.readObject();
        }
        catch (Exception e){
            System.out.println("Exception occured while deserialising Room");
            return null;
        }
        finally {
            try {
                if (in != null)
                    in.close();
            }
            catch(IOException f){
                ;
            }
        }
    }
    public void serializeRoom(Room r){
        try{
            ObjectOutputStream out = null;
            try{
                out = new ObjectOutputStream(new FileOutputStream("./src/AppData/Room/"+r.getRoomID()+".dat", false));
                out.writeObject(r);
            }
            finally {
                if(out!=null){
                    out.close();
                }
            }

        }
        catch (IOException e){
            ;
        }
    }
    public void run(){
        try{
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
            String request = (String)in.readObject();
            System.out.println("Connected to "+connection.getInetAddress().toString()+" | Performing "+request);
            Course c;
            User u;
            Room r;
            switch (request){
                case "ReadCourse":
                    String courseName = (String)in.readObject();
                    c = deserializeCourse(courseName);
                    out.writeObject(c);
                    out.flush();
                    break;
                case "AllCourses":
                    ArrayList<String> arr = getAllCourses();
                    out.writeObject(arr);
                    out.flush();
                    break;
                case "WriteCourse":
                    c = (Course)in.readObject();
                    serializeCourse(c);
                    break;
                case "GetUser":
                    String email = (String)in.readObject();
                    u = getUser(email);
                    out.writeObject(u);
                    out.flush();
                    break;
                case "WriteUser":
                    u = (User)in.readObject();
                    serializeUser(u);
                    break;
                case "ReadRoom":
                    String roomName = (String)in.readObject();
                    r = deserializeRoom(roomName);
                    out.writeObject(r);
                    out.flush();
                    break;
                case "WriteRoom":
                    r = (Room) in.readObject();
                    serializeRoom(r);
                    break;
            }
            in.close();
            out.close();
        }
        catch (IOException e){
            System.out.println("Error Occured while handling connection");
        }
        catch (ClassNotFoundException e){
            System.out.println("Error Occured while handling connection");
        }
    }
}
