import java.util.ArrayList;

import com.zeroc.Ice.Current;

import Demo.Observer;
import Demo.QueryStationPrx;
import Demo.VoteStationPrx;

public class Worker implements Observer {

    private ArrayList<VoteStationPrx> votes;
    private ArrayList<QueryStationPrx> querys;

    public Worker() {
        this.votes = new ArrayList<>();
        this.querys = new ArrayList<>();
    }

    @Override
    public void update(String message, Current current) {
        System.out.println("Received update: " + message);
    }

    @Override
    public void connect(String[] configs, String type, Current current) {
        if (type.equals("STATION_VOTE")) {
            for (String config : configs) {
                VoteStationPrx voteService = VoteStationPrx.checkedCast(Testeador.mainCommunicator.stringToProxy(config));
                if (voteService == null) {
                    throw new Error("Invalid proxy for VoteService");
                }
                votes.add(voteService);
            }
        } else if (type.equals("STATION_QUERY")) {
            for (String config : configs) {
                QueryStationPrx queryStation = QueryStationPrx.checkedCast(Testeador.mainCommunicator.stringToProxy(config));
                if (queryStation == null) {
                    throw new Error("Invalid proxy for QueryStation");
                }
                querys.add(queryStation);
            }
        }
    }

    @Override
    public void vote(int[][] votes, Current current) {
        for (int[] vote : votes) {
            int candidateId = vote[1];
            String document = String.valueOf(vote[0]);
            for (VoteStationPrx voteService : this.votes) {
                callVoto(document, candidateId, voteService);
                callVoto(document, candidateId, voteService);//se llama dos veces para simular un error en la estacion
            }
        }
    }

    public void callVoto(String document, int candidateId, VoteStationPrx voteService) {
        try {
            int result = voteService.vote(document, candidateId);
            if (result != 1) {
                System.err.println("Error con el voto del lado de la estacion: " + voteService.toString() + ", voto: " + result);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
