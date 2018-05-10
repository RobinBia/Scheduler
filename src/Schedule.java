import java.util.ArrayList;

public class Schedule
{
    private ArrayList<Operation> op_list;
    public Schedule()
    {
        op_list = new ArrayList<Operation>();
    }

    public void addOP(Operation op)
    {
        op_list.add(op);
    }


    public ArrayList<Operation> getOPs()
    {
        return op_list;
    }
}
