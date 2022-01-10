import utils.CryptoUtils;
import utils.FileManagement;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

class Client implements Runnable{

    private final int n;
    private final File inputFile;
    private final int minPasswordLength;
    private final int maxPasswordLength;
    private int nbClient = 1;

    Client(int n, File inputFile, int minPasswordLength, int maxPasswordLength, int nbClient){
        this.n = n;
        this.inputFile = inputFile;
        this.minPasswordLength = minPasswordLength;
        this.maxPasswordLength = maxPasswordLength;
        this.nbClient = nbClient;
    }

    private String generatePassword(int n){
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < n; i++){password.append((char) ('a' + getRandomNumber(0, 26)));}
        return password.toString();
    }

    static int getRandomNumber(int min, int max){return (int) ((Math.random() * (max - min)) + min);}

    public double getNext() {
        Random R = new Random();
        return (Math.log(1-R.nextDouble())/(-nbClient));
    }
    public void run(){
        Main m = new Main();
        if(nbClient!=1){
            try {
                Thread.sleep((long) (getNext()*10000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int passwordLength = -1;
        try {
            passwordLength = getRandomNumber(minPasswordLength, maxPasswordLength);
            String password = generatePassword(passwordLength);
            SecretKey keyGenerated = CryptoUtils.getKeyFromPassword(password);

            File encryptedFile = new File("client\\test_file-encrypted-client"+n+".pdf");
            File decryptedClient = new File("client\\test_file-decrypted-client"+n+".pdf");

            // This is an example to help you create your request
            CryptoUtils.encryptFile(keyGenerated, inputFile, encryptedFile);

            // Creating socket to connect to server (in this example it runs on the localhost on port 3333)
            Socket socket = null;
            try{
                socket = new Socket("192.168.1.16", 3333);
                // For any I/O operations, a stream is needed where the data are read from or written to. Depending on
                // where the data must be sent to or received from, different kind of stream are used.
                OutputStream outSocket = socket.getOutputStream();
                DataOutputStream out = new DataOutputStream(outSocket);
                InputStream inFile = new FileInputStream(encryptedFile);
                DataInputStream inSocket = new DataInputStream(socket.getInputStream());
                // SEND THE PROCESSING INFORMATION AND FILE
                byte[] hashPwd = Main.hashSHA1(password);
                int pwdLength = password.length();
                long file_Length = encryptedFile.length();
                System.out.println("SENT TO SERVER");
                Main.sendRequest(out, hashPwd, pwdLength, file_Length);
                out.flush();

                FileManagement.sendFile(inFile, out);

                // GET THE RESPONSE FROM THE SERVER
                OutputStream outFile = new FileOutputStream(decryptedClient);
                long fileLengthServer = inSocket.readLong();
                FileManagement.receiveFile(inSocket, outFile, fileLengthServer);
                System.out.println("DECRYPTED");

                out.close();
                outSocket.close();
                outFile.close();
                inFile.close();
                inSocket.close();
                socket.close();
            }catch (ConnectException e){
                System.out.println(e);
                m.countError();
            }
            encryptedFile.delete();
        } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
                | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | InvalidKeySpecException e){
            e.printStackTrace();
        }
    }
}