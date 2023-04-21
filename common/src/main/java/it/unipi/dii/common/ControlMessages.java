package it.unipi.dii.common;




import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;




public class ControlMessages {
    public enum Messages{
        START,
        COMPLETED,
        SUCCEDED,
        FAILED,
        MEASUREDBANDWIDTH,
        MEASUREDLATENCY,
        GETTESTMETADATA
    }
    private Socket controlSocket = null;
    private DataOutputStream dataOutputStream = null;
    private final int controlSocketTimeOut = 20000;



    public ControlMessages(Socket controlSocket){
        this.controlSocket = controlSocket;
    }



    public ControlMessages(String receiverAddress, int receiverPort){
        try {
            openControlConnection(InetAddress.getByName(receiverAddress), receiverPort);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }



    public ControlMessages(String receiverAddress, int receiverPort, InetAddress sourceAddr, int sourcePort){
        try {
            openControlConnection(InetAddress.getByName(receiverAddress), receiverPort, sourceAddr, sourcePort);


        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }



    private void openControlConnection(InetAddress receiverAddress, int receiverPort){
        try {
            this.controlSocket = new Socket(receiverAddress, receiverPort);

            this.controlSocket.setSoTimeout(controlSocketTimeOut);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    private void openControlConnection(InetAddress receiverAddress, int receiverPort, InetAddress sourceAddr, int sourcePort){
        try {
            this.controlSocket = new Socket();
            this.controlSocket.bind(new InetSocketAddress(sourceAddr, sourcePort));
            this.controlSocket.connect(new InetSocketAddress(receiverAddress, receiverPort));

            this.controlSocket.setSoTimeout(controlSocketTimeOut);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }



    public ControlMessages(){
        this.controlSocket = null;
        this.dataOutputStream = null;
    }



    public void closeConnection(){
        try {
            if (this.dataOutputStream != null)
                this.dataOutputStream.close();
            if (this.controlSocket != null)
                this.controlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.controlSocket = null;
        this.dataOutputStream = null;
    }



    public void sendCMD(String command) throws IOException {
        // get the output stream from the socket.
        if (this.dataOutputStream == null) {
            OutputStream outputStream = this.controlSocket.getOutputStream();
            this.dataOutputStream = new DataOutputStream(outputStream);
        }

        // write the message we want to send
        this.dataOutputStream.writeUTF(command);
        this.dataOutputStream.flush(); // send the message
    }



    public String receiveCMD() throws EOFException {
        String receivedCommand = null;
        InputStream inputStream = null;

        try {
            inputStream = this.controlSocket.getInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();

            return "";
        }

        DataInputStream dataInputStream = new DataInputStream(Objects.requireNonNull(inputStream));
        try {
            //The command received is composed by "command id-test"
            //System.out.println(dataInputStream.toString());//deb
            receivedCommand = dataInputStream.readUTF();
        }
        catch (IOException e){
            e.printStackTrace();

            return "";
        }

        return receivedCommand;
    }



    public void initializeNewMeasure(String receiverAddress, int receiverPort) {
        closeConnection();
        try {
            openControlConnection(InetAddress.getByName(receiverAddress), receiverPort);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }



    public Socket getSocket(){
        return this.controlSocket;
    }
}