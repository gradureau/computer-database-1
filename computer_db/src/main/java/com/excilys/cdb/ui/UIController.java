package main.java.com.excilys.cdb.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import main.java.com.excilys.cdb.model.Company;
import main.java.com.excilys.cdb.model.Computer;
import main.java.com.excilys.cdb.service.ComputerServiceImpl;
import main.java.com.excilys.cdb.service.IComputerService;
import main.java.com.excilys.cdb.vue.Pageur;
import main.java.com.excilys.cdb.vue.UITextes;
import main.java.com.excilys.cdb.vue.UIView;

/**
 * Controler de l'interface cli.
 * @author vogel
 *
 */
public class UIController {

	//Le service de gestion des computers et des compagnies.
	private IComputerService service = new ComputerServiceImpl();
	
	//Les énumérations permettant la gestion comme un automate du processus.
	private enum State { INITIAL, LIST_COMPANY, LIST_COMPUTER, 
		FORM_UPDATE, FORM_AJOUT, DELETE, SHOW, RETOUR};
	private enum Update { NONE, ID, NAME, INTRODUCED, DISCONTINUED, COMPANY_ID, VALIDATE};
	private enum Ajout { NONE, NAME, INTRODUCED, DISCONTINUED, COMPANY_ID, VALIDATE };
	
	//Le scanner de lecture
	private Scanner scanner = new Scanner(System.in);
	
	//Les états actuels.
	private State state;
	private Update stateUpdate;
	private Ajout stateAjout;
	
	//Les pageurs pour gérer la pagination
	private Pageur<Computer> pageurComputer;
	private Pageur<Company> pageurCompany;
	private static final int LIMIT = 20;
	
	//Les pages sur lesquels on est actuellement
	private int numPageComputer = 1;
	private int numPageCompany = 1;
	
	//La vue
	private UIView view;
	
	//Un objet de type computer pour avoir en mémoire les dernières demandes.
	private Computer inter;
	
	/**
	 * Constructor sans arguments initiant les variables d'états, la vue et les pageurs.
	 */
	public UIController() {
		state = State.INITIAL;
		stateUpdate = Update.NONE;
		stateAjout = Ajout.NONE;
		view = new UIView(UITextes.MENU_INITIAL);
		pageurComputer = new Pageur<Computer>(LIMIT);
		pageurCompany = new Pageur<Company>(LIMIT);
	}
	
	/**
	 * Le pas de lecture de l'automate.
	 */
	public void read() {
		view.display();
		String ligne = scanner.nextLine();
		interprate(ligne);
	}
	
	/**
	 * L'interprétation des résultats.
	 * @param ligne
	 */
	private void interprate(String ligne) {
		
		//Un retour a été demandé.
		if(ligne.trim().equals("r")) {
			
			//Si on est pas sur un retour pour l'ajout ou l'update, rafficher le menu principal.
			if(state==State.RETOUR || state==State.LIST_COMPANY || state==State.LIST_COMPUTER  
					|| state==State.DELETE || state==State.SHOW)
				view.setAffichage(UITextes.MENU_INITIAL);
			
			//Effetuer le retour demandé.
			actionRetour();
		}else {
			
			//Variable intermédiaire.
			long value;
			LocalDateTime timeInter;
			
			//Selon l'état de l'automate.
			switch(state) {
			
				//Si on est sur le menu initial.
				case INITIAL:
					
					//Gestion des choix sur le menu initial.
					int choix = Integer.valueOf(ligne);
					switch(choix) {
						case 1: 
							pageurComputer.postDatas(service.getAllComputer(), 1);
							view.setAffichage(pageurComputer.display(numPageComputer)
									+"\n Page:" + numPageComputer + "/" 
									+ pageurComputer.getSize() + "\n"+UITextes.LIST_PAGINATION);
							
							state = State.LIST_COMPUTER; break;
						case 2: 
							pageurCompany.postDatas(service.getAllCompany(), 1);
							view.setAffichage(pageurCompany.display(numPageCompany)
									+"\n Page:" + numPageCompany + "/" 
									+ pageurCompany.getSize() + "\n"+UITextes.LIST_PAGINATION);
							state = State.LIST_COMPANY; break;
						case 3: 
							view.setAffichage("Choissisez l'id.");
							state = State.SHOW; break;
						case 4: 
							state = State.FORM_AJOUT; 
							view.setAffichage(UITextes.AJOUT_NAME);
							break;
						case 5: 
							state = State.FORM_UPDATE; 
							view.setAffichage(UITextes.UPDATE_ID);
							break;
						case 6: 
							view.setAffichage("Choissisez l'id:");
							state = State.DELETE;
							break;
						default:break;
					}
					break;
					
				//Gestion des choix si on est dans le formulaire d'ajout.
				case FORM_AJOUT:
					switch(stateAjout) {
						case NAME: 
							inter = new Computer();
							inter.setName(ligne); 
							view.setAffichage(UITextes.AJOUT_INTRODUCED);
							break;
						case INTRODUCED: 
							if(!ligne.equals("no")) {
								timeInter = traitementDate(ligne);
								if(timeInter == null) {
									view.setAffichage("Error"+UITextes.AJOUT_INTRODUCED);
									stateAjout = Ajout.NAME;
									break;
								}
								inter.setIntroduced(timeInter);
							}
							view.setAffichage(UITextes.AJOUT_DISCONTINUED);
							break;
						case DISCONTINUED: 
							if(!ligne.equals("no")) {
								timeInter = traitementDate(ligne);
								if(timeInter == null) {
									view.setAffichage("Error"+UITextes.AJOUT_DISCONTINUED);
									stateAjout = Ajout.INTRODUCED;
									break;
								}
								inter.setDiscontinued(timeInter); 
							}
							view.setAffichage(UITextes.AJOUT_COMPANY_ID);
							break;
						case COMPANY_ID:
							if(!ligne.equals("no")) {
								Company c = service.getCompany(Integer.valueOf(ligne));
								inter.setCompany(c); 
							}
							view.setAffichage(UITextes.VALIDATION);
							break;
						case VALIDATE: 
							if(!ligne.equals("yes"))
								view.setAffichage(UITextes.MENU_INITIAL);
							else
								view.setAffichage(ajouterComputer()+"\n"+UITextes.MENU_INITIAL);
							break;
						default:break;
					}break;
					
				//Gestion des choix si on est dans le formulaire d'update.
				case FORM_UPDATE: 
					switch(stateUpdate) {
						case ID:
							value = Long.valueOf(ligne);
							if(service.getComputer(value)!=null) {
								inter = new Computer();
								inter.setId(value);
								view.setAffichage(UITextes.UPDATE_NAME);
							}else {
								//Quitte si erreur;
								stateUpdate = Update.VALIDATE;
							}
							break;
						case NAME: 
							if(!ligne.equals("no"))
								inter.setName(ligne); 
							view.setAffichage(UITextes.UPDATE_INTRODUCED);break;
						case INTRODUCED: 
							if(!ligne.equals("no")) {
								timeInter = traitementDate(ligne);
								if(timeInter == null) {
									view.setAffichage("Error"+UITextes.UPDATE_INTRODUCED);
									stateUpdate = Update.NAME;
									break;
								}
								inter.setIntroduced(traitementDate(ligne));
							}
							view.setAffichage(UITextes.UPDATE_DISCONTINUED);
							break;
						case DISCONTINUED: 
							if(!ligne.equals("no")) {
								timeInter = traitementDate(ligne);
								if(timeInter != null) {
									view.setAffichage("Error"+UITextes.UPDATE_DISCONTINUED);
									stateUpdate = Update.INTRODUCED;
									break;
								}
								inter.setDiscontinued(traitementDate(ligne));
							}
							view.setAffichage(UITextes.UPDATE_COMPANY_ID);break;
						case COMPANY_ID: 
							if(!ligne.equals("no")) {
								Company c = service.getCompany(Integer.valueOf(ligne));
								inter.setCompany(c); 
							}
							view.setAffichage(UITextes.VALIDATION);
							break;
						case VALIDATE: 
							if(!ligne.equals("yes"))
								view.setAffichage(UITextes.MENU_INITIAL);
							else
								view.setAffichage(updateComputer()+"\n"+UITextes.MENU_INITIAL);
							break;
						default:break;
					}break;
					
				//Gestion des choix si on est dans l'affichage des compagnies.
				case LIST_COMPANY:
					value = Long.valueOf(ligne);
					numPageCompany = (int)value;
					view.setAffichage(pageurCompany.display(numPageCompany)
							+"\n Page:" + numPageCompany + "/" 
							+ pageurCompany.getSize() + "\n"+UITextes.LIST_PAGINATION);
					break;
					
				//Gestion des choix si on est dans l'affichage des computers.
				case LIST_COMPUTER:
					value = Long.valueOf(ligne);
					numPageComputer = (int)value;
					view.setAffichage(pageurComputer.display(numPageComputer)
							+"\n Page:" + numPageComputer + "/" 
							+ pageurComputer.getSize() + "\n"+UITextes.LIST_PAGINATION);
					break;
					
				//Gestion des choix si on est dans l'affichage des détails d'un computer 
				case SHOW:
					value = Long.valueOf(ligne);
					view.setAffichage(detailComputer(value));
					break;
					
				//Gestion des choix si onest dans l'affichage des détails d'une compagnie.
				case DELETE:
					value = Long.valueOf(ligne);
					view.setAffichage(supprimerComputer(value));
					break;
				default: break;
			}
			actionAvance();
		}
		if(state != State.INITIAL) {
			view.setAffichage(view.getAffichage()+"["+UITextes.RETOUR+"]");
		}
	}
	
	/**
	 * Gestion du retour en arrière de l'automate.
	 */
	private void actionRetour() {
		switch(state) {
			case INITIAL:break;
			case FORM_AJOUT:
				switch(stateAjout) {
					case NONE: break;
					case NAME: 
						stateAjout = Ajout.NONE; 
						state = State.INITIAL; 
						break;
					case INTRODUCED: stateAjout = Ajout.NAME; break;
					case DISCONTINUED: stateAjout = Ajout.INTRODUCED; break;
					case COMPANY_ID: stateAjout = Ajout.DISCONTINUED; break;
					default: break;
				}break;
			case FORM_UPDATE: 
				switch(stateUpdate) {
					case NONE:
						break;
					case ID: 
						stateUpdate = Update.NONE; 
						state = State.INITIAL;
						break;
					case NAME: stateUpdate = Update.ID; break;
					case INTRODUCED: stateUpdate = Update.NAME; break;
					case DISCONTINUED: stateUpdate = Update.INTRODUCED; break;
					case COMPANY_ID: stateUpdate = Update.DISCONTINUED; break;
					default: break;
				}break;
				
			//Par défaut on revient au menu initial.
			default: state = State.INITIAL; break;
		}
	}
	
	/**
	 * Gestion du pas en avant de l'automate.
	 */
	private void actionAvance() {
		switch(state) {
			case INITIAL:break;
			case FORM_AJOUT:
				switch(stateAjout) {
					case NONE: stateAjout = Ajout.NAME; break;
					case NAME: stateAjout = Ajout.INTRODUCED; break;
					case INTRODUCED: stateAjout = Ajout.DISCONTINUED; break;
					case DISCONTINUED: stateAjout = Ajout.COMPANY_ID; break;
					case COMPANY_ID: stateAjout = Ajout.VALIDATE; break;
					case VALIDATE:
						state = State.INITIAL;
						stateAjout = Ajout.NONE;
						break;
					default: break;
				}break;
			case FORM_UPDATE: 
				switch(stateUpdate) {
					case NONE: stateUpdate = Update.ID; break;
					case ID: stateUpdate = Update.NAME; break;
					case NAME: stateUpdate = Update.INTRODUCED; break;
					case INTRODUCED: stateUpdate = Update.DISCONTINUED; break;
					case DISCONTINUED: stateUpdate = Update.COMPANY_ID; break;
					case COMPANY_ID: stateUpdate = Update.VALIDATE; break;
					case VALIDATE:
						state = State.INITIAL;
						stateUpdate = Update.NONE; 
						break;
					default: break;
				}break;
			//Par défaut on ne bouge pas.
			default: break;
		}
	}
	
	/**
	 * Ajouter un computer dans la bdd.
	 * @return l'affichage du résultat dans un string.
	 */
	private String ajouterComputer() {
		long id = -1;
		if(inter.getCompany()!=null)
			id= inter.getCompany().getId();
		boolean ajout = 
				service.createComputer(inter.getName(), inter.getIntroduced(), 
						inter.getDiscontinued(), id);
		
		if(ajout)
			return "Ajout réussit\n";
		
		return "Ajout fail\n";
	}
	
	/**
	 * Supprimer un computer dans la bdd
	 * @param id l'id du computer a supprimé
	 * @return l'affichage du résultat dans un string.
	 */
	private String supprimerComputer(long id) {
		boolean delete = 
				service.deleteComputer(id);
		
		if(delete)
			return "Suppression réussi\n";
		
		return "Suppression fail\n";		
	}
	
	/**
	 * Modifié un computer dans la bdd.
	 * @return l'affichage du résultat dans un string.
	 */
	private String updateComputer() {
		long id = -1;
		if(inter.getCompany()!=null)
			id = inter.getCompany().getId();
		
		boolean update = 
				service.updateComputer(inter.getId(), inter.getName(), 
						inter.getIntroduced(), inter.getDiscontinued(), 
						id);
		if(update)
			return "Update réussit\n";
		
		return "Update fail\n";
	}
	
	/**
	 * Afficher les détails d'un computer
	 * @param id l'id du computer a affiché
	 * @return les résultats dans un String.
	 */
	private String detailComputer(long id) {
		Computer c = service.getComputer(id);
		if(c==null)return "Erreur d'id";
		
		String affichage="Computer: " + id + "\n";
		affichage += c.getName() + " \n ";
		if(c.getIntroduced()!=null)
			affichage += c.getIntroduced() + " \n ";
		if(c.getDiscontinued()!=null)
			affichage += c.getDiscontinued() + " \n ";
		if(c.getCompany()!=null)
			affichage += c.getCompany().getName() + "\n ";
		return affichage;
	}
	
	/**
	 * Traitement de l'input de la date par l'utilisateur.
	 * @param ligne le String tappé par l'utilisateur.
	 * @return Une LocalDateTime spécifié par l'utilisateur ou null si une erreur.
	 */
	private LocalDateTime traitementDate(String ligne) {
		String regex = "^\\d{4}-\\d{2}-\\d{2}$"; //(yyyy-mm-dd)
		if(!ligne.matches(regex)) {
			return null;
		}
		
		LocalDateTime dateTime;
		try {
			ligne += " 00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			dateTime = LocalDateTime.parse(ligne, formatter);
		}catch(java.time.format.DateTimeParseException e) {
			return null;
		}
		return dateTime;
	}
	
}