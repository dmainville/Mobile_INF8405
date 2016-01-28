import java.awt.Container;
import javax.swing.JFrame;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
    /**
     * Constructor
     * Initialize the JFrame with a title.
     */
    protected MainWindow() {
    	super("TP1");
    }

    /**
     * Initialize every components of the JFrame.
     * Creates every sub-panels.
     * @param adapter : link to the model
     */
    protected void create(Grille g){
    	
    	Container c = this.getContentPane();
    	c.add(g);
    	
    	this.setBounds(30, 30, 500, 500);

    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Display the window.
        this.setVisible(true);
    }

}