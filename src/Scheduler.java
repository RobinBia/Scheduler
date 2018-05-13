import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class Scheduler
{
    private Schedule sched;
    private boolean printReadFrom;

    public Scheduler(String sched_string)
    {
        printReadFrom = true;
        sched = new Schedule(sched_string);
        System.out.println(isVSR(sched));

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
        List<List<Integer>> perm;

        /*Erzeugt für eine gegebene Integer-Liste (alle Zahlen von 1 bis Anzahl Transaktionen)
        * alle Permutationen
        */
        for(int i = 1;i<=num;i++)
        {
            l.add(i);
        }
        perm = listPermutations(l);

        //Erstelle anhand dieser Permutationen die Liste mit allen seriellen Schedules
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

    /**
     * Berechnet, ob ein Schedule sichtserialisierbar ist
     * @param sched der betrachtete Schedule
     * @return true => ist sichtserialisierbar, false => ist nicht sichtserialisierbar
     */
    public boolean isVSR(Schedule sched)
    {
        for(Schedule sched_serial : getSerialSchedules(sched))
        {
            if(isVEQ(sched,sched_serial))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Berechnet, ob s1 sichtäquivalent s2 ist.
     * @param s1 Schedule 1
     * @param s2 Schedule 2 (i.d.r. ein serieller Schedule von s1)
     * @return true => sichtäquivalent, false => nicht sichtäquivalent
     */
    public boolean isVEQ(Schedule s1, Schedule s2)
    {
        if(readFrom(s1).equals(readFrom(s2)))
            return true;
        else
            return false;
    }

    /**
     * Gibt die Liest-von-Relationen eines Schedules als Hashset aus.
     * @param sched der betrachtete Schedule
     * @return Hashset mit den Liest-von-Relationen
     */
    public HashSet readFrom(Schedule sched)
    {
        HashSet<String> rf = new HashSet();
        HashSet<String> objs = sched.getOperationObjects();
        ArrayList<Operation> ops_virtual = new ArrayList<Operation>();

        for(String obj:objs)
        {
            ops_virtual.add(new Operation("w","",0,obj));
        }
        ops_virtual.addAll(sched.getOPs());
        for(String obj:objs)
        {
            ops_virtual.add(new Operation("r","",Integer.MAX_VALUE,obj));
        }
        //Nun sind die Operationen durch T_0 und T_infinity ergänzt

        for(int i = 0;i<ops_virtual.size();i++)
        {
            if(ops_virtual.get(i).getRead_Write().equals("r"))
            {
                Operation last_write_op =  getLastWrite(ops_virtual,i);
                rf.add("(T"+last_write_op.getTransaction_Number()
                        +","+last_write_op.getObj()+",T"+ops_virtual.get(i).getTransaction_Number()+")");
            }
        }

        if(printReadFrom == true)
        {
            for (String sr : rf)
            {
                System.out.println(sr);
            }
        }

        return rf;
    }

    /**
     * Gibt aus einer Liste von Operationen das letzte Schreiben eines Elementes bis zu einem bestimmten Index zurück
     * @param ops die Operationsliste
     * @param i der Index, bis zu dem nach Schreiboperationen gesucht wird
     * @return die gesuchte Operation
     */
    public Operation getLastWrite(ArrayList<Operation> ops, int i)
    {
        String obj = ops.get(i).getObj();
        for(int j = i-1; j>=0;j=j-1)
        {
            if (ops.get(j).getRead_Write().equals("w")
                    && ops.get(j).getObj().equals(obj)
                    && (ops.get(j).getTransaction_Number() != ops.get(i).getTransaction_Number()))
            {
                return ops.get(j);
            }
        }
        return null;
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
        Scheduler scheduler = new Scheduler("r1(x)w3(y)r2(x)w1(x)r2(y)r3(x)c1a2c3");
    }
}