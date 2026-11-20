package br.org.amde.digital.service;

import br.org.amde.digital.domain.entity.Categoria;
import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.entity.RegistroProntuario;
import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.domain.enums.NivelAcesso;
import br.org.amde.digital.domain.enums.SituacaoDocumento;
import br.org.amde.digital.dto.paciente.AtualizarDadosClinicosRequest;
import br.org.amde.digital.dto.paciente.CategoriaHistoricoDTO;
import br.org.amde.digital.dto.paciente.ListaPacientesResponse;
import br.org.amde.digital.dto.paciente.PacienteFichaResponse;
import br.org.amde.digital.dto.paciente.PacienteResumoProjection;
import br.org.amde.digital.exception.RecursoNaoEncontradoException;
import br.org.amde.digital.exception.ValidacaoCampoException;
import br.org.amde.digital.repository.PacienteRepository;
import br.org.amde.digital.repository.RegistroProntuarioRepository;
import br.org.amde.digital.security.UsuarioAutenticado;
import br.org.amde.digital.service.usuario.CelularMasker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private RegistroProntuarioRepository registroProntuarioRepository;

    @Spy
    private CelularMasker celularMasker = new CelularMasker();

    @InjectMocks
    private PacienteService pacienteService;

    @Test
    void deveListarPacientesComCelularMascaradoEEscapeLiteralDeCuringas() {
        PacienteResumoProjection p1 = new PacienteResumoProjection(
                10L, 100L, "José Lucas", "jose@amde.example.org", GrauSuporte.NIVEL_2, "15999991234"
        );
        PacienteResumoProjection p2 = new PacienteResumoProjection(
                11L, 101L, "Marina Teste", "marina@amde.example.org", GrauSuporte.NIVEL_1, null
        );

        when(pacienteRepository.buscarAtivosPaginado(any(), eq(GrauSuporte.NIVEL_2), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 50), 2));

        ListaPacientesResponse resposta = pacienteService.listar(
                "José_100%!",
                GrauSuporte.NIVEL_2,
                0,
                120,
                "email",
                "DESC"
        );

        ArgumentCaptor<String> padraoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(pacienteRepository).buscarAtivosPaginado(padraoCaptor.capture(), eq(GrauSuporte.NIVEL_2), pageableCaptor.capture());

        assertThat(padraoCaptor.getValue()).isEqualTo("%jose!_100!%!!%");
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(50);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("usuario.email"))
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.DESC);
        assertThat(resposta.itens()).hasSize(2);
        assertThat(resposta.itens().get(0).numCelular()).isEqualTo("(15) *****-1234");
        assertThat(resposta.itens().get(1).numCelular()).isNull();
    }

    @Test
    void deveRejeitarTermoAcimaDe100CaracteresECampoSortInvalido() {
        String termoLongo = "a".repeat(101);

        assertThatThrownBy(() -> pacienteService.listar(termoLongo, null, 0, 10, "nome", "ASC"))
                .isInstanceOf(ValidacaoCampoException.class);
        assertThatThrownBy(() -> pacienteService.listar("ana", null, 0, 10, "descricao", "ASC"))
                .isInstanceOf(ValidacaoCampoException.class);
    }

    @Test
    void deveAbrirFichaComHistoricoAgrupadoECalculoDeSituacao() {
        Paciente paciente = criarPaciente(12L, 25L, "José Lucas", "15999990011", GrauSuporte.NIVEL_1, "Obs inicial");
        Categoria catExames = criarCategoria(1L, "Exames");
        Categoria catLaudos = criarCategoria(2L, "Laudos");
        Categoria catArquivado = criarCategoria(4L, "Arquivado");

        RegistroProntuario laudoAntigo = criarDocumento(98L, paciente, catLaudos, null, "Laudo v1", "l1.pdf");
        RegistroProntuario laudoAtual = criarDocumento(101L, paciente, catLaudos, laudoAntigo, "Laudo v2", "l2.pdf");
        RegistroProntuario exame = criarDocumento(102L, paciente, catExames, null, "Audiometria", "aud.pdf");
        RegistroProntuario arquivado = criarDocumento(103L, paciente, catArquivado, null, "Antigo", "ant.pdf");

        when(pacienteRepository.findAtivoByIdComUsuario(12L)).thenReturn(Optional.of(paciente));
        when(registroProntuarioRepository.findAtivosByPacienteIdComCategoria(12L))
                .thenReturn(List.of(arquivado, exame, laudoAtual, laudoAntigo));

        UsuarioAutenticado medico = new UsuarioAutenticado(2L, "Dra. Helena", "helena@amde.example.org", NivelAcesso.MEDICO);
        PacienteFichaResponse ficha = pacienteService.buscarFicha(12L, medico, true, 0, 10);

        assertThat(ficha.numCelular()).isEqualTo("(15) 99999-0011");
        assertThat(ficha.celularMascarado()).isFalse();
        assertThat(ficha.historicoPorCategoria()).hasSize(4);

        CategoriaHistoricoDTO laudosDto = ficha.historicoPorCategoria().stream()
                .filter(c -> c.categoria().equals("Laudos"))
                .findFirst()
                .orElseThrow();
        assertThat(laudosDto.totalDocumentos()).isEqualTo(2);
        assertThat(laudosDto.documentos().get(0).id()).isEqualTo(101L);
        assertThat(laudosDto.documentos().get(0).situacao()).isEqualTo(SituacaoDocumento.VIGENTE);
        assertThat(laudosDto.documentos().get(1).id()).isEqualTo(98L);
        assertThat(laudosDto.documentos().get(1).situacao()).isEqualTo(SituacaoDocumento.SUBSTITUIDO);

        CategoriaHistoricoDTO arquivadoDto = ficha.historicoPorCategoria().stream()
                .filter(c -> c.categoria().equals("Arquivado"))
                .findFirst()
                .orElseThrow();
        assertThat(arquivadoDto.documentos().get(0).situacao()).isEqualTo(SituacaoDocumento.ARQUIVADO);
    }

    @Test
    void deveBloquearPacienteAoAcessarOutroIdEManterCelularMascaradoNoProprio() {
        Paciente proprio = criarPaciente(12L, 25L, "José Lucas", "15999990011", GrauSuporte.NIVEL_1, null);
        UsuarioAutenticado usuarioPaciente = new UsuarioAutenticado(
                25L, "José Lucas", "jose@amde.example.org", NivelAcesso.PACIENTE
        );

        when(pacienteRepository.findAtivoByUsuarioIdComUsuario(25L)).thenReturn(Optional.of(proprio));
        when(registroProntuarioRepository.findAtivosByPacienteIdComCategoria(12L)).thenReturn(List.of());

        assertThatThrownBy(() -> pacienteService.buscarFicha(999L, usuarioPaciente, true, 0, 10))
                .isInstanceOf(AccessDeniedException.class);
        verify(pacienteRepository, never()).findAtivoByIdComUsuario(999L);

        PacienteFichaResponse minhaFicha = pacienteService.buscarFicha(12L, usuarioPaciente, true, 0, 10);
        assertThat(minhaFicha.numCelular()).isEqualTo("(15) *****-0011");
        assertThat(minhaFicha.celularMascarado()).isTrue();

        PacienteFichaResponse viaAtalho = pacienteService.buscarMeuProntuario(usuarioPaciente, 0, 10);
        assertThat(viaAtalho.id()).isEqualTo(12L);
        assertThat(viaAtalho.celularMascarado()).isTrue();
    }

    @Test
    void deveAtualizarDadosClinicosEValidarRegrasRn32() {
        Paciente paciente = criarPaciente(12L, 25L, "José Lucas", "15999990011", GrauSuporte.NIVEL_1, "Antiga");
        when(pacienteRepository.findAtivoByIdComUsuario(12L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.saveAndFlush(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(registroProntuarioRepository.findAtivosByPacienteIdComCategoria(12L)).thenReturn(List.of());

        PacienteFichaResponse atualizado = pacienteService.atualizarDadosClinicos(
                12L,
                new AtualizarDadosClinicosRequest(GrauSuporte.NIVEL_3, "  Evolução clínica atualizada.  ")
        );

        assertThat(atualizado.grauSuporte()).isEqualTo(GrauSuporte.NIVEL_3);
        assertThat(atualizado.descricao()).isEqualTo("Evolução clínica atualizada.");
        assertThat(atualizado.nome()).isEqualTo("José Lucas");

        assertThatThrownBy(() -> pacienteService.atualizarDadosClinicos(
                12L,
                new AtualizarDadosClinicosRequest(null, "Teste")
        )).isInstanceOf(ValidacaoCampoException.class);

        assertThatThrownBy(() -> pacienteService.atualizarDadosClinicos(
                12L,
                new AtualizarDadosClinicosRequest(GrauSuporte.NIVEL_2, "x".repeat(5001))
        )).isInstanceOf(ValidacaoCampoException.class);

        when(pacienteRepository.findAtivoByIdComUsuario(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> pacienteService.atualizarDadosClinicos(
                404L,
                new AtualizarDadosClinicosRequest(GrauSuporte.NIVEL_1, null)
        )).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private Paciente criarPaciente(
            Long pacienteId,
            Long usuarioId,
            String nome,
            String celular,
            GrauSuporte grau,
            String descricao
    ) {
        Usuario u = new Usuario();
        u.setId(usuarioId);
        u.setNome(nome);
        u.setEmail("paciente" + usuarioId + "@amde.example.org");
        u.setNivelAcesso(NivelAcesso.PACIENTE);
        u.setNumCelular(celular);
        u.setDataNascimento(LocalDate.of(2016, 3, 15));
        u.setPais("Brasil");
        u.setEstado("SP");
        u.setCidade("Sorocaba");

        Paciente p = new Paciente();
        p.setId(pacienteId);
        p.setUsuario(u);
        p.setGrauSuporte(grau);
        p.setDescricao(descricao);
        p.setAtualizadoEm(OffsetDateTime.now());
        return p;
    }

    private Categoria criarCategoria(Long id, String nome) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNome(nome);
        return c;
    }

    private RegistroProntuario criarDocumento(
            Long id,
            Paciente paciente,
            Categoria categoria,
            RegistroProntuario substituido,
            String titulo,
            String nomeArquivo
    ) {
        RegistroProntuario r = new RegistroProntuario();
        r.setId(id);
        r.setPaciente(paciente);
        r.setCategoria(categoria);
        r.setRegistroSubstituido(substituido);
        r.setTituloDocumento(titulo);
        r.setNomeArquivoOriginal(nomeArquivo);
        r.setNomeArquivoArmazenado("uuid-" + id + ".pdf");
        r.setCriadoEm(OffsetDateTime.now());
        return r;
    }
}

