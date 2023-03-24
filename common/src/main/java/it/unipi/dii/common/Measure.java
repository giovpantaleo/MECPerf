package it.unipi.dii.common;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import java.util.Map;

/**
 * This class is meant to be the packet sent to the aggregator in order to store measurement
 * coming from the three different entities of the system (Client, Observer, Remote Server)
 */
public class Measure implements Serializable {

    private String type;
    private int ID;
    private String sender;
    private String receiver;
    private Map<Integer, Long[]> bandwidth;
    private  Map<Integer, Long[]> latency;
    private String extra;

    //personalized settings
    private int len_pack;
    private int num_pack;

    private String senderAddress;
    private String receiverAddress;

    private static final long serialVersionUID = 3919700812200232178L;



    public Measure(){
    }

    /**
     * Constructor
     * @param type TCP or UDP type of used protocol
     * //@param ID  Unique identifier of the test
     * @param sender Name of the component that send the message
     * @param receiver Name of the component that receive the message
     * @param bandwidth Map containing the timestamps and the amount of data sent in the transmission
     * @param latency Latency measured
     * @param "extra" contains Timestamp when we would show Result in App or Keyword in Insert
     */
    public Measure(String type, String sender,String receiver, Map<Integer, Long[]>  bandwidth,
                   Map<Integer, Long[]> latency, String extra, int len_pack, int num_pack,
                   String senderAddress, String receiverAddress){
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.bandwidth= bandwidth;
        this.latency = latency;
        this.extra = extra;
        this.len_pack = len_pack;
        this.num_pack = num_pack;
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
    }

    /**
     * Copy contructor
     * @param m Object that contain the measure
     */
    public Measure(Measure m){
        type = m.type;
        sender = m.sender;
        receiver = m.receiver;
        bandwidth= m.bandwidth;
        latency = m.latency;
        extra = m.extra;

        num_pack = m.num_pack;
        len_pack = m.len_pack;

        senderAddress = m.senderAddress;
        receiverAddress = m.receiverAddress;
    }

    public int getLen_pack() {
        return len_pack;
    }

    public void setLen_pack(int len_pack) {
        this.len_pack = len_pack;
    }

    public int getNum_pack() {
        return num_pack;
    }

    public void setNum_pack(int num_pack) {
        this.num_pack = num_pack;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Map<Integer, Long[]>  getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Map<Integer, Long[]>  bandwidth) {
        this.bandwidth = bandwidth;
    }

    public  Map<Integer, Long[]> getLatency() {
        return latency;
    }

    public void setLatency( Map<Integer, Long[]> latency) {
        this.latency = latency;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }


    public String getTestJSON(String segment){
        String direction = null;

        if (this.getSender().equals("Client") && this.getReceiver().equals("Observer") || 
            this.getSender().equals("Observer") && this.getReceiver().equals("Server"))
            direction = "Upstream";
        else if (this.getSender().equals("Server") && this.getReceiver().equals("Observer") || 
                 this.getSender().equals("Observer") && this.getReceiver().equals("Client"))
            direction = "Downstream";
        else
            return null;


        return "\"test_info_" + segment + "_segment\":{\"Direction\":\"" + direction+"\"," +
               "\"Command\":\"" + this.getType()+"\", \"ReceiverIdentity\":\"" + this.getReceiver()+ "\","+
               "\"SenderIdentity\":\"" + this.getSender() + "\","+
               "\"SenderIPv4Address\":\"" + this.getSenderAddress() + "\","+
               "\"ReceiverIPv4Address\":\"" + this.getReceiverAddress() + "\","+
               "\"Keyword\":\"" + this.getExtra() + "\", \"PackSize\":\"" + this.getLen_pack() + "\","+
               "\"NumPack\":\"" + this.getNum_pack() +"\" }";
    }

    public String getValuesJSON(String segment){
        if (this.getType().equals("TCPBandwidth") || this.getType().equals("UDPBandwidth"))
            return this.getBandwidthValuesJSON(segment);
        else if (this.getType().equals("TCPRTT") || this.getType().equals("UDPRTT"))
            return this.getRTTValuesJSON(segment);
        else return null;


    }

    private String getBandwidthValuesJSON(String segment){
        String bandwidthvalues_JSON = "\"bandwidth_values_" + segment + "_segment\": [";
        int i = 0;
        long previous = 0;
        Boolean isTCP = this.getType().equals("TCPBandwidth");
        //System.out.println("Command: " + this.getType() + " MAP_SIZE: " + this.getBandwidth().size());
        for (Map.Entry<Integer, Long[]> entry : this.getBandwidth().entrySet()) { // per UDP ha un solo elemento
            long actualTime = entry.getValue()[0];
            long diff = actualTime - previous;
            previous = actualTime;
            i++;

            if (Long.MAX_VALUE < actualTime)
                return null;
            
            
            if (i == 1 && isTCP)
                continue;

            if ((i > 2) || (i> 1 && !isTCP))
                bandwidthvalues_JSON += ",";
            bandwidthvalues_JSON += "{\"sub_id\":\"" + entry.getKey() + "\"";
            if (this.getType().equals("TCPBandwidth"))
                bandwidthvalues_JSON += ",\"nanoTimes\":\"" + diff + "\"";
            else if (this.getType().equals("UDPBandwidth"))
                bandwidthvalues_JSON += ",\"nanoTimes\":\"" + actualTime + "\"";
            else return null;

            bandwidthvalues_JSON += ",\"kBytes\":\"" + ((double)entry.getValue()[1] / 1024) + "\"}";
        }

        bandwidthvalues_JSON += "]";

        return bandwidthvalues_JSON;   
    
      
    }

    private String getRTTValuesJSON(String segment) {
        String latencyValues_JSON = "\"latency_values_" + segment + "_segment\":[";
        int i = 0;
       
        for (Map.Entry<Integer, Long[]> entry : this.getLatency().entrySet()) {
            if (i > 0)
                latencyValues_JSON += ",";
            
            latencyValues_JSON += "{\"sub_id\":" + entry.getKey() + ",\"latency\":" + entry.getValue()[0] + 
                                  ",\"timestamp_millis\":" + entry.getValue()[1] + "}";
            i++;
        }

        return latencyValues_JSON + "]";
    }
}

