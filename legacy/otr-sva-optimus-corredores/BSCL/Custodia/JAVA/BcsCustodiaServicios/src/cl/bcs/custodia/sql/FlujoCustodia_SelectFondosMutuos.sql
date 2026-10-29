/********************************************************** 
Proposito	: Recuperar información de fondos 
			  mutuos para el flujo de custodia
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/

SELECT
	"BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL AS "IdDatosFlujoCustodia",
--	:p_out_KeyFlujoCustodia AS "KeyFlujoCustodia",
	MZU."IdUsoFlujoCustodia",
	MZU."UsoTipoOperacion",
	MZU."BusquedaEspecifica",
	MZU."IdTipoInstrumentoEspecifico",
--	I."ItemCalculo",
--	I."ItemOrigen",
--	:p_FechaSaldo AS "FechaEjecucion",
	FFMM."IdClasificacionInstrumento" as "IdClasificacionTipoInstrumento",
	FFMM."IdTipoInstrumento",
	FFMM."IdInstrumento",
	FFMM."IdEmisor",
	FFMM."Nemotecnico",
	FFMM."NemoSVS",
	FFMM."IdMonedaInstrumento",
	FFMM."IdTipoReajuste",
	FFMM."FechaEmisionInstrumento",	
	TO_DATE(FFMM."FechaVencimientoInstrumento") AS "FechaVencimientoInstrumento",
	FFMM."CodigoIFRS",
	FFMM."EsFungible",
	'' AS "TipoCarteraCorredor",
	FFMM."IdBoveda",
	FFMM."IdBolsa",
	FFMM."IdTipoOperacion",	
	TO_BIGINT(0) AS "IdMesa",
	CASE WHEN FFMM."IdTipoOperacion" = 'APORFM' THEN FFMM."FechaLiquidacion"  ELSE :FechaMinima END AS "FechaLiquidacion",
	FFMM."Cantidad",	
	TO_DECIMAL(FFMM."ValorRescate") AS "ValorRescate",
	FFMM."PrecioCompra",	
	TO_DECIMAL(FFMM."TasaCompra") AS "TasaCompra",
	FFMM."MontoATasaCompra",
	
	0.0 as "PrecioMercado",	
	0.0 as "TasaMercado",
	0.0 as "MontoATasaMercado",
	
	FFMM."IdCliente",
	FFMM."IdPortafolio",
--	(CASE WHEN FFMM."FechaLiquidacion" >= :v_FechaMinima THEN FFMM."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",
	FFMM."IdRegistro",	
	TO_BIGINT(0) AS "FolioRegistro",
	FFMM."IdConcepto",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion", --CASE WHEN FFMM."IdClasificacionInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
	TO_DECIMAL(1,28,6) AS "CorteMinimo",
	FFMM."IdRegimenTributario" AS "IdRegimenFondoMutuo",
	RFM."GrupoRegimen",
	FFMM."FechaCertificacion" AS "FechaOrigenCompra"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCustodia" as FFMM
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" AS MZU 
		ON ((MZU."IdCliente" IS NULL OR FFMM."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR FFMM."IdPortafolio" = MZU."IdPortafolio")
			AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR FFMM."IdClasificacionInstrumento" = MZU."IdClasificacionTipoInstrumento")
			AND (MZU."IdTipoInstrumento" IS NULL OR FFMM."IdTipoInstrumento" = MZU."IdTipoInstrumento")
			AND (MZU."IdInstrumento" IS NULL OR FFMM."IdInstrumento" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR FFMM."IdEmisor" = MZU."IdEmisor")
			AND (MZU."IdTipoReajuste" IS NULL OR FFMM."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR FFMM."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR FFMM."IdBoveda" = MZU."IdBoveda")
			AND (MZU."IdRegimenFondoMutuo" IS NULL OR FFMM."IdRegimenTributario" = MZU."IdRegimenFondoMutuo")
		   )
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON FFMM."IdRegimenTributario" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
WHERE FFMM."IdEstado" = 'ING'
	AND FFMM."FechaLiquidacion" <= CASE WHEN FFMM."IdTipoOperacion" = 'APORFM' THEN :FechaMaxima ELSE FFMM."FechaLiquidacion" END
	AND FFMM."IdEmpresa" = :IdEmpresa
	AND MZU."IdTrabajo" = :IdTrabajo
;