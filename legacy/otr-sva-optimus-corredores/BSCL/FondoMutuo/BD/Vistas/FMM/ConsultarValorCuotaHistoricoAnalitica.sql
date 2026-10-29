SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistoricoAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistoricoAnalitica" AS
SELECT
 	"IdValorCuotaHistorico"
   ,"ValorCuotaHistorico"."IdSerieFondo"
   ,TO_NVARCHAR("ValorCuotaHistorico"."FechaCierre",'YYYY-MM-DD') AS "FechaValorCuota"
   ,"ValorCuotaHistorico"."ValorCuota"
   ,"Fondo"."IdFondo"
   ,"ValorCuotaHistorico"."IdMoneda"
   ,"SerieFondo"."NombreSerieFondo"
   ,"SerieFondo"."CodigoSerie"
   ,TO_NVARCHAR("ValorCuotaHistorico"."FechaCarga",'YYYY-MM-DD') as "FechaCarga"
   ,TO_NVARCHAR("ValorCuotaHistorico"."FechaIngresoRegistro",'YYYY-MM-DD HH24:MI:SS') AS "FechaIngresoRegistro"
   ,"ValorCuotaHistorico"."EsUltimaCargaDiaria"
FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" AS "ValorCuotaHistorico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "ValorCuotaHistorico"."IdSerieFondo")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistoricoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
