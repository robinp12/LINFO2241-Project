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

public class ServerMain{

    /**
     * @param in Stream from which to read the request
     * @return Request with information required by the server to process encrypted file
     */
    public static Request readRequest(DataInputStream in) throws IOException {
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

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InterruptedException {

        // Server initialization
        ServerSocket ss = new ServerSocket(3333);
        System.out.println("Waiting connection");

        // Loop to receive multiple request from clients
        while (true) {
            // Create socket
            Socket socket = ss.accept();
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
            System.out.println("fileLength: " + fileLength);
            System.out.println("pwdLength: " + pwdLength);
            System.out.println("hashPwd: " + hashPwd);

            // Template decrypted file
            File decryptedFile = new File("test_file-decrypted-server" + hashPwd + ".pdf");
            // Template file from client
            File networkFile = new File("temp-server" + hashPwd + ".pdf");

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
            decryptedFile.delete();
            networkFile.delete();
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

    private static String passwordToHash(String passwordToHash) {
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
                System.out.println("1 :" + prefix);
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