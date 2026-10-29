SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarPreComprobanteEnvioContabilidad";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarPreComprobanteEnvioContabilidad"
AS 
SELECT
P."JdtNum"
,P."ReferenceDate"
,P."Memo"
,P."DueDate"
,P."TaxDate"
,PC."IdPreComprobante" AS "U_BCS_IDPreComp"
,PC."IdTipoCentralizacion" AS "U_BCS_TipoCen"
FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteEnvioContabilidad" P
INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteCentralizacionTipo" PC ON (P."JdtNum" = PC."IdPreComprobante")
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarPreComprobanteEnvioContabilidad" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO	
