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
        //String staticAddress = "ws://192.168.1.41:8080/";
        System.out.println("Please write master address");
        Scanner scanner = new Scanner(System.in);

        String address = scanner.nextLine();
        address = "ws://" + address + ":8080/";
        System.out.println(URI.create(address).toString());
        SlaveEndpoint slave = new SlaveEndpoint(URI.create(address));
        //slave.connect();
        slave.connectBlocking();
        String line = "";

        CudaGate cudaGate = new CudaGate(1500, 0);


        /* FUNCTION WORK TO READ FROM COMMAND LINE */
//        while((line = scanner.nextLine()) != null){
//
//            if(line.equals("send")){
//
//                for(int id : snowflakesQueues.keySet()){
//
//                    if(sizes.containsKey(id) && sizes.get(id) != null) {
//                        Queue<Float> positions = snowflakesQueues.get(id);
//                        Queue<Float> message = new ConcurrentLinkedQueue<Float>();
//                        message.add(sizes.get(id));
//                        message.addAll(positions);
//                        snowflakesQueues.put(id, message);
//                    }
//                }
//
//                slave.send(JSONValue.toJSONString(snowflakesQueues));
//                System.out.println("Sending positions for " + snowflakesQueues.keySet().size() + " snowflakes.");
//                snowflakesQueues.clear();
//
//
//
//
//            } else {
//                String[] temp = line.split(" ");
//                int id = Integer.parseInt(temp[0]);
//                float x = Float.parseFloat(temp[1]);
//                float y = Float.parseFloat(temp[2]);
//                float size = Float.parseFloat(temp[3]);
//                sizes.put(id, size);
//                if(!snowflakesQueues.containsKey(id) || snowflakesQueues.get(id) == null) snowflakesQueues.put(id, new ConcurrentLinkedQueue());
//                snowflakesQueues.get(id).add(x);
//                snowflakesQueues.get(id).add(y);
//            }
//
//            //slave.send(line);
//        }
        while(slave.getConnection() != null) {
//            snowflakesQueues = DataGenerator.generateSnowflakes();

            TimeUnit.SECONDS.sleep(5);
//            snowflakesQueues = cudaGate.getNextIteration(2.0f, 50.0f);
//
//            slave.send(JSONValue.toJSONString(snowflakesQueues));
//            System.out.println("Sending positions for " + snowflakesQueues.keySet().size() + " snowflakes.");
//            snowflakesQueues.clear();
//            TimeUnit.SECONDS.sleep(5);
        }

        //scanner.close();
    }
}
