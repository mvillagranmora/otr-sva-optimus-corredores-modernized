SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorTipoCambioHistorico";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorTipoCambioHistorico"  
AS
SELECT
	"ValorTipoCambioHistorico"."IdValorTipoCambioHistorico"
	,"ValorTipoCambioHistorico"."IdSerieFondo"
	,"ValorTipoCambioHistorico"."FechaCierre" as "FechaVigencia"
	,"ValorTipoCambioHistorico"."ValorDolarAporte"
	,"ValorTipoCambioHistorico"."ValorDolarRescate"
	,"ValorTipoCambioHistorico"."Observacion"
	,"ValorTipoCambioHistorico"."FechaCarga" as "FechaCalculo"
	,"ValorTipoCambioHistorico"."FechaIngresoRegistro"
	,"ValorTipoCambioHistorico"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.ValorTipoCambioHistorico" AS "ValorTipoCambioHistorico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "ValorTipoCambioHistorico"."IdSerieFondo")
WHERE "SerieFondo"."IdEmpresa" = "ValorTipoCambioHistorico"."IdEmpresa";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorTipoCambioHistorico" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
