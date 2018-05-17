import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class Scheduler
{
    private Schedule sched;
    private boolean printReadFrom;
    private boolean printHerbrandSemantics;
    private boolean printTransactionCount;

    public Scheduler(String sched_string)
    {
        printTransactionCount = true;
        printReadFrom = true;
        printHerbrandSemantics = true;

        sched = new Schedule(sched_string);

        System.out.println(isFSR(sched));
        System.out.println(isVSR(sched));

        //prettyPrint(filterAbort(sched.getOPs()));
        //prettyPrint(sched.getOPs());
        //prettyPrintTransaction(sched);
        //prettyPrintSerialSchedules(sched);
    }

    /**
     * Berechnet, ob ein Schedule Final-State-Serialisierbar ist
     * @param sched der betrachtete Schedule
     * @return True => sched ist in FSR, False => sched ist nicht in FSR
     */
    public boolean isFSR(Schedule sched)
    {
        for(Schedule sched_serial : getSerialSchedules(sched))
        {
            if(isFSEQ(sched,sched_serial))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Berechnet, ob Schedule s1 Final-State-Äquivalent zu Schedule s2
     * @param s1 Schedule s1
     * @param s2 Schedule s2
     * @return False => s1 nicht FSEQ s2, True => s1 ist FSEQ s2
     */
    public boolean isFSEQ(Schedule s1, Schedule s2)
    {

        if(printReadFrom)
        {
            System.out.println("Berechne Herbrand Semantiken der zu vergleichenden Schedules: ");
            if (getScheduleHerbrandSemantics(s1).equals(getScheduleHerbrandSemantics(s2)))
            {
                System.out.println("Die Herbrand-Semantiken der Schedules sind gleich.\n");
                return true;
            }
            else
            {
                System.out.println("Die Herbrand-Semantiken unterscheiden sich.\n");
                return false;
            }
        }
        else
        {
            if (getScheduleHerbrandSemantics(s1).equals(getScheduleHerbrandSemantics(s2)))
                return true;
            else
                return false;
        }
    }

    /**
     * Erzeugt die Herbrand-Semantiken eines Schedules sched
     * @param sched der betrachtete Schedule
     * @return Eine ArrayList vom Typ String mit den Herbrand-Semantiken des Schedules
     */
    public ArrayList<String> getScheduleHerbrandSemantics(Schedule sched)
    {
        ArrayList<String> schedSemantics = new ArrayList<>();
        ArrayList<Operation> ops = createT0(sched.getOperationObjects());
        ops.addAll(sched.getOPs());
        ArrayList<String> opSemantics = getOperationHerbrandSemantics(sched);
        //Element an Index i von ops bezieht sich auf Semantik i von opSemantics!

        //Nun müssen die Semantiken der letzten Lese- und Schreiboperationen aller Objekte extrahiert werden:

        for(String obj:sched.getOperationObjects())
        {
            for(int i = ops.size()-1;i>=0;i=i-1)
            {
                if(ops.get(i).getRead_Write().equals("w") && ops.get(i).getObject().equals(obj))
                {
                    schedSemantics.add("H(s)("+obj+")="+opSemantics.get(i));
                    break;
                }
            }
        }
        if(printHerbrandSemantics)
        {
            for(String s:schedSemantics)
            {
                System.out.println(s);
            }
        }
        return schedSemantics;
    }

    /**
     * Gibt die Herbrand-Semantiken aller Lese- und Schreiboperationen eines Schedules als String-ArrayList zurück.
     * @param sched der betrachtete Schedule
     * @return Die ArrayList (Typ String) mit den Herbrand-Strings
     */
    public ArrayList<String> getOperationHerbrandSemantics(Schedule sched)
    {
        ArrayList<String> herbsem = new ArrayList<>();
        HashSet<String> objs = sched.getOperationObjects();
        //Nun wird die Operationsliste erstellt und die Initialisierungstransaktion hinzugefügt
        ArrayList<Operation> ops = createT0(objs);
        ops.addAll(sched.getOPs());


        for(int k = 0;k<ops.size();k++)
        {
            int trans_num = ops.get(k).getTransaction_Number();
            int op_num = ops.get(k).getTransaction_Number();
            String op_obj = ops.get(k).getObject();

            //Hs(r_i(x)) := Hs(w_j(x)), j != i
            if(ops.get(k).getRead_Write().equals("r"))
            {
                String herbString = "";
                for(int l =k;l>=0;l = l-1)
                {
                    if(ops.get(l).getRead_Write().equals("w")
                            && ops.get(l).getTransaction_Number() != trans_num
                            && op_obj.equals(ops.get(l).getObject()))
                    {
                        herbString = herbsem.get(l);
                        break;
                    }
                }
                herbsem.add(herbString);
            }
            else if (ops.get(k).getRead_Write().equals("w"))
            {//Hs(w_i(x)) := f_i,x(Hs(r_i(y_1)),...,Hs(r_i(y_m)))
                String herbString = "f"+op_num+","+op_obj+"(";

                //Füge Herbrand Semantiken aller vorherigen Leseoperationen aus
                //Transaktion transnum anderer Objekte als op_obj hinzu
                boolean inif = false;
                for(int l = 0;l<k;l++)
                {
                    if(ops.get(l).getRead_Write().equals("r")
                            && trans_num == ops.get(l).getTransaction_Number())
                    {
                        if(inif)
                        {
                            herbString += ",";
                            herbString += herbsem.get(l);
                        }
                        else
                        {
                            herbString += herbsem.get(l);
                        }
                        inif = true; //Nötig für korrekte Kommasetzung
                    }

                }
                herbString += ")";
                herbsem.add(herbString);
            }
            else
            {
                herbsem.add("no rw");
            }
            if(printHerbrandSemantics)
            {
                if(!ops.get(k).getRead_Write().equals(""))
                System.out.println("Hs("+ops.get(k).getRead_Write()+""+ops.get(k).getTransaction_Number()
                                            +"("+ops.get(k).getObject()+"))="+herbsem.get(k));
            }
        }
        return herbsem;
    }

    public ArrayList<Operation> createT0(HashSet<String> objs)
    {
        ArrayList<Operation> ops = new ArrayList<>();
        for (String obj : objs)
        {
            ops.add(new Operation("w", "", 0, obj));
        }
        ops.add(new Operation("", "c", 0, ""));
        return ops;
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
                    System.out.println("Lesen von "+ op.getObject()+" in Transaktion "+op.getTransaction_Number());
                else
                    System.out.println("Schreiben von "+ op.getObject()+" in Transaktion "+op.getTransaction_Number());
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
        if(printTransactionCount)
        {
            System.out.println("Anzahl Transaktionen: " + num+"\n");
        }
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
        if(printReadFrom)
        {
            System.out.println("Vergleiche Schedules:");
            if (readFrom(s1).equals(readFrom(s2)))
            {
                System.out.println("Die RF-Mengen sind gleich.\n");
                return true;
            }
            else
            {
                System.out.println("Die RF-Mengen unterscheiden sich.\n");
                return false;
            }
        }
        else
        {
            if (readFrom(s1).equals(readFrom(s2)))
                return true;
            else
                return false;
        }
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
                        +","+last_write_op.getObject()+",T"+ops_virtual.get(i).getTransaction_Number()+")");
            }
        }

        if(printReadFrom == true)
        {
            String sp = "";
            sp += "{";
            for (String sr : rf)
            {
                sp += sr+",";
            }
            sp += "}";
            sp = sp.replaceAll(",}","}");
            System.out.println(sp);
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
        String obj = ops.get(i).getObject();
        for(int j = i-1; j>=0;j=j-1)
        {
            if (ops.get(j).getRead_Write().equals("w")
                    && ops.get(j).getObject().equals(obj)
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

    /**
     * Gibt die Operationsliste eines Schedules ohne Operationen einer abgebrochenen Transaktion zurück
     * @param ops betrachtete Operationsliste
     * @return Liste mit allen Operationsschritten zu Transaktionen, die nicht abgebrochen wurden
     */
    public ArrayList<Operation> filterAbort(ArrayList<Operation> ops)
    {
        HashSet<Integer> aborted_transactions = new HashSet();
        ArrayList<Operation> new_ops = new ArrayList<>();
        for(Operation op : ops)
        {
            if(op.getCommit_Abort().equals("a"))
            {
                aborted_transactions.add(op.getTransaction_Number());
            }
        }
        for(Operation op: ops)
        {
            if(!aborted_transactions.contains(op.getTransaction_Number()))
            {
                new_ops.add(op);
            }
        }
        return new_ops;

    }

    public static void main(String args[])
    {
        String ex1 = "r1(x)r2(y)w1(y)r3(z)w3(z)r2(x)w2(z)w1(x)";
        String ex2 = "r1(x)w3(y)r2(x)w1(x)r2(y)r3(x)c1a2c3";
        String ex3 = "r1(x)r2(y)w2(x)w1(y)c2c1";
        Scheduler scheduler = new Scheduler(ex2);


    }
}