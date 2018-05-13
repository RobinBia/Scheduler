import java.util.ArrayList;

public class Transaction
{
    ArrayList<Operation> trans_ops = new ArrayList<Operation>();

    public Transaction()
    {

    }

    public void addOp(Operation op)
    {
        trans_ops.add(op);
    }

    public ArrayList<Operation> getOPs()
    {
        return trans_ops;
    }


}
