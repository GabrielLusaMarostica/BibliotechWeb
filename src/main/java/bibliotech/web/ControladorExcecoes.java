package bibliotech.web;

import bibliotech.excecao.EstoqueInsuficienteException;
import bibliotech.excecao.PersistenciaException;
import bibliotech.excecao.RecursoNaoEncontradoException;
import bibliotech.excecao.RegraNegocioException;
import bibliotech.web.dto.ErroResposta;
import org.slf4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Traduz as exceções do domínio em códigos HTTP e mensagens claras para o front-end. */
@RestControllerAdvice
public class ControladorExcecoes {

    private static final Logger LOG = LoggerFactory.getLogger(ControladorExcecoes.class);

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ErroResposta> naoAutenticado(NaoAutenticadoException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErroResposta> estoque(EstoqueInsuficienteException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regra(RegraNegocioException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.");
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErroResposta> parametroComTipoErrado(TypeMismatchException e) {
        String nome = e instanceof MethodArgumentTypeMismatchException
                ? ((MethodArgumentTypeMismatchException) e).getName() : e.getPropertyName();
        String onde = nome == null ? "" : " '" + nome + "'";
        return resposta(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro" + onde + ".");
    }

    @ExceptionHandler(PersistenciaException.class)
    public ResponseEntity<ErroResposta> persistencia(PersistenciaException e) {
        LOG.error("Falha de persistência", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados.");
    }

    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResposta(status.value(), mensagem));
    }
}
