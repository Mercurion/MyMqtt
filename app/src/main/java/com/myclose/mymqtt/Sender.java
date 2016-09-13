package com.myclose.mymqtt;

import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by Giacomo.
 * Questa classe è il sender dell'applicazione. Ogni classe che vuole mandare SMS crea un oggetto Sender, inizializza il masterPhone
 * e MasterId, in seguito chiamando i metodi si possono mandare gli SMS predeterminati in questa classe.
 */
public class Sender {
    String masterPhone;
    int cellPhone;
    String masterID;


    /**
     * method to set the number of the master device
     * @param number the number of the master
     */
    public void setMasterPhone(String number) {
        this.masterPhone = number;
    }

    @Deprecated
    public void setCellPhone(int number) {
        this.cellPhone = number;
    }

    /**
     * set the master ID, to be used in the messages
     * @param id the ID ofthe master
     */
    public void setMasterID(String id) {
        this.masterID = id;
    }


    /**
     * This method sends the SMS to the device
     * @param phoneNumber the phone number of destination device
     * @param message the message to be sent
     */
    private void SendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    /**
     * this method send the message to shutdown the master
     */
    public void ShutdownMessage() {
        String tmp;
        tmp = this.masterID + " " + "00" + " " + "SHUTDOWN";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * this method send the ping message
     */
    public void PingMessage() {
        String tmp;
        tmp = this.masterID + " " + "01" + " " + "ALIVE?";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * this method send the STATUS message, used in the starting phase
     */
    public void StatusMessage() {
        String tmp;
        tmp = this.masterID + " " + "01" + " " + "STATUS?";
        SendSMS(this.masterPhone, tmp);
    }

    public void NewStatusMessage0 (){
        String tmp;
        tmp = this.masterID + " " + "01 0" + " " + "STATUS?";
        SendSMS(this.masterPhone, tmp);
    }

    public void NewStatusMessage1 (){
        String tmp;
        tmp = this.masterID + " " + "01 1" + " " + "STATUS?";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * this method send the message to shut down the alarm
     */
    public void AlarmOFFMessage() {
        String tmp;
        tmp = this.masterID + " " + "02" + " " + "ALARM OFF";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * this method send the message to ALARM ON the system
     */
    public void AlarmONMessage() {
        String tmp;
        tmp = this.masterID + " " + "03" + " " + "ALARM ON";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * Questo metodo fa la richiesta di posizione al master
     */
    public void AcquisisciPosizioneMessage() {
        String tmp;
        tmp = this.masterID + " " + "04" + " " + "POSITION REQUESTED";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * Questo metodo manda la configurazione standard al master
     */
    public void BasicConfigurationMessage() {
        String tmp;
        tmp = this.masterID + " " + "10" + " " + "BASIC ON";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * Questo metodo manda la configurazione TOP al master
     */
    public void TopConfigurationMessage() {
        String tmp;
        tmp = this.masterID + " " + "11" + " " + "TOP ON";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * questo metodo manda l'informazione per il time-out di verifica se il lucchetto è attivo
     * @param intervallo in valori da 1 a 5. 0 se è disattivo
     */
    public void IntervalloTimeOutMessage(int intervallo) {
        String tmp;
        tmp = this.masterID + " " + "12" + " " + intervallo + " " + "TIMEOUT";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * questo metodo manda il parametro di sensibilità, quanti movimenti al minuto per attivare un allarme
     * @param sensibility da 1 a 3, con 1 basso, 2 medio, 3 alto
     */
    public void SensibilitaLucchettoMessage(int sensibility) {
        String tmp;
        tmp = this.masterID + " " + "13" + " " + sensibility + " " + "SENS";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * Questo metodo manda la configurazione ON o OFF per l'allarme di batteria scarica (se ON avvisa oltre una certa soglia)
     * @param batteria un boolean: true è ON, false è OFF
     */
    public void BatteriaMessage(boolean batteria) {
        if (batteria) {
            String tmp;
            tmp = this.masterID + " " + "14" + " " + "BATT ON";
            SendSMS(this.masterPhone, tmp);
        } else {
            String tmp;
            tmp = this.masterID + " " + "14" + " " + "BATT OFF";
            SendSMS(this.masterPhone, tmp);
        }
    }

    /**
     * questo metodo aggiunge numeri nella lista dei numeri da contattare
     * @param pos posizione in lista del numero
     * @param numero numero da chiamare, con prefisso internazionale (0039 per L'italia, etc..)
     */
    public void NumeroChiamareMessage(int pos, String numero) {
        String tmp;
        tmp = this.masterID + " " + "20" + pos + " " + numero + " " + "PHONE NUMBER";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * metodo per la configurazione della lingua nei messaggi dal master al cellulare
     * @param lingua lingua da 0 a 6, in ordine: Inglese, Ialiano, Francese, Tedesco, Spagnolo, Olandese, Portoghese
     */
    public void LinguaMessage(int lingua) {
        String tmp;
        tmp = this.masterID + " " + "30" + " " + lingua + " " + "SMS LANGUAGE";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * questo metodo attiva la modalità furto
     */
    public void FurtoMessage() {
        String tmp;
        tmp = this.masterID + " " + "40" + " " + "THEFT ON";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * Questo metodo rimuove dal sistema un lucchetto
     * @param lucchetto ID del lucchetto da rimuovere
     */
    public void RimozioneLucchettoMessage(int lucchetto) {
        String tmp;
        tmp = this.masterID + " " + "50" + " " + lucchetto + " " + "REMOVE";
        SendSMS(this.masterPhone, tmp);
    }

    /**
     * Questo metodo attiva il flag dell'accensione del master: ON il master avvisa quando si accende
     * @param accensione se true è ON, false altrimenti
     */
    public void AccensioneMasterMessage(boolean accensione) {
        if (accensione) {
            String tmp;
            tmp = this.masterID + " " + "51" + " " + "MA ON";
            SendSMS(this.masterPhone, tmp);
        } else {
            String tmp;
            tmp = this.masterID + " " + "51" + " " + "MA OFF";
            SendSMS(this.masterPhone, tmp);
        }
    }

    /**
     * this method sends all the custom configuration in one SMS
     * @param batt true is ON, false otherwise
     * @param acc true is ON, false otherwise
     * @param intervallo the time-out interval tocheck the lock. can be 0-5 (0 means OFF)
     * @param sensibility the value of moovements to activate the moovement alarm (1-3)
     * @return the sent message
     */
    public String TotalCustomSetMessage (boolean batt, boolean acc, int intervallo, int sensibility) {
        String tmp;
        String batteria;
        String accensione;
        if (batt)
            batteria = " BATT ON ";
        else
            batteria = " BATT OFF ";
        if (acc)
            accensione =" MA ON";
        else
            accensione = " MA OFF";

        tmp = this.masterID + " " + "16" + batteria + "SENS "  + sensibility+ " TIMEOUT " +intervallo  + accensione;
        SendSMS(this.masterPhone, tmp);
        Log.println(Log.INFO,"Messaggio mandato",tmp);
        return tmp;
    }
}