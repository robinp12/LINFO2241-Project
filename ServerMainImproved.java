import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ServerMainImproved{

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

        // Template decrypted file
        File decryptedFile = new File("test_file-decrypted-server.pdf");
        // Template file from client
        File networkFile = new File("temp-server.pdf");
        int i = 0;
        // Server initialization
        ServerSocket ss = new ServerSocket(3333);
        System.out.println("Waiting connection");

        // Loop to receive multiple request from clients
        while (true) {
            // Create socket
            Socket socket = ss.accept();
            System.out.println("Connection from: " + socket);
            long responseTime = -1;

            // Stream to read request from socket
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Stream to write response to socket
            DataOutputStream outSocket = new DataOutputStream(socket.getOutputStream());

            // Stream to write the file to decrypt
            OutputStream outFile = new FileOutputStream(networkFile);

            // Read datas from client
            Request request = readRequest(dataInputStream);
            long fileLength = request.getLengthFile();
            int pwdLength = request.getLengthPwd();
            String hashPwd = arrayHashToString(request.getHashPassword());

            // GET THE RESPONSE FROM THE CLIENT
            FileManagement.receiveFile(inputStream, outFile, fileLength);

            /* Bruteforce password */
            long start = System.currentTimeMillis();
            BruteForcingImproved bruteForcing = new BruteForcingImproved(pwdLength, hashPwd, 0);
            BruteForcingImproved bruteForcing1 = new BruteForcingImproved(pwdLength, hashPwd, 1);

            // Put method in threads
            Thread thread = new Thread(bruteForcing);
            Thread thread1 = new Thread(bruteForcing1);

            // Running threads
            thread.start();
            thread1.start();

            // Waiting threads to stop processing before continuing instructions
            thread.join();
            thread1.join();
            long end = System.currentTimeMillis();

            // Password found
            System.out.println("DECRYPTED " + i);
            SecretKey serverKey = CryptoUtils.getKeyFromPassword(BruteForcingImproved.getPwd());

            CryptoUtils.decryptFile(serverKey, networkFile, decryptedFile);

            // Send the decryptedFile
            InputStream inDecrypted = new FileInputStream(decryptedFile);
            outSocket.writeLong(decryptedFile.length());
            outSocket.flush();
            FileManagement.sendFile(inDecrypted, outSocket);
            System.out.println("SENT "+ i);

            responseTime = end - start;

            i++;
            // Close stream and socket
            dataInputStream.close();
            inputStream.close();
            inDecrypted.close();
            outFile.close();
            socket.close();
            BufferedWriter writer = null;
            try {
                File print = new File("graphs/improvedbruteforce.txt");
                writer = new BufferedWriter(new FileWriter(print, true));
                if (responseTime != -1){writer.write(String.format("%s, %s\n", responseTime, pwdLength));}
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
}

class BruteForcingImproved implements Runnable{

    private final int pwdLength;
    private final String hashPwd;
    private final int isReversed;

    private static String pwd = "";
    private static boolean isFound = false;

    public BruteForcingImproved(int pwdLength, String hashPwd, int isReversed){
        this.pwdLength = pwdLength;
        this.hashPwd = hashPwd;
        this.isReversed = isReversed;
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

    // Brute force method by itering over a list of password
    private static String bruteForceDictionnary(String word) throws IOException {
        String st;
        File dictionnary = new File("10k-most-common_filered.txt");
        BufferedReader br = new BufferedReader(new FileReader(dictionnary));
        while ((st = br.readLine()) != null){
            System.out.println("dict: "+st);
            if(word.equals(passwordToHash(st))){
                pwd = st;
                return "";
            }
        }
        return "";
    }

    // Brute force method in alphabet order A-Z or Z-A
    private static void bruteForceAlphabet(String prefix, int mdpLength, String hash, int isReversed) {
        if (isReversed == 0){
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
                    bruteForceAlphabet(newPrefix, mdpLength - 1,hash,isReversed);
            }
        }
        else{
            for (char i = 'z'; i >= 'a'; i--) {
                if (mdpLength == 0) {
                    if(hash.equals(passwordToHash(prefix))) {
                        pwd = prefix;
                        isFound = true;
                    }
                    return;
                }
                String newPrefix = prefix + i;
                if (!isFound)
                    bruteForceAlphabet(newPrefix, mdpLength - 1,hash,isReversed);
            }
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
        bruteForceAlphabet("", pwdLength,hashPwd,isReversed);
    }
}