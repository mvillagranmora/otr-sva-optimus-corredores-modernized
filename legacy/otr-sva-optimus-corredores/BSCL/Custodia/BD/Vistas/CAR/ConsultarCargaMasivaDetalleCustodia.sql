SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CAR.ConsultarCargaMasivaDetalleCustodia";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CAR.ConsultarCargaMasivaDetalleCustodia" AS

SELECT
	CM."IdCargaMasiva",	
	CM."IdTipoCargaMasiva",
	CTCMC."Nombre"													AS "NombreTipoCargaMasiva",
	CM."IdEmpresa",
	CM."IdUsuario",
  	CM."FechaCarga",   			-- Fecha Sistema
 	CM."FechaProceso", 			-- Fecha en que se procesa CM
 	CM."FechaIngresoRegistro",	-- Fecha de BD al ingreso
 	CM."NombreArchivo",
	CM."IdEstado" 													AS "IdEstadoCargaMasiva",	
	CMD."IdCargaMasivaDetalle",
	CMD."DocumentoIdentidad",
	CMD."CodigoPortafolio",
	CMD."IdCondicionLiquidacion",
	CCL."Codigo",
	CMD."IdBoveda",	
	CMD."CuentaCustodio",
	CMD."IdConcepto",
	CMD."Nemotecnico",
	CMD."Emisor",
	CMD."IdTipoReajuste",
	CMD."FechaVencimiento",
	CMD."Cantidad",
	CMD."Precio",
	CMD."Tasa",
	CMD."Monto",
	CMD."Glosa",
	CMD."ObservacionValidacion",
	CMD."IdEstado" 													AS "IdEstadoCargaMasivaDetalle",
	ECMD."Nombre" 													AS "NombreEstadoCargaMasivaDetalle",
	CMD."CorrelativoReporte",		
	CMD."TipoOperacion",
	CMD."ClasificacionInstrumento",	
	CMD."Plazo",
	CMD."FechaLiquidacion",			
	CMD."FechaOperacion",
	CMD."TipoCambio",
	CMD."MontoContado",
	CMD."MontoPlazo",
	CMD."EsNominal",
	CMD."ReferenciaReportado",
	CMD."Bolsa",
	INS."IdTipoInstrumento",
	CMD."TipoEvento",
	CMD."NumeroDividendo",
	CMD."FechaLimite",
	CMD."FechaPago",
	CMD."Descripcion",
	CMD."Factor",
	CMD."Cuotas"
FROM "BSCL.Custodia.BD::CUS.CargaMasiva" AS CM
	INNER JOIN "BSCL.Custodia.BD::CUS.CargaMasivaDetalle" AS CMD 
		ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
	LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ECMD 
		ON (ECMD."IdEstado" = CMD."IdEstado")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" AS CCL
		ON CMD."IdCondicionLiquidacion" = CCL."IdCondicionDeLiquidacion"
	   AND CCL."IdTipoOperacion" = 'INGTIT'
	INNER JOIN "BSCL.Custodia.BD.Vistas::CAR.ConsultarTipoCargaMasivaCustodia"	AS CTCMC
		ON CM."IdTipoCargaMasiva" = CTCMC."IdTipoCargaMasiva"
	   AND CTCMC."IdEstado" = 'ACT'
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		ON INS."Nemotecnico" = CMD."Nemotecnico"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS
		ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
WHERE COALESCE(TINS."IdClasificacionTipoInstrumento", '') = CASE 
																WHEN TINS."IdClasificacionTipoInstrumento" = 'FM' AND CTCMC."Codigo" = 'DIVIDENDO_EN_CUOTAS_FFMM' THEN TINS."IdClasificacionTipoInstrumento"
																WHEN TINS."IdClasificacionTipoInstrumento" <> 'FM' AND CTCMC."Codigo" <> 'DIVIDENDO_EN_CUOTAS_FFMM' THEN TINS."IdClasificacionTipoInstrumento"
																ELSE ''
															END
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CAR.ConsultarCargaMasivaDetalleCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
