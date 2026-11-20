package br.org.amde.digital.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class ValidacaoCampoException extends RuntimeException {

    private final List<CampoErro> erros;

    public ValidacaoCampoException(String campo, String mensagem) {
        super(mensagem);
        this.erros = List.of(new CampoErro(campo, mensagem));
    }

    public ValidacaoCampoException(List<CampoErro> erros) {
        super("Erro de validação nos dados informados");
        this.erros = Collections.unmodifiableList(erros);
    }

    public record CampoErro(String campo, String mensagem) {
    }
}
