SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCuadraturaFondoMutuo";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCuadraturaFondoMutuo"
AS
SELECT
 C."IdCuadraturaCustodia"
,F."CodigoSVS" AS "RunFondo"
,F."NombreFondo"
,F."IdMoneda" AS "IdMonedaFondo"
,F."IdEmisor"
,C."IdEmpresa"
,C."IdSerieFondo"
,C."TotalCuotasAGF"
,C."TotalCuotas"
,C."DiferenciaCuotas"
,C."ValorizacionCuotasAGF"
,C."ValorizacionCuotas"
,C."DiferenciaValorizacion"
,C."FechaCuadratura"
,C."FechaIngresoRegistro"
,C."ValorCuota"
,C."IdRegimen"
,C."IdEstado"
,S."DecimalesValorCuota"
,S."ISIN"
,(CASE C."DiferenciaCuotas" WHEN 0 THEN 'SI'
	ELSE 'NO' END) AS "IndicadorCuadratura"
FROM "BSCL.FondoMutuo.BD::FMM.CuadraturaCustodia" C
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" S ON (S."IdSerieFondo" = C."IdSerieFondo")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" F ON (F."IdFondo" = S."IdFondo");
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCuadraturaFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO