package it.unipi.dii.observer;



import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
//import java.net.http.*;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unipi.dii.common.Measurements;
import it.unipi.dii.common.Measure;
import it.unipi.dii.common.ControlMessages;
import java.nio.charset.StandardCharsets;


public class Observer {
    private static String REMOTEIP = null,
                          AGGREGATORIP = null,
                          OBSERVERIP = null,
                          REMOTEID="NA";

    private static int REMOTECMDPORT = -1,
                       REMOTETCPPORT = -1,
                       REMOTEUDPPORT = -1,
                       AGGRPORT = -1,
                       OBSCMDPORT = -1,
                       OBSTCPPORT = -1,
                       OBSUDPPORT = -1,
                       timeout =  5 * 60 * 1000 ;

    private static ServerSocket cmdListener = null; //socket used to receive commands
    private static ServerSocket tcpListener = null; //socket used for tcp operations
    private static DatagramSocket udpListener = null;//socket used for udp operations


    private static void initializeSocket() throws Exception {
        cmdListener = new ServerSocket(OBSCMDPORT);
        tcpListener = new ServerSocket(OBSTCPPORT);
        udpListener = new DatagramSocket(OBSUDPPORT);



        cmdListener.setSoTimeout(timeout);
        tcpListener.setSoTimeout(timeout);
        udpListener.setSoTimeout(timeout);

        System.out.println("\n\nObserver address: " + OBSERVERIP);
        System.out.println("Observer CMD: inizializzato sulla porta " +
                cmdListener.getLocalPort() + "[Timeout = " + timeout/1000 + "s]");
        System.out.println("Observer TCP: inizializzato sulla porta " +
                tcpListener.getLocalPort() + "[Timeout = " + timeout/1000 + "s]");
        System.out.println("Observer UDP: inizializzato sulla porta " +
                udpListener.getLocalPort() + "[Timeout = " + timeout/1000 + "s]");
    }



    private static int restartSockets()  {
        try{

            if (!udpListener.isClosed())
                udpListener.close();
            if (!tcpListener.isClosed())
                tcpListener.close();
            if (!cmdListener.isClosed())
                cmdListener.close();

            initializeSocket();
        }
        catch (Exception ex) {
            ex.printStackTrace();

            return -1;
        }

        return 0;
    }


    public static void main(String[] args) throws Exception{
        OBSERVERIP = getAddress().replace("/", "");
        parseArguments(args);
        if (!checkArguments()){
            System.out.println("checkArguments() failed");
            System.exit(0);
        }

        System.out.println("checkArguments() DONE");
        try {
            initializeSocket();
        } catch (NullPointerException e) {
            e.printStackTrace();
            
           System.exit(1);
        }
        System.out.println("Socket initialized");


        ControlMessages controlSocketRemote = new ControlMessages();
        while (true) {
            //System.out.println("\nWaiting for  a command...");
            ControlMessages controlSocketApp = null;
            String cmd=null;
            try {
                controlSocketApp = new ControlMessages(cmdListener.accept());
                //System.out.println("connection accepted");
                cmd = Objects.requireNonNull(controlSocketApp).receiveCMD();
                //System.out.println("command " + cmd);

            } catch (Exception ex) {
                ex.printStackTrace();

                if (controlSocketApp != null && controlSocketApp.getSocket() != null  && !controlSocketApp.getSocket().isClosed())
                    controlSocketApp.closeConnection();

                while (true)
                {
                    System.out.println("Restart sockets");
                    int ret= restartSockets();

                    if (ret == 0)
                        break;

                    System.out.println("Failed... restart in 30 seconds");
                    Thread.sleep(30 * 1000);

                }

                continue;
            }


            //command received is <command ; id>
            String separator ="#";
            String[] cmdSplitted = cmd.split(separator);

            controlSocketRemote.initializeNewMeasure(REMOTEIP, REMOTECMDPORT);
            switch(cmdSplitted[0]) {
                case "TCPBandwidthSender": {
                    /*
                        the app sends packets to the observer
                        the observer sends packet to the server
                    */
                    int tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_bandwidth_stream = Integer.parseInt(cmdSplitted[3]) * tcp_bandwidth_pktsize;
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t\t[Packet size : " + tcp_bandwidth_pktsize);
                    System.out.println(", Number of packes : " + Integer.parseInt(cmdSplitted[3]) + "]");

                    try {
                        Socket tcpReceiverConnectionSocket = tcpListener.accept();
                        Socket tcpSenderConnectionSocket = new Socket(
                                                          InetAddress.getByName(REMOTEIP), REMOTETCPPORT);

                        //first measure (client -> observer)
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        Map<Integer, Long[]> mappaCO = Measurements.TCPBandwidthReceiver(
                                                tcpReceiverConnectionSocket, tcp_bandwidth_pktsize);


                        //second measure (observer -> remote)
                        controlSocketRemote.sendCMD(cmd + "#" + OBSCMDPORT + "#" + OBSERVERIP);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                                                          .START.toString()) != 0) {
                            System.out.println("Start measure with remote FAILED");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket,
                                                       tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Failed remote measure");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                        String resultRemote = controlSocketRemote.receiveCMD();


                        InputStream isr = controlSocketRemote.getSocket().getInputStream();
                        ObjectInputStream mapInputStream = new ObjectInputStream(isr);
                        Map<Integer, Long[]> remoteMeasure = (Map<Integer, Long[]>) mapInputStream.readObject();
                        isr.close();
                        mapInputStream.close();

                        //remote data
                        Measure measureSecondSegment = new Measure("TCPBandwidth",
                                                "Observer","Server", remoteMeasure,
                                                null, cmdSplitted[1], tcp_bandwidth_pktsize,
                                                 Integer.parseInt(cmdSplitted[3]), OBSERVERIP,
                                                 cutAddress(controlSocketRemote.getSocket()
                                                         .getRemoteSocketAddress().toString()));

                        //observer data
                        Measure measureFirstSegment = new Measure("TCPBandwidth","Client",
                                "Observer", mappaCO, null, cmdSplitted[1],
                                tcp_bandwidth_pktsize, Integer.parseInt(cmdSplitted[3]),
                                cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                          .toString()), OBSERVERIP);

                        sendAggregator(measureFirstSegment, measureSecondSegment, null, null);


                        //System.out.println("results sent to aggregator");


                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
                        //System.out.println("TCPBandwidthSender: completed");

                    } catch (Exception ex) {
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            Thread.sleep(30 * 1000);

                        }

                    }
                    break;
                }
                case "TCPBandwidthReceiver": {
                    Map<Integer, Long[]> mappaSO = null;
                    int tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_bandwidth_stream = Integer.parseInt(cmdSplitted[3]) * tcp_bandwidth_pktsize;
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t\t[Packet size : " + tcp_bandwidth_pktsize);
                    System.out.println(", Number of packes : " + Integer.parseInt(cmdSplitted[3]) + "]");

                    try {
                        Socket tcpSenderConnectionSocket = tcpListener.accept();
                        Socket tcpReceiverConnectionSocket = new Socket(InetAddress.getByName(
                                REMOTEIP), REMOTETCPPORT);

                        //first measure (observer -> client)
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket,
                                                       tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                        if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Edge measure failed");
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                        //second measure (remote -> observer)

                        controlSocketRemote.sendCMD(cmd);
                        mappaSO = Measurements.TCPBandwidthReceiver(tcpReceiverConnectionSocket,
                                                                    tcp_bandwidth_pktsize);
                        controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());


                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
                        String resultApp = controlSocketApp.receiveCMD();

                        //Socket connectionSocket = cmdListener.accept();
                        //InputStream isr = connectionSocket.getInputStream();
                        InputStream isr = controlSocketApp.getSocket().getInputStream();
                        ObjectInputStream mapInputStream = new ObjectInputStream(isr);
                        Map<Integer, Long[]> applicationMeasure = (Map<Integer, Long[]>) mapInputStream.readObject();
                        isr.close();
                        mapInputStream.close();


                        //application data
                        Measure measureFirstSegment = new Measure("TCPBandwidth", "Observer",
                                "Client", applicationMeasure, null, cmdSplitted[1],
                                tcp_bandwidth_pktsize, Integer.parseInt(cmdSplitted[3]),
                                OBSERVERIP, cutAddress(controlSocketApp.getSocket()
                                .getRemoteSocketAddress().toString()) );
                        //observer data
                        Measure measureSecondSegment = new Measure("TCPBandwidth", "Server",
                                "Observer", mappaSO, null, cmdSplitted[1],
                                tcp_bandwidth_pktsize, Integer.parseInt(cmdSplitted[3]),
                                cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                        .toString()), OBSERVERIP);

                        sendAggregator(measureFirstSegment, measureSecondSegment, null, null);
                        //System.out.println("results sent to aggregator");
                        //System.out.println("TCPBandwidthReceiver: completed");
                    } catch (Exception ex) {
                        ex.printStackTrace();


                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            Thread.sleep(30 * 1000);

                        }


                    }
                    break;
                }
                case "UDPCapacityPPSender": {
                    int udp_capacity_pktsize = Integer.parseInt(cmdSplitted[2]);
                    int udp_capacity_num_tests = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t\t[ " + udp_capacity_num_tests + "tests wof ");
                    System.out.println(udp_capacity_pktsize + " packet size" + "]");

                    Map<Integer, Long[]>  measureResult = null;
                    try {
                        //first measure (client -> observer)
                        //controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        measureResult = Measurements.UDPCapacityPPReceiver(udpListener,
                                                         udp_capacity_pktsize, udp_capacity_num_tests,
                                                         controlSocketApp);
                        //System.out.println("UDPCapacityPPReceiver returned");
                        if (measureResult == null)
                        {
                            System.out.println("Measure filed");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                        //second measure (observer -> remote)
                        controlSocketRemote.sendCMD(cmd + "#" + OBSCMDPORT + "#"+ OBSERVERIP);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages.
                                                                             START.toString()) !=0){
                            System.out.println("Start measure with remote FAILED");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }


                        DatagramSocket udpsocket = new DatagramSocket();
                        udpsocket.connect(InetAddress.getByName(REMOTEIP), REMOTEUDPPORT);
                        int ret = Measurements.UDPCapacityPPSender(udpsocket, udp_capacity_pktsize,
                                udp_capacity_num_tests, controlSocketRemote);
                        if (ret < 0 ){
                            System.out.println("Start measure with remote FAILED");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }


                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages.
                                                                          SUCCEDED.toString()) !=0){
                            System.out.println("Failed remote measure");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }


                        String resultRemote = controlSocketRemote.receiveCMD();

                        //Socket connectionSocket = cmdListener.accept();
                        //InputStream isr = connectionSocket.getInputStream();
                        InputStream isr = controlSocketRemote.getSocket().getInputStream();
                        ObjectInputStream mapInputStream = new ObjectInputStream(isr);
                        Map<Integer, Long[]>  remoteMeasure = (Map<Integer, Long[]> ) mapInputStream.readObject();

                        mapInputStream.close();
                        isr.close();

                        //remote data
                        Measure measureSecondSegment = new Measure("UDPBandwidth",
                                "Observer","Server",  remoteMeasure, null,
                                cmdSplitted[1], udp_capacity_pktsize, 2, OBSERVERIP,
                                cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                        .toString()));
                        //observer data
                        Measure measureFirstSegment = new Measure("UDPBandwidth", "Client",
                                "Observer", measureResult, null, cmdSplitted[1],
                                udp_capacity_pktsize,2, cutAddress(controlSocketApp
                                .getSocket().getRemoteSocketAddress().toString()), OBSERVERIP);

                        sendAggregator(measureFirstSegment, measureSecondSegment, null, null);

                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                        //System.out.println("results sent to aggregator");
                        //System.out.println("UDPBandwidthSender: completed");

                    } catch (Exception ex) {
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            Thread.sleep(30 * 1000);

                        }

                    }
                    break;
                }
                case "UDPCapacityPPReceiver":{
                    int udp_capacity_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_capacity_num_tests = Integer.parseInt(cmdSplitted[3]);;
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t\t[ " + udp_capacity_num_tests + " tests  of ");
                    System.out.println(udp_capacity_pktsize + " packet size" + "]");

                    byte[] buf = new byte[1000];
                    DatagramPacket dgp = new DatagramPacket(buf, buf.length);

                    try {
                        udpListener.receive(dgp);
                        udpListener.connect(dgp.getAddress(), dgp.getPort());

                        //first measure (observer -> client)
                        int ret = Measurements.UDPCapacityPPSender(udpListener, udp_capacity_pktsize, udp_capacity_num_tests, controlSocketApp);
                        if (ret < 0)
                        {
                            System.out.println("Start measure with app FAILED");
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                        }

                        udpListener.disconnect();

                        if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages.SUCCEDED
                                                                                    .toString()) != 0) {
                            System.out.println("Client-observer measure failed");
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }


                        //second measure (remote -> observer)
                        controlSocketRemote.sendCMD(cmd);
                        //MO has to send a packet to server to let the server knows Client's IP and Port
                        DatagramSocket udpsocket = null;
                        udpsocket = new DatagramSocket();
                        byte[] buff;
                        String outString = "UDPCapacityPPReceiver";
                        buff = outString.getBytes();
                        DatagramPacket out = new DatagramPacket(buff, buff.length, InetAddress.getByName(REMOTEIP), REMOTEUDPPORT);
                        udpsocket.send(out);

                        Map<Integer, Long[]> measureResult = Measurements.UDPCapacityPPReceiver(udpsocket,
                                                                                 udp_capacity_pktsize, udp_capacity_num_tests, controlSocketRemote);
                        if (measureResult == null)
                        {
                            System.out.println("Measure filed");
                            controlSocketRemote.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                        controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                        String resultApp = controlSocketApp.receiveCMD();

                        ObjectInputStream mapInputStream = new ObjectInputStream(controlSocketApp.getSocket().getInputStream());
                        Map<Integer, Long[]> applicationMeasure = (Map<Integer, Long[]>) mapInputStream.readObject();
                        //isr.close();
                        mapInputStream.close();

                        //application data
                        Measure measureFirstSegment = new Measure("UDPBandwidth","Observer",
                                "Client", applicationMeasure, null, cmdSplitted[1],
                                udp_capacity_pktsize, 2, OBSERVERIP,
                                cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                        .toString()));
                        //observer data
                        Measure measureSecondSegment = new Measure("UDPBandwidth","Server",
                                "Observer", measureResult, null, cmdSplitted[1],
                                udp_capacity_pktsize, 2,
                                cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                        .toString()), OBSERVERIP);

                        sendAggregator(measureFirstSegment, measureSecondSegment, null, null);

                        //System.out.println("results sent to aggregator");
                        //System.out.println("UDPCapacityPPReceiver: completed");
                    } catch (Exception e) {
                        e.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            Thread.sleep(30 * 1000);

                        }

                    }

                    break;
                }
                case "UDPRTTSender": {
                    //MO forwards the command to the server and sends the metrics observer-server
                    int udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t\t\t[Packet size : " + udp_rtt_pktsize);
                    System.out.print(", Number of packes : " + udp_rtt_num_pack + " - " );

                    try {
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        int ret = Measurements.UDPRTTReceiver(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (ret < 0 || controlSocketApp.receiveCMD().compareTo(ControlMessages
                                                              .Messages.SUCCEDED.toString()) != 0) {
                            System.out.println("measure with client FAILED");
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            System.out.println("closed");

                            System.out.println("Failed sent");
                            break;
                        }


                        //second measure (observer -> remote -> observer)
                        controlSocketRemote.sendCMD(cmd);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                .START.toString()) != 0) {
                            System.out.println("Start measure with remote FAILED");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            restartSockets();
                            break;
                        }

                        DatagramSocket udpsocketmo = new DatagramSocket();
                        udpsocketmo.connect(InetAddress.getByName(REMOTEIP), REMOTEUDPPORT);
                        Map<Integer, Long[]> latency = Measurements.UDPRTTSender(udpsocketmo, udp_rtt_pktsize,
                                                                   udp_rtt_num_pack);
                        if (latency == null)
                        {
                            controlSocketRemote.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            restartSockets();
                            break;
                        }
                        controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                        ObjectInputStream mapInputStream = new ObjectInputStream(controlSocketApp.getSocket().getInputStream());
                        Map<Integer, Long[]> resultApp = (Map<Integer, Long[]>) mapInputStream.readObject();
                        HashMap<String, String> testMetadata_App = (HashMap<String, String>) mapInputStream.readObject();
                        HashMap<String, String>  testMetadata_Server = new HashMap<String, String> (testMetadata_App);
                        testMetadata_App.put("Receiver-identity", "Observer");
                        testMetadata_App.put("AggregatorAddress", AGGREGATORIP);
                        testMetadata_Server.put("AggregatorAddress", AGGREGATORIP);
                        testMetadata_Server.put("RemoteServerAddress", REMOTEIP);
                        testMetadata_Server.put("nodeid_remoteserver", REMOTEID);
                        testMetadata_Server.put("Receiver-identity", "RemoteServer");
                        testMetadata_Server.put("Sender-identity", "Observer");


                        System.out.println(testMetadata_App.get("nodeid_client") + "]");
                        mapInputStream.close();

                        //application data
                        Measure measureFirstSegment = new Measure("UDPRTT", "Client",
                                "Observer", null, resultApp,
                                cmdSplitted[1], udp_rtt_pktsize, udp_rtt_num_pack,
                                cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                        .toString()), OBSERVERIP);
                        //observer data
                        Measure measureSecondSegment = new Measure("UDPRTT", "Observer",
                                "Server",null, latency, cmdSplitted[1],
                                udp_rtt_pktsize, udp_rtt_num_pack, OBSERVERIP,
                                cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                        .toString()));

                        sendAggregator(measureFirstSegment, measureSecondSegment, testMetadata_App,
                                       testMetadata_Server);
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            Thread.sleep(30 * 1000);

                        }

                    }


                    break;
                }
                case "UDPRTTReceiver": {
                    Map<Integer, Long[]> latency;
                    int udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t\t[Packet size : " + udp_rtt_pktsize);
                    System.out.print(", Number of packes : " + udp_rtt_num_pack + " - ");

                    try {
                        //MO has to receive a packet from the client to know Client's Address and Port
                        byte[] bufrtt = new byte[1000];
                        DatagramPacket dgprtt = new DatagramPacket(bufrtt, bufrtt.length);
                        try {
                            udpListener.receive(dgprtt);
                        } catch (IOException ex) {
                            Logger.getLogger(Observer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        udpListener.connect(dgprtt.getAddress(), dgprtt.getPort());
                        latency = Measurements.UDPRTTSender(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        udpListener.disconnect();
                        if (latency == null)
                        {
                            System.out.println ("Measure failed");
                            //controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            restartSockets();
                            break;
                        }

                        //System.out.println(controlSocketRemote);
                        controlSocketRemote.sendCMD(cmd);

                        DatagramSocket udpsocketRTT = new DatagramSocket();
                        byte[] bufRTT;
                        String outString = "UDPCapacityPPReceiver";
                        bufRTT = outString.getBytes();
                        DatagramPacket out = new DatagramPacket(bufRTT, bufRTT.length, InetAddress.getByName(REMOTEIP), REMOTEUDPPORT);
                        //Client has to send a packet to server to let the server knows Client's IP and Port
                        udpsocketRTT.send(out);
                        int ret = Measurements.UDPRTTReceiver(udpsocketRTT, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (ret < 0 || controlSocketRemote.receiveCMD().compareTo(ControlMessages
                                                              .Messages.SUCCEDED.toString()) != 0) {
                            System.out.println("Measure failed");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            restartSockets();
                            break;
                        }

                        ObjectInputStream mapInputStream = new ObjectInputStream(controlSocketRemote.getSocket().getInputStream());
                        Map<Integer, Long[]> resultRemote = (Map<Integer, Long[]>) mapInputStream.readObject();
                        mapInputStream.close();

                        controlSocketApp.sendCMD(ControlMessages.Messages.GETTESTMETADATA.toString());
                        ObjectInputStream inputStream = new ObjectInputStream(controlSocketApp.getSocket().getInputStream());
                        HashMap<String, String> testMetadata_App = (HashMap<String, String>) inputStream.readObject();

                        HashMap<String, String> testMetadata_Server = new HashMap<>(testMetadata_App);
                        testMetadata_App.put("Sender-identity", "Observer");
                        testMetadata_App.put("AggregatorAddress", AGGREGATORIP);
                        testMetadata_Server.put("AggregatorAddress", AGGREGATORIP);
                        testMetadata_Server.put("RemoteServerAddress", REMOTEIP);
                        testMetadata_Server.put("nodeid_remoteserver", REMOTEID);
                        testMetadata_Server.put("Sender-identity", "RemoteServer");
                        testMetadata_Server.put("Receiver-identity", "Observer");


                        System.out.println(testMetadata_App.get("nodeid_client") + "]");

                        //remote data
                        Measure measureSecondSegment = new Measure("UDPRTT",  "Server",
                                "Observer", null,
                                resultRemote, cmdSplitted[1], udp_rtt_pktsize,
                                udp_rtt_num_pack, cutAddress(controlSocketRemote.getSocket()
                                .getRemoteSocketAddress().toString()), OBSERVERIP);
                        //observer data
                        Measure measureFirstSegment = new Measure("UDPRTT", "Observer",
                                "Client", null, latency, cmdSplitted[1],
                                udp_rtt_pktsize, udp_rtt_num_pack, OBSERVERIP,
                                cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                        .toString()));

                        sendAggregator(measureFirstSegment, measureSecondSegment, testMetadata_App,
                                       testMetadata_Server);

                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
                        inputStream.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            Thread.sleep(30 * 1000);

                        }

                    }

                    break;
                    }
                case "TCPRTTSender": {
                    Map<Integer, Long[]> latency;
                    //the client start a TCP RTT measure using the observer as receiver
                    //the observer starts a TCP RTT measure using the remote server as receiver
                    int tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t\t\t[Packet size : " + tcp_rtt_pktsize);
                    System.out.print(", Number of packes : " + tcp_rtt_num_pack + " - ");

                    try {
                        Socket tcpRTTClient = tcpListener.accept();
                        //first measure (client -> observer -> client)
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        Measurements.TCPRTTReceiver(tcpRTTClient, tcp_rtt_pktsize, tcp_rtt_num_pack);


                        if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Measure failed");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            restartSockets();
                            break;
                        }


                        //second measure (observer -> remote -> observer)
                        controlSocketRemote.sendCMD(cmd);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages.START
                                                                                .toString()) != 0) {
                            System.out.println("Start measure with remote FAILED");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            restartSockets();
                            break;
                        }
                        latency = Measurements.TCPRTTSender(new Socket(InetAddress.getByName(REMOTEIP),
                                REMOTETCPPORT), tcp_rtt_pktsize, tcp_rtt_num_pack);
                        controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());


                        ObjectInputStream mapInputStream = new ObjectInputStream(controlSocketApp.getSocket().getInputStream());
                        Map<Integer, Long[]> resultApp = (Map<Integer, Long[]>) mapInputStream.readObject();
                        HashMap<String, String> testMetadata_App = (HashMap<String, String>) mapInputStream.readObject();
                        mapInputStream.close();
                        HashMap<String, String>  testMetadata_Server = new HashMap<String, String> (testMetadata_App);
                        testMetadata_App.put("Receiver-identity", "Observer");
                        testMetadata_App.put("AggregatorAddress", AGGREGATORIP);
                        testMetadata_Server.put("AggregatorAddress", AGGREGATORIP);
                        testMetadata_Server.put("RemoteServerAddress", REMOTEIP);
                        testMetadata_Server.put("nodeid_remoteserver", REMOTEID);
                        testMetadata_Server.put("Receiver-identity", "RemoteServer");
                        testMetadata_Server.put("Sender-identity", "Observer");


                        System.out.println(testMetadata_App.get("nodeid_client") + "]");


                        //application data
                        Measure measureFirstSegment = new Measure("TCPRTT","Client",
                                "Observer", null, resultApp,
                                cmdSplitted[1], tcp_rtt_pktsize, tcp_rtt_num_pack,
                                cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                        .toString()), OBSERVERIP);
                        //observer data
                        Measure measureSecondSegment = new Measure("TCPRTT","Observer",
                                "Server", null, latency, cmdSplitted[1],
                                tcp_rtt_pktsize, tcp_rtt_num_pack, OBSERVERIP,
                                cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                        .toString()));

                        sendAggregator(measureFirstSegment, measureSecondSegment, testMetadata_App,
                                       testMetadata_Server);
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            Thread.sleep(30 * 1000);

                        }

                    }

                    break;
                }
                case "TCPRTTReceiver": {
                    Map<Integer, Long[]> latency;
                    Socket tcpRTT = null;
                    //MO sends metrics client-observer and forwards the command to the server
                    int tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t\t[Packet size : " + tcp_rtt_pktsize);
                    System.out.print(", Number of packes : " + tcp_rtt_num_pack + " - ");

                    try {
                        tcpRTT = tcpListener.accept();

                        latency = Measurements.TCPRTTSender(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);


                        controlSocketRemote.sendCMD(cmd);
                        Measurements.TCPRTTReceiver(new Socket(InetAddress.getByName(REMOTEIP),
                                                    REMOTETCPPORT), tcp_rtt_pktsize, tcp_rtt_num_pack);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                .SUCCEDED.toString()) != 0) {
                            System.out.println("Measure failedjb");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            restartSockets();
                            break;
                        }


                        ObjectInputStream mapInputStream = new ObjectInputStream(controlSocketRemote.getSocket().getInputStream());
                        Map<Integer, Long[]> resultRemote = (Map<Integer, Long[]>) mapInputStream.readObject();
                        mapInputStream.close();

                        controlSocketApp.sendCMD(ControlMessages.Messages.GETTESTMETADATA.toString());
                        ObjectInputStream inputStream = new ObjectInputStream(controlSocketApp.getSocket().getInputStream());
                        HashMap<String, String> testMetadata_App = (HashMap<String, String>) inputStream.readObject();

                        HashMap<String, String> testMetadata_Server = new HashMap<>(testMetadata_App);
                        testMetadata_App.put("Sender-identity", "Observer");
                        testMetadata_App.put("AggregatorAddress", AGGREGATORIP);
                        testMetadata_Server.put("AggregatorAddress", AGGREGATORIP);
                        testMetadata_Server.put("RemoteServerAddress", REMOTEIP);
                        testMetadata_Server.put("nodeid_remoteserver", REMOTEID);
                        testMetadata_Server.put("Sender-identity", "RemoteServer");
                        testMetadata_Server.put("Receiver-identity", "Observer");

                        System.out.println(testMetadata_App.get("nodeid_client") + "]");

                        //remote data
                        Measure measureSecondSegment = new Measure("TCPRTT", "Server",
                                "Observer", null, resultRemote,
                                cmdSplitted[1], tcp_rtt_pktsize, tcp_rtt_num_pack , "" +
                                cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                        .toString()), OBSERVERIP);
                        //observer data
                        Measure measureFirstSegment = new Measure("TCPRTT", "Observer",
                                "Client",null, latency, cmdSplitted[1],
                                tcp_rtt_pktsize, tcp_rtt_num_pack,  OBSERVERIP,
                                cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                        .toString()));


                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
                        inputStream.close();
                        sendAggregator(measureFirstSegment, measureSecondSegment, testMetadata_App,
                                       testMetadata_Server);


                    } catch (Exception ex) {
                        //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            Thread.sleep(30 * 1000);

                        }

                    }
                    break;
                }
            }
            //System.out.println(controlSocketRemote.getSocket());
            if (controlSocketRemote != null  && controlSocketRemote.getSocket() != null  && !controlSocketRemote.getSocket().isClosed())
                controlSocketRemote.closeConnection();

            if (controlSocketApp != null  && controlSocketApp.getSocket() != null && !controlSocketApp.getSocket().isClosed())
                controlSocketApp.closeConnection();
        }
    }



    private static boolean checkArguments(){
        try {
            //check OBSERVERIP
            if (OBSERVERIP == null){
                System.out.println("Error: OBSERVERIP cannot be null");
                return false;
            }
            if (!(InetAddress.getByName(OBSERVERIP) instanceof Inet4Address)) {
                System.out.println("Error: OBSERVERIP is not an IPv4Address");
                return false;
            }

            //check AGGREGATORIP
            if (AGGREGATORIP == null){
                System.out.println("Error: AGGREGATORIP cannot be null");
                return false;
            }
            if (!(InetAddress.getByName(AGGREGATORIP) instanceof Inet4Address)) {
                System.out.println("Error: AGGREGATOR is not an IPv4Address");
                return false;
            }

            //check REMOTEIP
            if (REMOTEIP == null){
                System.out.println("Error: REMOTEIP cannot be null");
                return false;
            }
            if (!(InetAddress.getByName(REMOTEIP) instanceof Inet4Address)) {
                System.out.println("Error: REMOTEIP is not an IPv4Address");
                return false;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //check REMOTE ports
        if (REMOTECMDPORT < 0){
            System.out.println("Error: REMOTECMDPORT cannot be negative");
            return false;
        }
        if (REMOTETCPPORT < 0){
            System.out.println("Error: REMOTETCPPORT cannot be negative");
            return false;
        }
        if (REMOTEUDPPORT < 0){
            System.out.println("Error: REMOTEUDPPORT cannot be negative");
            return false;
        }

        //check OBSERVER ports
        if (OBSCMDPORT < 0){
            System.out.println("Error: OBSCMDPORT cannot be negative");
            return false;
        }
        if (OBSTCPPORT < 0){
            System.out.println("Error: OBSTCPPORT cannot be negative");
            return false;
        }
        if (OBSUDPPORT < 0){
            System.out.println("Error: OBSUDPPORT cannot be negative");
            return false;
        }

        //check AGGREGATOR port
        if (AGGRPORT < 0){
            System.out.println("Error: AGGRPORT cannot be negative");
            return false;
        }

        return true;
    }



    private static void parseArguments(String[] args){
        for (int i = 0; i< args.length; i++) {

            if (args[i].equals("-a") || args[i].equals("--aggregator-ip")) {
                AGGREGATORIP = args[++i];
                continue;
            }
            if (args[i].equals("-r") || args[i].equals("--remote-ip")) {
                REMOTEIP = args[++i];
                continue;
            }
            if (args[i].equals("-rid") || args[i].equals("--remote-id")) {
                REMOTEID= args[++i];
                continue;
            }
            if (args[i].equals("-ap") || args[i].equals("--aggregator-port")) {
                AGGRPORT = Integer.parseInt(args[++i]);
                continue;
            }

            if (args[i].equals("-rtp") || args[i].equals("--remote-tcp-port")) {
                REMOTETCPPORT = Integer.parseInt(args[++i]);
                continue;
            }

            if (args[i].equals("-rup") || args[i].equals("--remote-udp-port")) {
                REMOTEUDPPORT = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-rcp") || args[i].equals("--remote-cmd-port")) {
                REMOTECMDPORT = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-otp") || args[i].equals("--observer-tcp-port")) {
                OBSTCPPORT = Integer.parseInt(args[++i]);
                continue;
            }

            if (args[i].equals("-oup") || args[i].equals("--observer-udp-port")) {
                OBSUDPPORT = Integer.parseInt(args[++i]);
                continue;
            }

            if (args[i].equals("-ocp") || args[i].equals("--observer-cmd-port")) {
                OBSCMDPORT = Integer.parseInt(args[++i]);
                continue;
            }

            System.out.println("Unknown command " + args[i]);
        }
    }



    private static String cutAddress(String address){
        address = address.replace("/", "");
        return address.substring(0, address.indexOf(":"));
    }



    private static String getAddress(){
        Enumeration enumerator = null;
        try {
            enumerator = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        while(enumerator.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) enumerator.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                System.out.print(i.getHostAddress());


                if (i instanceof Inet6Address) {
                    System.out.println("\t IPv6 address (next)");

                    continue;
                }
                else
                    if (i instanceof Inet4Address) {
                        try {
                            InetAddress localhostAddress = InetAddress.getByName("127.0.0.1");
                            InetAddress localhostAddress2 = InetAddress.getByName("127.0.1.1");
                            InetAddress nullAddress = InetAddress.getByName("0.0.0.0");


                            if (i.equals(localhostAddress) || i.equals(localhostAddress2)){
                                System.out.println("\t localhost address (next)");

                                continue;
                            }
                            if (i.equals(nullAddress)){
                                System.out.println("\t null address (next)");

                                continue;
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                        System.out.println("\t IPv4 address (take)");
                        return i.toString();
                    }
            }
        }

        return null;
    }

    protected static String generate_metadataJSON(HashMap<String, String> metadata, String segment){
        String Metadata_JSON = "\"metadata_" + segment + "_segment\":{";
        int i = 0;
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            if (i != 0)
                Metadata_JSON += ",";
            Metadata_JSON += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
            i++;
        }
        return Metadata_JSON + "}"; 
    }
    protected static String generate_RequestJSONPayload(Measure measureFirstSegment, Measure measureSecondSegment,
                                         HashMap<String, String> metadataFirstSegment,
                                         HashMap<String, String> metadataSecondSegment){
        
        
        String firstsegment_TestJSON = measureFirstSegment.getTestJSON("first");
        String secondsegment_TestJSON = measureSecondSegment.getTestJSON("second");
        String firstsegment_valuesJSON = measureFirstSegment.getValuesJSON("first");
        String secondsegment_valuesJSON = measureSecondSegment.getValuesJSON("second");
        if (firstsegment_TestJSON == null || secondsegment_TestJSON == null ||
            firstsegment_valuesJSON == null || secondsegment_valuesJSON == null)
            return null;
        
        String request_JSON = "{" + firstsegment_TestJSON + "," + secondsegment_TestJSON + "," + 
                              firstsegment_valuesJSON + "," + secondsegment_valuesJSON;
        
        if (metadataFirstSegment != null && metadataSecondSegment != null){
            if (metadataFirstSegment.size() == 0 || metadataSecondSegment.size() == 0)
                return null;

            String firstsegment_metadataJSON = generate_metadataJSON(metadataFirstSegment, "first");
            String secondsegment_metadataJSON = generate_metadataJSON(metadataFirstSegment, "second");
            
            request_JSON +=  "," + firstsegment_metadataJSON + "," + secondsegment_metadataJSON;
        }
        
        return request_JSON + "}";
     }
    protected static String generate_RequestURL(Measure measureFirstSegment){
        String measurementType = null;
        if (measureFirstSegment.getType().equals("UDPBandwidth") || 
            measureFirstSegment.getType().equals("TCPBandwidth"))
            measurementType = "bandwidth";
        else if (measureFirstSegment.getType().equals("UDPRTT") || 
                 measureFirstSegment.getType().equals("TCPRTT"))
            measurementType = "latency";
        else
            return null;
    
        return "http://" + AGGREGATORIP + ":" + AGGRPORT + "/post_active_measures/insert_" + measurementType + "_measure";
     }
    protected static void sendPOSTRequest(String payloadPOST, String serverURL){
        try {
            URL url = new URL(serverURL);
            System.out.println("connection to: " + url); // deb
            System.out.println("paylod post: " + payloadPOST); // deb
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("HTTP-Version", "HTTP/1.1");//deb
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            //deb
            try (OutputStream output = connection.getOutputStream()) {
                output.write(query.getBytes("UTF-8"));
                System.out.println(output.write(query.getBytes("UTF-8")));
            }
            System.out.println(output);
            //deb//
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payloadPOST);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer jsonString = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected static void sendAggregator(Measure measureFirstSegment, Measure measureSecondSegment,
                                         HashMap<String, String> metadataFirstSegment,
                                         HashMap<String, String> metadataSecondSegment){        
        String payloadPOST = generate_RequestJSONPayload(measureFirstSegment, measureSecondSegment,
                                                metadataFirstSegment, metadataSecondSegment);
        if (payloadPOST == null){
            System.out.println("empty POST paylod. Returning");
            return;
        }

        String serverURL = generate_RequestURL(measureFirstSegment);
        if (serverURL == null) {
            System.out.println("empty destination URL. Returning");
            return;
        }

        //System.out.println("Sending to " + serverURL);
        sendPOSTRequest(payloadPOST, serverURL);
    }
}





