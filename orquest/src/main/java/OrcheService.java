import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import com.zeroc.Ice.Current;

import Demo.ObserverPrx;
import Demo.Subject;
import Demo.Task;

public class OrcheService implements Subject {

    public static final Integer MAX_VOTES_WORKER = 1000;
    private HashMap<String, ObserverPrx> observers;
    private Queue<Task> tasksQueue;
    private Map<Integer, String> configsPrx;
    private Semaphore semaphore = new Semaphore(1);
    Scanner scanner;

    public OrcheService() {
        observers = new HashMap<>();
        tasksQueue = new LinkedList<>();
        scanner = new Scanner(System.in);
    }

    @Override
    public void registerObserver(String id, ObserverPrx o, Current current) {
        System.out.println("Registering observer id: " + id + " with connection info: " + o.toString());
        observers.put(id, o);
        System.out.println("Total workers registered: " + observers.size());
    }

    @Override
    public void removeObserver(String id, Current current) {
        observers.remove(id);
    }

    public void loadClientsProxies() {
        System.out.println("Loading client proxies...");
        configsPrx = new HashMap<>();
        System.out.println("If end configurations, type 'end'.");
        System.out.println("Enter identity for conection ZEROC ICE (e.g., SimplePrinter)");
        String tag = scanner.nextLine();
        System.out.println("Enter protocol (e.g., tcp, udp or enter for default):");
        String protocol = scanner.nextLine()+ " ";
        if (protocol.isEmpty()) {
            protocol = "default ";
        }
        System.out.println("Enter 'end' to finish or press any key to keep adding configurations:");
        System.out.println("Enter host and port: ex: -h localhost -p 8080");
        System.out.println("Enter mesa id");
        String proxyConnection = scanner.nextLine();
        do {
            String mesaId = scanner.nextLine();
            configsPrx.put(Integer.parseInt(mesaId), tag + ":" + protocol + proxyConnection);
            proxyConnection = scanner.nextLine();
        } while (!proxyConnection.equals("end"));
    }

    public void startEvaluationStationVote() {
        // vamoos a leer un archivo csv con los votos el archivo se llama
        // evatuationVotes.csv
        System.out.println("Starting evaluation of voting stations...");
        try (BufferedReader br = new BufferedReader(new FileReader("evatuationVotes.csv"))) {
            String line = br.readLine(); // Read header line
            Map<Integer, Integer> votesCount = new HashMap<>();
            int total = 0;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length == 3) {
                    String candidateId = fields[0];
                    String candidateName = fields[1];
                    String totalVotes = fields[2];
                    System.out.println("Candidate ID: " + candidateId);
                    System.out.println("Candidate Name: " + candidateName);
                    System.out.println("Total Votes: " + totalVotes);
                    votesCount.put(Integer.parseInt(candidateId), Integer.parseInt(totalVotes));
                    total += Integer.parseInt(totalVotes);
                }
            }
            callVoteService(votesCount, total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void callVoteService(Map<Integer, Integer> votesCount, int totalVotes) {
        if (Orquest.documentos.size() < totalVotes) {
            System.out.println("No hay suficientes documentos para votar.");
            return;
        }
        Map<Integer, Integer> votesDones = new HashMap<>();
        int votesStations = configsPrx.size();
        Map<Integer, List<String>> documents = Orquest.documentos;
        int votesPerStation = totalVotes / votesStations;

        List<Task> tasks = new ArrayList<>();
        Iterator<Integer> keys = configsPrx.keySet().iterator();
        while (keys.hasNext()) {
            Integer mesaId = keys.next();
            Task task = new Task();
            task.mesaId = mesaId;
            task.type = "STATION_VOTE";
            task.conection = configsPrx.get(mesaId);
            task.votes = new HashMap<>();
            task.documentsInvalids = new String[]{};
            List<String> docs = documents.get(mesaId);

            if (docs.size() > votesPerStation) {
                docs = docs.subList(0, votesPerStation);
            }
            for (String doc : docs) {
                for (Map.Entry<Integer, Integer> entry : votesCount.entrySet()) {
                    Integer candidateId = entry.getKey();
                    Integer voteCount = entry.getValue() - votesDones.getOrDefault(candidateId, 0);
                    if (voteCount <= 0) {
                        continue; // Skip if no votes left for this candidate
                    } else {
                        task.votes.put(doc, candidateId);
                        votesDones.put(candidateId, votesDones.getOrDefault(candidateId, 0) + 1);
                        break;
                    }
                }
            }

            tasks.add(task);
        }
        for (int i = 0; i < tasks.size() - 1; i++) {
            Task current = tasks.get(i);
            // Task next = tasks.get(i + 1);
            // current.documentsInvalids = new String[]{next.votes.keySet()};
            tasksQueue.add(current);
        }
        Task lastTask = tasks.get(tasks.size() - 1);
        tasksQueue.add(lastTask);
        observers.values().stream().forEach(o -> {
            System.out.println("Notifying observer: " + o.toString());
            o.vote();
        });

    }

    public void startEvaluationStationQuery() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startEvaluationStationQuery'");
    }

    @Override
    public Task getTask(Current current) {
        try {
            semaphore.acquire();
            System.out.println("Getting task from queue, current size: " + tasksQueue.size());
            Task t = tasksQueue.poll();
            return t;
        } catch (Exception e) {
            return null;
        }finally{
            semaphore.release();
        }
    }

}
