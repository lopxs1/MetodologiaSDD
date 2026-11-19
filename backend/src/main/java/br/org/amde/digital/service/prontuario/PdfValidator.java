package br.org.amde.digital.service.prontuario;

import br.org.amde.digital.exception.ValidacaoCampoException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

@Component
public class PdfValidator {

    public static final long TAMANHO_MAXIMO_BYTES = 10L * 1024L * 1024L;
    private static final byte[] MAGIC_BYTES_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final int TAMANHO_MINIMO_NOME = 5;
    private static final int TAMANHO_MAXIMO_NOME = 255;

    public String validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty() || arquivo.getSize() <= 0) {
            throw new ValidacaoCampoException("arquivo", "Arquivo PDF obrigatório e não pode estar vazio");
        }

        if (arquivo.getSize() >= TAMANHO_MAXIMO_BYTES) {
            throw new ValidacaoCampoException("arquivo", "Arquivo deve ter tamanho inferior a 10 MB");
        }

        String nomeOriginal = extrairNomeSeguro(arquivo.getOriginalFilename());
        if (nomeOriginal == null
                || nomeOriginal.length() < TAMANHO_MINIMO_NOME
                || nomeOriginal.length() > TAMANHO_MAXIMO_NOME
                || !nomeOriginal.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ValidacaoCampoException(
                    "arquivo",
                    "Nome do arquivo deve ter entre 5 e 255 caracteres e extensão .pdf"
            );
        }

        if (!isContentTypePdf(arquivo.getContentType())) {
            throw new ValidacaoCampoException("arquivo", "Tipo de conteúdo inválido: apenas application/pdf é aceito");
        }

        validarMagicBytes(arquivo);
        return nomeOriginal;
    }

    private String extrairNomeSeguro(String rawName) {
        if (rawName == null) {
            return null;
        }
        int separador = Math.max(rawName.lastIndexOf('/'), rawName.lastIndexOf('\\'));
        String nomeBase = separador >= 0 ? rawName.substring(separador + 1) : rawName;
        String trimmed = nomeBase.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isContentTypePdf(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return false;
        }
        String tipoPrincipal = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        return "application/pdf".equals(tipoPrincipal);
    }

    private void validarMagicBytes(MultipartFile arquivo) {
        try (InputStream stream = arquivo.getInputStream()) {
            byte[] cabecalho = stream.readNBytes(MAGIC_BYTES_PDF.length);
            if (cabecalho.length < MAGIC_BYTES_PDF.length || !Arrays.equals(cabecalho, MAGIC_BYTES_PDF)) {
                throw new ValidacaoCampoException("arquivo", "Arquivo inválido: assinatura binária PDF não reconhecida");
            }
        } catch (IOException ex) {
            throw new ValidacaoCampoException("arquivo", "Não foi possível ler o conteúdo do arquivo PDF");
        }
    }
}

