package com.excilys.cdb.servlet;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.excilys.cdb.dto.ComputerDTO;
import com.excilys.cdb.mapper.MapperComputer;
import com.excilys.cdb.model.Computer;
import com.excilys.cdb.persistence.Page;
import com.excilys.cdb.persistence.exceptions.DaoException;
import com.excilys.cdb.service.CompanyService;
import com.excilys.cdb.service.ComputerService;
import com.excilys.cdb.service.exceptions.ServiceException;
import com.excilys.cdb.servlet.ressources.DefaultValues;
import com.excilys.cdb.servlet.ressources.JspRessources;
import com.excilys.cdb.servlet.ressources.UrlID;
import com.excilys.cdb.servlet.ressources.UrlRessources;

@Controller
@RequestMapping("/computer")
public class ComputerController {

	@Autowired
	private ComputerService serviceComputer;
	@Autowired
	private CompanyService serviceCompany;

	public static final String ADD_COMPUTER = "addComputer";
	public static final String EDIT_COMPUTER = "editComputer";
	public static final String SEARCH_COMPUTER = "searchComputer";
	public static final String DELETE_COMPUTER = "deleteComputer";
	public static final String ADD_FORM_COMPUTER = "addFormComputer";
	public static final String EDIT_FORM_COMPUTER = "editFormComputer";
	public static final String LIST_COMPUTERS = "listComputers";

	/**
	 * Direction liste des compagnies.
	 * @param numeropage le numero de la page à afficher.
	 * @param limit nombres de résultats par bloc
	 * @return nom de la jsp
	 */
	@GetMapping("/" + LIST_COMPUTERS)
	public ModelAndView liste(@RequestParam(UrlID.PAGE) Integer numeropage,
			@RequestParam(UrlID.LIMIT) Integer limit) {
		Page<Computer> page = new Page<Computer>(limit, 0);
		ModelAndView mv = new ModelAndView(UrlRessources.LIST_COMPUTERS);
		try {
			page = serviceComputer.getPage(numeropage, limit);
		} catch (ServiceException e) {
			mv.addObject(JspRessources.ERROR, e.getMessage());
		}
		mv.addObject(UrlID.ACTION_PAGINATION, LIST_COMPUTERS);
		mv.addObject(UrlID.PAGE, page);
		return mv;
	}

	/**
	 * Résultat recherche des compagnies.
	 * @param search la recherche.
	 * @param numeropage le numero de la page à afficher.
	 * @param limit nombres de résultats par bloc
	 * @return nom de la jsp
	 */
	@GetMapping("/" + SEARCH_COMPUTER)
	public ModelAndView search(@RequestParam(UrlID.SEARCH) String search,
			@RequestParam(UrlID.PAGE) Integer numeropage, @RequestParam(UrlID.LIMIT) Integer limit) {
		Page<Computer> page = new Page<Computer>(limit, 0);
		ModelAndView mv = new ModelAndView(UrlRessources.LIST_COMPUTERS);
		try {
			page = serviceComputer.getPageSearch(search, numeropage, limit);
		} catch (ServiceException e) {
			mv.addObject(JspRessources.ERROR, e.getMessage());
		}
		mv.addObject(UrlID.ACTION_PAGINATION, SEARCH_COMPUTER);
		mv.addObject(UrlID.PAGE, page);
		return mv;
	}

	/**
	 * Suppression des computers.
	 * @param deletes l'id des computers à supprimer dans un string.
	 * @return nom de la jsp.
	 */
	@PostMapping("/" + DELETE_COMPUTER)
	public ModelAndView delete(@RequestParam(JspRessources.DELETE_SELECT) String deletes) {
		String[] selections = deletes.split(",");
		Set<Long> set = Arrays.stream(selections).map(l -> Long.valueOf(l)).collect(Collectors.toSet());
		ModelAndView mv;
		try {
			serviceComputer.deleteAll(set);
			mv = liste(DefaultValues.DEFAULT_PAGE, DefaultValues.DEFAULT_LIMIT);
			mv.addObject(JspRessources.SUCCESS, "Delete computer " + Arrays.toString(selections) + " success.");
		} catch (DaoException | ServiceException e) {
			mv = liste(DefaultValues.DEFAULT_PAGE, DefaultValues.DEFAULT_LIMIT);
			mv.addObject(JspRessources.ERROR, e.getMessage());
		}
		return mv;
	}

	/**
	 * Ajouter d'un computer.
	 * @param nameS le nom du computer
	 * @param introducedS la date d'introduction
	 * @param discontinuedS la date de termination
	 * @param idCompanyS l'id de la company du computer
	 * @return jsp de redirection.
	 */
	@PostMapping("/" + ADD_COMPUTER)
	public ModelAndView add(@RequestParam(JspRessources.FORM_COMPUTER_PARAM_NAME) String nameS,
			@RequestParam(JspRessources.FORM_COMPUTER_PARAM_INTRODUCED) String introducedS,
			@RequestParam(JspRessources.FORM_COMPUTER_PARAM_DISCONTINUED) String discontinuedS,
			@RequestParam(JspRessources.FORM_COMPUTER_PARAM_IDCOMPANY) String idCompanyS) {

		Optional<Computer> c = MapperComputer.map(nameS, introducedS, discontinuedS, idCompanyS);
		ModelAndView mv = formAdd();
		if (c.isPresent()) {
			try {
				serviceComputer.create(c.get());
				mv.addObject(JspRessources.SUCCESS, "Create Computer success.");
			} catch (ServiceException | DaoException e) {
				mv.addObject(JspRessources.ERROR, e.getMessage());
			}
		} else {
			mv.addObject(JspRessources.ERROR, "Invalide arguments");
		}
		return mv;
	}

	/**
	 * Redirection vers le form d'addtion d'un computer.
	 * @return jsp de redirection.
	 */
	@GetMapping("/" + ADD_FORM_COMPUTER)
	private ModelAndView formAdd() {
		ModelAndView mv = new ModelAndView(UrlRessources.FORM_ADD_COMPUTER);
		try {
			mv.addObject(JspRessources.ALL_COMPANY, serviceCompany.getAll());
		} catch (ServiceException e) {
			mv.addObject(JspRessources.ERROR, e.getMessage());
		}
		return mv;
	}

	/**
	 * Edition d'un computer.
	 * @param nameS le nom du computer
	 * @param introducedS la date d'introduction
	 * @param discontinuedS la date de termination
	 * @param idCompanyS l'id de la company du computer
	 * @param idComputerS l'id du computer à modifier.
	 * @return jsp de redirection.
	 */
	@PostMapping("/" + EDIT_COMPUTER)
	public ModelAndView edit(@RequestParam(JspRessources.FORM_COMPUTER_PARAM_NAME) String nameS,
			@RequestParam(JspRessources.FORM_COMPUTER_PARAM_INTRODUCED) String introducedS,
			@RequestParam(JspRessources.FORM_COMPUTER_PARAM_DISCONTINUED) String discontinuedS,
			@RequestParam(JspRessources.FORM_COMPUTER_PARAM_IDCOMPANY) String idCompanyS,
			@RequestParam(JspRessources.COMPUTER_ID) String idComputerS) {
		ModelAndView mv = formEdit(idComputerS);
		Optional<Computer> c = MapperComputer.map(idComputerS, nameS, introducedS, discontinuedS, idCompanyS);
		if (c.isPresent()) {
			try {
				serviceComputer.update(c.get());
				mv.addObject(JspRessources.SUCCESS, "Update Computer success.");
			} catch (ServiceException | DaoException e) {
				mv.addObject(JspRessources.ERROR, e.getMessage());
			}
		} else {
			mv.addObject(JspRessources.ERROR, "Invalide arguments");
		}
		return mv;
	}

	/**
	 * Redirection vers le form d'édition d'un computer
	 * @param idS l'id du computer à modifié.
	 * @return jsp de redirection.
	 */
	@GetMapping("/" + EDIT_FORM_COMPUTER)
	public ModelAndView formEdit(@RequestParam(JspRessources.COMPUTER_ID) String idS) {
		long id = Long.valueOf(idS);
		ModelAndView mv = new ModelAndView(UrlRessources.FORM_EDIT_COMPUTER);
		try {
			mv.addObject(JspRessources.COMPUTER, new ComputerDTO(serviceComputer.get(id)));
		} catch (ServiceException | DaoException e) {
			mv.addObject(JspRessources.ERROR, e.getMessage());
		}
		return mv;
	}

}
