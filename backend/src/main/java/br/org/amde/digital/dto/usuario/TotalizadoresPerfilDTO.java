package br.org.amde.digital.dto.usuario;

public record TotalizadoresPerfilDTO(
        long total,
        long administradores,
        long medicos,
        long pacientes
) {
}
