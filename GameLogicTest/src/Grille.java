import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JComponent;


public class Grille extends JComponent implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	int dimensionX;
	int dimensionY;
	Case[][] cases;
	int TOP_PIXELS = 33;
	int LEFT_PIXELS = 9;
	
	ECouleurCase couleurSelectionnee = ECouleurCase.Vide;
	Case caseSelectionnee = null;
	
	Grille(int niveau)
	{
		
	}
	
	Grille()
	{
		this.dimensionX = 7;
		this.dimensionY = 7;
		
		cases = new Case[dimensionX][dimensionY];
		
		//Initialisation des cases à vide
		for(int i =0; i<dimensionX; i++)
		{
			for(int j =0; j<dimensionY; j++)
			{
				cases[i][j] = new Case(i,j);
			}
		}
		
		String basePath = "src";
		String nomLevel = "7_7_Niveau1.txt";
		
		String levelData = "Error loading level data";
		
		//Lire le fichier de données du niveau (Commas separated values)
		File file = new File(basePath+"\\"+nomLevel);
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
  
  	
  	public void EffaceCaseSuivantes(Case c)
  	{
  		if(c.caseSuivante==null)
  			return;
  		
		EffaceCaseSuivantes(c.caseSuivante);
		c.caseSuivante.etat = EEtatCase.Vide;
		c.caseSuivante.casePrecedente = null;
		c.caseSuivante.caseSuivante = null;
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
						continue;
					
					c.couleur = ECouleurCase.Vide;
					c.etat = EEtatCase.Vide;
					c.casePrecedente = null;
					c.caseSuivante = null;
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
  		int difX = GetPositionGrilleX(c1.posX) - GetPositionGrilleX(c2.posX);
  		if(difX > 1 || difX <-1)
  			return EPositionRelative.Invalide;
  		
  		int difY = GetPositionGrilleY(c1.posY) - GetPositionGrilleY(c2.posY);
  		if(difY > 1 || difY <-1)
  			return EPositionRelative.Invalide;
  		
  		if(difX == 0)
  		{
  			//if(difY == 1)
  				//TO BE CONTINUED
  			//TODO
  		}
  		
  		
  		return EPositionRelative.Invalide;
  		
  	}
  	
	public void CliqueCase(int x, int y, ETypeClique click)
	{
		
		Case c = cases[x][y];
		if(c == null)
			return;
		
		switch(click)
		{
			case Click :
				
				if(c.etat == EEtatCase.Vide)
					return;
				
				if(c.etat == EEtatCase.Depart)
				{
					EffaceCouleur(c.couleur);
				}
				else
				{
					EffaceCaseSuivantes(c);
				}
				
				couleurSelectionnee = c.couleur;
				//System.out.println(couleurSelectionnee);
				caseSelectionnee = c;
				
				break;
				
			case Drag:
				
				if(couleurSelectionnee == ECouleurCase.Vide)
					return;
				
				if(caseSelectionnee == null)
					return;
				
				
				
				if(c.etat == EEtatCase.Vide)
				{
					System.out.println("asdf");
					
					c.etat = EEtatCase.Occupe;
					c.couleur = couleurSelectionnee;
					caseSelectionnee.caseSuivante = c;
					c.casePrecedente = caseSelectionnee;
					caseSelectionnee = c;
				}
					
				break;
		}
		
		repaint();
		System.out.println(this.toString());
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
