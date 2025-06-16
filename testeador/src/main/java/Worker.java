import java.util.HashMap;
import java.util.Map;

import com.zeroc.Ice.Current;

import Demo.Observer;
import Demo.QueryStationPrx;
import Demo.SubjectPrx;
import Demo.Task;
import Demo.VoteStationPrx;

public class Worker implements Observer {

    private Map<Integer, VoteStationPrx> votes;
    private Map<Integer, QueryStationPrx> querys;
    private SubjectPrx subject;

    public Worker(SubjectPrx subjectPrx) {
        this.votes = new HashMap<>();
        this.querys = new HashMap<>();
        subject = subjectPrx;
    }

    @Override
    public void update(String message, Current current) {
        System.out.println("Received update: " + message);
    }

    @Override
    public void connect(Map<Integer, String> configs, String type, Current current) {
        if (type.equals("STATION_VOTE")) {
            for (Map.Entry<Integer, String> entry : configs.entrySet()) {
                String config = entry.getValue();
                VoteStationPrx voteService = VoteStationPrx
                        .checkedCast(Testeador.mainCommunicator.stringToProxy(config));
                if (voteService == null) {
                    throw new Error("Invalid proxy for VoteService");
                }
                votes.put(entry.getKey(), voteService);
            }
        } else if (type.equals("STATION_QUERY")) {
            for (Map.Entry<Integer, String> entry : configs.entrySet()) {
                String config = entry.getValue();
                QueryStationPrx voteService = QueryStationPrx
                        .checkedCast(Testeador.mainCommunicator.stringToProxy(config));
                if (voteService == null) {
                    throw new Error("Invalid proxy for VoteService");
                }
                querys.put(entry.getKey(), voteService);
            }
        }
    }

    @Override
    public void vote(Current current) {
        Thread t = new Thread(() -> {
            System.out.println("Starting vote process...");
            Task task = subject.getTask();
            while (task != null) {
                VoteStationPrx voteService = VoteStationPrx
                        .checkedCast(Testeador.mainCommunicator.stringToProxy(task.conection));
                for(Map.Entry<String, Integer> votes : task.votes.entrySet()){
                    callVoto(votes.getKey(), votes.getValue(), voteService);
                }
                task = subject.getTask();
            }
        });

        t.start();
    }

    public void callVoto(String document, int candidateId, VoteStationPrx voteService) {
        try {
            int result = voteService.vote(document, candidateId);
            if (result != 1) {
                System.err.println(
                        "Error con el voto del lado de la estacion: " + voteService.toString() + ", voto: " + result);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
