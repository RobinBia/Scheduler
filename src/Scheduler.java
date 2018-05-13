import java.util.ArrayList;
import java.util.List;


public class Scheduler
{
    private Schedule sched;

    public Scheduler(String s)
    {
        sched = new Schedule(s);
        //prettyPrint(sched.getOPs());
        //prettyPrintTransaction(sched);
        //prettyPrintSerialSchedules(sched);

    }


    /**
     * Gibt einen Schedule lesbar aus
     * @param ops Eine Liste von Operationen
     */
    public void prettyPrint(ArrayList<Operation> ops)
    {
        for(Operation op : ops)
        {
            if(op.getCommit_Abort().equals(""))
            {
                if(op.getRead_Write().equals("r"))
                    System.out.println("Lesen von "+ op.getObj()+" in Transaktion "+op.getTransaction_Number());
                else
                    System.out.println("Schreiben von "+ op.getObj()+" in Transaktion "+op.getTransaction_Number());
            }
            else if(op.getRead_Write().equals(""))
            {
                if(op.getCommit_Abort().equals("a"))
                    System.out.println("Abbruch von Transaktion "+ op.getTransaction_Number());
                else
                    System.out.println("Commit von Transaktion "+ op.getTransaction_Number());
            }
        }
    }

    /**
     * Gibt die Transaktionen eines Schedules sched leserlich aus.
     * @param sched Der betrachtete Schedule
     */
    public void prettyPrintTransaction(Schedule sched)
    {
        for(Transaction trans : getTransactions(sched))
        {
            System.out.println("\nTransaktion "+trans.getOPs().get(0).getTransaction_Number()+":");
            prettyPrint(trans.getOPs());
        }
    }


    /**
     * Gibt alle seriellen Schedules eines Schedules sched leserlich aus.
     * @param sched Der betrachtete Schedule
     */
    public void prettyPrintSerialSchedules(Schedule sched)
    {
        for(Schedule ss : getSerialSchedules(sched))
        {
            System.out.println("\n");
            prettyPrint(ss.getOPs());
            System.out.println("\n");
        }
    }

    /**
     * Gibt alle seriellen Schedules basierend auf dem Schedule sched aus.
     * @param sched Schedule, dessen seriellen Schedules berechnet werden sollen.
     * @return ArrayList aller seriellen Schedules.
     */
    public ArrayList<Schedule> getSerialSchedules(Schedule sched)
    {
        ArrayList<Schedule> sched_list = new ArrayList<Schedule>();
        ArrayList<Transaction> trans_list = getTransactions(sched);
        int num = getNumberOfTransactions(sched);
        List<Integer> l = new ArrayList<>();
        List<List<Integer>> perm = new ArrayList<>();
        for(int i = 1;i<=num;i++)
        {
            l.add(i);
        }
        perm = listPermutations(l);

        for(List<Integer> p:perm)
        {
            Schedule sched_serial = new Schedule();
            for(int i:p)
            {
                sched_serial.appendOPs(trans_list.get(i-1).getOPs());
            }
            sched_list.add(sched_serial);
        }
        return sched_list;
    }

    /**
     * Gibt eine ArrayList mit Transaktionen in aufsteigender Reihenfolge zurück, die in Schedule sched vorkommen
     * @param sched Der betrachtete Schedule
     * @return Die Transaktionsliste
     */
    public ArrayList<Transaction> getTransactions(Schedule sched)
    {
        int num = getNumberOfTransactions(sched);
        System.out.println("Anzahl Transaktionen "+num);
        ArrayList<Transaction> trans = new ArrayList<Transaction>();

        for(int i = 1;i<=num;i++)
        {
            Transaction t = new Transaction();
            for(Operation op : sched.getOPs())
            {
                if(op.getTransaction_Number() == i)
                {
                    t.addOp(op);
                }
            }
            trans.add(t);
        }

        return trans;
    }


    /**
     * Ermittelt die Anzahl der Transaktionen in dem gegebenen Schedule
     * @param sched Betrachteter Schedule
     * @return Anzahl der Transaktionen
     */
    public int getNumberOfTransactions(Schedule sched)
    {
        int num = 0;
        for(Operation op : sched.getOPs())
        {
            if (op.getTransaction_Number() > num)
                num = op.getTransaction_Number();
        }
        return num;
    }

    public boolean isVSR(String[] s)
    {

        return false;
    }


    public  List<List<Integer>> listPermutations(List<Integer> list)
    {

        if (list.size() == 0) {
            List<List<Integer>> result = new ArrayList<List<Integer>>();
            result.add(new ArrayList<Integer>());
            return result;
        }

        List<List<Integer>> returnMe = new ArrayList<List<Integer>>();

        Integer firstElement = list.remove(0);

        List<List<Integer>> recursiveReturn = listPermutations(list);
        for (List<Integer> li : recursiveReturn) {

            for (int index = 0; index <= li.size(); index++) {
                List<Integer> temp = new ArrayList<Integer>(li);
                temp.add(index, firstElement);
                returnMe.add(temp);
            }

        }
        return returnMe;
    }


    public static void main(String args[])
    {
        Scheduler scheduler = new Scheduler("r1(x)r3(y)r2(x)w1(x)r2(y)c1a2c3");
    }
}