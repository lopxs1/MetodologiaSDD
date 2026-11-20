package br.org.amde.digital.dto.dashboard;

public record DashboardContadoresDTO(
        long totalConsultas,
        long consultasHoje,
        long totalPacientes,
        long profissionaisAtivos
) {
}

