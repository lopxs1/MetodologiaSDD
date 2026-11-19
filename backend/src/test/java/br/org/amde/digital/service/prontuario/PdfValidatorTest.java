package br.org.amde.digital.service.prontuario;

import br.org.amde.digital.exception.ValidacaoCampoException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfValidatorTest {

    private final PdfValidator validator = new PdfValidator();

    @Test
    void deveAceitarPdfValidoComMagicBytesETamanhoPermitido() {
        byte[] conteudo = "%PDF-1.7\n1 0 obj\n<<>>\nendobj\n".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "C:\\fakepath\\Laudo Clínico.PDF",
                "application/pdf",
                conteudo
        );

        String nome = validator.validar(arquivo);
        assertThat(nome).isEqualTo("Laudo Clínico.PDF");
    }

    @Test
    void deveRejeitarArquivoVazioOuSemExtensaoOuSemMagicBytes() {
        MockMultipartFile vazio = new MockMultipartFile("arquivo", "vazio.pdf", "application/pdf", new byte[0]);
        MockMultipartFile extensaoInvalida = new MockMultipartFile(
                "arquivo", "laudo.png", "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII)
        );
        MockMultipartFile contentTypeInvalido = new MockMultipartFile(
                "arquivo", "laudo.pdf", "image/png", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII)
        );
        MockMultipartFile falsoPdf = new MockMultipartFile(
                "arquivo", "falso.pdf", "application/pdf", "PNG_FAKE_CONTENT".getBytes(StandardCharsets.US_ASCII)
        );

        assertThatThrownBy(() -> validator.validar(null)).isInstanceOf(ValidacaoCampoException.class);
        assertThatThrownBy(() -> validator.validar(vazio)).isInstanceOf(ValidacaoCampoException.class);
        assertThatThrownBy(() -> validator.validar(extensaoInvalida)).isInstanceOf(ValidacaoCampoException.class);
        assertThatThrownBy(() -> validator.validar(contentTypeInvalido)).isInstanceOf(ValidacaoCampoException.class);
        assertThatThrownBy(() -> validator.validar(falsoPdf)).isInstanceOf(ValidacaoCampoException.class);
    }

    @Test
    void deveAplicarLimiteExclusivoDe10MbELimitesDoNomeOriginal() throws Exception {
        byte[] cabecalho = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);

        MultipartFile exatamente10Mb = mock(MultipartFile.class);
        when(exatamente10Mb.isEmpty()).thenReturn(false);
        when(exatamente10Mb.getSize()).thenReturn(10_485_760L);

        assertThatThrownBy(() -> validator.validar(exatamente10Mb))
                .isInstanceOf(ValidacaoCampoException.class);

        MultipartFile abaixoDe10Mb = mock(MultipartFile.class);
        when(abaixoDe10Mb.isEmpty()).thenReturn(false);
        when(abaixoDe10Mb.getSize()).thenReturn(10_485_759L);
        when(abaixoDe10Mb.getOriginalFilename()).thenReturn("exame.pdf");
        when(abaixoDe10Mb.getContentType()).thenReturn("application/pdf");
        when(abaixoDe10Mb.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(cabecalho));

        assertThat(validator.validar(abaixoDe10Mb)).isEqualTo("exame.pdf");

        MockMultipartFile nomeCurto = new MockMultipartFile("arquivo", ".pdf", "application/pdf", cabecalho);
        MockMultipartFile nomeLongo = new MockMultipartFile(
                "arquivo", "a".repeat(252) + ".pdf", "application/pdf", cabecalho
        );

        assertThatThrownBy(() -> validator.validar(nomeCurto)).isInstanceOf(ValidacaoCampoException.class);
        assertThatThrownBy(() -> validator.validar(nomeLongo)).isInstanceOf(ValidacaoCampoException.class);
    }
}

