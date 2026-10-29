package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.CustodiaPortafolioRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

@Repository
public class CustodiaPortafolioJdbc implements CustodiaPortafolioRepositorio {

    private static final String SQL_INFORMACION = SqlNombrado.recurso("ConsultarCustodiaPortafolioAPV_SelectInformacionPortafolio");
    private static final String SQL_REGIMEN = SqlNombrado.recurso("ConsultarCustodiaPortafolioAPV_SelectRegimenTributarioAPV_A");
    private static final String SQL_DETALLE = SqlNombrado.recurso("ConsultarCustodiaPortafolioAPV_SelectDetalleOrigenTraspaso");

    private final SqlNombrado sql;

    public CustodiaPortafolioJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<InformacionPortafolioFila> informacionPortafolio(List<Long> idsPortafolio) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("IdPortafolio", idsPortafolio);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_INFORMACION, p, InformacionPortafolioFila.class));
    }

    @Override
    public List<DetalleOrigenTraspasoFila> detalleOrigenTraspaso(List<Long> idsInstrumento, Long idTraspaso) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("IdInstrumento", idsInstrumento);
        p.put("IdTraspaso", idTraspaso);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_DETALLE, p, DetalleOrigenTraspasoFila.class));
    }

    @Override
    public RegimenFila regimenApvA() {
        List<RegimenFila> filas = Sneaky.sinChequear(() -> sql.consultar(SQL_REGIMEN, new HashMap<String, Object>(), RegimenFila.class));
        return filas.stream().findFirst().orElse(RegimenFila.vacio());
    }
}
