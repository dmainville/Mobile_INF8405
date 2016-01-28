import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JComponent;


public class Grille extends JComponent {
	private static final long serialVersionUID = 1L;
	
	int dimensionX;
	int dimensionY;
	Case[][] cases;
	
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
	
}
