import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class ServerMain{

    public static void main(String[] args) throws IOException, InterruptedException {

        // Server initialization
        ServerSocket ss = new ServerSocket(3333,10);
        System.out.println("Waiting connection");

        // Loop to receive multiple request from clients
        while (true) {
            synchronized (ss) {

                ClientHandler CH = new ClientHandler(ss);

                Thread thread = new Thread(CH);
                thread.start();
                thread.join();

            }
        }
    }
}

class BruteForcing implements Runnable{

    private final int pwdLength;
    private final String hashPwd;

    private static String pwd = "";
    private static boolean isFound = false;

    public BruteForcing(int pwdLength, String hashPwd){
        this.pwdLength = pwdLength;
        this.hashPwd = hashPwd;
    }

    public static String getPwd() {
        return pwd;
    }

    public static String passwordToHash(String passwordToHash) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    // Brute force method in alphabet order A to Z
    private static void bruteForceAlphabet(String prefix, int mdpLength, String hash) {
        for (char i = 'a'; i <= 'z'; i++) {
            if (mdpLength == 0) {
                if(hash.equals(passwordToHash(prefix))) {
                    pwd = prefix;
                    isFound = true;
                }
                return;
            }
            String newPrefix = prefix + i;
            if (!isFound)
                bruteForceAlphabet(newPrefix, mdpLength - 1,hash);
        }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        isFound = false;
        bruteForceAlphabet("", pwdLength,hashPwd);
    }
}
class ClientHandler implements Runnable{

    private final ServerSocket ss;

    public ClientHandler(ServerSocket ss) {

        this.ss = ss;
    }

    /**
     * @param in Stream from which to read the request
     * @return Request with information required by the server to process encrypted file
     */
    public synchronized static Request readRequest(DataInputStream in) throws IOException {
        byte [] hashPwd = new byte[20];
        int count = in.read(hashPwd,0, 20);
        if (count < 0){
            throw new IOException("Server could not read from the stream");
        }
        int pwdLength = in.readInt();
        long fileLength = in.readLong();

        return new Request(hashPwd, pwdLength, fileLength);
    }

    private static String arrayHashToString(byte[]  passwordToHash) {
        String generatedPassword = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < passwordToHash.length; i++) {
            sb.append(Integer.toString((passwordToHash[i] & 0xff) + 0x100, 16).substring(1));
        }
        generatedPassword = sb.toString();
        return generatedPassword;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        // Template decrypted file
        File decryptedFile = new File("server\\test_file-decrypted-server.pdf");
        // Template file from client
        File networkFile = new File("server\\temp-server.pdf");

        Socket socket = null;
        try {
            // Create socket
            socket = ss.accept();
            System.out.println("Connection from: " + socket);

            // Stream to read request from socket
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Stream to write response to socket
            DataOutputStream outSocket = new DataOutputStream(socket.getOutputStream());

            // Read data from client
            Request request = readRequest(dataInputStream);
            long fileLength = request.getLengthFile();
            int pwdLength = request.getLengthPwd();
            String hashPwd = arrayHashToString(request.getHashPassword());
            //System.out.println("fileLength: " + fileLength);
            //System.out.println("pwdLength: " + pwdLength);
            System.out.println("hashPwd: " + hashPwd);


            // Stream to write the file to decrypt
            OutputStream outFile = new FileOutputStream(networkFile);

            // GET THE RESPONSE FROM THE CLIENT
            FileManagement.receiveFile(inputStream, outFile, fileLength);

            /* Bruteforce password */
            BruteForcing bruteForcing = new BruteForcing(pwdLength, hashPwd);

            // Put method in a thread
            Thread thread = new Thread(bruteForcing);

            // Running thread
            thread.start();

            // Waiting thread to stop processing before continuing instructions
            thread.join();

            // Password found
            System.out.println("Found password : " + BruteForcing.getPwd());
            System.out.println();
            SecretKey serverKey = CryptoUtils.getKeyFromPassword(BruteForcing.getPwd());

            CryptoUtils.decryptFile(serverKey, networkFile, decryptedFile);

            // Send the decryptedFile
            InputStream inDecrypted = new FileInputStream(decryptedFile);
            outSocket.writeLong(decryptedFile.length());
            outSocket.flush();
            FileManagement.sendFile(inDecrypted, outSocket);

            // Close stream and socket
            dataInputStream.close();
            inputStream.close();
            inDecrypted.close();
            outFile.close();
            socket.close();
        } catch (IOException | InterruptedException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {

            e.printStackTrace();
        }
        networkFile.delete();
        decryptedFile.delete();
    }
}