import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Scheduler
{
    public String schedule;

    public Scheduler(String s)
    {
        schedule = s;
    }

    public String[] getSerialSchedules(String[] s)
    {
        return null;
    }

    public int getNumberOfTransactions(String[] s)
    {
        return 1;
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
     * Gibt das Datum der Transaktion zurück
     * @param s Die Operation als String z.B. r1(x)
     * @return Das Datum x
     */
    public String getTransactionObject(String s)
    {
        Pattern p = Pattern.compile("(\\()([a-z])(\\))");
        Matcher m = p.matcher(s);
        m.find();
        return m.group(2);
    }

    /**
     * Gibt einen Schedule lesbar aus
     * @param sched ein Schedule
     */
    public void pretty_print(Schedule sched)
    {
        for(Operation op : sched.getOPs())
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

    public Schedule parse(String s)
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
        System.out.println(Arrays.toString(splitted));
        Schedule sched = new Schedule();

        for(int i=0;i<splitted.length;i++)
        {
            if(splitted[i].contains("r"))
            {
                sched.addOP(new Operation("r","",
                        getTransactionNumber(splitted[i]),getTransactionObject(splitted[i])));
            }
            else if(splitted[i].contains("w"))
            {
                sched.addOP(new Operation("w","",
                        getTransactionNumber(splitted[i]),getTransactionObject(splitted[i])));
            }
            else if(splitted[i].contains("c"))
            {
                sched.addOP(new Operation("","c",
                        getTransactionNumber(splitted[i]),""));
            }
            else if(splitted[i].contains("a"))
            {
                sched.addOP(new Operation("","a",
                        getTransactionNumber(splitted[i]),""));
            }
        }
        pretty_print(sched);
        return sched;
    }




    public boolean isVSR(String[] s)
    {

        return false;
    }

    public static void main(String args[])
    {
        Scheduler scheduler = new Scheduler("r1(x)r2(x)w1(x)r2(y)c1a2");
        scheduler.parse("r1(x)r2(x)w1(x)r2(y)c1a2");
    }
}
