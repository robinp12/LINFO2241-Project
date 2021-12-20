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

public class Client{

    private final int N;
    private final String host;
    private final int port;
    private final String filename;
    private final String printfile;
    private final int minPasswordLength;
    private final int maxPasswordLength;

    public Client(int n, String host, int port, String filename, String printfile, int minPasswordLength, int maxPasswordLength){
        this.N = n;
        this.host = host;
        this.port = port;
        this.filename = filename;
        this.printfile = printfile;
        this.minPasswordLength = minPasswordLength;
        this.maxPasswordLength = maxPasswordLength;
    }

    public void launch(){
        Run run = new Run(N, host, port, filename, printfile, minPasswordLength, maxPasswordLength);
        Thread[] threadpool = new Thread[this.N];
        for (int i = 0; i < this.N; i++){threadpool[i] = new Thread(run);}
        for (int i = 0; i < N; i++){threadpool[i].start();}
        for (int i = 0; i < N; i++){
            try {threadpool[i].join();}
            catch (InterruptedException e){e.printStackTrace();}
        }
    }
}

class Run implements Runnable{

    private final int N;
    private final String host;
    private final int port;
    private final String filename;
    private final String printfile;
    private final int minPasswordLength;
    private final int maxPasswordLength;

    Run(int n, String host, int port, String filename, String printfile, int minPasswordLength, int maxPasswordLength){
        N = n;
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
        System.out.println(password);
        return password.toString();
    }

    private int getRandomNumber(int min, int max){return (int) ((Math.random() * (max - min)) + min);}

    public void run(){
        long responseTime = -1;
        int passwordLength = -1;
        try {
            passwordLength = getRandomNumber(minPasswordLength, maxPasswordLength);
            String password = generatePassword(passwordLength);
            SecretKey keyGenerated = CryptoUtils.getKeyFromPassword(password);

            File inputFile = new File(filename);
            File encryptedFile = new File("test_file-encrypted-client.pdf");
            File decryptedClient = new File("test_file-decrypted-client.pdf");

            // This is an example to help you create your request
            CryptoUtils.encryptFile(keyGenerated, inputFile, encryptedFile);

//            System.out.println("Encrypted file length: " + encryptedFile.length());

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
            long fileLength = encryptedFile.length();
            Main.sendRequest(out, hashPwd, pwdLength, fileLength);
            out.flush();

            FileManagement.sendFile(inFile, out);

            // GET THE RESPONSE FROM THE SERVER
            OutputStream outFile = new FileOutputStream(decryptedClient);
            long fileLengthServer = inSocket.readLong();
//            System.out.println("Length from the server: " + fileLengthServer);
            FileManagement.receiveFile(inSocket, outFile, fileLengthServer);
            long end = System.currentTimeMillis();
            responseTime = end - start;

            out.close();
            outSocket.close();
            outFile.close();
            inFile.close();
            inSocket.close();
            socket.close();
        } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
                | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | InvalidKeySpecException e){
            e.printStackTrace();
        }
        BufferedWriter writer = null;
        try {
            File print = new File(printfile);

            writer = new BufferedWriter(new FileWriter(print, true));
            writer.write(String.format("%s, %s, %s\n", this.N, responseTime, passwordLength));
        } catch (Exception e){e.printStackTrace();}
        finally {
            try {
                // Close the writer regardless of what happens...
                assert writer != null;
                writer.close();
            } catch (Exception e){e.printStackTrace();}
        }
    }
}