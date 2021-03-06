package com.excilys.cdb.selenium;

import static org.testng.Assert.assertEquals;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.excilys.cdb.controllermessage.ControllerMessage;
import com.excilys.cdb.messagehandler.MessageHandler;
import com.excilys.cdb.model.Company;
import com.excilys.cdb.model.Computer;
import com.excilys.cdb.ressources.Action;
import com.excilys.cdb.ressources.JspRessources;
import com.excilys.cdb.service.exceptions.ServiceException;

public class AjouterComputerIT extends SeleniumSuite {

	private static Computer ajout;
	private static final Logger LOGGER = LoggerFactory.getLogger(AjouterComputerIT.class);
	private static final String NAMEINVALID = "";
	private static final String NAMEVALID = "nameValid";

	/**
	 * Beforeclass.
	 * @throws DaoConfigurationException configuration exception
	 * @throws ServiceException service exception
	 */
	@BeforeClass
	public static void beforeClass() throws ServiceException {
		ajout = new Computer(NAMEVALID, Computer.BEGIN_DATE_VALID.plus(Period.ofDays(1)),
				Computer.END_DATE_VALID.minus(Period.ofDays(1)), new Company(2, "Thinking Machines"));
	}

	/**
	 */
	@AfterClass
	public void afterClass() {
		LOGGER.info("AfterClass in IT ajouter computer");
		this.closeInstance();
	}
	
	@BeforeTest
	public void verif() {
		if(driver == null) {
			LOGGER.info("Le driver est null");
		}
	}

	/**
	 */

	/**
	 * Method used in ajoutComputer.
	 * @param name name à insérer
	 */
	private void ajoutTextComputerName(String name) {
		driver.findElement(By.id(JspRessources.FORM_COMPUTER_PARAM_NAME)).sendKeys(name);
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.SECONDS);
		assertEquals(name, driver.findElement(By.id(JspRessources.FORM_COMPUTER_PARAM_NAME)).getAttribute("value"));
	}

	/**
	 * Method used in ajoutComputer.
	 * @param introduced la date à insérer
	 */
	private void ajoutTextIntroduced(LocalDate introduced) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Computer.PATTERN_DATE);
		String date = introduced.format(formatter);
		je.executeScript("return document.getElementById('introduced').value = '" + date + "';");
		Assert.assertEquals((String) je.executeScript("return document.getElementById('introduced').value;"), date);
	}

	/**
	 * Method used in ajoutComputer.
	 * @param discontinued date discontinued à insérer.
	 */
	public void ajoutTextDiscontinued(LocalDate discontinued) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Computer.PATTERN_DATE);
		String date = discontinued.format(formatter);
		je.executeScript("return document.getElementById('discontinued').value = '" + date + "';");
		Assert.assertEquals((String) je.executeScript("return document.getElementById('discontinued').value;"), date);
	}

	/**
	 * Method used in ajoutComputer.
	 * @param company la company lié à choisir.
	 */
	private void ajoutTextCompanyIdName(Company company) {
		if (company != null) {
			WebElement select = driver.findElement(By.id(JspRessources.FORM_COMPUTER_PARAM_IDCOMPANY));
			List<WebElement> options = select.findElements(By.tagName("option"));
			for (WebElement option : options) {
				if (option.getText().equals(company.getName())) {
					option.click();
					break;
				}
			}
			driver.manage().timeouts().implicitlyWait(1000, TimeUnit.SECONDS);
			Assert.assertEquals(new Long(company.getId()), Long.valueOf(
					driver.findElement(By.id(JspRessources.FORM_COMPUTER_PARAM_IDCOMPANY)).getAttribute("value")));

		} else {
			driver.manage().timeouts().implicitlyWait(1000, TimeUnit.SECONDS);
			Assert.assertEquals(new Long(-1), Long.valueOf(
					driver.findElement(By.id(JspRessources.FORM_COMPUTER_PARAM_IDCOMPANY)).getAttribute("value")));
		}
	}

	/**
	 * Verify click du bouton + ajout fonctionnel.
	 * @throws ServiceException erreur de service.
	 * @throws DaoException erreur de reqûete.
	 */
	@Test
	public void verifyAjouterButtonAjoutOk() throws ServiceException {
		LOGGER.info("ajoutButtonAjoutOk");
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.SECONDS);
		driver.get("http://localhost:" + PORT + "/webapp/computer/" + Action.ADD_FORM_COMPUTER.getValue());
		this.ajoutTextComputerName(ajout.getName());
		this.ajoutTextIntroduced(ajout.getIntroduced());
		this.ajoutTextDiscontinued(ajout.getDiscontinued());
		this.ajoutTextCompanyIdName(ajout.getCompany());
		driver.findElement(By.id(JspRessources.BUTTON_ADD)).click();
		String result = "";
		LOGGER.info("Button add ok Modification work: test result");
		try {
			Thread.sleep(300L);
			result = driver.findElement(By.id(JspRessources.SUCCESS)).getText();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOGGER.info("Result: " + result);
		Assert.assertEquals(result, MessageHandler.getMessage(ControllerMessage.SUCCESS_CREATE, null));
	}

	/**
	 * Verify click du bouton + ajout fonctionnel.
	 * @throws ServiceException erreur de service.
	 * @throws DaoException erreur de reqûete.
	 */
	@Test
	public void verifyAjouterButtonAjoutFailForName() throws ServiceException {
		LOGGER.info("verifyAjouterButtonAjoutFailForName");
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.SECONDS);
		driver.get("http://localhost:" + PORT + "/webapp/computer/" + Action.ADD_FORM_COMPUTER.getValue());
		this.ajoutTextComputerName(NAMEINVALID);
		this.ajoutTextIntroduced(ajout.getIntroduced());
		this.ajoutTextDiscontinued(ajout.getDiscontinued());
		this.ajoutTextCompanyIdName(ajout.getCompany());
		driver.findElement(By.id(JspRessources.BUTTON_ADD)).click();
		String result = "";
		try {
			Thread.sleep(300L);
			result = driver.findElement(By.id(JspRessources.ERROR)).getText();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOGGER.info("Result: " + result);
		LOGGER.info(MessageHandler.getMessage(ControllerMessage.VALIDATION_NAME_LENGTH,
				Arrays.asList(Computer.TAILLE_MIN_NAME, Computer.TAILLE_MAX_NAME).toArray()));
		Assert.assertEquals(result, MessageHandler.getMessage(ControllerMessage.VALIDATION_NAME_LENGTH,
				Arrays.asList(Computer.TAILLE_MIN_NAME, Computer.TAILLE_MAX_NAME).toArray()));
	}

	/**
	 * Verify click du bouton + ajout fonctionnel.
	 * @throws ServiceException erreur de service.
	 * @throws DaoException erreur de reqûete.
	 */
	@Test
	public void verifyAjouterButtonAjoutFailForIntroduced() throws ServiceException {
		LOGGER.info("verifyAjouterButtonAjoutFailForIntroduced");
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.SECONDS);
		driver.get("http://localhost:" + PORT + "/webapp/computer/" + Action.ADD_FORM_COMPUTER.getValue());
		this.ajoutTextComputerName(ajout.getName());
		this.ajoutTextIntroduced(ajout.getIntroduced().minus(Period.ofYears(100)));
		this.ajoutTextDiscontinued(ajout.getDiscontinued());
		this.ajoutTextCompanyIdName(ajout.getCompany());
		driver.findElement(By.id(JspRessources.BUTTON_ADD)).click();
		String result = "";
		LOGGER.info("Modification work: test result");
		try {
			Thread.sleep(300L);
			LOGGER.info("Avant driver findElement");
			result = driver.findElement(By.id(JspRessources.ERROR)).getText();
			LOGGER.info("Après driver findElement");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOGGER.info("Result: " + result);
		LOGGER.info("Result: " + MessageHandler.getMessage(ControllerMessage.VALIDATION_DATE_INTRODUCED,
				Arrays.asList(Computer.BEGIN_DATE_VALID, Computer.END_DATE_VALID).toArray()));
		LOGGER.info(MessageHandler.getMessage(ControllerMessage.VALIDATION_DATE_INTRODUCED,
				Arrays.asList(Computer.BEGIN_DATE_VALID, Computer.END_DATE_VALID).toArray()));
		Assert.assertEquals(result, MessageHandler.getMessage(ControllerMessage.VALIDATION_DATE_INTRODUCED,
				Arrays.asList(Computer.BEGIN_DATE_VALID, Computer.END_DATE_VALID).toArray()));
	}

	/**
	 * Verify click du bouton + ajout fonctionnel.
	 * @throws ServiceException erreur de service.
	 * @throws DaoException erreur de reqûete.
	 */
	@Test
	public void verifyAjouterButtonAjoutFailForDiscontinued() throws ServiceException {
		LOGGER.info("verifyAjouterButtonAjoutFailForDiscontinued");
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.SECONDS);
		driver.get("http://localhost:" + PORT + "/webapp/computer/" + Action.ADD_FORM_COMPUTER.getValue());
		this.ajoutTextComputerName(ajout.getName());
		this.ajoutTextIntroduced(ajout.getIntroduced());
		this.ajoutTextDiscontinued(ajout.getDiscontinued().minus(Period.ofYears(100)));
		this.ajoutTextCompanyIdName(ajout.getCompany());
		driver.findElement(By.id(JspRessources.BUTTON_ADD)).click();
		String result = "";
		LOGGER.info("Modification work: test result");
		try {
			Thread.sleep(300L);
			LOGGER.info("Avant driver findElement");
			result = driver.findElement(By.id(JspRessources.ERROR)).getText();
			LOGGER.info("Après driver findElement");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOGGER.info("Result: " + result);
		Assert.assertEquals(result, MessageHandler.getMessage(ControllerMessage.VALIDATION_DATE_DISCONTINUED,
				Arrays.asList(Computer.BEGIN_DATE_VALID, Computer.END_DATE_VALID).toArray()));
	}

	/**
	 * Verify click du bouton + ajout fonctionnel.
	 * @throws ServiceException erreur de service.
	 * @throws DaoException erreur de reqûete.
	 */
	@Test
	public void verifyAjouterButtonAjoutFailForDiscontinuedBeforeIntroduced() throws ServiceException {
		LOGGER.info("verifyAjouterButtonAjoutFailForDiscontinuedBeforeIntroduced");
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.SECONDS);
		driver.get("http://localhost:" + PORT + "/webapp/computer/" + Action.ADD_FORM_COMPUTER.getValue());
		this.ajoutTextComputerName(ajout.getName());
		this.ajoutTextIntroduced(ajout.getDiscontinued());
		this.ajoutTextDiscontinued(ajout.getIntroduced());
		this.ajoutTextCompanyIdName(ajout.getCompany());
		driver.findElement(By.id(JspRessources.BUTTON_ADD)).click();
		String result = "";
		LOGGER.info("Modification work: test result");
		try {
			Thread.sleep(300L);
			result = driver.findElement(By.id(JspRessources.ERROR)).getText();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOGGER.info("Result: " + result);
		Assert.assertEquals(result, MessageHandler.getMessage(ControllerMessage.COMPUTER_INTRODUCED_AFTER, null));
	}

}