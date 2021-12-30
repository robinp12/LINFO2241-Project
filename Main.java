import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Main {

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
        System.out.println("Enter a password size... (or -1 for pool of client)");
        while(true){
            Scanner sc = new Scanner(System.in);
            //Only int input
            while (!sc.hasNextInt()) sc.next();
            int i = sc.nextInt();
            if (i==-1){break;}
            System.out.println("One password of length "+i +" generated");
            ExecutorService executor = Executors.newFixedThreadPool(1);
            Runnable cli = new Client(1, 1, "localhost", 3333, "test_file.pdf", "graphs/onepassword.txt", i, i);
            executor.execute(cli);
            executor.shutdown();
        }
        System.out.println("Enter number of client request... (or -1 to exit)");
        while (true){
            Scanner sc = new Scanner(System.in);
            //Only int input
            while (!sc.hasNextInt()) sc.next();
            int k = sc.nextInt();
            if (k==-1){break;}
            ExecutorService executor = Executors.newFixedThreadPool(k);
            System.out.println("Pool of " + k + " clients generated");
            for (int i = 0; i < k; i++){
                Runnable cli = new Client(i, k, "localhost", 3333, "test_file.pdf", "graphs/poolpassword.txt", 5, 5);
                executor.execute(cli);
            }
            executor.shutdown();
        }

    }
}
