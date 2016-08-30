package com.myclose.mymqtt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

//import android.os.DropBoxManager;

public class MySqlClose extends OrmLiteSqliteOpenHelper {

	private final static String DATABASE_NAME = "Iclose.db";
	private final static int DATABASE_version = 1;

	private static MySqlClose instance;

	public MySqlClose(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_version);
		// TODO Auto-generated constructor stub
	}

	public static synchronized MySqlClose getIstance(Context c) {
		if (instance == null) {

			instance = new MySqlClose(c);

		}
		return instance;

	}

	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource suorce) {
		// TODO Auto-generated method stub
		try {
			TableUtils.createTable(suorce, Utente.class);
			TableUtils.createTable(suorce, StatoIclose.class);
			Log.d("database", "table created");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.d("database", "table NOT created");
			e.printStackTrace();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
			int arg3) {
		
		
		// TODO Auto-generated method stub

	}

	public void addUtente( String email, String password,
			String idMaster, String sim, long timestamp, String tel, String telMaster) {
		Utente user = new Utente();
		user.setEmail(email);
		user.setPassword(password);
		user.setIdMaster(idMaster);
		user.setIdSim(sim);
		user.setTimeSession(timestamp);
		user.setIdTel(tel);
		user.setIdTelMaster(telMaster);
		try {
			Dao<Utente, Integer> uDao = getDao(Utente.class);
			uDao.create(user);
			Log.d("database", "user created");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Utente getUtente() {
		List<Utente> user = null;
		

		try {
			Dao<Utente, Integer> uDao = getDao(Utente.class);
			user = uDao.queryForAll();
			Log.d("database", "users retrieved " + user.size());
			Log.d("database", user.toString());
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (user.size() != 0)
			return user.get(0);
		else
			return null;

	}
	
	public void upDateUtente(Utente id){
		try {
			Dao<Utente, Integer> uDao = getDao(Utente.class);
			
			uDao.update(id);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addStatoiclose(boolean onOff, boolean topBasic, String lista) {
		StatoIclose stato = new StatoIclose();
		stato.setOnOff(onOff);
		stato.setTopBasic(topBasic);
		stato.setLista(lista);
		try {
			Dao<StatoIclose, Integer> uDao = getDao(StatoIclose.class);
			uDao.create(stato);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public StatoIclose getStatoIclose() {

		StatoIclose stato = null;

		try {
			Dao<StatoIclose, Integer> uDao = getDao(StatoIclose.class);
			stato = uDao.queryForId(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stato;
	}

}
