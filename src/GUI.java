import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI implements ActionListener
{
    JFrame frame;
    JScrollPane scrollPane;
    JTextArea textArea;
    JTextField textField;
    JPanel panel1;
    JPanel panel2;
    JPanel buttonPanel;
    JPanel textPanel;
    JButton button;


    public GUI()
    {

        Font font1 = new Font("SansSerif", Font.BOLD, 20);
        Font font2 = new Font("SansSerif", Font.PLAIN, 20);

        frame = new JFrame("Scheduler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setResizable(true);

        panel1 = new JPanel(new BorderLayout()); //Hauptpanel
        panel1.setPreferredSize(new Dimension(1280,800));
        panel1.setBorder(new EmptyBorder(10,10,10,10));

        panel2 = new JPanel(new BorderLayout());//Unteres Panel

        button = new JButton("Check");
        button.setPreferredSize(new Dimension(250,50));
        button.addActionListener(this);

        buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(10,10,10,10));
        buttonPanel.add(button,BorderLayout.PAGE_START);

        textField = new JTextField("r1(x)r2(y)w1(y)r3(z)w3(z)r2(x)w2(z)w1(x)c1c2c3");
        textField.setMargin(new Insets(10,30,10,20));
        //textField.setPreferredSize(new Dimension(500,50));
        textField.setFont(font1);

        textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(new EmptyBorder(10,10,10,10));
        textPanel.setPreferredSize(new Dimension(900,50));
        textPanel.add(textField,BorderLayout.PAGE_START);

        textArea = new JTextArea("");
        textArea.setFont(font2);
        textArea.setMargin(new Insets(20,20,20,20));

        scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(900,700));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(new EmptyBorder(10,10,10,10));


        panel2.add(scrollPane,BorderLayout.LINE_START);
        panel1.add(buttonPanel,BorderLayout.LINE_END);
        panel1.add(textPanel,BorderLayout.LINE_START);
        panel1.add(panel2,BorderLayout.PAGE_END);

        frame.add(panel1);
        frame.pack();
        frame.setVisible(true);
    }



    /**
     * Nimmt einen Graphen und wandelt diesen in ein Objekt um, welches einem ContentPane hinzugefügt werden kann
     * @param g Betrachteter Graph
     * @return Visualisierungsobjekt für z.B. ein JFrame
     */
    public BasicVisualizationServer<Integer,String> getGraphVisualisation(Graph g)
    {
        // The Layout<V, E> is parameterized by the vertex and edge types
        Layout<Integer, String> layout = new CircleLayout(g);
        layout.setSize(new Dimension(300,300)); // sets the initial size of the space
        // The BasicVisualizationServer<V,E> is parameterized by the edge types
        BasicVisualizationServer<Integer,String> vv =
                new BasicVisualizationServer<Integer,String>(layout);

        vv.setPreferredSize(new Dimension(350,350)); //Sets the viewing area size
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

        return vv;
    }

    public void actionPerformed (ActionEvent ae)
    {
        if(ae.getSource() == this.button)
        {
            Scheduler scheduler = new Scheduler(textField.getText());
            String result = scheduler.check();
            Graph graph = scheduler.getConflictGraph(scheduler.getSchedule());
            BasicVisualizationServer<Integer, String> vv = getGraphVisualisation(graph);
            //vv.setPreferredSize(new Dimension(500,500));
            panel2.add(vv,BorderLayout.LINE_END);
            textArea.setText(result);
            frame.validate();

        }

    }

    public static void main(String[] args)
    {
        String ex1 = "r1(x)r2(y)w1(y)r3(z)w3(z)r2(x)w2(z)w1(x)c1c2c3";
        String ex2 = "r1(x)w3(y)r2(x)w1(x)r2(y)r3(x)c1a2c3";
        String ex3 = "r1(x)r2(y)w2(x)w1(y)c2c1";
        String ex4 = "r2(y)w2(y)r2(x)r1(y)r1(x)w1(x)w1(z)r3(z)r3(x)w3(z)c1c2c3";//Blatt3 Aufgabe 3(a)
        String ex5 = "r1(x)r2(z)w3(y)r1(y)r2(x)r3(y)w1(x)w2(z)r3(z)w1(z)w3(x)c1c2c3";//Blatt3 Aufgabe 3(b)
        String ex6 = "r2(z)r1(x)w2(x)r4(x)r1(y)r4(y)w3(y)r4(z)w4(y)c1c2c3c4";//Blatt3 Aufgabe 3(c)

        new GUI();
    }
}
