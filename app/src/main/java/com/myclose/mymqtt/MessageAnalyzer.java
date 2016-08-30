package com.myclose.mymqtt;


import android.util.Log;

/**
 * Created by jackb on 06/06/2016.
 * @author jack
 */
public class MessageAnalyzer {
    int phoneNumber;
    String masterID;
    String lockID;
    int locksensibility;
    int language;
    int comandID;


    public MessageAnalyzer() {
    }

    public void setLanguage (int value) {
        this.language = value;
    }

    public int getLanguage (){
        return this.language;
    }

    public void setComandID (int value)  {
        this.comandID = value;
    }

    public int getComandID () {
        return this.comandID;
    }

    public String getMasterID() {
        return masterID;
    }

    public void setMasterID(String masterID) {
        this.masterID = masterID;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getLocksensibility() {
        return locksensibility;
    }

    public void setLocksensibility(int locksensibility) {
        this.locksensibility = locksensibility;
    }

    /*
    questo metodo si può esare nel controllo che i parametri inviati siano stati recepiti giusti
    peresempio nelle impostazioni custom
     */
    public boolean checkIfSameString (String recived, String sent) {

        Log.println(Log.INFO,"controllo","controllo sui messaggi");

        sent = sent.replace(" " , "");
        if (recived.contains("CONFIRMED"))
            recived = recived.replace("CONFIRMED"," ");
        recived = recived.replace(" " , "");

        Log.println(Log.INFO, "mandato", sent);
        Log.println(Log.INFO, "ricevuto", recived);
        if (sent.equals(recived)) {
            return true;
        } else
            return false;
    }

    /*
    controllo se è allarme di movimento
     */
    private boolean checkIfMovementAlarm (String recived) {
        if (recived.contains("movimento")||
                recived.contains("Movement"))
        return true;
        else
            return false;
    }

    /*
    controllo se è un allarme di perdita segnale
     */
    private boolean checkIfLostSignalAlarm (String recived) {
        if (recived.contains("perdita")
                || recived.contains("signal"))
            return true;
        else
            return false;
    }


    /**
     *  Questo metodo analizza il tipo di allarme ricevuto.
     * @param recived: messaggio ricevuto, senza filtri
     * @return 1 se è allarme di assenza segnale. 2 se è allarme di movimento.
     */
    public int chekTypeAlarm(String recived) {
        if (checkIfLostSignalAlarm(recived))
            return 1;
        else if (checkIfMovementAlarm(recived))
            return 2;
        else
            return 0;
    }


    /**
     * Questo metodo analizza il messaggio di status e verifica lo stato del lucchetto.
     * @param recived il messaggio di status che viene ricevuto. da passare intero, senza modifiche
     * @return tre possibili valori interi di return. 1 se il lucchetto è attivo e senza allarmi.
     *  2 se il lucchetto è in timeout di comunicazione. 3 se è in allarme di movimento.
     *
     *
     */
    public int checkStatus (String recived) {
        Log.println(Log.INFO, "messaggio in analisi", recived);
        if (recived.contains("ON"))
            return 1;
        else if (recived.contains("OFF"))
            return 2;
        else if (recived.contains("MOV"))
            return 3;
        else return 0;
    }

    /**
     * Questo metodo controlla se l'ID del master è quello atteso.
     * @param recived il messaggio ricevuto, intero e senza modifiche
     * @return un boolean che è true se l'ID è corretto
     */
    public boolean checkIfCorrectMasterId (String recived) {
        if (recived.contains(this.masterID))
            return true;
        return false;
    }

}
