package it.unipi.dii.common;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;



public class Measurements {
    private static final long serialVersionUID = 3919700812200232178L;
    private static final int timer= 3 * 1000;



    /**
     * This function is used by the receiver to measure the RTT using the UDP protocol
     * The receiver receives a packet and sends another packet to the sender
     *
     * @param serverSocket The Socket used for the communication
     * @param udp_rtt_pktsize The size of each UDP packet used
     * @param udp_rtt_num_pack  The number of UDP packets used
     * @throws IOException
     */
    public static int UDPRTTReceiver(DatagramSocket serverSocket, int  udp_rtt_pktsize,
                                      int udp_rtt_num_pack) {
        byte[] sendData = new byte[udp_rtt_pktsize];
        byte[] receiveData = new byte[udp_rtt_pktsize];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);


        int receivedPkt = 0;
        try {
            serverSocket.setSoTimeout(timer);

            for (; receivedPkt < udp_rtt_num_pack; receivedPkt++) {

                serverSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress();
                sendPacket.setAddress(IPAddress);
                int port = receivePacket.getPort();
                sendPacket.setPort(port);
                serverSocket.send(sendPacket);
            }
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("RECEIVED " + receivedPkt + " of " +  udp_rtt_num_pack  +
                               " packets");

            return -1;
        }

        return 0;
    }



    /**
     * This function is used by the sender to connect with the receiver using the UDP protocol.
     * The sender starts the communication sending a packet and when receive a packet compute the latency.
     * After NUMLATENCYTEST communications, the function computes the mean latency value.
     *
     * @param connectionSocket The Socket use for the comunication
     * @param udp_rtt_pktsize The size of each UDP packet used
     * @param udp_rtt_num_pack The number of UDP packets used
     * @return -1 if the timeout expired. Otherwise the mean latency value calculated for a RTT
     *         measure is returned
     */
    public static Map<Integer, Long[]> UDPRTTSender(DatagramSocket connectionSocket,
                                                    int  udp_rtt_pktsize, int udp_rtt_num_pack) {
        byte[] sendData = new byte[udp_rtt_pktsize];
        byte[] receiveData = new byte[udp_rtt_pktsize];
        Random rand = new Random();
        rand.nextBytes(sendData);

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        Map<Integer, Long[]> mappa = new LinkedHashMap<>();

        int receivedPkt = 0;
        try {
            connectionSocket.setSoTimeout(timer);

            for (; receivedPkt < udp_rtt_num_pack; receivedPkt++) {
                long startTime = System.currentTimeMillis();
                connectionSocket.send(sendPacket);
                connectionSocket.receive(receivePacket);
                long endTime = System.currentTimeMillis();

                Long[] mapValue = new Long[2];
                mapValue[0] = endTime - startTime;
                mapValue[1] = endTime;

                mappa.put(new Integer (receivedPkt), mapValue);
            }
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("RECEIVED " + receivedPkt + " of " +  udp_rtt_num_pack  + " packets");

            return null;
        }

        return mappa;
    }



    /**
     * This function is used by the sender to connect with the receiver using the TCP protocol.
     * The sender starts the communication sending a packet and when receive a packet compute the latency.
     * After NUMLATENCYTEST communications, the function computes the mean latency value.
     *
     * @param socket The Socket use for the comunication
     * @param tcp_rtt_pktsize The size of each TCP packet used
     * @param tcp_rtt_num_pack The number of TCP packets used
     * @return The mean latency calculated using the Round Trip Time
     * @throws IOException
     */
    public static Map<Integer, Long[]> TCPRTTSender(Socket socket, int tcp_rtt_pktsize,
                                                    int tcp_rtt_num_pack) {
        OutputStream outputStream;
        InputStream inputStream;
        Map<Integer, Long[]> mappa = new LinkedHashMap<>();

        byte[] sendData = new byte[tcp_rtt_pktsize];
        Random rand = new Random();
        rand.nextBytes(sendData);

        int receivedPkt = 0;

        try {
            socket.setSoTimeout(timer);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            long lBegin,
                 lEnd;

            for (; receivedPkt < tcp_rtt_num_pack; receivedPkt++) {
                outputStream.write(sendData, 0, tcp_rtt_pktsize);
                lBegin = System.currentTimeMillis();
                int ret = 0;
                while (ret < tcp_rtt_pktsize){
                    ret += inputStream.read(sendData);
                }
                lEnd = System.currentTimeMillis();

                Long[] mapValue = new Long[2];
                mapValue[0] = lEnd - lBegin;
                mapValue[1] = lEnd;

                mappa.put(new Integer (receivedPkt), mapValue);
            }


            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("RECEIVED " + receivedPkt + " of " +  tcp_rtt_num_pack  + " packets");

            return null;

        }
        return mappa;
    }



    /**
     * This function is used by the receiver to measure the RTT using the UDP protocol
     * The receiver receives a packet and sends another packet to the sender
     *
     * @param sock The Socket use for the comunication
     * @param tcp_rtt_pktsize The size of each TCP packet used
     * @param tcp_rtt_num_pack The number of TCP packets used
     * @throws IOException
     */
    public static void TCPRTTReceiver(Socket sock, int tcp_rtt_pktsize, int tcp_rtt_num_pack)
                                                                                throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        byte[] sendData = new byte[tcp_rtt_pktsize]; //TODO BUG: se diverso da 1 Causa Eccezione
        Random rand = new Random();
        rand.nextBytes(sendData);

        int receivedPkt = 0;

        try {
            sock.setSoTimeout(timer);//timeout = 30s
            inputStream = sock.getInputStream();
            outputStream = sock.getOutputStream();
           // System.out.print("TCPRTTReceiver: ");
            for (; receivedPkt < tcp_rtt_num_pack; receivedPkt++) {
                int ret = 0;
                while (ret < tcp_rtt_pktsize){
                    ret += inputStream.read(sendData);
                }

                outputStream.write(sendData, 0, tcp_rtt_pktsize);
            }


            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (sock != null)
                sock.close();

        }

        catch (IOException e){

            System.out.println("RECEIVED " + receivedPkt + " of " +  tcp_rtt_num_pack  + " packets");

            throw e;
        }
    }



    /**
     * This function is used by the sender to connect with the receiver using the TCP protocol.
     * The sender send a file through the network
     *
     * @param socket The Socket use for the comunication
     * @param number_of_bytes the number of bytes to be transferred
     * @param tcp_bandwidth_pktsize The size of each TCP packet used
     * @throws IOException
     */
    public static void TCPBandwidthSender(Socket socket, int number_of_bytes,
                                          int tcp_bandwidth_pktsize) throws IOException {
        OutputStream outputStream = null;
        byte[] buffer = new byte[tcp_bandwidth_pktsize];
        Random random = new Random();
        random.nextBytes(buffer);//fill the buffer with random bytes
        int measureSize = number_of_bytes;

        try {
            outputStream = socket.getOutputStream();


            while (number_of_bytes > 0) {
                if (number_of_bytes < tcp_bandwidth_pktsize) {
                    outputStream.write(buffer, 0, number_of_bytes);
                    number_of_bytes = 0;
                } else {
                    outputStream.write(buffer, 0, tcp_bandwidth_pktsize);
                    number_of_bytes -= tcp_bandwidth_pktsize;
                }
            }


            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e){
            System.out.println("REMAINING " + number_of_bytes + " of " + measureSize + " bytes");
            throw e;
        }
    }




    /**
     * This function is used by the receiver to connect with the sender using the TCP protocol.
     * The receiver receive the file and for each packet save in the map the timestamp and the amount of
     * data received.
     *
     * @param connectionSocket The Socket use for the comunication
     * @param tcp_bandwidth_pktsize The size of each TCP packet used
     * @return The map that contain all the timestamps and the amount of data for each packet received
     */
    public static Map<Integer, Long[]>  TCPBandwidthReceiver(Socket connectionSocket,
                                                          int tcp_bandwidth_pktsize) throws IOException {

        InputStream isr = null;
        Map<Integer, Long[]> mappa = new LinkedHashMap<>();
        int totalRead;
        byte[] cbuf = new byte[tcp_bandwidth_pktsize];

        int receivedBytes = 0;

        try {
            connectionSocket.setSoTimeout(timer);//timeout = 30s
            isr = connectionSocket.getInputStream();

            long last = 0;
            int i = 0;

            while ((totalRead = isr.read(cbuf)) != -1) {

                long actualTime = System.nanoTime();
                long diff =  actualTime - last;

                if (diff < 0)
                    System.exit(1);

                Long[] mapValue = new Long[2];
                mapValue[0] = actualTime;
                mapValue[1] = new Long (totalRead);

                mappa.put(new Integer (i), mapValue);

                last = actualTime;
                i++;

                receivedBytes += totalRead;
            }


            if (isr != null)
                isr.close();
            if (connectionSocket != null)
                connectionSocket.close();
        }

        catch (IOException e){
            System.out.println("RECEIVED " + receivedBytes + "bytes");
            throw e;
        }

        return mappa;
    }



    /**
     * This function is used by the sender to connect with the receiver using the UDP protocol.
     * This function send two datagram packets through the network
     *
     *
     * @param connectionSocket The Socket used for the comunication
     * @param packet_size The size of each packet used
     */
    public static int UDPCapacityPPSender(DatagramSocket connectionSocket, int packet_size, int numTest, ControlMessages controlSocket) {
        //System.out.println(numTest);
        int numberOfTestPerformed = 0;
        for (; numberOfTestPerformed < numTest; numberOfTestPerformed++) {
            byte[] buf = new byte[packet_size];
            Random rand = new Random();
            rand.nextBytes(buf);

            DatagramPacket packet1 = new DatagramPacket(buf, buf.length);
            DatagramPacket packet2 = new DatagramPacket(buf, buf.length);

            //send 2 packets
            try {
                String receivedCommand = controlSocket.receiveCMD();

                if (receivedCommand == null) {
                    System.out.println("Measure failed: received command is null");
                    return -1;
                }
                if (receivedCommand.compareTo(ControlMessages.Messages.START
                        .toString()) != 0) {
                    return -1;
                }

                connectionSocket.send(packet1);
                connectionSocket.send(packet2);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(numberOfTestPerformed + " of " + numTest + "test done");

                return -1;
            }
        }

        return 0;
    }



    /**
     * This function is used by the receiver to connect with the sender using the UDP protocol.
     * This function receive two datagram packets and compute the difference between the two timestamps
     *
     * @param serverSocket The Socket use for the comunication
     * @param pktSize    Amount of data to receive for each comunication
     * @return The latency calculated using the packet pair approach
     */
    public static Map<Integer, Long[]> UDPCapacityPPReceiver (DatagramSocket serverSocket, int pktSize, int numTest, ControlMessages controlSocket) {
        Map<Integer, Long[]> measureResult = new LinkedHashMap<>();

        int numberOfTestPerformed = 0;

        for (; numberOfTestPerformed < numTest; numberOfTestPerformed++) {

            byte[] receiveData1 = new byte[pktSize];
            byte[] receiveData2 = new byte[pktSize];

            long firstTime = 0,
                    currentTime,
                    timeNs;

            //wait for first packet on socket
            DatagramPacket receivePacket1 = new DatagramPacket(receiveData1, receiveData1.length);
            DatagramPacket receivePacket2 = new DatagramPacket(receiveData2, receiveData2.length);

            try {
                controlSocket.sendCMD(ControlMessages.Messages.START.toString());

                serverSocket.setSoTimeout(timer);//timeout = 2min

                serverSocket.receive(receivePacket1);
                firstTime = System.nanoTime();
                //receive the second packet
                serverSocket.receive(receivePacket2);

                //System.out.println("received " + i);
            } catch (IOException e) {
                e.printStackTrace();

                System.out.println(numberOfTestPerformed + " of " + numTest + "test done");

                return null;
            }

            currentTime = System.nanoTime();
            timeNs = currentTime - firstTime;

            Long[] mapValue = new Long[2];
            mapValue[0] = timeNs;
            mapValue[1] = (long)pktSize;

            measureResult.put(numberOfTestPerformed, mapValue);

        }

        return measureResult;
    }
}

