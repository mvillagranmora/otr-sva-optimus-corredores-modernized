SELECT
"JdtNum"
,"ReferenceDate"
,"Memo"
,"DueDate"
,"TaxDate"
,"U_BCS_IDPreComp"
,"U_BCS_TipoCen"
FROM "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarPreComprobanteEnvioContabilidad"
WHERE "JdtNum" IN (:IdPrecomprobante);