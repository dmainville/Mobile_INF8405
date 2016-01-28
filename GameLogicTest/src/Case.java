import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JComponent;

public class Case extends JComponent {

	private static final long serialVersionUID = 1L;
	ECouleurCase couleur;
	EEtatCase etat;
	
	int posX;
	int posY;
	int DIMENSION = 100;
	
	//Initialise la case comme étant vide
	Case(int x, int y){
		etat = EEtatCase.Vide;
		this.posX = x;
		this.posY = y;
	}
	
	Case(int x, int y,ECouleurCase couleur){
		etat = EEtatCase.Depart;
		this.couleur = couleur;
		this.posX = x;
		this.posY = y;
	}
	
	Case(int x, int y, String couleur){
		etat = EEtatCase.Depart;
		this.couleur = GetCouleurFromString(couleur);
		this.posX = x;
		this.posY = y;
	}
	
	//Sélectionne la case si c'est une case de départ
	public Case TrySelectionneCase()
	{
		if(this.etat == EEtatCase.Depart)
			return this;
		
		return null;
	}
	
	//Vérifie si la case est disponible
	public boolean IsCaseDisponible()
	{
		return this.etat == EEtatCase.Vide;
	}
	
	//Colorise et occupe la case si possible
	public boolean TryColoriseCase(ECouleurCase c)
	{
		if(!IsCaseDisponible())
			return false;
		
		this.etat = EEtatCase.Occupe;
		this.couleur = c;
		
		return true;
	}
	
	public ECouleurCase GetCouleurFromString(String couleur)
	{
		couleur = couleur.trim();
		switch (couleur)
		{
			case "b":
				return ECouleurCase.Bleu;
			case "r":
				return ECouleurCase.Rouge;
			case "j":
				return ECouleurCase.Jaune;
			case "o":
				return ECouleurCase.Orange;
			case "v":
				return ECouleurCase.Vert;
			case "l":
				return ECouleurCase.Lime;
			case "m":
				return ECouleurCase.Melon;
			case "p":
				return ECouleurCase.Pourpre;
			case "g":
				return ECouleurCase.Gris;
			default:
				return ECouleurCase.Bleu;
		}
	}
	
	public String GetStringFromCouleur()
	{
		if(this.etat == EEtatCase.Vide)
			return " ";
		
		switch (this.couleur)
		{
			case Bleu:
				return "b";
			case Rouge:
				return "r";
			case Jaune:
				return "j";
			case Orange:
				return "o";
			case Vert:
				return "v";
			case Lime:
				return "l";
			case Melon:
				return "m";
			case Pourpre:
				return "p";
			case Gris:
				return "g";
			default:
				return "b";
		}
	}
	
	public Image GetImageCase()
	{
		String basePath = "src\\Images";
		String imagePath = "caseVide.png";
		
		switch (this.etat) {
		case Depart:
			imagePath = "cercle_"+GetStringFromCouleur()+".png";
			break;
		case Vide:
			imagePath = "caseVide.png";
			break;
		default:
			imagePath = "caseVide.png";
			break;
		}
			
		Image img = Toolkit.getDefaultToolkit().getImage(basePath+"\\"+imagePath);
		return img;
	}
	
}
