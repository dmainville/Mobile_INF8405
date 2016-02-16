
public class GestionnaireNiveau implements IObserver
{

	Grille grille;
	int niveau;
	MainWindow window;
	
	
	GestionnaireNiveau(int niveau, MainWindow window)
	{
		this.niveau = niveau;
		this.grille = new Grille(niveau, this);
		this.window = window;
		
		window.addMouseListener(grille);
		window.addMouseMotionListener(grille);
		
		window.create(grille);
		
	}
	
	@Override
	public void notifyCase(Case c) {
		// TODO Auto-generated method stub
		System.out.println("Notifié de case");
		
	}

	@Override
	public void notifyVictoire() {
		// TODO Auto-generated method stub
		System.out.println("Notifié de victoire");
		
	}

}
