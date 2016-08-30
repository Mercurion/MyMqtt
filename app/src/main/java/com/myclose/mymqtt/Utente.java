package com.myclose.mymqtt;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Utente {
	@DatabaseField(generatedId= true)
	private int id;
	
	@DatabaseField
	private String Email;
	
	@DatabaseField
	private String Password;
	
	@DatabaseField
	private String idMaster;
	
	@DatabaseField
	private String idSim;
	
	@DatabaseField
	private long timeSession;
	
	
	@DatabaseField
	private String idTel;
	
	@DatabaseField
	private String idTelMaster;

	


	public Utente(){
	
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}

	public String getIdMaster() {
		return idMaster;
	}

	public void setIdMaster(String idMaster) {
		this.idMaster = idMaster;
	}
 
	public String getIdSim() {
		return idSim;
	}

	public void setIdSim(String idSim) {
		this.idSim = idSim;
	}
	
	
	public String getIdTel() {
		return idTel;
	}

	public void setIdTel(String idTel) {
		this.idTel = idTel;
	}

	public long getTimeSession() {
		return timeSession;
	}

	public void setTimeSession(long timeSession) {
		this.timeSession = timeSession;
	}
	
	

	public void setIdTelMaster(String idTelMaster) {
		this.idTelMaster = idTelMaster;
	}
	
	public String getIdTelMaster() {
		return idTelMaster;
		// TODO Auto-generated method stub
		
	}
	


	
}
