package com.excilys.cdb.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.excilys.cdb.model.Computer;

public class DateValidation {

    /**
     * Classe non instanciable.
     */
    private DateValidation() { }

    /**
     * Traitement de l'input de la date par l'utilisateur.
     * @param ligne le String tappé par l'utilisateur.
     * @return Une LocalDateTime spécifié par l'utilisateur ou null si une
     *         erreur.
     */
    public static LocalDate validDateFormat(final String ligne) {
        LocalDate date = null;
        if (ligne != null) {
            String regex = Computer.PATTERN_DATE_REGEX; // (yyyy-MM-dd)
            try {
                if (ligne.matches(regex)) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Computer.PATTERN_DATE);
                    date = LocalDate.parse(ligne, formatter);
                }
            } catch (java.time.format.DateTimeParseException e) {
                return null;
            }
        }
        return date;
    }

    /**
     * Return if a date is strictly between two others.
     * @param ldt la date à valider
     * @param first la première date not included
     * @param last la seconde date not included
     * @return un boolean définissant la validité de la date
     */
    public static boolean validDateInBetween(LocalDate ldt, LocalDate first, LocalDate last) {
        return ldt.isAfter(first) && ldt.isBefore(last);
    }

}
