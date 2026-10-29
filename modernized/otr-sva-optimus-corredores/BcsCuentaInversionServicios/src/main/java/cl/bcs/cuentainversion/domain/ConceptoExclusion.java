package cl.bcs.cuentainversion.domain;

import java.sql.Date;

/**
 * Account-statement concept excluded from the commission charge (row of {@code CTA.CobroComisionExclusionConcepto}); also the
 * request element of the create / delete exclusion services and the result row of the validation query
 * ({@code Existe} / {@code Excluido} are "S" / "N" flags). Public fields: it is read and written by name by the SQL helper.
 */
public class ConceptoExclusion {
    public Long IdExclusionConcepto = 0L;
    public String IdConcepto = "";
    public String NombreConcepto = "";
    public Date FechaVigenciaDesde = Date.valueOf("0001-01-01");
    public String Existe = "N";
    public String Excluido = "N";
}
