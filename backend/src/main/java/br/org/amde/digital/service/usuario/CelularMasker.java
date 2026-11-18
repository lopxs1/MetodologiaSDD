package br.org.amde.digital.service.usuario;

import org.springframework.stereotype.Component;

/**
 * Formata e mascara numeros de celular/telefone (10 ou 11 digitos) conforme RN28 e Constituicao §4.4.
 */
@Component
public class CelularMasker {

    public String mascarar(String numCelular) {
        if (numCelular == null || numCelular.isBlank()) {
            return null;
        }
        String digitos = numCelular.trim();
        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") *****-" + digitos.substring(7);
        }
        if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") ****-" + digitos.substring(6);
        }
        return "****";
    }

    public String formatarCompleto(String numCelular) {
        if (numCelular == null || numCelular.isBlank()) {
            return null;
        }
        String digitos = numCelular.trim();
        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 7) + "-" + digitos.substring(7);
        }
        if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
        }
        return digitos;
    }
}
