package br.org.amde.digital.service.storage;

import br.org.amde.digital.exception.ErroStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@Profile("!prod-s3")
public class LocalFileSystemStorageService implements ProntuarioStorageService {

    private final Path diretorioRaiz;

    public LocalFileSystemStorageService(
            @Value("${amde.storage.upload-dir:uploads/prontuarios}") String uploadDir
    ) {
        this.diretorioRaiz = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void armazenar(Long pacienteId, String nomeArquivoArmazenado, InputStream conteudo) {
        Path destino = resolverCaminho(pacienteId, nomeArquivoArmazenado);
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(conteudo, destino);
        } catch (IOException ex) {
            log.error("Falha ao armazenar documento do paciente [pacienteId={}, erro={}]",
                    pacienteId, ex.getClass().getSimpleName());
            throw new ErroStorageException("Não foi possível armazenar o arquivo do prontuário", ex);
        }
    }

    @Override
    public Resource carregar(Long pacienteId, String nomeArquivoArmazenado) {
        Path destino = resolverCaminho(pacienteId, nomeArquivoArmazenado);
        if (!Files.exists(destino) || !Files.isRegularFile(destino) || !Files.isReadable(destino)) {
            log.error("Arquivo físico do prontuário ausente ou ilegível [pacienteId={}]", pacienteId);
            throw new ErroStorageException("Arquivo físico do prontuário não encontrado");
        }
        return new FileSystemResource(destino);
    }

    @Override
    public void remover(Long pacienteId, String nomeArquivoArmazenado) {
        Path destino = resolverCaminho(pacienteId, nomeArquivoArmazenado);
        try {
            boolean removido = Files.deleteIfExists(destino);
            if (!removido) {
                log.error("Arquivo físico não localizado para remoção [pacienteId={}]", pacienteId);
                throw new ErroStorageException("Arquivo físico do prontuário não encontrado para remoção");
            }
        } catch (IOException ex) {
            log.error("Falha ao remover arquivo físico [pacienteId={}, erro={}]",
                    pacienteId, ex.getClass().getSimpleName());
            throw new ErroStorageException("Não foi possível remover o arquivo físico do prontuário", ex);
        }
    }

    @Override
    public void removerSilencioso(Long pacienteId, String nomeArquivoArmazenado) {
        try {
            Path destino = resolverCaminho(pacienteId, nomeArquivoArmazenado);
            Files.deleteIfExists(destino);
        } catch (Exception ex) {
            log.warn("Falha na limpeza compensatória de arquivo [pacienteId={}, erro={}]",
                    pacienteId, ex.getClass().getSimpleName());
        }
    }

    private Path resolverCaminho(Long pacienteId, String nomeArquivoArmazenado) {
        if (pacienteId == null || pacienteId <= 0 || nomeArquivoArmazenado == null || nomeArquivoArmazenado.isBlank()) {
            throw new ErroStorageException("Identificador de armazenamento inválido");
        }
        if (nomeArquivoArmazenado.contains("/") || nomeArquivoArmazenado.contains("\\") || nomeArquivoArmazenado.contains("..")) {
            throw new ErroStorageException("Nome de arquivo armazenado inválido");
        }
        Path pastaPaciente = diretorioRaiz.resolve("paciente-" + pacienteId).normalize();
        Path destino = pastaPaciente.resolve(nomeArquivoArmazenado).normalize();
        if (!destino.startsWith(diretorioRaiz)) {
            throw new ErroStorageException("Caminho de armazenamento fora do diretório permitido");
        }
        return destino;
    }
}

