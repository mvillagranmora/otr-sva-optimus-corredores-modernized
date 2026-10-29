SET SCHEMA "BSCL";

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaVigente";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaVigente"  
AS
SELECT
ValorCuota."IdSerieFondo"
,ValorCuota."FechaVigencia"
,ValorCuota."ValorCuota"
,ValorCuota."IdMoneda"
,ValorCuota."Observacion"
,ValorCuota."FechaCalculo"
,ValorCuota."FechaIngresoRegistro"
,ValorCuota."IdEmpresa"
,SerieFondo."DecimalesValorCuota"
,SerieFondo."CodigoSerie"
,SerieFondo."IdFamiliaFondo"
,SerieFondo."IdInstrumento"
,SerieFondo."NombreSerieFondo"
,Fondo."Nemotecnico"
,Fondo."IdEmisor"
,Fondo."NombreFondo"
FROM (
	SELECT   
	ValorCuota."IdSerieFondo"
	,ValorCuota."FechaVigencia"
	,ValorCuota."ValorCuota"
	,ValorCuota."IdMoneda"
	,ValorCuota."Observacion"
	,ValorCuota."FechaCalculo"
	,ValorCuota."FechaIngresoRegistro"
	,ValorCuota."IdEmpresa"
	FROM 
	(
		SELECT 
		row_number() over (partition by "IdSerieFondo","FechaVigencia" order by "IdSerieFondo","FechaVigencia" DESC, "RowValorCuota" ASC) AS "row_num"
		,ValorCuota."IdSerieFondo"
		,ValorCuota."FechaVigencia"
		,ValorCuota."ValorCuota"
		,ValorCuota."IdMoneda"
		,ValorCuota."Observacion"
		,ValorCuota."FechaCalculo"
		,ValorCuota."FechaIngresoRegistro"
		,ValorCuota."IdEmpresa"
		FROM (
			SELECT
			-1 AS "RowValorCuota"
			,ValorCuota."IdSerieFondo"
			,ValorCuota."FechaCierre" as "FechaVigencia"
			,ValorCuota."ValorCuota"
			,ValorCuota."IdMoneda"
			,ValorCuota."Observacion"
			,ValorCuota."FechaCarga" as "FechaCalculo"
			,ValorCuota."FechaIngresoRegistro"
			,ValorCuota."IdEmpresa"
			FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS ValorCuota
			UNION ALL 
			SELECT 
			ValorCuota."RowValorCuota"
			,ValorCuota."IdSerieFondo"
			,ValorCuota."FechaVigencia"
			,ValorCuota."ValorCuota"
			,ValorCuota."IdMoneda"
			,ValorCuota."Observacion"
			,ValorCuota."FechaCalculo"
			,ValorCuota."FechaIngresoRegistro"
			,ValorCuota."IdEmpresa"
			FROM(
				SELECT
				row_number() over (partition by ValorCuota."IdSerieFondo",ValorCuota."FechaCierre" 
				order by ValorCuota."IdSerieFondo", ValorCuota."FechaCierre" desc,ValorCuota."IdValorCuotaHistorico" desc) AS "RowValorCuota"
				,ValorCuota."IdSerieFondo"
				,ValorCuota."FechaCierre" as "FechaVigencia"
				,ValorCuota."ValorCuota"
				,ValorCuota."IdMoneda"
				,ValorCuota."Observacion"
				,ValorCuota."FechaCarga" as "FechaCalculo"
				,ValorCuota."FechaIngresoRegistro"
				,ValorCuota."IdEmpresa"
				FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" AS ValorCuota
			) AS ValorCuota WHERE "RowValorCuota" = 1
		) AS ValorCuota
	) AS ValorCuota
	WHERE "row_num" = 1
) AS ValorCuota
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFondo ON (SerieFondo."IdSerieFondo" = ValorCuota."IdSerieFondo")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (Fondo."IdFondo" = SerieFondo."IdFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaVigente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;