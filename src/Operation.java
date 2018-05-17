public class Operation
{
    private String commit_abort;
    private String read_write;
    private int transaction_number;
    private String obj;

    public Operation(String read_write, String commit_abort, int transaction_number, String obj)
    {
        this.commit_abort = commit_abort;//Kein Commit oder Abort => ""
        this.read_write = read_write; //Keine Lese oder Schreiboperation => ""
        this.transaction_number = transaction_number;
        this.obj = obj;
    }

    public String getCommit_Abort()
    {
        return commit_abort;
    }

    public String getRead_Write()
    {
        return read_write;
    }

    public int getTransaction_Number()
    {
        return transaction_number;
    }

    public String getObject()
    {
        return obj;
    }

}
