package ca.polymtl.inf8405.inf8405tp1;

public interface IObserver {

	public void notifyCase(Case c);
	public void notifyVictoire();
	public void notifyNbConnexion(int nbConnexion);
}
