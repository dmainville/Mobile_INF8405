
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MainWindow m = new MainWindow();

		GestionnaireNiveau g = new GestionnaireNiveau(1,m);
		//Grille g = new Grille(2);
		
		/*m.addMouseListener(g);
		m.addMouseMotionListener(g);*/
		
		System.out.println(g.toString());
		

		

		
	}

}
