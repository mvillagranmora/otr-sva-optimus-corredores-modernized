SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorTipoCambio";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorTipoCambio"
AS
SELECT 
 "ValorTipoCambio"."IdSerieFondo"
, "ValorTipoCambio"."FechaCierre"
, "ValorTipoCambio"."ValorDolarAporte"
, "ValorTipoCambio"."ValorDolarRescate"
, "ValorTipoCambio"."FechaCarga"
, "SerieDeFondo"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" AS "ValorTipoCambio"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "ValorTipoCambio"."IdSerieFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorTipoCambio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
