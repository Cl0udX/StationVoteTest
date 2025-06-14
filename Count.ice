module Demo
{   
    sequence<string> ListString;
    sequence<int> IntSeq;
    sequence<IntSeq> MatrixInt;

    //server
    interface VoteService
    {
        int addVote(string document, int candidateId);
        string executeQuery(string document);
    }

    //cliente
    interface QueryStation{
        string query(string document);
    }
    //Usted debe votar en Nombre del lugar de votación ubicado en dirección del lugar de votación en ciudad, departamento en la mesa #Mesa.

    interface VoteStation{
        int vote(string document, int candidateId);
    }
    //candidateId, candidateName, totalVotes


    // Testeador
    interface Observer
    {
        void update(string s);
        void connect(ListString configs, string type);
        void vote(MatrixInt votes);
    }

    // Orquest
    interface Subject
    {
        void registerObserver(string name, Observer* o);
        void removeObserver(string name);

    }
}