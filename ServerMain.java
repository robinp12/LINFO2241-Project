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

        // Template decrypted file
        File decryptedFile = new File("test_file-decrypted-server.pdf");
        // Template file from client
        File networkFile = new File("temp-server.pdf");

        ServerSocket ss = new ServerSocket(3333);
        System.out.println("Waiting connection");

        while (true) {
            // Create socket
            Socket socket = ss.accept();
            System.out.println("Connection from: " + socket);
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
            System.out.println("fileLength: " + fileLength);
            System.out.println("pwdLength: " + pwdLength);
            System.out.println("hashPwd: " + hashPwd);

            // GET THE RESPONSE FROM THE CLIENT
            FileManagement.receiveFile(inputStream, outFile, fileLength);
            /*
            int readFromFile = 0;
            int bytesRead = 0;
            byte[] readBuffer = new byte[64];
            System.out.println("[Server] File length: "+ fileLength);
            while((readFromFile < fileLength)){
                bytesRead = inputStream.read(readBuffer);
                readFromFile += bytesRead;
                outFile.write(readBuffer, 0, bytesRead);
            }*/

            /* Bruteforce password with thread */
            BruteForcing bruteForcing = new BruteForcing(pwdLength, hashPwd, 0);
            BruteForcing bruteForcing1 = new BruteForcing(pwdLength, hashPwd, 1);

            Thread thread = new Thread(bruteForcing);
            Thread thread1 = new Thread(bruteForcing1);

            // Running thread
            thread.start();
            thread1.start();

            // Waiting thread to stop processing before continuing instructions
            thread.join();
            thread1.join();
            System.out.println("Found password : " + BruteForcing.getPwd());
            SecretKey serverKey = CryptoUtils.getKeyFromPassword(BruteForcing.getPwd());

            CryptoUtils.decryptFile(serverKey, networkFile, decryptedFile);

            // Send the decryptedFile
            InputStream inDecrypted = new FileInputStream(decryptedFile);
            outSocket.writeLong(decryptedFile.length());
            outSocket.flush();
            FileManagement.sendFile(inDecrypted, outSocket);
            /*
            int readCount;
            byte[] buffer = new byte[64];
            //read from the file and send it in the socket
            while ((readCount = inDecrypted.read(buffer)) > 0){
                outSocket.write(buffer, 0, readCount);
            }*/

            dataInputStream.close();
            inputStream.close();
            inDecrypted.close();
            outFile.close();
            socket.close();
        }
    }
}

class BruteForcing implements Runnable{

    private final int pwdLength;
    private final String hashPwd;
    private final int isReversed;

    private static String pwd = "";
    private static boolean isFound = false;

    public BruteForcing(int pwdLength, String hashPwd, int isReversed){
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

    private static void bruteForceAlphabet(String prefix, int mdpLength, String hash, int isReversed) {
        if (isReversed == 0){
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
                    bruteForceAlphabet(newPrefix, mdpLength - 1,hash,isReversed);
            }
        }
        else{
            for (char i = 'z'; i >= 'a'; i--) {
                if (mdpLength == 0) {
                    System.out.println("2 :" + prefix);
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