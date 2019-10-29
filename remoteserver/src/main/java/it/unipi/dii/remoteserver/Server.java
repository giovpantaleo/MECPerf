package it.unipi.dii.remoteserver;

/*
javac Measure/src/measure/Measure.java Measure/src/measure/Measure.java Measurements/src/measurements/Measurements.java Server/src/server/Server.java
java -cp ".:Measure/src/:Measurements/src:Server/src" server.Server


*/

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import it.unipi.dii.common.Measurements;
import it.unipi.dii.common.Measure;

/**
 *
 * @author Bernardi Leonardo
 */


public class Server {

    //command listener, tcp data and udp data ports

    private static final int CMDPORT = 6789;
    private static final int TCPPORT = 6788;
    private static final int UDPPORT = 6787;
    private static final String AGGREGATORIP = "131.114.73.3";
    private static final int AGGRPORT = 6766;


    //used in udp measurements
//    private static final int PKTSIZE = 1024;
    private static int udp_bandwidth_pktsize = 1024;
    private static int tcp_bandwidth_pktsize = 1024;
    private static int tcp_bandwidth_stream = 1024*1024;
    private static int tcp_rtt_pktsize = 1;
    private static int tcp_rtt_num_pack = 100;
    private static int udp_rtt_pktsize = 1;
    private static int udp_rtt_num_pack = 100;


    public static void main(String[] args){
        ServerSocket cmdListener = null;//ServerSocket per la ricezione dei comandi
        ServerSocket tcpListener = null;//ServerSocket per le misurazioni TCP
        DatagramSocket udpListener = null;//ServerSocket per le misurazioni UDP

        //socket initialization
        try {
            cmdListener = new ServerSocket(CMDPORT);
            tcpListener = new ServerSocket(TCPPORT);
            udpListener = new DatagramSocket(UDPPORT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Log
        System.out.println("Server CMD: inizializzato sulla porta " + cmdListener.getLocalPort());
        System.out.println("Server TCP: inizializzato sulla porta " + tcpListener.getLocalPort());
        System.out.println("Server UDP: inizializzato sulla porta " + udpListener.getLocalPort());

        while (true) {
            //Listening for commands
            Socket cmdSocket = null;
            try {
                cmdSocket = cmdListener.accept();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            //Creating Data Stream from socket
            InputStream inputStream = null;
            try {
                inputStream = cmdSocket.getInputStream();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            //Reading and parsing received command. Command message structure is "COMMAND TEST-ID"
            String cmd = null;
            try {
                cmd = dataInputStream.readUTF();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            String separator ="#";
            String[] cmdSplitted = cmd.split(separator);

            //Log
            System.out.println("\nThe cmd sent from the socket was: " + cmdSplitted[0]);

            double bandwidth = 0.0;
            double latency = 0.0;

            //Start test based on command received

            switch(cmdSplitted[0]){

                case "TCPBandwidthSender":
                    //the observer sends to the remote server
                    Map<Long, Integer> mappa;
                    tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[3]);

//                    System.out.println("dim-pack: " + tcp_bandwidth_pktsize);

                    try {
                        Socket tcpReceiverConnectionSocket = tcpListener.accept();
                        mappa = Measurements.TCPBandwidthReceiver(tcpReceiverConnectionSocket, tcp_bandwidth_pktsize);
                        sendDataToAggregator("TCPBandwidth", Integer.parseInt(cmdSplitted[1]),
                                           "Observer", "Server", -1, mappa, cmdSplitted[2], tcp_bandwidth_pktsize, Integer.parseInt(cmdSplitted[4]) );

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("TCPBandwidth: Observer -> RemoteServer finished");
                    break;

                case "TCPBandwidthReceiver":
                    tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[3]);
                    tcp_bandwidth_stream = Integer.parseInt(cmdSplitted[4]) * tcp_bandwidth_pktsize;

 //                   System.out.println("dim-pack: " + tcp_bandwidth_pktsize);
 //                   System.out.println("dim-stream: " + tcp_bandwidth_stream);
                    try {
                        //the remote server sends packet to the observer

                        Socket tcpSenderConnectionSocket = tcpListener.accept();
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket, tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("TCPBandwidth: RemoteServer -> Observer finished");
                    break;

                //UDP latency test using Packet Pair, MRS has to receive
                case "UDPCapacityPPSender":
                    udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[3]);
                    Map<Long, Integer> measureResult =  Measurements.UDPCapacityPPReceiver(udpListener, udp_bandwidth_pktsize);

                    //send data to Aggregator
                    sendDataToAggregator("UDPBandwidth", Integer.parseInt(cmdSplitted[1]), "Observer", "Server", -1 , measureResult, cmdSplitted[2], udp_bandwidth_pktsize, 2);

                    System.out.println("TCPBandwidthSender: mappa ricevuta da OBSERVER: \n" +measureResult + "\n");
                    break;

                //UDP Latency test using Packet Pair, MRS has to send
                case "UDPCapacityPPReceiver":
                    udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[3]);
                    //MRS first has to receive a packet from the client to know Client's Address and Port
                    byte[] buf = new byte[1000];
                    DatagramPacket dgp = new DatagramPacket(buf, buf.length);
                    try {
                        udpListener.receive(dgp);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //Connecting and actually starting the test

                    udpListener.connect(dgp.getAddress(), dgp.getPort());
                    Measurements.UDPCapacityPPSender(udpListener, udp_bandwidth_pktsize);
                    udpListener.disconnect();

                    //Log
                    System.out.println("Finished!");
                    break;

                //UDP RTT test, MRS has to receive
                case "UDPRTTMO":
                    udp_rtt_pktsize = Integer.parseInt(cmdSplitted[3]);
                    udp_rtt_num_pack = Integer.parseInt(cmdSplitted[4]);

                    System.out.println("udp_rtt_pktsize: " +udp_rtt_pktsize);
                    System.out.println("udp_rtt_num_pack: " +udp_rtt_num_pack);
                    try {
                        Measurements.UDPRTTReceiver(udpListener, udp_rtt_pktsize, udp_rtt_num_pack );
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;

                //UDP RTT test, MRS has to send
                case "UDPRTTMRS":
                    udp_rtt_pktsize = Integer.parseInt(cmdSplitted[3]);
                    udp_rtt_num_pack = Integer.parseInt(cmdSplitted[4]);

                    System.out.println("udp_rtt_pktsize: " +udp_rtt_pktsize);
                    System.out.println("udp_rtt_num_pack: " +udp_rtt_num_pack);
                    try {
                        //MRS has to first receive a packet from the client to know Client's Address and Port
                        byte[] bufrtt = new byte[1000];
                        DatagramPacket dgprtt = new DatagramPacket(bufrtt, bufrtt.length);
                        try {
                            udpListener.receive(dgprtt);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        //connecting and actually starting the test
                        udpListener.connect(dgprtt.getAddress(), dgprtt.getPort());
                        latency = Measurements.UDPRTTSender(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        udpListener.disconnect();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //send data to Aggregator
                    sendDataToAggregator("UDPRTT", Integer.parseInt(cmdSplitted[1]), "Server", "Observer", latency , null, cmdSplitted[2], udp_rtt_pktsize, udp_rtt_num_pack);
                    //Log
                    System.out.println("Server UDP RTT : " + latency + " Ms");
                    break;


                case "TCPRTTMO":
                    // the observer starts a RTT measure using the remote server s receiver
                    tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[3]);
                    tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[4]);
                    System.out.println("tcp_rtt_pktsize: " +tcp_rtt_pktsize);
                    System.out.println("tcp_rtt_num_pack: " +tcp_rtt_num_pack);

                    try {
                        Socket tcpRTT = tcpListener.accept();
                        Measurements.TCPRTTReceiver(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;


                case "TCPRTTMRS":
                    //the observer starts a TCP RTT using the remote server as sender
                    tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[3]);
                    tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[4]);
                    System.out.println("tcp_rtt_pktsize: " +tcp_rtt_pktsize);
                    System.out.println("tcp_rtt_num_pack: " +tcp_rtt_num_pack);
                    try {
                        Socket tcpRTT = tcpListener.accept();
                        latency = Measurements.TCPRTTSender(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    //send data to Aggregator
                    sendDataToAggregator("TCPRTT", Integer.parseInt(cmdSplitted[1]), "Server", "Observer", latency , null, cmdSplitted[2], tcp_rtt_pktsize, tcp_rtt_num_pack);

                    //Log
                    System.out.println("Server TCP RTT : " + latency + " Ms");
                    break;
            }
        }
    }

    protected static void sendDataToAggregator(String type, int id, String sender, String receiver, double latency, Map<Long, Integer> bandwidth, String keyword, int len_pack, int num_pack){
        Socket socket = null;
        ObjectOutputStream objOutputStream = null;
        try {
            socket = new Socket(InetAddress.getByName(AGGREGATORIP), AGGRPORT);
            objOutputStream = new ObjectOutputStream(socket.getOutputStream());
            Measure measure = new Measure();
            measure.setType(type);
            //measure.setID(id);
            measure.setSender(sender);
            measure.setReceiver(receiver);
            measure.setLatency(latency);
            measure.setBandwidth(bandwidth);
            measure.setExtra(keyword);
            measure.setLen_pack(len_pack);
            measure.setNum_pack(num_pack);

            // write the message we want to send
            objOutputStream.writeObject(measure);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                objOutputStream.close(); // close the output stream when we're done.
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

