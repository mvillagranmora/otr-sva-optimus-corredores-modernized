/**************************************************************
Proposito	: Recuperar informacion de asignaciones reporte
			  no enviadas a facturar para el flujo de custodia
autor		: JCASTRO
fecha		: 27/02/2019
***************************************************************/

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
	MZU."IdClasificacionTipoInstrumento",--ASIG."IdClasificacionTipoInstrumento",
	MZU."IdTipoInstrumento",--ASIG."IdTipoInstrumento",
	ASIG."IdInstrumentoSubyacente" AS "IdInstrumento", --ASIG."IdInstrumento",
	ASIG."IdEmisor",
	ASIG."Nemotecnico",
	'' AS "NemoSVS",
	--ASIG."IdMonedaAsignacion" AS "IdMonedaInstrumento",
	INS."IdMoneda" AS "IdMonedaInstrumento",
	ASIG."IdTipoReajuste",
	ASIG."FechaEmision" AS "FechaEmisionInstrumento",
	ASIG."FechaVencimiento" AS "FechaVencimientoInstrumento",
	'' AS "CodigoIFRS",
	ASIG."EsFungible",
	'' AS "TipoCarteraCorredor",
	ASIG."IdBoveda",
	ASIG."IdBolsa" as "IdBolsa",
	ASIG."IdTipoOperacion",
	TO_BIGINT(0) AS "IdMesa",
	ASIG."FechaLiquidacion",
	ASIG."CantidadAsignada" AS "Cantidad",
	ASIG."ValorRescateAsignado" AS "ValorRescate",
	0.0 AS "PrecioCompra",						---	revisar
	0.0 AS "TasaCompra",						---	revisar
	ASIG."MontoAsignado" AS "MontoATasaCompra",
	
	0.0 AS "PrecioMercado",
	0.0 AS "TasaMercado",
	0.0 AS "MontoATasaMercado",

	ASIG."IdCliente",
	ASIG."IdPortafolio",
--	(CASE WHEN ASIG."FechaLiquidacion" >= :v_FechaMinima THEN ASIG."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
	ASIG."IdOrden" as "IdRegistro",
	TO_BIGINT(0) as "FolioRegistro",
	ASIG."IdConcepto",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion", --CASE WHEN ASIG."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
	ASIG."Accion",
	'' AS "IdRegimenFondoMutuo",
	'' AS "GrupoRegimen",
	TO_DATE('0001-01-01') AS "FechaOrigenCompra"
FROM "BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCustodia" as ASIG
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" as MZU
		ON ((MZU."IdCliente" IS NULL OR ASIG."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR ASIG."IdPortafolio" = MZU."IdPortafolio")
			AND (ASIG."IdClasificacionTipoInstrumento" = 'RE')
			AND (ASIG."IdTipoInstrumento" IN ('RRV','RRF'))
			AND (MZU."IdInstrumento" IS NULL OR ASIG."IdInstrumentoSubyacente" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR ASIG."IdEmisor" = MZU."IdEmisor")
			AND (MZU."IdTipoReajuste" IS NULL OR ASIG."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR ASIG."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR ASIG."IdBoveda" = MZU."IdBoveda")
		   )
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON (INS."IdInstrumento" = ASIG."IdInstrumentoSubyacente")
WHERE ASIG."FechaLiquidacion" <= :FechaMaxima
	AND ASIG."IdEmpresa" = :IdEmpresa
	AND MZU."IdTrabajo" = :IdTrabajo
;
