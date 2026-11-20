package br.org.amde.digital.service.storage;

import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface ProntuarioStorageService {

    void armazenar(Long pacienteId, String nomeArquivoArmazenado, InputStream conteudo);

    Resource carregar(Long pacienteId, String nomeArquivoArmazenado);

    void remover(Long pacienteId, String nomeArquivoArmazenado);

    void removerSilencioso(Long pacienteId, String nomeArquivoArmazenado);
}

