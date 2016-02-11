
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MainWindow m = new MainWindow();

		Grille g = new Grille();
		
		m.addMouseListener(g);
		m.addMouseMotionListener(g);
		
		System.out.println(g.toString());
		
		m.create(g);
		

		
	}

}
