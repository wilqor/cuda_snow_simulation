package snowflakes.cuda.kask.eti.pg.gda.pl.slave;

import org.json.simple.JSONValue;
import snowflakes.cuda.kask.eti.pg.gda.pl.cuda.CudaGate;

import javax.xml.crypto.Data;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        Map<Integer, Float> sizes = new HashMap();

        Map<Integer, Queue<Float>> snowflakesQueues = new LinkedHashMap<Integer, Queue<Float>>();
        System.out.println("Please write master address");
        Scanner scanner = new Scanner(System.in);

        String address = scanner.nextLine();
        address = "ws://" + address + ":8080/";
        System.out.println(URI.create(address).toString());
        SlaveEndpoint slave = new SlaveEndpoint(URI.create(address));
        slave.connectBlocking();
        String line = "";

        while(slave.getConnection() != null) {

            TimeUnit.SECONDS.sleep(5);
        }

        scanner.close();
    }
}
