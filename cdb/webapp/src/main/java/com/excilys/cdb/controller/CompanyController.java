package com.excilys.cdb.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.excilys.cdb.model.Company;
import com.excilys.cdb.ressources.DefaultValues;
import com.excilys.cdb.ressources.JspRessources;
import com.excilys.cdb.ressources.UrlID;
import com.excilys.cdb.ressources.UrlRessources;
import com.excilys.cdb.service.ICompanyService;
import com.excilys.cdb.service.exceptions.ServiceException;

@Controller
@RequestMapping("/company")
public class CompanyController {

	private ICompanyService serviceCompany;

	public CompanyController(ICompanyService companyService) {
		this.serviceCompany = companyService;
	}
	
	public static final String SEARCH_COMPANY = "searchCompany";
	public static final String LIST_COMPANIES = "listCompanies";

	/**
	 * Direction liste des compagnies.
	 * @param numeropage le numero de la page à afficher.
	 * @param limit nombres de résultats par bloc
	 * @return nom de la jsp
	 */
	@GetMapping("/" + LIST_COMPANIES)
	public ModelAndView liste(@RequestParam(UrlID.PAGE) Integer numeropage, @RequestParam(UrlID.LIMIT) Integer limit) {
		Page<Company> page=null;
		ModelAndView mv = new ModelAndView(UrlRessources.LIST_COMPANIES);
		try {
			page = serviceCompany.getPage(numeropage, limit);
		} catch (ServiceException e) {
			mv.addObject(JspRessources.ERROR, e.getMessage());
		}
		mv.addObject(UrlID.ACTION_PAGINATION, LIST_COMPANIES);
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
	@GetMapping("/" + SEARCH_COMPANY)
	public ModelAndView search(@RequestParam(UrlID.SEARCH) String search,
			@RequestParam(value = UrlID.PAGE, required = false) Integer iNumpage,
			@RequestParam(value = UrlID.LIMIT, required = false) Integer paramLimit) {
		int numpage = (iNumpage == null) ? 1 : iNumpage;
		int limit = (paramLimit == null) ? DefaultValues.DEFAULT_LIMIT : paramLimit;
		Page<Company> page=null;
		ModelAndView mv = new ModelAndView(UrlRessources.LIST_COMPANIES);
		try {
			page = serviceCompany.getPageSearch(search, numpage, limit);
		} catch (ServiceException e) {
			mv.addObject(JspRessources.ERROR, e.getMessage());
		}
		mv.addObject(UrlID.SEARCH, search);
		mv.addObject(UrlID.ACTION_PAGINATION, SEARCH_COMPANY);
		mv.addObject(UrlID.PAGE, page);
		return mv;
	}

}
