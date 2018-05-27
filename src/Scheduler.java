import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.*;


public class Scheduler
{
    private String resultString;
    private Schedule sched;
    private boolean printHerbrandSemantics;
    private boolean printTransactionCount;
    private boolean printReadFrom;


    public Scheduler(String sched_string)
    {
        resultString = "";
        printHerbrandSemantics = false;
        printTransactionCount = false;
        printReadFrom = false;

        sched = new Schedule(sched_string);
    }


    /**
     * Gibt den Schedule sched zurück, der in Konstruktor initialisiert wurde
     * @return Schedule dieser Instanz
     */
    public Schedule getSchedule()
    {
        return sched;
    }

    /**
     * Überprüft nacheinander den an den Konstruktor übergebenen Schedule auf FSR, VSR, CSR und striktes 2PL
     * @return Ausgabestring für die GUI
     */
    public String check()
    {
        isFSR(filterAbort(sched));
        isVSR(filterAbort(sched));
        isCSR(filterAbort(sched));
        String plres = (new S2PL(sched)).strict2PL();
        resultString += plres;
        return resultString;
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
            //System.out.println(prettyPrint(sched_serial.getOps()));
            if(isFSEQ(sched,sched_serial))
            {
                resultString += "Der Schedule ist Final-State-Äquivalent mit "+prettyPrint(sched_serial.getOps());
                resultString += " und somit Final-State-Serialisierbar.\n\n";
                return true;
            }
        }
        resultString += "Der Schedule ist nicht Final-State-Serialisierbar\n\n";
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
        if (getScheduleHerbrandSemantics(s1).equals(getScheduleHerbrandSemantics(s2)))
        {
            resultString += "Es wurde ein Final-State-Äquivalenter serieller Schedule gefunden!\n";
            resultString += "Herbrand-Semantiken von Eingabeschedule:\n";
            resultString += getScheduleHerbrandSemantics(s1)+"\n";
            resultString += "Herbrand-Semantiken von seriellem Schedule:\n";
            resultString += getScheduleHerbrandSemantics(s2)+"\n";
            return true;
        }
        else
        {
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
        ops.addAll(sched.getOps());
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
        ops.addAll(sched.getOps());


        for(int k = 0;k<ops.size();k++)
        {
            int trans_num = ops.get(k).getTransactionNumber();
            int op_num = ops.get(k).getTransactionNumber();
            String op_obj = ops.get(k).getObject();

            //Hs(r_i(x)) := Hs(w_j(x)), j != i
            if(ops.get(k).getRead_Write().equals("r"))
            {
                String herbString = "";
                for(int l =k;l>=0;l = l-1)
                {
                    if(ops.get(l).getRead_Write().equals("w")
                            && ops.get(l).getTransactionNumber() != trans_num
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
                            && trans_num == ops.get(l).getTransactionNumber())
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
                System.out.println("Hs("+ops.get(k).getRead_Write()+""+ops.get(k).getTransactionNumber()
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
     * Erzeugt aus einer Operationsliste einen lesbaren Schedule-String
     * @param ops Eine Liste von Operationen
     * @return die Stringrepräsentation des Schedules
     */
    public String prettyPrint(ArrayList<Operation> ops)
    {
        String print = "";
        for(Operation op : ops)
        {
            if(op.getCommit_Abort().equals(""))
            {
                if(op.getRead_Write().equals("r"))
                    print += "r"+op.getTransactionNumber()+"("+op.getObject()+")";
                else
                    print += "w"+op.getTransactionNumber()+"("+op.getObject()+")";
            }
            else if(op.getRead_Write().equals(""))
            {
                if(op.getCommit_Abort().equals("a"))
                    print += "a"+op.getTransactionNumber();
                else
                    print += "c"+op.getTransactionNumber();
            }
        }
        return print;
    }


    /**
     * Gibt alle seriellen Schedules basierend auf dem Schedule sched aus.
     * @param sched Schedule, dessen seriellen Schedules berechnet werden sollen.
     * @return ArrayList aller seriellen Schedules.
     */
    public ArrayList<Schedule> getSerialSchedules(Schedule sched)
    {
        ArrayList<Schedule> sched_list = new ArrayList<Schedule>();
        HashMap<Integer,Transaction> trans_map = getTransactions(sched.getOps());
        List<Integer> l = new ArrayList<>();
        List<List<Integer>> perm;

        //Erzeugt für alle Transaktionsnummern alle Permutationen
        for(int i:getTransactionNumbers(sched.getOps()))
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
                sched_serial.appendOPs(trans_map.get(i).getOPs());
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
    public HashMap<Integer, Transaction> getTransactions(ArrayList<Operation> ops)
    {

        if(printTransactionCount)
        {
            System.out.println("Anzahl Transaktionen: " +getTransactionNumbers(ops).size()+"\n");
        }
        HashMap<Integer,Transaction> trans = new HashMap<>();

        for(Integer tn: getTransactionNumbers(ops))
        {
            Transaction t = new Transaction();
            for(Operation op : ops)
            {
                if(op.getTransactionNumber() == tn)
                {
                    t.addOp(op);
                }
            }
            trans.put(tn,t);
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
            transnums.add(op.getTransactionNumber());
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
            //System.out.println(prettyPrint(sched_serial.getOps()));
            if(isVEQ(sched,sched_serial))
            {
                resultString += "Der Schedule ist View-Äquivalent mit "+prettyPrint(sched_serial.getOps());
                resultString += " und somit View-Serialisierbar.\n\n";
                return true;
            }
        }
        resultString += "Der Schedule ist nicht View-Serialisierbar\n\n";
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
        if (readFrom(s1).equals(readFrom(s2)))
        {
            resultString += "Es wurde ein View-Äquivalenter serieller Schedule gefunden!\n";
            resultString += "RF-Menge von Eingabeschedule:\n"+readFrom(s1)+"\n";
            resultString += "RF-Menge von seriellem Schedule:\n"+readFrom(s2)+"\n";
            return true;
        }
        else
        {
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
        ops_virtual.addAll(sched.getOps());
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
                rf.add("(T"+last_write_op.getTransactionNumber()
                        +","+last_write_op.getObject()+",T"+ops_virtual.get(i).getTransactionNumber()+")");
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
                    && (ops.get(j).getTransactionNumber() != ops.get(i).getTransactionNumber()))
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
     * Gibt einen Schedule ohne Operationen einer abgebrochenen Transaktion zurück
     * @param sched betrachteter Schedule
     * @return neuer Schedule mit allen Operationsschritten zu Transaktionen, die nicht abgebrochen wurden
     */
    public Schedule filterAbort(Schedule sched)
    {
        ArrayList<Operation> ops = sched.getOps();
        HashSet<Integer> aborted_transactions = new HashSet();
        ArrayList<Operation> new_ops = new ArrayList<>();
        for(Operation op : ops)
        {
            if(op.getCommit_Abort().equals("a"))
            {
                aborted_transactions.add(op.getTransactionNumber());
            }
        }
        for(Operation op: ops)
        {
            if(!aborted_transactions.contains(op.getTransactionNumber()))
            {
                new_ops.add(op);
            }
        }
        Schedule sched_new = new Schedule();
        sched_new.setOps(new_ops);
        return sched_new;
    }

    /**
     * Berechnet ob ein Schedule konfliktserialisierbar ist.
     * @param sched Erster betrachteter Schedule
     * @return true = > Schedule ist konfliktserialisierbar, false => Schedule ist nicht konfliktserialisierbar
     */
    public boolean isCSR(Schedule sched)
    {
        Graph g = getConflictGraph(sched);
        if(hasNoCycle(g))
        {
            resultString += "Der Konfliktgraph hat keine Zyklen. Die Eingabe ist somit Konflikt-Serialisierbar\n\n";
        }
        else
        {
            resultString += "Der Konfliktgraph hat Zyklen. Die Eingabe ist somit nicht Konflikt-Serialisierbar\n\n";
        }
        return hasNoCycle(g); //Hat der erzeugte Graph einen Zyklus?
    }

    public Graph<Integer,String> getConflictGraph(Schedule sched)
    {
        ArrayList<Operation> ops = sched.getOps();
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
                int itn = iop.getTransactionNumber();
                int jtn = jop.getTransactionNumber();

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
        return g;
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

}