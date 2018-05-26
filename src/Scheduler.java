import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.*;


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

        System.out.println(isCSR(sched));
        //System.out.println(isFSR(sched));
        //System.out.println(isVSR(sched));

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
        for(Transaction trans : getTransactions(sched.getOPs()))
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
        ArrayList<Transaction> trans_list = getTransactions(sched.getOPs());
        int num = getTransactionNumbers(sched.getOPs()).size();
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
     * Gibt eine ArrayList mit Transaktionen in aufsteigender Reihenfolge zurück, die in der Operationsliste ops vorkommen.
     * @param ops die betrachtete Operationsliste
     * @return Die Transaktionsliste
     */
    public ArrayList<Transaction> getTransactions(ArrayList<Operation> ops)
    {

        if(printTransactionCount)
        {
            System.out.println("Anzahl Transaktionen: " +getTransactionNumbers(ops).size()+"\n");
        }
        ArrayList<Transaction> trans = new ArrayList<Transaction>();

        for(Integer tn: getTransactionNumbers(ops))
        {
            Transaction t = new Transaction();
            for(Operation op : ops)
            {
                if(op.getTransaction_Number() == tn)
                {
                    t.addOp(op);
                }
            }
            trans.add(t);
        }

        return trans;
    }


    /**
     * Gibt die Nummern aller Transaktionen zurück, die sich in der Operationsliste befinden
     * @param ops Betrachtete Liste
     * @return HashSet mit den Transaktionsnummern
     */
    public HashSet<Integer> getTransactionNumbers(ArrayList<Operation> ops)
    {
        HashSet<Integer> transnums = new HashSet<>();
        for(Operation op : ops)
        {
            transnums.add(op.getTransaction_Number());
        }
        return transnums;
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

    /**
     * Berechnet ob ein Schedule konfliktserialisierbar ist.
     * @param sched Erster betrachteter Schedule
     * @return true = > Schedule ist konfliktserialisierbar, false => Schedule ist nicht konfliktserialisierbar
     */
    public boolean isCSR(Schedule sched)
    {
        ArrayList<Operation> ops = filterAbort(sched.getOPs()); //Nur nicht abgebrochene Transaktionen betrachten
        Graph<Integer, String> g = new SparseMultigraph<>();
        HashSet<Integer> transnums = getTransactionNumbers(ops);

        for(int i : transnums) //Für jede Transaktion einen Knoten erstellen
        {
            g.addVertex(i);
        }

        for(int i = 0;i<ops.size();i++)
        {
            for(int j = i;j<ops.size();j++)
            {
                Operation iop = ops.get(i);
                Operation jop = ops.get(j);
                int itn = iop.getTransaction_Number();
                int jtn = jop.getTransaction_Number();

                //Folgendes if ist der Filter für die Konfliktrelation
                if(iop.getCommit_Abort().equals("") && jop.getCommit_Abort().equals("") //kein commit oder abort
                    && itn != jtn //unterschiedliche Transaktion
                    && iop.getObject().equals(jop.getObject()) //Gleiches Objekt
                    && (iop.getRead_Write().equals("w") || jop.getRead_Write().equals("w"))) //min eine Transaktion schreibt
                {
                    //Konflikt gefunden!
                    if(!getNumericEdges(g).contains(new Pair(itn,jtn))) //Schauen, ob Kante schon im Graphen vorhanden
                    {
                        //System.out.println(iop.getRead_Write()+""+itn+","+jop.getRead_Write()+""+jtn);
                        g.addEdge(itn+"->"+jtn, itn,jtn,EdgeType.DIRECTED);
                    }
                }
            }
        }
        new GUI(g);
        return hasNoCycle(g); //Hat der erzeugte Graph einen Zyklus?
    }

    /**
     * Alernative repräsentation für gerichtete Kanten als Liste von Paaren
     * @param g der betrachtete Graph
     * @return Die Liste mit den Paaren <a,b>, wobei a->b eine gerichtete Kante von a nach b ist
     */
    public ArrayList<Pair> getNumericEdges(Graph<Integer, String> g)
    {
        ArrayList<Pair> al = new ArrayList<>();
        for(String s : g.getEdges())
        {
            al.add(g.getEndpoints(s));
        }
        return al;
    }

    /**
     * Überprüft, ob ein gerichteter Graph keinen Zyklus hat
     * @param g der betrachtete Graph
     * @return true => g hat keinen Zyklus, false => g hat einen Zyklus
     */
    public boolean hasNoCycle(Graph g)
    {
        Collection<Integer> vertices = g.getVertices();

        for(Integer startVertex: vertices)//Führe für jeden Knoten als Startknoten die Tiefensuche aus
        {
            //Stacklösung statt rekursive Tiefensuche
            Stack visited = new Stack();
            Stack tovisit = new Stack();
            tovisit.push(startVertex);
            while (!tovisit.isEmpty())
            {
                int currentVertex = (Integer) tovisit.pop();
                //Visit current node:
                if (!visited.contains(currentVertex))
                {
                    Collection<String> cs = g.getOutEdges(currentVertex);
                    for (String s : cs)
                    {
                        tovisit.push(g.getDest(s));
                    }
                    visited.push(currentVertex);
                    //System.out.println("currentVertex: " + currentVertex);
                } else
                {
                    return false; //Schon mal besucht => Zyklus
                }

            }
        }
        return true;
    }

    public static void main(String args[])
    {
        String ex1 = "r1(x)r2(y)w1(y)r3(z)w3(z)r2(x)w2(z)w1(x)";
        String ex2 = "r1(x)w3(y)r2(x)w1(x)r2(y)r3(x)c1a2c3";
        String ex3 = "r1(x)r2(y)w2(x)w1(y)c2c1";
        String ex4 = "r2(y)w2(y)r2(x)r1(y)r1(x)w1(x)w1(z)r3(z)r3(x)w3(z)c1c2c3";//Blatt3 Aufgabe 3(a)
        String ex5 = "r1(x)r2(z)w3(y)r1(y)r2(x)r3(y)w1(x)w2(z)r3(z)w1(z)w3(x)c1c2c3";//Blatt3 Aufgabe 3(b)
        String ex6 = "r2(z)r1(x)w2(x)r4(x)r1(y)r4(y)w3(y)r4(z)w4(y)c1c2c3c4";//Blatt3 Aufgabe 3(c)
        Scheduler scheduler = new Scheduler(ex6);
        //GUI gui = new GUI();
    }
}