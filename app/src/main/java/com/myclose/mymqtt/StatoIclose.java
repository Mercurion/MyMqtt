package com.myclose.mymqtt;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class StatoIclose {
@DatabaseField
private boolean onOff;
@DatabaseField
private boolean topBasic;
@DatabaseField
private String lista;

@DatabaseField
private int lingua;

public StatoIclose(){
	
}

public boolean isOnOff() {
	return onOff;
}

public void setOnOff(boolean onOff) {
	this.onOff = onOff;
}

public boolean isTopBasic() {
	return topBasic;
}

public void setTopBasic(boolean topBasic) {
	this.topBasic = topBasic;
}

public String getLista() {
	return lista;
}

public void setLista(String lista) {
	this.lista = lista;
}

public int getLingua() {
	return lingua;
}

public void setLingua(int lingua) {
	this.lingua = lingua;
}

}
