package br.org.amde.digital.dto.dashboard;

import java.util.List;

public record DashboardResponse(
        DashboardContadoresDTO contadores,
        List<ConsultaDiaDTO> consultasAoLongoDoTempo,
        List<ConsultaEspecialidadeDTO> consultasPorEspecialidade
) {
}

