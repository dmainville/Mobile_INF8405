import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JComponent;


public class Case extends JComponent {

	private static final long serialVersionUID = 1L;
	ECouleurCase couleur;
	EEtatCase etat;
	
	int posX;
	int posY;
	int DIMENSION = 50;
	
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
			default:
				return ECouleurCase.Bleu;
		}
	}
	
}
