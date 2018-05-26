import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schedule
{
    private ArrayList<Operation> op_list;

    /**
     * Erstellt den Schedule basierend auf der Stringrepräsentation s
     * @param s Der String, welcher in die Datenstruktur Schedule überführt wird.
     */
    public Schedule(String s)
    {
        op_list = new ArrayList<>();
        parse(s);
    }

    /**
     * Leeren Schedule erstellen
     */
    public Schedule()
    {
        op_list = new ArrayList<Operation>();
    }

    /**
     * Wandelt einen Schedule gegeben in der Form eines Strings in die Datenstruktur Schedule um
     * @param s Stringrepräsentation des Schedules
     * @return Schedule Datenstruktur
     */
    public void parse(String s)
    {
        String ns = "";
        ns = s.replaceAll("\\)r", "\\).r");
        ns = ns.replaceAll("\\)w", "\\).w");
        ns = ns.replaceAll("\\)a", "\\).a");
        ns = ns.replaceAll("\\)c", "\\).c");
        ns = ns.replaceAll("(\\d+)a", "$1.a");
        ns = ns.replaceAll("(\\d+)c", "$1.c");
        ns = ns.replaceAll("(\\d+)w", "$1.w");
        ns = ns.replaceAll("(\\d+)r", "$1.r");
        String[] splitted = ns.split("\\.");
        System.out.println("\n"+Arrays.toString(splitted)+"\n");


        for(int i=0;i<splitted.length;i++)
        {
            if(splitted[i].contains("r"))
            {
                op_list.add(new Operation("r","",
                        parseTransactionNumber(splitted[i]), parseOperationObject(splitted[i])));
            }
            else if(splitted[i].contains("w"))
            {
                op_list.add(new Operation("w","",
                        parseTransactionNumber(splitted[i]), parseOperationObject(splitted[i])));
            }
            else if(splitted[i].contains("c"))
            {
                op_list.add(new Operation("","c",
                        parseTransactionNumber(splitted[i]),""));
            }
            else if(splitted[i].contains("a"))
            {
                op_list.add(new Operation("","a",
                        parseTransactionNumber(splitted[i]),""));
            }
        }
    }

    /**
     * Gibt die Transaktionsnummer einer Operation zurück
     * @param s Die Operation als String z.B. r1(x)
     * @return Nummer der Transaktion (hier 1)
     */
    public int parseTransactionNumber(String s)
    {
        Pattern p = Pattern.compile("([0-9]+)");
        Matcher m = p.matcher(s);
        m.find();
        return Integer.parseInt(m.group(0));
    }

    /**
     * Gibt das Datum der Operation zurück
     * @param s Die Operation als String z.B. r1(x)
     * @return Das Datum x
     */
    public String parseOperationObject(String s)
    {
        Pattern p = Pattern.compile("(\\()([a-z])(\\))");
        Matcher m = p.matcher(s);
        m.find();
        return m.group(2);
    }

    public HashSet<String> getOperationObjects()
    {
        HashSet<String> obj = new HashSet<String>();
        for(Operation op : op_list)
        {
            if(!op.getObject().equals(""))
                obj.add(op.getObject());
        }
        return obj;
    }

    public ArrayList<Operation> getOps()
    {
        return op_list;
    }

    public void setOps(ArrayList<Operation> new_ops)
    {
        op_list = new_ops;
    }

    public void appendOPs(ArrayList<Operation> ops)
    {
        op_list.addAll(ops);
    }
}
