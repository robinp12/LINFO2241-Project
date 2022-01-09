import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Main {

    private static int errorRate = 0;
    public void countError(){
        errorRate++;
    }
    /**
     * This function hashes a string with the SHA-1 algorithm
     * @param data The string to hash
     * @return An array of 20 bytes which is the hash of the string
     */
    public static byte[] hashSHA1(String data) throws NoSuchAlgorithmException{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(data.getBytes());
    }

    /**
     * This function is used by a client to send the information needed by the server to process the file
     * @param out Socket stream connected to the server where the data are written
     * @param hashPwd SHA-1 hash of the password used to derive the key of the encryption
     * @param pwdLength Length of the clear password
     * @param fileLength Length of the encrypted file
     */
    public static void sendRequest(DataOutputStream out, byte[] hashPwd, int pwdLength,
                       long fileLength) throws IOException {
        out.write(hashPwd,0, 20);
        out.writeInt(pwdLength);
        out.writeLong(fileLength);
    }

    public static void main(String[] args) {

        File inputFile = new File("client\\test_file.pdf");
        long fileLength = inputFile.length();

        System.out.println("Enter a password size... (or -1 for pool of client)");
        while(true){
            long responseTime = -1;

            Scanner sc = new Scanner(System.in);
            //Only int input
            while (!sc.hasNextInt()) sc.next();
            int i = sc.nextInt();
            if (i==-1){break;}

            ExecutorService executor = Executors.newFixedThreadPool(1);
            System.out.println("One password of length "+i +" generated");

            long start = System.currentTimeMillis();
            Runnable cli = new Client(1, inputFile, i, i,1);
            executor.execute(cli);
            executor.shutdown();

            while (!executor.isTerminated()) {}

            long end = System.currentTimeMillis();
            responseTime = end - start;

            BufferedWriter writer = null;
            try {
                File print = new File("graphs/NetworkTimeSimple.txt");
                writer = new BufferedWriter(new FileWriter(print, true));
                if (responseTime != -1){writer.write(String.format("%s, %s, %s, %s\n",1, fileLength, responseTime, i));}
                responseTime = 0;
                errorRate = 0;
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
        System.out.println("Enter number of client request... (or -1 to exit)");
        while (true){
            long responseTime = -1;
            int pwdlen = 2;

            Scanner sc = new Scanner(System.in);

            //Only int input
            while (!sc.hasNextInt()) sc.next();
            int k = sc.nextInt();
            if (k==-1){break;}

            ExecutorService executor = Executors.newFixedThreadPool(k);
            System.out.println("Pool of " + k + " clients generated");

            long start = System.currentTimeMillis();
            for (int i = 0; i < k; i++){
                Runnable cli = new Client(i, inputFile, pwdlen, pwdlen, k);
                executor.execute(cli);
            }
            executor.shutdown();

            while (!executor.isTerminated()) {}

            long end = System.currentTimeMillis();
            System.out.println("REFUSED REQUEST : " + errorRate);
            responseTime = end - start;

            BufferedWriter writer = null;
            try {
                File print = new File("graphs/NetworkTimePool.txt");
                writer = new BufferedWriter(new FileWriter(print, true));
                if (responseTime != -1){writer.write(String.format("%s, %s, %s, %s, %s\n", k, fileLength, responseTime, pwdlen, errorRate));}
                responseTime = 0;
                errorRate = 0;
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
