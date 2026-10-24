package bibliotech.web;

import bibliotech.config.ContextoAplicacao;
import bibliotech.repositorio.jdbc.ConfiguracaoBanco;
import bibliotech.servico.CodificadorSenha;
import bibliotech.servico.CodificadorSenhaPbkdf2;
import bibliotech.servico.MultaPorDiaFixo;
import bibliotech.servico.PoliticaMulta;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Raiz de composição da versão web: monta as dependências do núcleo (etapa 6).
 *
 * <p>{@code bibliotech.modo=memoria} (padrão) roda sem banco, com dados de demonstração;
 * {@code bibliotech.modo=jdbc} usa o MySQL configurado por BIBLIOTECH_DB_*.</p>
 */
@Configuration
public class ConfiguracaoBeans {

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguracaoBeans.class);

    @Bean
    public Clock relogio() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public PoliticaMulta politicaMulta() {
        return new MultaPorDiaFixo();
    }

    @Bean
    public CodificadorSenha codificadorSenha() {
        return new CodificadorSenhaPbkdf2();
    }

    @Bean
    public SessaoServico sessaoServico(Clock relogio) {
        return new SessaoServico(relogio);
    }

    @Bean
    public Apresentador apresentador(Clock relogio, PoliticaMulta politica) {
        return new Apresentador(relogio, politica);
    }

    @Bean
    public ContextoAplicacao contexto(Clock relogio, PoliticaMulta politica, CodificadorSenha codificador,
                                      @Value("${bibliotech.modo:memoria}") String modo,
                                      @Value("${bibliotech.admin.login:admin}") String loginAdmin,
                                      @Value("${bibliotech.admin.senha:}") String senhaAdmin) {
        ContextoAplicacao contexto;
        if ("jdbc".equalsIgnoreCase(modo)) {
            contexto = ContextoAplicacao.comJdbc(ConfiguracaoBanco.doAmbiente(), relogio, codificador, politica);
            LOG.info("Bibliotech rodando com MySQL/JDBC.");
        } else {
            contexto = ContextoAplicacao.emMemoria(relogio, codificador, politica);
            DadosDemonstracao.popular(contexto, relogio);
            if (senhaAdmin.isBlank()) {
                senhaAdmin = "admin123"; // só no modo memória (demonstração)
            }
            LOG.warn("Bibliotech rodando EM MEMÓRIA com dados de demonstração (nada é gravado em banco).");
        }
        criarAdministrador(contexto, loginAdmin, senhaAdmin);
        return contexto;
    }

    private void criarAdministrador(ContextoAplicacao contexto, String login, String senha) {
        if (senha == null || senha.isBlank()) {
            LOG.info("Nenhuma senha de administrador informada (BIBLIOTECH_ADMIN_SENHA): usuários não foram criados.");
            return;
        }
        try {
            contexto.autenticacao().cadastrar(login, senha);
            LOG.info("Usuário '{}' criado.", login);
        } catch (bibliotech.excecao.RegraNegocioException e) {
            LOG.info("Usuário '{}' já existe; mantido como está.", login);
        }
    }
}
