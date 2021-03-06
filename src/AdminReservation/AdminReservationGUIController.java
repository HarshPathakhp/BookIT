// Author   : Nihesh Anderson
// Date     : 4 Oct, 2017
// File     : BookIT.java

package AdminReservation;

import HelperClasses.*;
//import com.sun.org.apache.bcel.internal.generic.LoadClass;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.Event;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;

class SlotComparator implements Comparator<String>{

    @Override
    public int compare(String a, String b) {
        if(Reservation.getSlotID(a) < Reservation.getSlotID(b)){
            return -1;
        } else {
            return 1;
        }
    }
}

public class AdminReservationGUIController implements Initializable{
    private HostServices hostservices = null;
	public static String admin_email_used = null;
    private int appearAfter_HoverPane = 200;

    @FXML
    private GridPane roomGrid;
    @FXML
    private StackPane HoverPane;
    @FXML
    private Label RoomNo, joiningCodeMessage, AccRejCourseName, AccRejDate, AccRejVenue, AccRejMessage;
    @FXML
    private Button BackBtn, cancelSlotBooking;
    @FXML
    private Button BookBtn;
    @FXML
    private ImageView logo;
    @FXML
    private StackPane pullDownPane;
    @FXML
    private StackPane roomGridPane, topPane, cancelMessagePane;
    @FXML
    private StackPane classStatus, slotInfoPane, changePasswordPane, joiningCodePane;
    @FXML
    private ImageView classStatusBG, slotStatusBG, changePasswordBG, cancelSlotBookingImage, joiningCodeBG, cancelMessageBG;
    @FXML
    private Label statusRoomID, slotInfo,statusClassSize, statusFreeSlots;
    @FXML
    private StackPane leftPane,rightPane,mainPane, pullDownPane2, allStudentRequests, singleStudentRequest, daysCheckPane;
    @FXML
    private AnchorPane selectedSlotsScrollPane, requestedSlotsScrollPane;
    @FXML
    private Label error1, AccRejSource;
    @FXML
    private ComboBox courseDropDown;
    @FXML
    private DatePicker datePicker, startDate, endDate;
    @FXML
    private Label curDate,curMon,curYear;
    @FXML
    private ChoiceBox joinCodeDropDown;
    @FXML
    private ArrayList<Button> slotButtons;
    @FXML
    private Label slotInfoFaculty, slotInfoCourse, slotInfoMessage, slotInfoReserver;
    @FXML
    private PasswordField oldPass, newPass, renewPass;
    @FXML
    private ComboBox groupDropDown, optionDropDown;
    @FXML
    private TextArea requestMessage, requestMessage2, cancelMessageText;
    @FXML
    private CheckBox mon, tue, wed, thu, fri, sat, sun;

    @FXML
    private VBox rootPane;
    @FXML
    private ComboBox purposeDropDown;
    @FXML
    private StackPane preBooking, courseBooking, otherBooking, HolidayMessage, BlockedDayMessage;
    @FXML
    private TextField purposeBox;
    @FXML
    private ArrayList<LocalDate> date;
    @FXML
    private VBox notificationOverviewBox;


    private String currentPurpose;
    private LocalDate activeDate;
    private String activeRoom;
    private String currentlyShowingSlot;
    private ArrayList<Integer> chosenSlots;
    private Admin activeUser;
    private int pullDownPaneInitial = 650;
    private HashMap<Button,Integer> selection = new HashMap<>();
    private Boolean isActiveReservation,requestProcessing,changepassProcessing, joinCodeProcessing, cancelBookingProcessing;
    private Event classEvent;
    private LocalDate StartDate;
    private LocalDate EndDate;
    private Button[] b1;
    private static int animation = 200;
    private ArrayList<Reservation> currentRequest;
    private ArrayList<String> allCourses;
    private Boolean holiday;
    private Boolean blockedday;

    /**
     * Constructor for setting up Faculty Reservation GUI. It includes the adaptor code to suit any dimensional screen
     */
    @Override
    public void initialize(URL location, ResourceBundle resources){

        // Scaling elements
    	File fileE = new File("./src/AppData/ActiveUser/Email.txt");
    	if(fileE.exists()) {
    	BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(fileE));
			admin_email_used = bf.readLine();
            } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if(bf != null) {
				try {
					bf.close();
					fileE.delete();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    	}
		Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double width = visualBounds.getWidth();
        double height = visualBounds.getHeight();
        double scaleWidth = (width)/1920;
        double scaleHeight = (height)/1037;

        rootPane.setScaleX(scaleWidth);
        rootPane.setScaleY(scaleHeight);

        try {
            Socket server = new Socket(BookITconstants.serverIP, BookITconstants.serverPort);
            ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
            out.writeObject("Pass");        // Takes lock
            out.flush();
            out.writeObject("GetStartEndDate");
            out.flush();
            ArrayList<LocalDate> temp = (ArrayList<LocalDate>) in.readObject();
            StartDate = temp.get(0);
            EndDate = temp.get(1);
            out.close();
            in.close();
            server.close();
        }
        catch (IOException e){
            System.out.println("IO exception occurred while writing to server");
        }
        catch (ClassNotFoundException c){
            System.out.println("Class Not found exception occurred while getting Start and End date of semester");
        }

        activeUser = (Admin) User.getActiveUser();
        joinCodeProcessing = false;
        isActiveReservation = false;
        requestProcessing = false;
        changepassProcessing = false;
        cancelBookingProcessing = false;
        holiday = false;
        blockedday = false;
        allCourses = null;
        File file = new File("./src/BookIT_logo.jpg");
        Image image = new Image(file.toURI().toString());
        logo.setImage(image);
        file = new File("./src/AdminReservation/classStatusBG.jpg");
        image = new Image(file.toURI().toString());
        classStatusBG.setImage(image);
        slotStatusBG.setImage(image);
        changePasswordBG.setImage(image);
        joiningCodeBG.setImage(image);
        cancelMessageBG.setImage(image);
        pullDownPane.setTranslateY(pullDownPaneInitial);
        pullDownPane.setVisible(true);

        optionDropDown.getItems().clear();
        optionDropDown.getItems().add("Course");
        optionDropDown.getItems().add("Other");
        optionDropDown.setValue("Course");

        datePicker.setEditable(false);
        startDate.setEditable(false);
        endDate.setEditable(false);
        datePicker.setValue(LocalDate.now());
        Callback<DatePicker, DateCell> dayCellFactory = dp -> new DateCell()
        {
            @Override
            public void updateItem(LocalDate item, boolean empty)
            {
                super.updateItem(item, empty);

                if(item.isBefore(LocalDate.now()) || item.isBefore(StartDate) || item.isAfter(EndDate))
                {
                    setStyle("-fx-background-color: #ffc0cb;");
                    Platform.runLater(() -> setDisable(true));
                }
            }
        };
        datePicker.setDayCellFactory(dayCellFactory);
        datePicker.setValue(LocalDate.now());
        startDate.setDayCellFactory(dayCellFactory);
        endDate.setDayCellFactory(dayCellFactory);
        activeDate=LocalDate.now();
        setDate(activeDate);
        file = new File("./src/AdminReservation/cancel.png");
        image = new Image(file.toURI().toString());
        cancelSlotBookingImage.setImage(image);
        joinCodeDropDown.getItems().add("Student");
        joinCodeDropDown.getItems().add("Faculty");
        joinCodeDropDown.getSelectionModel().selectFirst();
        joinCodeDropDown.setStyle("-fx-font-size : 13pt;-fx-background-color: #922B21;");
        loadDate();
        generateRoomTableGUI();
    }
    public void generateRoomTableGUI(){
        ArrayList<String> roomlist = Room.getRoomList(false);
        roomGrid.setAlignment(Pos.CENTER);
        roomGrid.setPrefHeight(((double)349/(double)5)*ceil((double)roomlist.size()/(double)4));
        int i=0,j=0;
        roomGrid.setVgap(20);
        while(4*i+j<roomlist.size()){
            while(4*i+j<roomlist.size() && j<4){
                Button temp = new Button();
                temp.setText(roomlist.get(4*i+j));
                temp.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        openBooking(event);
                    }
                });
                temp.getStylesheets().add("/AdminReservation/button.css");
                temp.setPrefWidth(225);
                temp.setPrefHeight(54);
                roomGrid.add(temp,j,i);
                j++;
            }
            j=0;
            i++;
        }
    }
    /**
     * Event handler for downloading a csv of all completed bookings so far
     */
    public void downloadBookings(){
        try{
            Socket server = new Socket(BookITconstants.serverIP, BookITconstants.serverPort);
            ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
            if(false){
                out.writeObject("Hold");
            }
            else{
                out.writeObject("Pass");
            }
            out.flush();
            out.writeObject("admin_getBookingReport");
            out.flush();
            ArrayList<String> c = (ArrayList<String>) in.readObject();
            File f = new File("./Downloads");
            f.mkdirs();
            f = new File("./Downloads/bookings.csv");
            FileWriter file = new FileWriter(f, false);
            for(int i=0; i<c.size();i++){
                file.write(c.get(i)+"\n");
                file.flush();
            }
            file.close();
            out.close();
            in.close();
            server.close();
            Notification.throwAlert("Notification","Successfully generated bookings.csv and moved it to Downloads folder within the BookIT directory");
        }
        catch(IOException e){
            System.out.println("IO Exception occurred while downloading requests");
        }
        catch (ClassNotFoundException c){
            System.out.println("ClassNotFound exception occurred while downloading requests");
        }
    }
    @FXML
    public void launchFeedbackController(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Feedback/Feedback.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            File file = new File("./src/BookIT_icon.jpg");
            stage.getIcons().add(new Image(file.toURI().toString()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            double width = visualBounds.getWidth();
            double height = visualBounds.getHeight();
            double scaleWidth = (width) / 1920;
            double scaleHeight = (height) / 1037;
            stage.setTitle("Feedback");
            stage.setWidth(600 * scaleWidth);
            stage.setHeight(430 * scaleHeight);
            stage.setScene(new Scene(root1, 600 * scaleWidth, 400 * scaleHeight));
            stage.show();
        }
        catch (Exception e){
            System.out.println("Exception occurred while loading feedback fxml");
        }
    }
    @FXML
    public void openManual(){
        File file = new File("./src/AppData/Manual/manual.pdf");
        if(file.exists() == false){
            Notification.throwAlert("Error", "Unable to open the pdf");
            return;
        }
        if(hostservices != null){
            hostservices.showDocument(file.getAbsolutePath());
        }

    }
    /**
     * Event handler for downloading list of pending and active student requests
     */
    public void downloadRequests(){
        try{
            Socket server = new Socket(BookITconstants.serverIP, BookITconstants.serverPort);
            ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
            if(false){
                out.writeObject("Hold");
            }
            else{
                out.writeObject("Pass");
            }
            out.flush();
            out.writeObject("admin_getRequestsQueue");
            out.flush();
            LinkedList<ArrayList<Reservation>> c = (LinkedList<ArrayList<Reservation>>) in.readObject();
            File f = new File("./Downloads");
            f.mkdirs();
            FileWriter file = new FileWriter(new File("./Downloads/Requests.txt"), false);
            int i=1;
            while(!c.isEmpty()){
                ArrayList<Reservation> r = c.get(0);
                c.remove(0);
                file.write("Reservation "+i+" - "+r.get(0).getCreationDate().toString()+"\n");
                file.flush();
                file.write("Sender: "+r.get(0).getReserverEmail()+"\n");
                file.flush();
                file.write("Target Date: "+r.get(0).getTargetDate()+"\n");
                file.flush();
                file.write("Room: "+r.get(0).getRoomName()+"\n");
                file.flush();
                file.write("Course: "+r.get(0).getCourseName()+"\n");
                file.flush();
                file.write("Message: "+r.get(0).getMessageWithoutVenue()+"\n");
                file.flush();
                file.write("Purpose: "+r.get(0).getType()+"\n");
                file.flush();
                file.write("Requested Slots: ");
                file.flush();
                r.forEach(items->{
                    try {
                        file.write(Reservation.getSlotRange(items.getReservationSlot()) + ", ");
                        file.flush();
                    }
                    catch (Exception e){
                        ;
                    }
                });
                file.write("\n\n");
                file.flush();
                i++;
            }
            file.close();
            out.close();
            in.close();
            server.close();
            Notification.throwAlert("Notification","Successfully generated Report.txt and moved it to Downloads folder within the BookIT directory");
        }
        catch(IOException e){
            System.out.println("IO Exception occurred while downloading requests");
        }
        catch (ClassNotFoundException c){
            Notification.throwAlert("Error","There are no pending student requests to download");
            System.out.println("ClassNotFound exception occurred while downloading requests");
        }
    }

    /**
     * Key shortcuts handler
     */
    @FXML
    void keyPressed(KeyEvent event) {
    	KeyCombination kb = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
    	if(kb.match(event)) {
    		signout();
    	}
    	else{
            switch(event.getCode()) {
                case BACK_SPACE:
                    if(requestProcessing){
                        loadStudentRequestList();
                    }
                case ESCAPE:
                    if(pullDownPane.isVisible()){
                        closeReservationPane();
                    }
                    else if(pullDownPane2.isVisible()){
                        hideRequests();
                    }
                    else if(cancelMessagePane.isVisible()){
                        exitCancelBooking();
                    }
                    else if(HoverPane.isVisible()){
                        exitReadOnlyBookings();
                    }
                    break;
                case ENTER:
                    if(HoverPane.isVisible()){
                        pullDownReservationPane();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Event handler for opening a list of notifications in a new GUI
     */
    public void OpenNotifications() {
    	try {
    		User x=User.getActiveUser();
    		if(x.getNotifications(false).size()==0) {
    		    Notification.throwAlert("Information Dialog", "There are no new notifications");
			    return;
    	    }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Notification/Notify.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            File file = new File("./src/BookIT_icon.jpg");
            stage.getIcons().add(new Image(file.toURI().toString()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            double width = visualBounds.getWidth();
            double height = visualBounds.getHeight();
            double scaleWidth = (width)/1920;
            double scaleHeight = (height)/1037;
            stage.setTitle("Notification");
            stage.setWidth(1000*scaleWidth);
            stage.setHeight(666*scaleHeight);
            stage.setScene(new Scene(root1,1000*scaleWidth, 666*scaleHeight));
            stage.show();
    	}
    	catch(Exception e) {
            System.out.println("Error occurred while opening notifications");
        }
    }

    /**
     * Event handler for restarting the server configuration and reloading it without killing active connections
     */
    public void restartServer(){
        rootPane.setDisable(true);
        activeUser.softResetServer(false);
        Notification.throwAlert("Notification","Server has been successfully restarted");
        rootPane.setDisable(false);
    }

    /**
     * Event handler for getting settings.zip
     */
    public void uploadData(){
        rootPane.setDisable(true);
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        try{
            ZipFile zip = new ZipFile(selectedFile);
        }
        catch (ZipException z) {
            Notification.throwAlert("Notification","Unable to parse zip file. Please upload zipped version of settings file.");
            rootPane.setDisable(false);
            return;
        }
        catch(IOException e){
            System.out.println("IO Exception occurred while uploading data");
        }
        try{
            Socket server = new Socket(BookITconstants.serverIP, BookITconstants.serverPort);
            ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
            if(false){
                out.writeObject("Hold");
            }
            else{
                out.writeObject("Pass");
            }
            out.flush();
            out.writeObject("admin_uploadData");
            out.flush();

            // Upload selected file to outstream
            out.writeObject(Files.readAllBytes(selectedFile.toPath()));
            out.flush();

            // Read output
            String error_log = (String) in.readObject();
            File f = new File("./Downloads");
            f.mkdirs();
            f = new File("./Downloads/Upload_log.txt");
            FileWriter file = new FileWriter(f, false);
            file.write(error_log);
            file.close();

            out.close();
            in.close();
            server.close();
        }
        catch(IOException e){
            System.out.println("IO Exception occurred while uploading data");
        }
        catch (ClassNotFoundException c){
            System.out.println("Class not found exception occurred while uploading data");
        }
        Notification.throwAlert("Notification","Upload_log.txt has been generated and moved to Downloads folder within the BookIT directory");
        rootPane.setDisable(false);
    }

    /**
     * Event handler that closes cancel booking interface
     */
    public void exitCancelBooking(){
        cancelBookingProcessing = false;
        leftPane.setDisable(false);
        rightPane.setDisable(false);
        mainPane.setDisable(false);
        cancelMessagePane.setVisible(false);
    }

    /**
     * Event handler to cancel a booked slot
     */
    public void cancelSlotBooking(){
        if(cancelMessageText.getText().equals("")){
            return;
        }
        if(!Notification.throwConfirmation("Warning", "You are about to cancel the booking. Are you sure you want to proceed?")){
            return;
        }
        cancelBookingProcessing = false;
        leftPane.setDisable(false);
        rightPane.setDisable(false);
        mainPane.setDisable(false);
        activeUser.cancelBooking(activeDate,Reservation.getSlotID(currentlyShowingSlot),activeRoom, cancelMessageText.getText().substring(0,min(400, cancelMessageText.getText().length())), admin_email_used,false);
        Button current = slotButtons.get(Reservation.getSlotID(currentlyShowingSlot));
        current.setDisable(false);
        current.setText("Free");
        cancelMessagePane.setVisible(false);
        updateClassStatus(classEvent);
    }

    /**
     * Event handler to generate a new joining code. Not being used anymore
     */
    public void generateCode(){
        try {
            String type = (String) joinCodeDropDown.getSelectionModel().getSelectedItem();
            String joiningCode = activeUser.generateJoincode(type, false);
            joiningCodeMessage.setText(joiningCode);
        }
        catch(Exception e){
            System.out.println("Exception occured in generateCode function");
        }
    }

    /**
     * Event handler that displays the pane where joining code can be generated. Not being used anymore
     */
    public void showJoiningCodePane(){
        joinCodeProcessing = true;
        joiningCodePane.setVisible(true);
        leftPane.setDisable(true);
        rightPane.setDisable(true);
        mainPane.setDisable(true);
        hideLogo();
        FadeTransition appear = new FadeTransition(Duration.millis(animation), joiningCodePane);
        appear.setFromValue(0);
        appear.setToValue(1);
        appear.play();
    }

    /**
     * Event handler that closes the pane where joining code is generated. Not being used anymore.
     */
    public void hideJoiningCodePane(){
        joiningCodeMessage.setText("");
        joinCodeProcessing = false;
        joiningCodePane.setVisible(false);
        leftPane.setDisable(false);
        rightPane.setDisable(false);
        mainPane.setDisable(false);
        showLogo();
    }
    /**
     * Event handler for opening change password pane. Not being used anymore.
     */
    public void openChangePassword(){
        changepassProcessing = true;
        hideLogo();
        leftPane.setDisable(true);
        rightPane.setDisable(true);
        mainPane.setDisable(true);
        FadeTransition appear = new FadeTransition(Duration.millis(animation), changePasswordPane);
        changePasswordPane.setVisible(true);
        appear.setFromValue(0);
        appear.setToValue(1);
        appear.play();
    }
    /**
     * Event handler that closes change password pane. Not being used anymore
     */
    public void cancelChangePassword(){
        oldPass.clear();
        newPass.clear();
        renewPass.clear();
        changepassProcessing = false;
        changePasswordPane.setVisible(false);
        rightPane.setDisable(false);
        leftPane.setDisable(false);
        mainPane.setDisable(false);
        showLogo();
    }
    /**
     * Event handler that saves new password after validation and closes change password pane. Not being used anymore
     */
    public void saveChangePassword(){
        String oldPassString = oldPass.getText();
        String newPassString = newPass.getText();
        String renewPassString = renewPass.getText();
        if(newPassString.equals(renewPassString)) {
            Boolean status = activeUser.changePassword(oldPassString, newPassString, false);
            if(status) {
                changepassProcessing = false;
                leftPane.setDisable(false);
                changePasswordPane.setVisible(false);
                rightPane.setDisable(false);
                mainPane.setDisable(false);
                showLogo();
            }
            else{
                Notification.throwAlert("Error","Either the old password is wrong, or the new passwords don't match");
            }
        }
        oldPass.clear();
        newPass.clear();
        renewPass.clear();
    }

    /**
     * Event handler that sets the selected date on the date pane
     * @param d selected date
     */
    private void setDate(LocalDate d){
        String date = Integer.toString(d.getDayOfMonth());
        String month = Integer.toString(d.getMonthValue());
        String year = Integer.toString(d.getYear());
        if(date.length() == 1){
            date = "0"+date;
        }
        if(month.length() == 1){
            month = "0"+month;
        }
        curDate.setText(date);
        curMon.setText(month);
        curYear.setText(year);
    }

    /**
     * Event handler that reads date from the input field and sets the date into the date pane
     */
    public void loadDate(){
        LocalDate date = datePicker.getValue();
        if(!date.isBefore(StartDate) && !date.isAfter(EndDate)){
            activeDate = date;
            datePicker.setValue(activeDate);
            setDate(activeDate);
        }
        else{
            datePicker.setValue(activeDate);
            setDate(activeDate);
            topPane.setDisable(true);
            mainPane.setDisable(true);
            Notification.throwAlert("Server Error","Sorry, BookIT server is down");
        }
        if(activeUser.isHoliday(activeDate, false)){
            holiday = true;
        }
        else{
            holiday = false;
        }
        if(activeUser.isBlockedDay(activeDate, false)){
            blockedday = true;
        }
        else{
            blockedday = false;
        }
    }

    /**
     * Returns the slot that a time range is mapped to
     * @param buttonID Slot id in the form of string
     * @return Slot index corresponding to the string
     */
    private String getReserveButtonInfo(String buttonID){
        switch(buttonID){
            case "btn1":
                return "0800AM - 0830AM";
            case "btn2":
                return "0830AM - 0900AM";
            case "btn3":
                return "0900AM - 0930AM";
            case "btn4":
                return "0930AM - 1000AM";
            case "btn5":
                return "1000AM - 1030AM";
            case "btn6":
                return "1030AM - 1100AM";
            case "btn7":
                return "1100AM - 1130AM";
            case "btn8":
                return "1130AM - 1200PM";
            case "btn9":
                return "1200PM - 1230PM";
            case "btn10":
                return "1230PM - 0100PM";
            case "btn11":
                return "0100PM - 0130PM";
            case "btn12":
                return "0130PM - 0200PM";
            case "btn13":
                return "0200PM - 0230PM";
            case "btn14":
                return "0230PM - 0300PM";
            case "btn15":
                return "0300PM - 0330PM";
            case "btn16":
                return "0330PM - 0400PM";
            case "btn17":
                return "0400PM - 0430PM";
            case "btn18":
                return "0430PM - 0500PM";
            case "btn19":
                return "0500PM - 0530PM";
            case "btn20":
                return "0530PM - 0600PM";
            case "btn21":
                return "0600PM - 0630PM";
            case "btn22":
                return "0630PM - 0700PM";
            case "btn23":
                return "0700PM - 0730PM";
            case "btn24":
                return "0730PM - 0800PM";
            case "btn25":
                return "0800PM - 0830PM";
            case "btn26":
                return "0830PM - 0900PM";
            case "btn27":
                return "0900PM - 0930PM";
            case "btn28":
                return "0930PM - 1000PM";
        }
        return "";
    }
    public void validateBulkBookingDate(){
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        if(start == null || end == null){
            return;
        }
        if(end.isBefore(start)){
            end = start;
            endDate.setValue(start);
        }
        mon.setDisable(true);
        tue.setDisable(true);
        wed.setDisable(true);
        thu.setDisable(true);
        fri.setDisable(true);
        sat.setDisable(true);
        sun.setDisable(true);
        mon.setSelected(false);
        tue.setSelected(false);
        wed.setSelected(false);
        thu.setSelected(false);
        fri.setSelected(false);
        sat.setSelected(false);
        sun.setSelected(false);
        Boolean init = false;
        int counter = 1;
        while(!start.isAfter(end) && counter<=7){
            counter+=1;
            switch (start.getDayOfWeek().toString().toLowerCase()){
                case "monday":
                    if(!init){
                        init = true;
                        mon.setSelected(true);
                    }
                    else{
                        mon.setSelected(false);
                    }
                    mon.setDisable(false);
                    break;
                case "tuesday":
                    if(!init){
                        init = true;
                        tue.setSelected(true);
                    }
                    else{
                        tue.setSelected(false);
                    }
                    tue.setDisable(false);
                    break;
                case "wednesday":
                    if(!init){
                        init = true;
                        wed.setSelected(true);
                    }
                    else{
                        wed.setSelected(false);
                    }
                    wed.setDisable(false);
                    break;
                case "thursday":
                    if(!init){
                        init = true;
                        thu.setSelected(true);
                    }
                    else{
                        thu.setSelected(false);
                    }
                    thu.setDisable(false);
                    break;
                case "friday":
                    if(!init){
                        init = true;
                        fri.setSelected(true);
                    }
                    else{
                        fri.setSelected(false);
                    }
                    fri.setDisable(false);
                    break;
                case "saturday":
                    if(!init){
                        init = true;
                        sat.setSelected(true);
                    }
                    else{
                        sat.setSelected(false);
                    }
                    sat.setDisable(false);
                    break;
                case "sunday":
                    if(!init){
                        init = true;
                        sun.setSelected(true);
                    }
                    else{
                        sun.setSelected(false);
                    }
                    sun.setDisable(false);
                    break;
            }
            start = start.plusDays(1);
        }
    }
    /**
     * Loads the most prioritized request onto the requests pane
     * @param requests
     */
    private void loadRequest(ArrayList<Reservation> requests){
        Reservation firstRequest = requests.get(0);
        String course = "";
        if(firstRequest.getCourseName().equals("")){
            course = firstRequest.getPurpose();
        }
        else{
            course = firstRequest.getCourseName();
        }
        AccRejCourseName.setText(course);
        String date = Integer.toString(firstRequest.getTargetDate().getDayOfMonth());
        if(date.length()==1){
            date = "0"+date;
        }
        AccRejSource.setText(firstRequest.getReserverEmail());
        AccRejDate.setText(date+"-"+firstRequest.getTargetDate().getMonthValue()+"-"+firstRequest.getTargetDate().getYear());
        AccRejVenue.setText(firstRequest.getVenueName());
        String group = firstRequest.getTopGroup();
        if(group.equals("")){
            group="N/A";
        }
        if(group.equals("0")){
            group.equals("All groups");
        }
        AccRejMessage.setText(firstRequest.getMessage());
        ArrayList<String> items = new ArrayList<String>();
        for(int j=0;j<requests.size();j++){
            items.add(Reservation.getSlotRange(requests.get(j).getReservationSlot()));
        }
        Label[] label = new Label[50];
        b1 = new Button[50];
        int i=0;
        while(i<items.size()){
            label[i] = new Label();
            b1[i] = new Button();
            Label curLabel = label[i];
            Button curButton = b1[i];
            b1[i].getStylesheets().add("./AdminReservation/buttonAccRej.css");
            b1[i].setPrefSize(80,50);
            b1[i].setTranslateY(i*49);
            b1[i].setText("Del");
            curButton.setOnMouseClicked(e->{
                if(curButton.getText().equals("Del")){
                    curButton.setText("Add");
                    curLabel.setStyle("-fx-background-color: red; -fx-border-color:  #2a2a2a; -fx-border-width:3");
                }
                else{
                    curButton.setText("Del");
                    curLabel.setStyle("-fx-background-color: green; -fx-border-color:  #2a2a2a; -fx-border-width:3");
                }
            });
            label[i].setText(items.get(i));
            label[i].setPrefSize(414, 50);
            label[i].setAlignment(Pos.CENTER);
            label[i].setTranslateX(80);
            label[i].setTranslateY(i*49);
            label[i].setStyle("-fx-background-color: green; -fx-border-color:  #2a2a2a; -fx-border-width:3");
            label[i].setFont(new Font(20));
            requestedSlotsScrollPane.getChildren().add(b1[i]);
            requestedSlotsScrollPane.getChildren().add(label[i]);
            i++;
        }
        b1[i] = new Button();
        b1[i].setText("");
        requestedSlotsScrollPane.setPrefSize(494,max(474,49*i));
    }

    /**
     * Accepts a booking requested by the student
     */
    public void acceptRequest(){
        if(currentRequest == null || currentRequest.size()==0){
            Notification.throwAlert("Error", "An unexpected error occurred. Please reload the app and try again");
            return;
        }
        int i=0;
        ArrayList<Reservation> acceptList = new ArrayList<>();
        ArrayList<Reservation> rejectList = new ArrayList<>();
        while(!b1[i].getText().equals("")){
            if(!b1[i].getText().equals("Del")) {
                rejectList.add(currentRequest.get(i));
            }
            else{
                acceptList.add(currentRequest.get(i));
            }
            i++;
        }
        activeUser.acceptRequest(acceptList, rejectList,false);                                             // Throw not accepted warning...
        goBackToStudentList();
    }

    /**
     * Rejects a request requested by the student
     */
    public void deleteRequest(){
        if(currentRequest == null || currentRequest.size()==0){
            Notification.throwAlert("Error", "An unexpected error occurred. Please reload the app and try again");
            return;
        }
        activeUser.rejectRequest(currentRequest, false);
        goBackToStudentList();
    }
    public void goBackToStudentList(){
        if(!loadStudentRequestList()){
            hideRequests();
        }
    }
    public Boolean loadStudentRequestList(){
        LinkedList< ArrayList<Reservation> > requests = activeUser.getRequest(false);              // GUI Integration begins
        if(requests == null || requests.size() == 0){
            Notification.throwAlert("Notification","There are no more pending requests");
            hideRequests();
            return false;
        }
        requestProcessing = true;
        leftPane.setDisable(true);
        rightPane.setDisable(true);
        roomGridPane.setDisable(true);
        allStudentRequests.setVisible(true);
        singleStudentRequest.setVisible(false);
        notificationOverviewBox.getChildren().clear();
        notificationOverviewBox.setStyle("-fx-background-color:  green;");
        notificationOverviewBox.setPadding(new Insets(6,0,6,0));
        notificationOverviewBox.setSpacing(6);
        for(int i=0;i<requests.size();i++){
            String content = "";
            ArrayList<Reservation> r = requests.get(i);
            content+=r.get(0).getReserverEmail()+"\t\t\t\t"+r.get(0).getTargetDate()+"\t\t\t\t"+r.get(0).getRoomName()+"\t\t\t\t"+r.size()+" slots"+"\n\nPurpose: "+r.get(0).getPurpose();
            StackPane s = new StackPane();
            s.setStyle("-fx-background-color: #ADD8E6; -fx-border-width: 2; -fx-border-color: black;");
            s.setPrefSize(1295, 100);
            StackPane l_wrapper = new StackPane();
            Label l = new Label(content);
            l.setTextAlignment(TextAlignment.LEFT);
            l.setStyle("-fx-font-size: 13pt; -fx-font-weight: bold");
            l.setPrefSize(1100, 100);
            l_wrapper.setPadding(new Insets(0,10,0,10));
            l_wrapper.getChildren().add(l);
            StackPane b_wrapper = new StackPane();
            Button b = new Button("View");
            b.setOnMouseClicked(e->{
                currentRequest = r;
                allStudentRequests.setVisible(false);
                singleStudentRequest.setVisible(true);
                loadRequest(r);
            });
            b.getStylesheets().add("./AdminReservation/button_requestview.css");
            b_wrapper.getChildren().add(b);
            s.setMargin(l_wrapper, new Insets(0,200,0,0));
            s.setMargin(b_wrapper, new Insets(0,0,0,1100));
            s.getChildren().add(l_wrapper);
            s.getChildren().add(b_wrapper);
            notificationOverviewBox.getChildren().add(s);
        }
        return true;
    }
    /**
     * Opens requests pane
     */
    public void showRequests(){
        if(!loadStudentRequestList()){
            return;
        }
        startDate.setValue(activeDate);
        endDate.setValue(activeDate);
        validateBulkBookingDate();
        requestedSlotsScrollPane.getChildren().clear();                                                // GUI Integration Ends
        SequentialTransition sequence = new SequentialTransition();
        int step=1;
        int location=pullDownPaneInitial;
        pullDownPane2.setTranslateY(location);
        pullDownPane2.setVisible(true);
        while(location>30) {
            TranslateTransition translate = new TranslateTransition();
            translate.setNode(pullDownPane2);
            translate.setToY(location);
            translate.setDuration(Duration.millis(15));
            step+=2;
            location-=max(step,10);
            sequence.getChildren().add(translate);
        }
        sequence.play();
    }

    /**
     * Logs out the user
     */
    public void signout(){
        try {
            activeUser.logout();
        }
        catch (LoggedOutException l){
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Closes the requests pane
     */
    public void hideRequests(){
        requestProcessing = false;
        SequentialTransition sequence = new SequentialTransition();
        int step=1;
        double location=pullDownPane2.getTranslateY();
        while(location<pullDownPaneInitial+20) {
            TranslateTransition translate = new TranslateTransition();
            translate.setNode(pullDownPane2);
            translate.setToY(location);
            translate.setDuration(Duration.millis(15));
            step++;
            location+=step;
            sequence.getChildren().add(translate);
        }
        sequence.play();
        sequence.setOnFinished(e->{
            pullDownPane2.setVisible(false);
            leftPane.setDisable(false);
            rightPane.setDisable(false);
            roomGridPane.setDisable(false);
        });
    }

    /**
     * Displays details attached to the slot on the top center pane
     * @param e Event object
     */
    public void showSlotInfo(Event e){
        Reservation[] bookings = Room.getDailySchedule(activeDate, statusRoomID.getText(), false);
        if(bookings == null){
            return;
        }
        slotInfoPane.setVisible(true);
        Label curLabel = (Label) e.getSource();
        slotInfo.setText(curLabel.getText());
        currentlyShowingSlot = curLabel.getText();         // GUI-Helper Integration starts
        if(bookings[Reservation.getSlotID(curLabel.getText())]!=null) {
            cancelSlotBooking.setDisable(false);
            String facultyName="~~~~";
            if (!bookings[Reservation.getSlotID(curLabel.getText())].getFacultyEmail(false).equals("")){
                Faculty f = (Faculty)User.getUser(bookings[Reservation.getSlotID(curLabel.getText())].getFacultyEmail(false), false);
                if(f!=null) {
                    facultyName = f.getName();
                }
            }
            slotInfoFaculty.setText(facultyName);
            String reserver = bookings[Reservation.getSlotID(curLabel.getText())].getReserverEmail();
            if(reserver.equals(BookITconstants.NoReplyEmail)){
                reserver = "Admin";
            }
            slotInfoReserver.setText(reserver);
            if(bookings[Reservation.getSlotID(curLabel.getText())].getCourseName().length()>30) {
                slotInfoCourse.setText(bookings[Reservation.getSlotID(curLabel.getText())].getCourseName().substring(0,15)+"..."+bookings[Reservation.getSlotID(curLabel.getText())].getCourseName().substring(bookings[Reservation.getSlotID(curLabel.getText())].getCourseName().length()-10,bookings[Reservation.getSlotID(curLabel.getText())].getCourseName().length()));
            }
            else{
                slotInfoCourse.setText(bookings[Reservation.getSlotID(curLabel.getText())].getCourseName());
            }
            slotInfoMessage.setText(bookings[Reservation.getSlotID(curLabel.getText())].getCourseName()+"\n"+bookings[Reservation.getSlotID(curLabel.getText())].getMessage());
        }
        else{
            cancelSlotBooking.setDisable(true);
            slotInfoFaculty.setText("N/A");
            slotInfoCourse.setText("N/A");
            slotInfoMessage.setText("N/A");
            slotInfoReserver.setText("N/A");
        }                                                               // GUI-Helper Integration ends
    }

    /**
     * Hides the pane that shows information regarding a slot
     */
    private void hideSlotPane(){
        slotInfoPane.setVisible(false);
    }

    /**
     * Displays the pane describing the class room
     * @param e Event object
     */
    public void updateClassStatus(Event e){
        hideLogo();
        hideSlotPane();
        statusRoomID.setText(activeRoom);
        int capacity = Room.getCapacity(activeRoom,false);                                  // GUI-Helper integration begins here
        statusClassSize.setText("  "+Integer.toString(capacity));
        Reservation[] reservation = Room.getDailySchedule(activeDate,activeRoom, false);
        if(reservation == null){
            showLogo();
            exitReadOnlyBookings();
            return;
        }
        int freeSlots=0;
        for(int i=0;i<28;i++){
            if(reservation[i] == null){
                freeSlots++;
            }
        }
        statusFreeSlots.setText("  "+Integer.toString(freeSlots));                         // GUI-Helper integration ends here
        FadeTransition appear = new FadeTransition(Duration.millis(animation), classStatus);
        classStatus.setOpacity(0);
        classStatus.setVisible(true);
        if(holiday){
            HolidayMessage.setVisible(true);
        }
        else{
            HolidayMessage.setVisible(false);
        }
        if(blockedday){
            BlockedDayMessage.setVisible(true);
            HolidayMessage.setVisible(false);
        }
        else{
            BlockedDayMessage.setVisible(false);
        }
        appear.setFromValue(0);
        appear.setToValue(1);
        appear.play();
    }

    /**
     * Hides the pane that desplays the description of the class room
     */
    private void closeClassStatus(){
        if(classStatus.isVisible()) {
            hideSlotPane();
            classStatus.setVisible(false);
            HolidayMessage.setVisible(false);
            BlockedDayMessage.setVisible(false);
            showLogo();
        }
    }

    /**
     * Shows BookIT logo
     */
    private void showLogo(){
        FadeTransition appear = new FadeTransition(Duration.millis(animation), logo);
        logo.setOpacity(0);
        logo.setVisible(true);
        appear.setFromValue(0);
        appear.setToValue(1);
        appear.play();
    }

    /**
     * Hides BookIT logo
     */
    private void hideLogo(){
        logo.setVisible(false);
    }

    /**
     * Sleeps for some time in milli seconds
     * @param time
     */
    private void induceDelay(long time){
        try {
            Thread.sleep(time);
        }
        catch(Exception e){
            System.out.println("Error in AdminReservationGUIController: InduceDelay");
        }
    }

    /**
     * Adds the selected slot to selected list so that booking can be performed later
     * @param e
     */
    public void addSlotToBookQueue(Event e){
        hideSlotPane();
        Button currentBtn = (Button) e.getSource();
        if(currentBtn.getText().equals("")){
            currentBtn.setText("Free");
            currentBtn.setStyle("-fx-background-color:  #424949");
            selection.remove(currentBtn);
        }
        else{
            selection.put(currentBtn,1);
            currentBtn.setText("");
            currentBtn.setStyle("-fx-background-color:  linear-gradient(#229954,#27AE60,#229954)");
        }
        if(selection.size()==0){
            BookBtn.setDisable(true);
            BookBtn.setOpacity(1);
            BookBtn.setVisible(true);
            error1.setVisible(true);
        }
        else{
            BookBtn.setVisible(true);
            BookBtn.setOpacity(1);
            BookBtn.setDisable(false);
            error1.setVisible(false);
        }
    }

    /**
     * Booking confirmation pane disappears
     */
    public void closeReservationPane(){
        isActiveReservation = false;
        hideSlotPane();
        SequentialTransition sequence = new SequentialTransition();
        int step=1;
        double location=pullDownPane.getTranslateY();
        while(location<pullDownPaneInitial+20) {
            TranslateTransition translate = new TranslateTransition();
            translate.setNode(pullDownPane);
            translate.setToY(location);
            translate.setDuration(Duration.millis(15));
            step++;
            location+=max(20,step);
            sequence.getChildren().add(translate);
        }
        sequence.play();
        FadeTransition appearBookBtn = new FadeTransition(Duration.millis(animation), BookBtn);
        appearBookBtn.setToValue(1);
        FadeTransition appearBackBtn = new FadeTransition(Duration.millis(animation), BackBtn);
        appearBackBtn.setToValue(1);
        ParallelTransition inParallel = new ParallelTransition(appearBookBtn, appearBackBtn);
        inParallel.play();
        sequence.setOnFinished(e->{
            pullDownPane.setTranslateY(pullDownPaneInitial);
            rightPane.setDisable(false);
            leftPane.setDisable(false);
            HoverPane.setDisable(false);
            roomGridPane.setDisable(false);
            roomGridPane.setVisible(true);
            pullDownPane.setVisible(false);
        });
    }

    /**
     * Booking confirmation pane appears
     */
    public void pullDownReservationPane(){
        startDate.setValue(activeDate);
        endDate.setValue(activeDate);
        validateBulkBookingDate();
        courseBooking.setVisible(false);
        otherBooking.setVisible(false);
        preBooking.setVisible(true);
        chosenSlots = new ArrayList<>();
        isActiveReservation = true;
        hideSlotPane();
        HoverPane.setDisable(true);
        rightPane.setDisable(true);
        leftPane.setDisable(true);
        roomGridPane.setDisable(true);
        roomGridPane.setVisible(false);
        pullDownPane.setVisible(true);
        Label[] label = new Label[30];
        ArrayList<String> items = new ArrayList<>();
        selection.forEach((btn, num)->{
            items.add(getReserveButtonInfo(btn.getId()));
        });
        items.sort(new SlotComparator());
        selectedSlotsScrollPane.getChildren().clear();
        int i=0;
        while(i<items.size()){
            label[i] = new Label();
            label[i].setText(items.get(i));
            label[i].setPrefSize(494, 50);
            chosenSlots.add(Reservation.getSlotID(items.get(i)));
            label[i].setAlignment(Pos.CENTER);
            label[i].setTranslateY(i*50);
            label[i].setStyle("-fx-background-color: white; -fx-border-color:  #2a2a2a; -fx-border-width:3");
            label[i].setFont(new Font(22));
            selectedSlotsScrollPane.getChildren().add(label[i]);
            i++;
        }
        selectedSlotsScrollPane.setPrefSize(494,max(474,50*i));
        if(allCourses == null) {
            allCourses = Course.getAllCourses();
            allCourses.sort(String::compareToIgnoreCase);
        }
        courseDropDown.getItems().clear();
        purposeDropDown.getItems().clear();
        groupDropDown.getItems().clear();
        for(int j=0;j<allCourses.size();j++) {
            courseDropDown.getItems().add(allCourses.get(j));
        }
        new AutoCompleteComboBoxListener<>(courseDropDown);
        purposeDropDown.getItems().add("Lecture");
        purposeDropDown.getItems().add("Lab");
        purposeDropDown.getItems().add("Tutorial");
        purposeDropDown.getItems().add("Quiz");
        for(int j=0;j<6;j++) {
            groupDropDown.getItems().add(Integer.toString(j+1));
        }
        SequentialTransition sequence = new SequentialTransition();
        int step=1;
        int location=pullDownPaneInitial;
        while(location>30) {
            TranslateTransition translate = new TranslateTransition();
            translate.setNode(pullDownPane);
            translate.setToY(location);
            translate.setDuration(Duration.millis(15));
            step+=2;
            location-=max(10,step);
            sequence.getChildren().add(translate);
        }
        sequence.play();
        FadeTransition appearBookBtn = new FadeTransition(Duration.millis(animation), BookBtn);
        appearBookBtn.setToValue(0);
        FadeTransition appearBackBtn = new FadeTransition(Duration.millis(animation), BackBtn);
        appearBackBtn.setToValue(0);
        ParallelTransition inParallel = new ParallelTransition(appearBookBtn, appearBackBtn);
        inParallel.play();
    }

    /**
     * Event handler for confirming booking of a room
     */
    public void bookingCompleted1(){
        String chosenCourse;
        try {
            chosenCourse = courseDropDown.getSelectionModel().getSelectedItem().toString();
        }
        catch(NullPointerException e){
            Notification.throwAlert("Error","Course Field can't be empty");
            return;
        }
        if(!allCourses.contains(chosenCourse)){
            Notification.throwAlert("Error", "This course can't be chosen! Ensure to choose only those courses available in the drop down box");
            return;
        }
        ArrayList<String> chosenGroup = new ArrayList<>();
        try {
            chosenGroup.add(groupDropDown.getSelectionModel().getSelectedItem().toString());
        }
        catch(NullPointerException e){
            chosenGroup.add("0");
        }
        String chosenPurpose;
        try {
            chosenPurpose = purposeDropDown.getSelectionModel().getSelectedItem().toString();
        }
        catch(NullPointerException e){
            Notification.throwAlert("Error","Purpose Field can't be empty");
            return;
        }
        String chosenFaculty;
        if(chosenCourse == ""){
            chosenFaculty = "";
        }
        else{
            chosenFaculty = Course.getCourseFaculty(chosenCourse, false);
        }
        String chosenMessage;
        chosenMessage = requestMessage.getText();
        ArrayList<Reservation> listOfReservations = new ArrayList<>();
        LocalDateTime creat_time = LocalDateTime.now();
        for(int i=0;i<chosenSlots.size();i++){              // GUI Integration Begins
            Reservation r;
            r = new Reservation(chosenMessage, chosenGroup, chosenCourse, chosenFaculty, activeRoom, chosenPurpose, chosenSlots.get(i));
            r.setTargetDate(activeDate);
            r.setCreationDate(creat_time);
            r.setReserverEmail(activeUser.getEmail().getEmailID());
            listOfReservations.add(r);
        }                                                   // GUI Integration Ends
        if(!Admin.checkBulkBooking(activeRoom, chosenSlots, date, false)){
            Notification.throwAlert("Error","Cannot complete booking as there is some other confirmed booking in one of the slots that you are trying to book");
            return;
        }
        Boolean failure = false;
        ArrayList<Integer> slots = new ArrayList<>();
        for(int i=0;i<listOfReservations.size();i++){
            slots.add(listOfReservations.get(i).getReservationSlot());
        }
        if(!activeUser.bookRoom(date, slots, listOfReservations.get(0),admin_email_used ,false)){
            failure = true;
        }
        if(failure){
            Notification.throwAlert("Booking Failed", "Some bookings couldn't be completed. Kindly check notifications for successful bookings");
        }
        closeReservationPane();
        flyRight();
        requestMessage.clear();
    }

    public void bookingCompleted2(){
        String chosenCourse="";
        ArrayList<String> chosenGroup = new ArrayList<>();
        chosenGroup.add("0");
        String chosenPurpose;
        chosenPurpose = purposeBox.getText();
        if(chosenPurpose.equals("")){
            Notification.throwAlert("Error","Purpose field can't be empty");
            return;
        }
        String chosenFaculty="";
        String chosenMessage;
        chosenMessage = requestMessage2.getText();
        ArrayList<Reservation> listOfReservations = new ArrayList<>();
        for(int i=0;i<chosenSlots.size();i++){              // GUI Integration Begins
            Reservation r;
            r = new Reservation(chosenMessage, chosenGroup, chosenCourse, chosenFaculty, activeRoom, chosenPurpose, chosenSlots.get(i));
            r.setTargetDate(activeDate);
            r.setReserverEmail(activeUser.getEmail().getEmailID());
            listOfReservations.add(r);
        }                                                   // GUI Integration Ends
        if(!Admin.checkBulkBooking(activeRoom, chosenSlots, date, false)){
            Notification.throwAlert("Cannot complete booking. Please close this session and try again", "Error");
            return;
        }
        ArrayList<Integer> slots = new ArrayList<>();
        for(int i=0;i<listOfReservations.size();i++){
               slots.add(listOfReservations.get(i).getReservationSlot());
        }
        if(!activeUser.bookRoom(date, slots, listOfReservations.get(0), admin_email_used,false)){
            Notification.throwAlert("Booking Error","The booking couldn't be completed as one of the slots you've chosen has been booked by someone else");
        }
        closeReservationPane();
        flyRight();
        purposeBox.clear();
        requestMessage2.clear();
    }
    public void cancelBookingMessage(){
        cancelBookingProcessing = true;
        leftPane.setDisable(true);
        rightPane.setDisable(true);
        mainPane.setDisable(true);
        slotInfoPane.setVisible(false);
        FadeTransition appear = new FadeTransition(Duration.millis(animation), cancelMessagePane);
        cancelMessagePane.setOpacity(0);
        cancelMessagePane.setVisible(true);
        appear.setFromValue(0);
        appear.setToValue(1);
        appear.play();
    }
    /**
     * Reservation pane flys right
     */
    public void flyRight(){
        FadeTransition sequence = new FadeTransition(Duration.millis(animation), HoverPane);
        sequence.setToValue(0);
        sequence.play();
        closeClassStatus();
        rightPane.setDisable(false);
        leftPane.setDisable(false);
        sequence.setOnFinished(e->{
            exitReadOnlyBookings();
        });
    }

    /**
     * Resercation pane appears
     * @param action Event object
     */
    public void openBooking(Event action){
        Button current = (Button) action.getSource();
        activeRoom = current.getText();
        Boolean check = Room.exists(current.getText(),false);                               // Loading buttons
        if(check==false){
            return;
        }
        Reservation[] reservation = Room.getDailySchedule(activeDate, current.getText(), false);
        if(reservation == null){
            return;
        }
        selection.clear();
        updateClassStatus(action);
        classEvent = action;
        HoverPane.setTranslateX(0);
        datePicker.setVisible(false);
        error1.setVisible(true);
        BookBtn.setDisable(true);
        BackBtn.setVisible(true);
        BookBtn.setVisible(true);
        BookBtn.setOpacity(0);
        BackBtn.setOpacity(0);
        RoomNo.setText(current.getText());
        for(int i=0;i<28;i++){
            if(reservation[i] != null){
                slotButtons.get(i).setText("Booked");
                slotButtons.get(i).setDisable(true);
            }
            else{
                slotButtons.get(i).setText("Free");
                slotButtons.get(i).setDisable(false);
            }
        }                                                                               // Loading ends
        induceDelay(appearAfter_HoverPane);
        HoverPane.setVisible(true);
        HoverPane.setDisable(false);
        FadeTransition appear = new FadeTransition(Duration.millis(animation), HoverPane);
        appear.setToValue(1);
        FadeTransition appearBookBtn = new FadeTransition(Duration.millis(animation), BookBtn);
        appearBookBtn.setToValue(1);
        FadeTransition appearBackBtn = new FadeTransition(Duration.millis(animation), BackBtn);
        appearBackBtn.setToValue(1);
        ParallelTransition inParallel = new ParallelTransition(appear, appearBookBtn, appearBackBtn);
        inParallel.play();
    }
    public void preBookingProceed(){
        try {
            if(startDate.getValue().isAfter(endDate.getValue())){
                Notification.throwAlert("Error","Start Date is after End Date");
                return;
            }
            ArrayList<Integer> daysSelected = new ArrayList<>();
            if(mon.isSelected()){
                daysSelected.add(1);
            }
            if(tue.isSelected()){
                daysSelected.add(2);
            }
            if(wed.isSelected()){
                daysSelected.add(3);
            }
            if(thu.isSelected()){
                daysSelected.add(4);
            }
            if(fri.isSelected()){
                daysSelected.add(5);
            }
            if(sat.isSelected()){
                daysSelected.add(6);
            }
            if(sun.isSelected()){
                daysSelected.add(7);
            }
            date = new ArrayList<>();
            LocalDate temp = LocalDate.of(startDate.getValue().getYear(), startDate.getValue().getMonth(), startDate.getValue().getDayOfMonth());
            while(!temp.isAfter(endDate.getValue())){
                if(daysSelected.contains(temp.getDayOfWeek().getValue())){
                    date.add(temp);
                }
                temp = temp.plusDays(1);
            }
            if(date.size() == 0){
                Notification.throwAlert("Error", "No date has been selected. All the days in the selected date range will be booked");
                return;
            }
            if (!Admin.checkBulkBooking(activeRoom,chosenSlots, date, false)) {
                Notification.throwAlert("Error","There's another confirmed booking in the selected range of slots. Try a different range");
                return;
            }
            currentPurpose = optionDropDown.getSelectionModel().getSelectedItem().toString();
            preBooking.setVisible(false);
            if(currentPurpose.equals("Course")){
                courseBooking.setVisible(true);
            }
            else{
                otherBooking.setVisible(true);
            }
        }
        catch (Exception e){
            System.out.println("No option has been selected case in preBookingProceed function");
            return;
        }
    }
    /**
     * Reservation pane appears, but it remains disabled
     * @param action Event object
     */
    public void showReadOnlyBookings(Event action){
        Button current = (Button) action.getSource();
        Boolean check = Room.exists(current.getText(),false);                               // Loading buttons
        if(check==false){
            return;
        }
        updateClassStatus(action);
        HoverPane.setTranslateX(0);
        BackBtn.setVisible(false);
        BookBtn.setVisible(false);
        double opacitySaturation = 0.92;
        RoomNo.setText(current.getText());
        Reservation[] reservation = Room.getDailySchedule(activeDate, current.getText(), false);
        for(int i=0;i<28;i++){
            if(reservation[i] != null){
                slotButtons.get(i).setText("Booked");
                slotButtons.get(i).setDisable(true);
            }
            else{
                slotButtons.get(i).setText("Free");
                slotButtons.get(i).setDisable(false);
            }
        }                                                                               // Loading ends
        induceDelay(appearAfter_HoverPane);
        HoverPane.setVisible(true);
        HoverPane.setDisable(true);
        FadeTransition appear = new FadeTransition(Duration.millis(animation), HoverPane);
        if(HoverPane.getOpacity()==opacitySaturation){
            appear.setFromValue(0.6);
        }
        else {
            appear.setFromValue(0);
        }
        appear.setToValue(opacitySaturation);
        appear.play();
    }

    /**
     * Closes disabled reservation pane
     */
    public void exitReadOnlyBookings(){
        if(!isActiveReservation && !requestProcessing) {
            FadeTransition appear = new FadeTransition(Duration.millis(animation), HoverPane);
            appear.setToValue(0);
            appear.play();
            datePicker.setVisible(true);
            closeClassStatus();
            selection.forEach((currentBtn, val) -> {
                currentBtn.setText("Free");
                currentBtn.setStyle("-fx-background-color:  #424949;");
            });
            selection = new HashMap<Button, Integer>();
            appear.setOnFinished(e -> {
                HoverPane.setVisible(false);
                HoverPane.setDisable(false);
                HoverPane.setOpacity(1);
                rightPane.setDisable(false);
                leftPane.setDisable(false);
                RoomNo.setText("Not Set");
                if(changepassProcessing){
                    leftPane.setDisable(true);
                    rightPane.setDisable(true);
                }
                if(requestProcessing){
                    leftPane.setDisable(true);
                    rightPane.setDisable(true);
                    pullDownPane2.setVisible(true);
                }
                if(joinCodeProcessing){
                    leftPane.setDisable(true);
                    rightPane.setDisable(true);
                    mainPane.setDisable(true);
                }
                if(cancelBookingProcessing){
                    leftPane.setDisable(true);
                    rightPane.setDisable(true);
                    mainPane.setDisable(true);
                }
            });
        }
    }
    public void setHostservices(HostServices hostservices) {
        this.hostservices = hostservices;
    }


}
