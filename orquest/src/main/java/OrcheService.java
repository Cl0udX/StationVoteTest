import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.zeroc.Ice.Current;

import Demo.ObserverPrx;
import Demo.Subject;

public class OrcheService implements Subject {

    private HashMap<String, ObserverPrx> observers;
    Scanner scanner;

    public OrcheService() {
        observers = new HashMap<>();
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

    public void balanceStationsByType(String stationType) {
        System.out.println("Starting balance stations process... for type: " + stationType);
        ArrayList<String> configsPrx = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        System.out.println("If end configurations, type 'end'.");
        System.out.println("Enter identity for conection ZEROC ICE (e.g., SimplePrinter)");
        String tag = scanner.nextLine();
        System.out.println("Enter protocol (e.g., tcp, udp or enter for default):");
        String protocol = scanner.nextLine();
        if (protocol.isEmpty()) {
            protocol = "default";
        }
        String n;
        do {
            System.out.println("Enter host:");
            String host = scanner.nextLine();
            System.out.println("Enter ports (comma separated): ex: 8080,8081,8082");
            String ports = scanner.nextLine();
            for (String port : ports.split(",")) {
                configsPrx.add(tag.trim() + ":" + protocol + " -p " + port.trim() + " -h " + host.trim());
            }
            System.out.println("Enter 'end' to finish or press any key to keep adding configurations:");
            n = scanner.nextLine();
        } while (!n.equals("end"));
        int totalWorkers = observers.size();
        if (totalWorkers == 0) {
            System.out.println("No workers registered. Cannot balance stations.");
            return;
        }
        int totalConfigs = configsPrx.size();
        if (totalConfigs == 0) {
            System.out.println("No configurations provided. Cannot balance stations.");
            return;
        }
        System.out.println("Configurations received: " + configsPrx.size());

        int base = totalConfigs / totalWorkers;
        int remainder = totalConfigs % totalWorkers;
        int start = 0;

        for (Map.Entry<String, ObserverPrx> entry : observers.entrySet()) {
            String id = entry.getKey();
            ObserverPrx observer = entry.getValue();
            int end = start + base + (remainder > 0 ? 1 : 0);
            List<String> assignedConfigs = configsPrx.subList(start, Math.min(end, totalConfigs));
            start = end;
            if (remainder > 0)
                remainder--;
            System.out.println("Assigning " + assignedConfigs.size() + " configurations to worker id: " + id);
            String[] assignedConfigsArray = assignedConfigs.toArray(new String[0]);
            observer.connect(assignedConfigsArray, stationType);
        }
    }

    public void balanceStationsMenu() {
        while (true) {
            System.out.println("\n--- Menú de Balanceo de Estaciones ---");
            System.out.println("1. Balancear estaciones de voto");
            System.out.println("2. Balancear estaciones de consulta");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    System.out.println("Iniciando balanceo de estaciones de voto...");
                    balanceStationsByType("STATION_VOTE");
                    System.out.println("Balanceo de estaciones de voto completado.");
                    break;
                case "2":
                    System.out.println("Iniciando balanceo de estaciones de consulta...");
                    balanceStationsByType("STATION_QUERY");
                    System.out.println("Balanceo de estaciones de consulta completado.");
                    break;
                case "3":
                    System.out.println("Saliendo del menú.");
                    return;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    public void startEvaluationStationVote() {
        // vamoos a leer un archivo csv con los votos el archivo se llama
        // evatuationVotes.csv
        System.out.println("Starting evaluation of voting stations...");
        try (BufferedReader br = new BufferedReader(new FileReader("evatuationVotes.csv"))) {
            String line = br.readLine(); // Read header line
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length == 3) {
                    String candidateId = fields[0];
                    String candidateName = fields[1];
                    String totalVotes = fields[2];
                    System.out.println("Candidate ID: " + candidateId);
                    System.out.println("Candidate Name: " + candidateName);
                    System.out.println("Total Votes: " + totalVotes);
                    callVoteService(Integer.parseInt(candidateId), Integer.parseInt(totalVotes));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void callVoteService(int candidateId, int totalVotes) {
        if (Orquest.documentos.size() < totalVotes) {
            System.out.println("No hay suficientes documentos para votar.");
            return;
        }

        int numWorkers = observers.size();
        List<ObserverPrx> observerList = new ArrayList<>(observers.values());

        List<List<String[]>> votosPorWorker = new ArrayList<>();
        for (int i = 0; i < numWorkers; i++) {
            votosPorWorker.add(new ArrayList<>());
        }

        for (int i = 0; i < totalVotes; i++) {
            String documento = Orquest.documentos.get(i);
            int workerIndex = i % numWorkers;
            votosPorWorker.get(workerIndex).add(new String[] { documento, String.valueOf(candidateId) });
        }

        for (int i = 0; i < numWorkers; i++) {
            ObserverPrx observer = observerList.get(i);
            int[][] votos = votosPorWorker.get(i).stream()
                    .map(voto -> new int[] { Integer.parseInt(voto[0]), Integer.parseInt(voto[1]) })
                    .toArray(int[][]::new);
            observer.vote(votos);
        }

        System.out
                .println("Calling vote service for candidate ID: " + candidateId + " with total votes: " + totalVotes);

    }

    public void startEvaluationStationQuery() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startEvaluationStationQuery'");
    }

}
