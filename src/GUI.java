import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import javax.swing.*;
import java.awt.*;

public class GUI
{
    public GUI(Graph g)
    {
        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Graph g = createGraph();
        BasicVisualizationServer<Integer, String> vv = getGraphVisualisation(g);
        frame.getContentPane().add(vv);
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

}
