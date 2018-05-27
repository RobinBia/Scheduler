import java.util.ArrayList;

public class S2PL
{
    private ArrayList<Integer> commitedOrAborted; //Liste mit allen abgeschlossenen oder abgebrochenen Transaktionen (Nummern)
    private ArrayList<Operation> todo; //Falls eine Transaktion wartet
    private ArrayList<Triplet> locks;//Liste mit allen aktuellen locks: Triplet speichert <tn,pl,obj>, p = r | w
    private ArrayList<Operation> ops;
    private ArrayList<String> advanced;//Um rl,wl,u erweiterter Schedule

    public S2PL(Schedule sched)
    {
        commitedOrAborted = new ArrayList<>();
        todo = new ArrayList<>();
        locks = new ArrayList<>();
        ops = sched.getOps();
        advanced = new ArrayList<>();
    }


    /**
     * Achtung:LR1-LR3 gelten auch für abgebrochene Transaktionen!
     */
    public String strict2PL()
    {
        for(Operation op : ops)
        {
            ArrayList<Operation> correctToDoOps = new ArrayList<>();
            for(Operation tdop : todo)
            {
                System.out.print(tdop.getRead_Write()+tdop.getTransactionNumber()+"("+tdop.getObject()+"),");
                if(addRoutine(tdop,true).equals("todo succeed"))
                {
                    correctToDoOps.add(tdop);
                }
                else if(addRoutine(tdop,true).equals("fail"))
                {
                    return "Der Schedule kann das strikte 2-Phasen-Protokoll nicht erfüllen.";
                }
            }
            todo.removeAll(correctToDoOps);//Entferne alle korrekt ausgeführten todos
            System.out.println("");

            if(addRoutine(op,false).equals("fail"))
            {
                return "Der Schedule kann das strikte 2-Phasen-Protokoll nicht erfüllen.";
            }
        }
        String result = "\nDer um Read-Locks, Write-Locks und Unlocks erweiterte Schedule bezüglich S2PL ist:\n";
        for (String adop : advanced)
        {
            result += adop;
        }
        return result;
    }

    public String addRoutine(Operation op, boolean todoOp)
    {
        int tn = op.getTransactionNumber(); //Transaktionsnummer der betrachteten Operation
        String obj = op.getObject(); //Objekt der betrachteten Operation

        if(commitedOrAborted.contains(tn))
        {
            return "fail";
        }

        if(op.getRead_Write().equals("r"))
        {
            boolean found = false; //Objekt schon gelockt? false => nein
            for(Triplet<Integer,String,String> lock: locks)//Schaue ob das Objekt schon gelockt ist
            {
                if(lock.getThird().equals(op.getObject())) //Objekt wurde schon gelockt
                {
                    found = true;
                    if(tn != lock.getFirst()) //Andere Transaktion will sperren
                    {
                        if (!todo.contains(op)) //Wird schon gewartet?
                        {
                            todo.add(op); //Diese Transaktion muss warten, bis Objekt freigegeben wird
                        }
                        return "wait";
                    }
                    else //Erneuter lock? falsch
                    {
                        return "fail";
                    }
                }
            }
            if(!found) //Objekt noch nicht gelockt, führe Routine durch
            {
                advanced.add("rl"+tn+"("+obj+")");
                advanced.add("r"+tn+"("+obj+")");
                locks.add(new Triplet<>(tn,"rl",obj));
                if(todoOp) //Diese Operation darf gelöscht werden, da die Routine erfolgreich war
                {
                    return "todo succeed";
                }
                return "succeed";
            }
        }
        else if(op.getRead_Write().equals("w"))
        {
            boolean found = false;
            Triplet<Integer,String,String> upgrade = null;
            for(Triplet<Integer,String,String> lock: locks)//Schaue ob das Objekt schon gelockt ist
            {
                if(lock.getThird().equals(op.getObject())) //Objekt wurde schon gelockt
                {
                    found = true;
                    if(tn != lock.getFirst()) //Andere Transaktion will sperren, muss aber warten
                    {
                        if (!todo.contains(op)) //Wird schon gewartet?
                        {
                            todo.add(op); //Diese Transaktion muss warten, bis Objekt freigegeben wird
                        }
                        return "wait";
                    }
                    else if(lock.getSecond().equals("rl")) //rl -> wl upgrade
                    {
                        advanced.add("wl"+tn+"("+obj+")");
                        advanced.add("w"+tn+"("+obj+")");
                        upgrade = lock;
                    }
                    else
                    {
                        return "fail";
                    }
                }
            }
            if(!(upgrade == null)) //Ein Lock soll upgraded werden
            {
                locks.remove(upgrade);
                locks.add(new Triplet<>(upgrade.getFirst(),"wl",upgrade.getThird()));
                return "upgrade";
            }

            if(!found) //Objekt noch nicht gelockt, führe Routine durch
            {
                advanced.add("wl"+tn+"("+obj+")");
                advanced.add("w"+tn+"("+obj+")");
                locks.add(new Triplet<>(tn,"wl",obj));
                if(todoOp) //Diese Operation darf gelöscht werden, da die Routine erfolgreich war
                {
                    return "todo succeed";
                }
                return "succeed";
            }
        }
        else //commit und abort werden gleich behandelt, da Regeln zur Wohlgeformtheit auch für abg. Transaktionen gelten.
        {
            advanced.add(op.getCommit_Abort() + tn);
            commitedOrAborted.add(tn);
            ArrayList<Triplet> found = new ArrayList<>();
            for (Triplet<Integer, String, String> lock : locks)
            {//Transaktion tn wurde commited oder abebrochen => jetzt muss alles freigegeben werden
                if (lock.getFirst() == tn)
                {
                    advanced.add("u" + tn + "(" + lock.getThird() + ")");
                    found.add(lock);
                }
            }
            locks.removeAll(found); //Lösche alle Locks, für die ein Unlock hinzugefügt wurde
            return "succeed";
        }
        return "succeed";
    }

}
