import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JComponent;


public class Grille extends JComponent implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	IObserver gestionnaire = null;
	int dimensionX;
	int dimensionY;
	Case[][] cases;
	int TOP_PIXELS = 33;
	int LEFT_PIXELS = 9;
	
	ECouleurCase couleurSelectionnee = ECouleurCase.Vide;
	Case caseSelectionnee = null;
	
	Grille(int niveau, IObserver gestionnaire)
	{
		this.gestionnaire = gestionnaire;
		
		int dim = getDimensionNiveau(niveau);
		this.dimensionX = dim;
		this.dimensionY = dim;
		
		initialiseCases();
		chargeNiveau(niveau);
	}
	
	Grille()
	{
		//Constructeur test, charge le premier niveau
		
		this.dimensionX = 7;
		this.dimensionY = 7;
		
		initialiseCases();
		chargeNiveau(1);
	}
	
	private int getDimensionNiveau(int niveau)
	{
		if(niveau>3)
			return 8;
		return 7;
	}
	
	private String getNomNiveau(int niveau)
	{
		String nomNiveau = "Niveau";
		String sousNiveau = "";
		String categorie = "7_7_";
		
		if(niveau>3)
			categorie = "8_8_";
		
		sousNiveau = Integer.toString(((niveau+2)%3)+1);
		
		return categorie+nomNiveau+sousNiveau+".txt";
	}
	
	private void initialiseCases()
	{
		cases = new Case[dimensionX][dimensionY];
		
		//Initialisation des cases à vide
		for(int i =0; i<dimensionX; i++)
		{
			for(int j =0; j<dimensionY; j++)
			{
				cases[i][j] = new Case(i,j);
			}
		}
	}
	
	private void chargeNiveau(int niveau)
	{
		String basePath = "src";
		String nomNiveau = getNomNiveau(niveau);
		String levelData = "Error loading level data";
		
		//Lire le fichier de données du niveau (Commas separated values)
		File file = new File(basePath+"\\"+nomNiveau);
		FileInputStream fis;
		try
		{
			fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			levelData = new String(data, "UTF-8");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String[] rows = levelData.split(";");
		for(int i=0; i<rows.length; i++)
		{
			String[] donneeCase = rows[i].split(",");
			int posX = Integer.parseInt(donneeCase[0].trim());
			int posY = Integer.parseInt(donneeCase[1].trim());
			String couleur = donneeCase[2];
			
			cases[posX][posY] = new Case(posX,posY,couleur);
		}
		
		System.out.println(levelData);
		
	}
	
  public void paint(Graphics g) {
	  
	  Graphics2D g2 = (Graphics2D) g;
	  
	  for(int i=0; i<dimensionX; i++)
	  {
		  for(int j=0; j<dimensionY; j++)
		  {
			  Case c = cases[i][j];
			  g2.drawImage(c.GetImageCase(), c.posX*c.DIMENSION, c.posY*c.DIMENSION, this);	  
		  }
	  }
	  		    
	  g2.finalize();
  }
  
  public String toString()
  {
	  String strGrille = "";
	  
	  for(int i=0; i<dimensionY; i++)
	  {
		  for(int j=0; j<dimensionX; j++)
		  {
			  Case c = cases[j][i];
			  strGrille+="["+c.GetStringFromCouleur()+"] ";
		  }
		  strGrille+="\n";
	  }
	  
	  return strGrille;
  }
  
  public boolean testVictoire()
  {
	  for(int i=0; i<dimensionX; i++)
	  {
		  for(int j=0; j<dimensionY; j++)
		  {
			  Case c = cases[j][i];
			  
			  if(c.etat == EEtatCase.Vide)
				  return false;
			  
			  if(c.etat == EEtatCase.Depart && c.posRelativeSuivant == EPositionRelative.Invalide)
				  return false;
		  }
	  }
		  
	  return true;
  }
  
  	public void EffaceCaseSuivantes(Case c)
  	{
  		if(c.caseSuivante==null)
  			return;
  		
		EffaceCaseSuivantes(c.caseSuivante);
		
		c.posRelativeSuivant = EPositionRelative.Invalide;
		
		if(c.caseSuivante.etat != EEtatCase.Depart)
			c.caseSuivante.etat = EEtatCase.Vide;
		
		c.caseSuivante.posRelativeSuivant = EPositionRelative.Invalide;
		c.caseSuivante.posRelativePrecedant = EPositionRelative.Invalide;
		c.caseSuivante.casePrecedente = null;
		c.caseSuivante.caseSuivante = null;
		
		notifyCase(c.caseSuivante);
		notifyCase(c);
		
		c.caseSuivante = null;
		
  	}
  	
  	public void EffaceCouleur(ECouleurCase couleur)
  	{
		for(int i=0; i<dimensionY; i++)
		{
			for(int j=0; j<dimensionX; j++)
			{
				Case c = cases[i][j];
				if(c.couleur == couleur)
				{
					if(c.etat == EEtatCase.Depart)
					{
						c.posRelativeSuivant = EPositionRelative.Invalide;
						notifyCase(c);
						continue;
					}

					
					c.couleur = ECouleurCase.Vide;
					c.etat = EEtatCase.Vide;
					c.posRelativeSuivant = EPositionRelative.Invalide;
					c.posRelativePrecedant = EPositionRelative.Invalide;
					c.casePrecedente = null;
					c.caseSuivante = null;
					
					notifyCase(c);
					
				}
			}
		}
	}
  	
  	public int GetPositionGrilleX(int x)
  	{
		return (x-LEFT_PIXELS)/100;
  	}
  	
  	public int GetPositionGrilleY(int y)
  	{
		return (y-TOP_PIXELS)/100;
  	}
  	
  	public EPositionRelative getPositionRelative(Case c1, Case c2)
  	{
  		//Position relative de 2 par rapport a 1
  		int difX = c1.posX - c2.posX;
  		if(difX > 1 || difX <-1)
  			return EPositionRelative.Invalide;
  		
  		int difY = c1.posY - c2.posY;
  		if(difY > 1 || difY <-1)
  			return EPositionRelative.Invalide;
  		
  		if(difX == 0)
  		{
  			if(difY == 1)
  				return EPositionRelative.Haut;
  			
  			if(difY == -1)
  				return EPositionRelative.Bas;
  		}
  		
  		if(difY == 0)
  		{
  			if(difX == 1)
  				return EPositionRelative.Gauche;
  			
  			if(difX == -1)
  				return EPositionRelative.Droite;
  		}
  		
  		return EPositionRelative.Invalide;
  		
  	}
  	
  	public boolean estCaseFinale(Case caseChemin, Case caseFinale)
  	{
  		if(caseChemin.couleur != caseFinale.couleur)
  			return false;
  		
  		if(caseFinale.etat != EEtatCase.Depart)
			return false;
			
  		if(caseFinale.posRelativeSuivant != EPositionRelative.Invalide)
			return false;
  			
  		return true;
  	}
  	
  	public boolean connecteCaseFinale(Case caseChemin)
  	{
  		if(caseChemin.caseSuivante!=null)
  			return false;
  		
  		int x = caseChemin.posX;
  		int y = caseChemin.posY;
  		
		Case caseFinale = null;
		for(int i= -1; i<=1; i+=2)
		{
			if(x+i < dimensionX && x+i >=0)
			{
				if(estCaseFinale(caseChemin, cases[x+i][y]))
				{
					caseFinale = cases[x+i][y];
					break;
				}
			}
			
			if(y+i < dimensionY && y+i >=0)
			{
				if(estCaseFinale(caseChemin, cases[x][y+i]))
				{
					caseFinale = cases[x][y+i];
					break;
				}
			}
		}
		
		if(caseFinale == null)
			return false;
		
		caseChemin.caseSuivante = caseFinale;
		caseFinale.casePrecedente = caseChemin;
		caseChemin.posRelativeSuivant = getPositionRelative(caseFinale, caseChemin);
		caseFinale.posRelativeSuivant = getPositionRelative(caseChemin, caseFinale);
  		
		notifyCase(caseChemin);
		notifyCase(caseChemin.caseSuivante);
		
  		return true;
  	}
  	
	public void CliqueCase(int x, int y, ETypeClique click)
	{	
		if(x >= dimensionX || x <0 || y>= dimensionY || y<0)
			return;
		
		Case c = cases[x][y];
		if(c == null)
			return;
		
		switch(click)
		{
			case Click :
				if(c.etat == EEtatCase.Vide)
					return;
				
				if(c.etat == EEtatCase.Depart)
					EffaceCouleur(c.couleur);
				else
					EffaceCaseSuivantes(c);
				
				couleurSelectionnee = c.couleur;
				//System.out.println(couleurSelectionnee);
				caseSelectionnee = c;
				
				//Essayer de connecter avec la fin si possible
				connecteCaseFinale(c);
				
				break;
				
			case Drag:
				
				if(couleurSelectionnee == ECouleurCase.Vide)
					return;
				
				if(caseSelectionnee == null)
					return;
				
				
				if(c.etat == EEtatCase.Vide)
				{
					EPositionRelative pos = getPositionRelative(caseSelectionnee, c);
					
					if(pos == EPositionRelative.Invalide)
						return;
					
					
					if(caseSelectionnee.caseSuivante != null && caseSelectionnee.caseSuivante.etat == EEtatCase.Depart)
						caseSelectionnee.caseSuivante.posRelativeSuivant = EPositionRelative.Invalide;
					
					c.etat = EEtatCase.Occupe;
					c.couleur = couleurSelectionnee;
					caseSelectionnee.caseSuivante = c;
					c.casePrecedente = caseSelectionnee;
					
					c.posRelativePrecedant = pos;
					caseSelectionnee.posRelativeSuivant = getPositionRelative(c, caseSelectionnee);
					
					caseSelectionnee = c;
					
					notifyCase(c);
					notifyCase(c.casePrecedente);
					
					
					//Essayer de connecter avec la fin si possible
					if(connecteCaseFinale(c))
					{
						if(testVictoire())
							notifyVictoire();
					}

							
					
				}
					
				break;
		default:
			return;
		}
		
		repaint();
		//System.out.println(this.toString());
	}

	//Notification des observes lors d'une modification de case
	public void notifyCase(Case c)
	{
		if(gestionnaire!=null)
			gestionnaire.notifyCase(c);
	}
	
	//Notification des observes lors d'une victoire
	public void notifyVictoire()
	{
		if(gestionnaire!=null)
			gestionnaire.notifyVictoire();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
			
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
			
		CliqueCase(GetPositionGrilleX(e.getX()),
					GetPositionGrilleY(e.getY()),
					ETypeClique.Click
		);
		
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int posX, posY;
		
		posX = (e.getX()-LEFT_PIXELS)/100;
		posY = (e.getY()-TOP_PIXELS)/100;
		
		CliqueCase(posX, posY, ETypeClique.Drag);
		
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
