package it.unipi.dii.mecperfng.commandlineapp;


import java.util.HashMap;
import java.util.Map;
import it.unipi.dii.mecperfng.MainUtils;



public class CommandLineApp {
    private static int CMDPORT = -1,
                       TCPPORT = -1,
                       UDPPORT = -1,
                       pktSizeTCPBandwidth = -1,
                       pktSizeUDPCapacity = -1,
                       pktSizeTCPLatency = -1,
                       pktSizeUDPLatency = -1,
                       numPktTCPBandwidth = -1,
                       numTestsUDPCapacity = -1,
                       numPktTCPLatency = -1,
                       numPktUDPLatency = -1,
                       timerInterval = -1,
                       attemptTimer = 5 * 1000,
                       MAXATTEMPTNUMBER = 3,
                       numberofclients = -1;

    private static String observerAddress = null,
                          measureType = "all",
                          direction = "all",
                          keyword = "DEFAULT",
                          interfaceName = null,
                          client_nodeID = "NA",
                          crosstraffic = "NA",
                          observerposition = "NA",
                          accesstechnology = "NA",
                          observerID="NA";



    public static void main(String[] args){
        parseArguments(args);

        System.out.println("NodeID (client): " + client_nodeID);
        System.out.println("Observer address: " + observerAddress);
        System.out.println("NodeID (observer): " + observerID);
        System.out.println("Command port: " + CMDPORT);
        System.out.println("TCP port: " + TCPPORT);
        System.out.println("UDP port: " + UDPPORT);

        System.out.println("Type of measure: " + measureType);
        System.out.println("direction: " + direction);
        System.out.println("Keyword: " + keyword);

        System.out.println("pktSizeTCPBandwidth: " + pktSizeTCPBandwidth);
        System.out.println("pktSizeUDPCapacity: " + pktSizeUDPCapacity);
        System.out.println("pktSizeTCPLatency: " + pktSizeTCPLatency);
        System.out.println("pktSizeUDPLatency: " + pktSizeUDPLatency);
        System.out.println("numPktTCPBandwidth: " + numPktTCPBandwidth);
        System.out.println("numTestsUDPCapacity: " + numTestsUDPCapacity);
        System.out.println("numPktTCPLatency: " + numPktTCPLatency);
        System.out.println("numPktUDPLatency: " + numPktUDPLatency);
        System.out.println("MAXATTEMPTNUMBER: " + MAXATTEMPTNUMBER);


        if (interfaceName != null) {
            System.out.println("interface name: " + interfaceName);
        }


        if (timerInterval < 0)
            startMeasure();
        else{
            System.out.println("\n\nTimer interval = " + timerInterval);

            while (true){
                startMeasure();
                System.out.println("\n*************************** SLEEPING ***************************\n");

                try {
                    Thread.sleep(timerInterval * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private static void parseArguments(String[] args){
        for (int i = 0; i< args.length; i++) {

            if (args[i].equals("-o") || args[i].equals("--observer-address")) {
                observerAddress = args[++i];
                continue;
            }
            if (args[i].equals("-c") || args[i].equals("--command-port")){
                 CMDPORT= Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-t") || args[i].equals("--tcp-port")){
                TCPPORT = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-u") || args[i].equals("--udp-port")){
                UDPPORT = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-m") || args[i].equals("--measure-type")){
                 measureType = args[++i];
                continue;
            }
            if (args[i].equals("-d") || args[i].equals("--direction")){
                direction = args[++i];
                continue;
            }
            if (args[i].equals("-k") || args[i].equals("--keyword")){
                 keyword= args[++i];
                continue;
            }

            if (args[i].equals("-stb") || args[i].equals("--packet-size-tcp-bandwidth")){
                pktSizeTCPBandwidth = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-suc") || args[i].equals("--packet-size-udp-capacity")){
                pktSizeUDPCapacity = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-nuc") || args[i].equals("--num-tests-udp-capacity")){
                numTestsUDPCapacity = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-ntb") || args[i].equals("--num-packet-tcp-bandwidth")){
                numPktTCPBandwidth = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-stl") || args[i].equals("--packet-size-tcp-latency")){
                pktSizeTCPLatency = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-sul") || args[i].equals("--packet-size-udp-latency")){
                pktSizeUDPLatency = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-ntl") || args[i].equals("--num-packet-tcp-latency")){
                numPktTCPLatency = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-nul") || args[i].equals("--num-packet-udp-latency")){
                numPktUDPLatency = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-t") || args[i].equals("--timer-interval")){
                timerInterval = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-i") || args[i].equals("--interface-name")){
                interfaceName = args[++i];
                continue;
            }
            if (args[i].equals("-nid") || args[i].equals("--node-id")) {
                client_nodeID = args[++i];
                continue;
            }
            if (args[i].equals("-noc") || args[i].equals("--number-of-clients")){
                if (args[++i] != null)
                    numberofclients = Integer.parseInt(args[i]);
                continue;
            }
            if (args[i].equals("-ct") || args[i].equals("--crosstraffic")) {
                crosstraffic = args[++i];
                continue;
            }
            if (args[i].equals("-obsp") || args[i].equals("--observer-position")) {
                observerposition = args[++i];
                continue;
            }
            if (args[i].equals("-at") || args[i].equals("--access-technology")) {
                accesstechnology = args[++i];
                continue;
            }
            if (args[i].equals("-obsid") || args[i].equals("--observer-id")) {
                observerID = args[++i];
                continue;
            }

            System.out.println("Unknown command " + args[i]);
        }
    }


    private static HashMap<String, String> initializeTestMetadata(String measurementType, String measurementDirection){
        HashMap<String, String> testMetadata_client = new HashMap<>();

        testMetadata_client.put("numberofclients", String.valueOf(numberofclients));
        testMetadata_client.put("nodeid_client", client_nodeID);
        testMetadata_client.put("interfacename_client", interfaceName);
        testMetadata_client.put("accesstechnology_client", accesstechnology);
        testMetadata_client.put("crosstraffic", crosstraffic);
        testMetadata_client.put("command", measurementType);
        testMetadata_client.put("measure-type", "active");

        testMetadata_client.put("observerposition", observerposition);
        testMetadata_client.put("ObserverAddress", observerAddress);
        testMetadata_client.put("nodeid_observer", observerID);
        testMetadata_client.put("ObserverCMDPort", String.valueOf(CMDPORT));




        switch (measurementType){
            case "bandwidthTCP":
                testMetadata_client.put("TCPPort", String.valueOf(TCPPORT));
                testMetadata_client.put("pktsize-TCPBandwidth", String.valueOf(pktSizeTCPBandwidth));
                testMetadata_client.put("numpkt-TCPBandwidth", String.valueOf(numPktTCPBandwidth));
                break;
            case "bandwidthUDP":
                testMetadata_client.put("UDPPort", String.valueOf(UDPPORT));
                testMetadata_client.put("pktsize-UDPCapacity", String.valueOf(pktSizeUDPCapacity));
                testMetadata_client.put("numtests-UDPCapacity", String.valueOf(numTestsUDPCapacity));
                break;
            case "latencyTCP":
                testMetadata_client.put("TCPPort", String.valueOf(TCPPORT));
                testMetadata_client.put("pktsize-TCPRTT", String.valueOf(pktSizeTCPLatency));
                testMetadata_client.put("numtests-TCPRTT", String.valueOf(numPktTCPLatency));
                break;
            case "latencyUDP":
                testMetadata_client.put("UDPPort", String.valueOf(UDPPORT));
                testMetadata_client.put("pktsize-UDPRTT", String.valueOf(pktSizeUDPLatency));
                testMetadata_client.put("numtests-UDPRTT", String.valueOf(numPktUDPLatency));
                break;
            default:
                System.out.println("Unknown measurement type " + measurementType);
        }
        testMetadata_client.put("direction", measurementDirection);
        testMetadata_client.put("keyword", keyword);
        testMetadata_client.put("MAXnumberofattempt", String.valueOf(MAXATTEMPTNUMBER));
        testMetadata_client.put("experiment_timer", String.valueOf(timerInterval));

        return testMetadata_client;
    }




    private static void startMeasure() {
        int ret;

        if (measureType.equalsIgnoreCase("bandwidthTCP") ||
            measureType.equalsIgnoreCase("all")){

            if (numPktTCPBandwidth < 0){
                System.out.println("Error: numPktTCPBandwidth missing");
                System.exit(1);
            }
            if (pktSizeTCPBandwidth < 0){
                System.out.println("Error: pktSizeTCPBandwidth missing");
                System.exit(1);
            }


            if (direction.equalsIgnoreCase("receiver") ||
                direction.equalsIgnoreCase("all")) {
                int remainingAttempt = MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: TCP bandwidth Receiver(" +
                                       numPktTCPBandwidth + "packets of " + pktSizeTCPBandwidth +
                                       "bytes)\t\t\t[Attempt #" + (MAXATTEMPTNUMBER - remainingAttempt) + "]");


                    ret = MainUtils.tcpBandwidthMeasure("Receiver", keyword, CMDPORT,
                                                        observerAddress, TCPPORT,
                                                        pktSizeTCPBandwidth, numPktTCPBandwidth,
                                                        interfaceName);


                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (direction.equalsIgnoreCase("sender") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt= MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: TCP bandwidth Sender (" +
                                       numPktTCPBandwidth + "packets of " + pktSizeTCPBandwidth +
                                       "bytes)\t\t\t[Attempt #" + (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret = MainUtils.tcpBandwidthMeasure("Sender", keyword, CMDPORT,
                                                        observerAddress, TCPPORT, pktSizeTCPBandwidth,
                                                        numPktTCPBandwidth, interfaceName);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (measureType.equalsIgnoreCase("bandwidthUDP") ||
            measureType.equalsIgnoreCase("all")){

            if (pktSizeUDPCapacity < 0){
                System.out.println("Error: pktSizeUDPCapacity missing");
                System.exit(1);
            }
            if (numTestsUDPCapacity < 0){
                System.out.println("Error: numTestsUDPCapacity missing");
                System.exit(1);
            }


            if (direction.equalsIgnoreCase("receiver") ||
                direction.equalsIgnoreCase("all")) {
                int remainingAttempt= MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: UDP capacity Receiver (" +
                                       numTestsUDPCapacity + " tests of " + pktSizeUDPCapacity +
                                       "bytes)\t\t\t[Attempt #" +
                                       (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret = MainUtils.udpBandwidthMeasure("Receiver", keyword, CMDPORT,
                                                        observerAddress, UDPPORT,
                            pktSizeUDPCapacity, interfaceName, numTestsUDPCapacity);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (direction.equalsIgnoreCase("sender") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt= MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: UDP capacity Sender (" +
                                       numTestsUDPCapacity+ " tests of " + pktSizeUDPCapacity +
                                       "bytes)\t\t\t\t[Attempt #" +
                                       (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret = MainUtils.udpBandwidthMeasure("Sender", keyword, CMDPORT,
                                                        observerAddress, UDPPORT,
                            pktSizeUDPCapacity, interfaceName, numTestsUDPCapacity);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        if (measureType.equalsIgnoreCase("latencyTCP")  ||
            measureType.equalsIgnoreCase("RTT") ||
            measureType.equalsIgnoreCase("all")){

            if (pktSizeTCPLatency < 0){
                System.out.println("Error: pktSizeTCPLatency missing");
                System.exit(1);
            }
            if (numPktTCPLatency < 0){
                System.out.println("Error: numPktTCPLatency missing");
                System.exit(1);
            }

            if (direction.equalsIgnoreCase("receiver") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt= MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    HashMap<String, String> testMetadata_client = initializeTestMetadata("latencyTCP", "receiver");
                    int attemptnumber = MAXATTEMPTNUMBER - remainingAttempt;
                    int numberoffailures = attemptnumber - 1;
                    testMetadata_client.put("number-of-attempts", String.valueOf(attemptnumber));
                    testMetadata_client.put("Number-of-failures", String.valueOf(numberoffailures));

                    System.out.println("Type of measure: TCP RTT Receiver (" + numPktTCPLatency +
                                       "packets of " + pktSizeTCPLatency +
                                       "bytes)\t\t\t\t\t[Attempt #" +
                                       (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret =  MainUtils.tcpRTTMeasure("Receiver", keyword, CMDPORT,
                                                   observerAddress, TCPPORT, pktSizeTCPLatency,
                                                   numPktTCPLatency, interfaceName, testMetadata_client);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (direction.equalsIgnoreCase("sender") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt = MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    HashMap<String, String> testMetadata_client = initializeTestMetadata("latencyTCP", "sender");
                    int attemptnumber = MAXATTEMPTNUMBER - remainingAttempt;
                    int numberoffailures = attemptnumber - 1;
                    testMetadata_client.put("number-of-attempts", String.valueOf(attemptnumber));
                    testMetadata_client.put("Number-of-failures", String.valueOf(numberoffailures));

                    System.out.println("Type of measure: TCP RTT Sender (" + numPktTCPLatency +
                                       "packets of " + pktSizeTCPLatency +
                                       "bytes)\t\t\t\t\t[Attempt #" +
                                       (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret =  MainUtils.tcpRTTMeasure("Sender", keyword, CMDPORT,
                                                   observerAddress, TCPPORT, pktSizeTCPLatency,
                                                   numPktTCPLatency, interfaceName, testMetadata_client);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (measureType.equalsIgnoreCase("latencyUDP")  ||
            measureType.equalsIgnoreCase("RTT") ||
            measureType.equalsIgnoreCase("all")){
            if (pktSizeUDPLatency < 0){
                System.out.println("Error: pktSizeUDPLatency missing");
                System.exit(1);
            }
            if (numPktUDPLatency < 0){
                System.out.println("Error: numPktUDPLatency missing");
                System.exit(1);
            }


            if (direction.equalsIgnoreCase("receiver") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt = MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;
                    
                    HashMap<String, String> testMetadata_client = initializeTestMetadata("latencyUDP", "receiver");
                    int attemptnumber = MAXATTEMPTNUMBER - remainingAttempt;
                    int numberoffailures = attemptnumber - 1;
                    testMetadata_client.put("number-of-attempts", String.valueOf(attemptnumber));
                    testMetadata_client.put("Number-of-failures", String.valueOf(numberoffailures));
                    

                    System.out.println("Type of measure: UDP RTT Receiver Receiver (" +
                                       numPktUDPLatency + "packets of " + pktSizeUDPLatency +
                            "bytes)\t\t\t\t[Attempt #" + attemptnumber + "]");

                    ret = MainUtils.udpRTTMeasure("Receiver", keyword, CMDPORT,
                                                  observerAddress, UDPPORT, pktSizeUDPLatency,
                                                  numPktUDPLatency, interfaceName, testMetadata_client);

                    if (ret == 0)
                        break;
                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (direction.equalsIgnoreCase("sender") ||
                    direction.equalsIgnoreCase("all")) {

                int remainingAttempt= MAXATTEMPTNUMBER;

                

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    HashMap<String, String> testMetadata_client = initializeTestMetadata("latencyUDP", "sender");
                    int attemptnumber = MAXATTEMPTNUMBER - remainingAttempt;
                    int numberoffailures = attemptnumber - 1;
                    testMetadata_client.put("number-of-attempts", String.valueOf(attemptnumber));
                    testMetadata_client.put("Number-of-failures", String.valueOf(numberoffailures));

                    System.out.println("Type of measure: UDP RTT Sender (" + numPktUDPLatency +
                            "packets of " + pktSizeUDPLatency + "bytes)\t\t\t\t\t[Attempt #" +
                            (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret = MainUtils.udpRTTMeasure("Sender", keyword, CMDPORT,
                                                  observerAddress, UDPPORT,  pktSizeUDPLatency,
                                                  numPktUDPLatency, interfaceName, testMetadata_client);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}


