import java.util.ArrayList;
import java.util.Arrays;
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
        op_list = new ArrayList<Operation>();
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
        //Schedule sched = new Schedule();

        for(int i=0;i<splitted.length;i++)
        {
            if(splitted[i].contains("r"))
            {
                op_list.add(new Operation("r","",
                        getTransactionNumber(splitted[i]), getOperationObject(splitted[i])));
            }
            else if(splitted[i].contains("w"))
            {
                op_list.add(new Operation("w","",
                        getTransactionNumber(splitted[i]), getOperationObject(splitted[i])));
            }
            else if(splitted[i].contains("c"))
            {
                op_list.add(new Operation("","c",
                        getTransactionNumber(splitted[i]),""));
            }
            else if(splitted[i].contains("a"))
            {
                op_list.add(new Operation("","a",
                        getTransactionNumber(splitted[i]),""));
            }
        }
    }

    /**
     * Gibt die Transaktionsnummer einer Operation zurück
     * @param s Die Operation als String z.B. r1(x)
     * @return Nummer der Transaktion (hier 1)
     */
    public int getTransactionNumber(String s)
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
    public String getOperationObject(String s)
    {
        Pattern p = Pattern.compile("(\\()([a-z])(\\))");
        Matcher m = p.matcher(s);
        m.find();
        return m.group(2);
    }

    public ArrayList<Operation> getOPs()
    {
        return op_list;
    }

    public void appendOPs(ArrayList<Operation> ops)
    {
        op_list.addAll(ops);
    }
}
