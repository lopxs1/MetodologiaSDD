package br.org.amde.digital.service.storage;

import br.org.amde.digital.exception.ErroStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Profile("prod-s3")
public class S3ProntuarioStorageService implements ProntuarioStorageService {

    private final String bucketName;

    public S3ProntuarioStorageService(@Value("${amde.storage.s3.bucket:}") String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public void armazenar(Long pacienteId, String nomeArquivoArmazenado, InputStream conteudo) {
        exigirBucketConfigurado();
        throw new ErroStorageException("Cliente AWS S3 não configurado neste ambiente");
    }

    @Override
    public Resource carregar(Long pacienteId, String nomeArquivoArmazenado) {
        exigirBucketConfigurado();
        throw new ErroStorageException("Cliente AWS S3 não configurado neste ambiente");
    }

    @Override
    public void remover(Long pacienteId, String nomeArquivoArmazenado) {
        exigirBucketConfigurado();
        throw new ErroStorageException("Cliente AWS S3 não configurado neste ambiente");
    }

    @Override
    public void removerSilencioso(Long pacienteId, String nomeArquivoArmazenado) {
    }

    protected String gerarChaveObjeto(Long pacienteId, String nomeArquivoArmazenado) {
        return "uploads/prontuarios/paciente-" + pacienteId + "/" + nomeArquivoArmazenado;
    }

    private void exigirBucketConfigurado() {
        if (bucketName == null || bucketName.isBlank()) {
            throw new ErroStorageException("Bucket S3 não configurado");
        }
    }
}

