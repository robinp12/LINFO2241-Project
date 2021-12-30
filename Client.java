import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

class Client implements Runnable{

    private final int n;
    private int clientNumber;
    private final String host;
    private final int port;
    private final String filename;
    private final String printfile;
    private final int minPasswordLength;
    private final int maxPasswordLength;

    Client(int n, int clientNumber, String host, int port, String filename, String printfile, int minPasswordLength, int maxPasswordLength){
        this.n = n;
        this.clientNumber = clientNumber;
        this.host = host;
        this.port = port;
        this.filename = filename;
        this.printfile = printfile;
        this.minPasswordLength = minPasswordLength;
        this.maxPasswordLength = maxPasswordLength;
    }

    private String generatePassword(int n){
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < n; i++){password.append((char) ('a' + getRandomNumber(0, 26)));}
        return password.toString();
    }

    static int getRandomNumber(int min, int max){return (int) ((Math.random() * (max - min)) + min);}

    public void run(){
        //int wait = getRandomNumber(0, 10000);
        //try {Thread.sleep(wait);}
        //catch (InterruptedException e){e.printStackTrace();}
        long responseTime = -1;
        int passwordLength = -1;
        long fileLength = -1;
        try {
            passwordLength = getRandomNumber(minPasswordLength, maxPasswordLength);
            String password = generatePassword(passwordLength);
            SecretKey keyGenerated = CryptoUtils.getKeyFromPassword(password);

            File inputFile = new File(filename);
            fileLength = inputFile.length();
            File encryptedFile = new File("client\\test_file-encrypted-client"+n+".pdf");
            File decryptedClient = new File("client\\test_file-decrypted-client"+n+".pdf");

            // This is an example to help you create your request
            CryptoUtils.encryptFile(keyGenerated, inputFile, encryptedFile);

            // Creating socket to connect to server (in this example it runs on the localhost on port 3333)
            Socket socket = new Socket(host, port);

            // For any I/O operations, a stream is needed where the data are read from or written to. Depending on
            // where the data must be sent to or received from, different kind of stream are used.
            OutputStream outSocket = socket.getOutputStream();
            DataOutputStream out = new DataOutputStream(outSocket);
            InputStream inFile = new FileInputStream(encryptedFile);
            DataInputStream inSocket = new DataInputStream(socket.getInputStream());

            // SEND THE PROCESSING INFORMATION AND FILE
            long start = System.currentTimeMillis();
            byte[] hashPwd = Main.hashSHA1(password);
            int pwdLength = password.length();
            long file_Length = encryptedFile.length();
            System.out.println("SENT TO SERVER :" + password);
            Main.sendRequest(out, hashPwd, pwdLength, file_Length);
            out.flush();

            FileManagement.sendFile(inFile, out);

            // GET THE RESPONSE FROM THE SERVER
            OutputStream outFile = new FileOutputStream(decryptedClient);
            long fileLengthServer = inSocket.readLong();
            FileManagement.receiveFile(inSocket, outFile, fileLengthServer);
            System.out.println("DECRYPTED " + n);

            long end = System.currentTimeMillis();
            responseTime = end - start;

            out.close();
            outSocket.close();
            outFile.close();
            inFile.close();
            inSocket.close();
            socket.close();
            encryptedFile.delete();
        } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
                | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | InvalidKeySpecException e){
            e.printStackTrace();
        }
        BufferedWriter writer = null;
        try {
            File print = new File(printfile);
            writer = new BufferedWriter(new FileWriter(print, true));
            if (responseTime != -1){writer.write(String.format("%s, %s, %s, %s\n", this.clientNumber, fileLength, responseTime, passwordLength));}
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                // Close the writer regardless of what happens...
                assert writer != null;
                writer.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}