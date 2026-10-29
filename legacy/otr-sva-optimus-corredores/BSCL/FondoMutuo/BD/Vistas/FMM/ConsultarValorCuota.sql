SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota"
AS
SELECT 
 "ValorCuota"."IdSerieFondo"
, "ValorCuota"."FechaCierre"
, "ValorCuota"."ValorCuota"
, "ValorCuota"."IdMoneda"
, "ValorCuota"."FechaCarga"
, "SerieDeFondo"."IdEmpresa"
, "SerieDeFondo"."IdInstrumento"
FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "ValorCuota"."IdSerieFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
