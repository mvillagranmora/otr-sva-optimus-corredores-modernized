package cl.bcs.contabilidad.domain.model.sap;

/** Header row of the view {@code ITZ.ConsultarPreComprobanteEnvioContabilidad} (InterfazContable_SelectPreComprobante.sql). */
public record PreComprobanteEnvio(Long JdtNum, String ReferenceDate, String Memo, String DueDate, String TaxDate,
                                  Long U_BCS_IDPreComp, Long U_BCS_TipoCen) {}
