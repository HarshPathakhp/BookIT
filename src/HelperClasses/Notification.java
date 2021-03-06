package HelperClasses;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Notification implements Serializable{
	private static final long serialVersionUID = 1L;
    private String type="";
    private String status="";
	private String message="";
    private String course="";
    private ArrayList<LocalDate> targetDate;
    private String room="";
    private String reserverEmail="";
    private ArrayList<Integer> slotIDs;
    private String cancelledBy = null;
	private String reason_cancel = null;

	public String getReason_cancel() {
		return reason_cancel;
	}

	public void setReason_cancel(String reason_cancel) {
		this.reason_cancel = reason_cancel;
	}

	public String getCancelledBy() {
		return cancelledBy;
	}

	public void setCancelledBy(String cancelledBy) {
		this.cancelledBy = cancelledBy;
	}

	public LocalDate getMax_targetdate() {
		return max_targetdate;
	}

	private LocalDate max_targetdate = null;
	public LocalDateTime getReservationStamp() {
		return ReservationStamp;
	}

	private LocalDateTime ReservationStamp = null;
	private LocalDateTime NotificationDateTime = LocalDateTime.now();
	public Notification(String type, String status, String message, String course, ArrayList<LocalDate> targetDate,
						String room, String reserverEmail, ArrayList<Integer> slotID, LocalDateTime reservationtime) {
		if(type!=null) {
			this.type = type;}
		if(status!=null) {
			this.status = status;}
		if(message!=null) {
			this.message = message;}
		if(course!=null) {
			this.course = course;}
		if(targetDate!=null) {
			this.targetDate = targetDate;}
		if(room!=null) {
			this.room = room;}
		this.reserverEmail = reserverEmail;
		this.slotIDs = slotID;
		this.ReservationStamp =  reservationtime;
		max_targetdate = this.targetDate.get(0);
		for (LocalDate date: this.targetDate) {
			if(max_targetdate.isBefore(date)){
				max_targetdate = date;
			}
		}
	}
	public Notification(String type, String status, String message, String course, ArrayList<LocalDate> targetDate,
						String room, String reserverEmail, ArrayList<Integer> slotID) {
		if (type != null) {
			this.type = type;
		}
		if (status != null) {
			this.status = status;
		}
		if (message != null) {
			this.message = message;
		}
		if (course != null) {
			this.course = course;
		}
		if (targetDate != null) {
			this.targetDate = targetDate;
		}
		if (room != null) {
			this.room = room;
		}
		this.reserverEmail = reserverEmail;
		this.slotIDs = slotID;
		max_targetdate = this.targetDate.get(0);
		for (LocalDate date: this.targetDate) {
			if(max_targetdate.isBefore(date)){
				max_targetdate = date;
			}
		}
	}
	public static void throwAlert(String title, String message){
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	public static Boolean throwConfirmation(String title, String message){
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
		return alert.getResult() == ButtonType.OK;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String slots="";
		String date = "";
		if(slotIDs!=null) {
		for (Integer integer : slotIDs) {
			slots+=Reservation.getSlotRange(integer)+", ";
		}
		for (LocalDate d : targetDate) {
			date+=d.toString()+", ";
		}
		}
		String text = type
				+" by "+
				reserverEmail +",\tStatus: "+
				status;
				if(this.cancelledBy != null){
					text += "\nCancelled By: " + this.cancelledBy;
				}
				if(this.reason_cancel != null){
					text += "\nReason for cancellation: " + this.reason_cancel;
				}
				text += "\n" + message + "Course: "+
				course+"\nDate: "+
				date
		+"\nSlots: "+slots;
		return text;
	}
	public String getSlotIDasString() {
		String ans="";
		for (Integer integer : slotIDs) {
			ans+=Reservation.getSlotRange(integer)+"\n";
		}
		return ans;
	}
	public String getType() {
		return type;
	}
	public String getStatus() {
		return status;
	}
	public String getMessage() {
		return message;
	}
	public String getCourse() {
		return course;
	}

	public ArrayList<LocalDate> getTargetDate() {
		return targetDate;
	}
	public String getRoom() {
		return room;
	}
	public String getReserverEmail() {
		return reserverEmail;
	}
	public ArrayList<Integer> getSlotIDs() {
		return slotIDs;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setCourse(String course) {
		this.course = course;
	}
	public void setTargetDate(ArrayList<LocalDate> targetDate) {
		this.targetDate = targetDate;
	}
	public LocalDateTime getNotificationDateTime() {
		return NotificationDateTime;
	}
	public void setNotificationDateTime(LocalDateTime notificationDateTime) {
		NotificationDateTime = notificationDateTime;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	public void setReserverEmail(String reserverEmail) {
		this.reserverEmail = reserverEmail;
	}
	public void setSlotID(ArrayList<Integer> slotID) {
		this.slotIDs = slotID;
	}
	
    
}
