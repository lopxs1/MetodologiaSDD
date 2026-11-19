package br.org.amde.digital.exception;

import br.org.amde.digital.dto.erro.CampoErroDTO;
import br.org.amde.digital.dto.erro.ErroResponse;
import br.org.amde.digital.dto.erro.ErroValidacaoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ValidacaoCampoException.class)
    public ResponseEntity<ErroValidacaoResponse> handleValidacaoCampo(ValidacaoCampoException ex) {
        List<CampoErroDTO> erros = ex.getErros().stream()
                .map(e -> new CampoErroDTO(e.campo(), e.mensagem()))
                .toList();
        return ResponseEntity.badRequest().body(
                new ErroValidacaoResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), erros, OffsetDateTime.now())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacaoResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<CampoErroDTO> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new CampoErroDTO(err.getField(), err.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(
                new ErroValidacaoResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Erro de validação nos dados informados",
                        erros,
                        OffsetDateTime.now()
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroValidacaoResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        List<CampoErroDTO> erros = List.of(new CampoErroDTO(ex.getName(), "Valor inválido para o parâmetro"));
        return ResponseEntity.badRequest().body(
                new ErroValidacaoResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Parâmetro de requisição inválido",
                        erros,
                        OffsetDateTime.now()
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroValidacaoResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                new ErroValidacaoResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Corpo da requisição inválido ou malformado",
                        List.of(),
                        OffsetDateTime.now()
                )
        );
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErroResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErroResponse(
                        HttpStatus.FORBIDDEN.value(),
                        "Acesso negado para o perfil autenticado",
                        OffsetDateTime.now()
                ));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> handleConflito(ConflitoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(ErroAuditoriaPreviaException.class)
    public ResponseEntity<ErroResponse> handleErroAuditoriaPrevia(ErroAuditoriaPreviaException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(),
                        OffsetDateTime.now()
                ));
    }

    @ExceptionHandler(ErroStorageException.class)
    public ResponseEntity<ErroResponse> handleErroStorage(ErroStorageException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Não foi possível processar o arquivo do prontuário",
                        OffsetDateTime.now()
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroValidacaoResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        List<CampoErroDTO> erros = List.of(new CampoErroDTO("arquivo", "Arquivo deve ter tamanho inferior a 10 MB"));
        return ResponseEntity.badRequest().body(
                new ErroValidacaoResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Arquivo excede o limite máximo permitido",
                        erros,
                        OffsetDateTime.now()
                )
        );
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErroValidacaoResponse> handleMissingPart(MissingServletRequestPartException ex) {
        List<CampoErroDTO> erros = List.of(new CampoErroDTO(ex.getRequestPartName(), "Campo obrigatório não informado"));
        return ResponseEntity.badRequest().body(
                new ErroValidacaoResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Dados obrigatórios não informados",
                        erros,
                        OffsetDateTime.now()
                )
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroValidacaoResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        List<CampoErroDTO> erros = List.of(new CampoErroDTO(ex.getParameterName(), "Parâmetro obrigatório não informado"));
        return ResponseEntity.badRequest().body(
                new ErroValidacaoResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Parâmetros obrigatórios não informados",
                        erros,
                        OffsetDateTime.now()
                )
        );
    }
}
