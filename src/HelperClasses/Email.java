package HelperClasses;
import java.io.*;
import java.net.Socket;

/**
 * The Email class that is integrated with the user classes
 * contains the email of a user and has methods to authenticate access of a user
 * @author Harsh Pathak
 *	@since 27-10-2017
 */
public class Email implements Serializable{
	private static final long serialVersionUID = 1L;
	private static  String domain="@iiitd.ac.in";
	private static String check="@@iiitd.ac.in";
	private String emailID;
	/**
	 * constructor for the email class
	 * @param x string denoting the email address 
	 */
	public Email(String x) {
		StringBuilder y=new StringBuilder();
		for(int i=0;i<x.length();i++) {
			if(!(x.charAt(i)==' ')) {
				y.append(x.charAt(i));
			}
		}
		emailID=y.toString();
	}
	/**
	 * method for validating sign-up of user.
	 * Validations follows rules such as being a valid iiitd email beside other rules 
	 * @return returns 0 if the email entered is not in the database , 1 if it exists 2 if it doesn'tfollow the nomenclature
	 */
	public int validateSignup(Boolean lock) {
		StringBuilder x=new StringBuilder();
		for(int i=0;i<emailID.length();i++) {
			if(!(emailID.charAt(i)==' ')) {
				x.append(emailID.charAt(i));
			}
		}
		String y=x.toString();
		boolean exists=true;
		try{
			Socket server = new Socket(BookITconstants.serverIP, BookITconstants.serverPort);
			ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(server.getInputStream());
			if(lock){
				out.writeObject("Hold");
			}
			else{
				out.writeObject("Pass");
			}
			out.flush();
			out.writeObject("validateLogin");
			out.flush();
			out.writeObject(y);
			out.flush();
			Boolean c = (Boolean) in.readObject();
			out.close();
			in.close();
			server.close();
			exists = c;
		}
		catch(IOException e){
			System.out.println("IO Exception occurred while booking room");
		}
		catch (ClassNotFoundException c){
			System.out.println("Class not found exception occurred while booking room");
		}
		if(exists) {
			return 1; //user already exists
		}
		if(y.contains(domain) && 
				!y.contains(check) && 
				(y.indexOf(domain)+12==y.length())) {
			String[] temp=y.split("@");
			if(temp[1].equals("iiitd.ac.in") && temp[0].length()>1) {
				if(Character.isLetter(temp[0].charAt(0))){
					if(temp[0].matches("[A-Za-z0-9]+")) {
						this.emailID=y;
						return 0; //okay proceed
					}}
		}
		}
		return 2; //bad email including special characters or missing @
	}
	/**
	 * used to validate the email of a user by checking for the email in the database
	 * @return true if email was found in the user database else returns false
	 */
	public boolean validateLogin(Boolean lock) {
		StringBuilder x=new StringBuilder();
		for(int i=0;i<emailID.length();i++) {
			if(!(emailID.charAt(i)==' ')) {
				x.append(emailID.charAt(i));
			}
		}
		String y=x.toString();
		try{
			Socket server = new Socket(BookITconstants.serverIP, BookITconstants.serverPort);
			ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(server.getInputStream());
			if(lock){
				out.writeObject("Hold");
			}
			else{
				out.writeObject("Pass");
			}
			out.flush();
			out.writeObject("validateLogin");
			out.flush();
			out.writeObject(y);
			out.flush();
			Boolean c = (Boolean) in.readObject();
			out.close();
			in.close();
			server.close();
			return c;
		}
		catch(IOException e){
			System.out.println("IO Exception occurred while booking room");
		}
		catch (ClassNotFoundException c){
			System.out.println("Class not found exception occurred while booking room");
		}
		return false;
	}	
	/**
	 * getter method for emailID
	 * @return String 
	 */
	public String getEmailID() {
		return emailID;
	}
	
	
}
