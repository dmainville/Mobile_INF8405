package ca.polymtl.inf8405.inf8405tp1;


public class Case {

	ECouleurCase couleur;
	EEtatCase etat;
	Case caseSuivante = null;
	Case casePrecedente = null;
	EPositionRelative posRelativeSuivant = EPositionRelative.Invalide;
	EPositionRelative posRelativePrecedant = EPositionRelative.Invalide;
	
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
	
	//V�rifie si la case est disponible
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
	
	public String GetImageCase()
	{
		String basePath = "src\\Images";
		String imagePath = "caseVide.png";
		
		switch (this.etat) {
		case Occupe:
			
			imagePath = "cercle_"+GetStringFromCouleur()+".png";
			
			if		//Horizontale
			(		(posRelativePrecedant == EPositionRelative.Gauche && 
						posRelativeSuivant == EPositionRelative.Droite) ||
						(posRelativePrecedant == EPositionRelative.Droite && 
								posRelativeSuivant == EPositionRelative.Gauche) ||
						(posRelativePrecedant == EPositionRelative.Gauche && 
						posRelativeSuivant == EPositionRelative.Invalide) ||
						(posRelativePrecedant == EPositionRelative.Droite && 
						posRelativeSuivant == EPositionRelative.Invalide)
			)
			{
				imagePath = "ligne_horizontale_"+GetStringFromCouleur()+".png";
			}
			else if		//Verticale
			(		(posRelativePrecedant == EPositionRelative.Haut && 
					posRelativeSuivant == EPositionRelative.Bas) ||
					(posRelativePrecedant == EPositionRelative.Bas && 
							posRelativeSuivant == EPositionRelative.Haut) ||
					(posRelativePrecedant == EPositionRelative.Haut && 
					posRelativeSuivant == EPositionRelative.Invalide) ||
					(posRelativePrecedant == EPositionRelative.Bas && 
					posRelativeSuivant == EPositionRelative.Invalide)
					
			)
			{
				imagePath = "ligne_verticale_"+GetStringFromCouleur()+".png";
			}
			else if		//Coin1
			(
				(posRelativePrecedant == EPositionRelative.Bas && 
				posRelativeSuivant == EPositionRelative.Gauche) ||
				(posRelativePrecedant == EPositionRelative.Gauche && 
				posRelativeSuivant == EPositionRelative.Bas)
			)
			{
				imagePath = "coin1_"+GetStringFromCouleur()+".png";
			}
			else if		//Coin2
			(
				(posRelativePrecedant == EPositionRelative.Bas && 
				posRelativeSuivant == EPositionRelative.Droite) ||
				(posRelativePrecedant == EPositionRelative.Droite && 
				posRelativeSuivant == EPositionRelative.Bas)
			)
			{
				imagePath = "coin2_"+GetStringFromCouleur()+".png";
			}
			else if		//Coin3
			(
				(posRelativePrecedant == EPositionRelative.Haut && 
				posRelativeSuivant == EPositionRelative.Droite) ||
				(posRelativePrecedant == EPositionRelative.Droite && 
				posRelativeSuivant == EPositionRelative.Haut)
			)
			{
				imagePath = "coin3_"+GetStringFromCouleur()+".png";
			}
			else if		//Coin4
			(
				(posRelativePrecedant == EPositionRelative.Haut && 
				posRelativeSuivant == EPositionRelative.Gauche) ||
				(posRelativePrecedant == EPositionRelative.Gauche && 
				posRelativeSuivant == EPositionRelative.Haut)
			)
			{
				imagePath = "coin4_"+GetStringFromCouleur()+".png";
			}
			break;
			
		case Depart:
			
			if(posRelativeSuivant == EPositionRelative.Invalide)
				imagePath = "cercle_"+GetStringFromCouleur()+".png";
			else if(posRelativeSuivant == EPositionRelative.Haut)
				imagePath = "cercle_bas_"+GetStringFromCouleur()+".png";
			else if(posRelativeSuivant == EPositionRelative.Bas)
				imagePath = "cercle_haut_"+GetStringFromCouleur()+".png";
			else if(posRelativeSuivant == EPositionRelative.Droite)
				imagePath = "cercle_gauche_"+GetStringFromCouleur()+".png";
			else if(posRelativeSuivant == EPositionRelative.Gauche)
				imagePath = "cercle_droite_"+GetStringFromCouleur()+".png";

			break;
		case Vide:
			imagePath = "caseVide.png";
			break;
		default:
			imagePath = "caseVide.png";
			break;
		}
			
		String img = basePath+"\\"+imagePath;
		return img;
	}
	
}
