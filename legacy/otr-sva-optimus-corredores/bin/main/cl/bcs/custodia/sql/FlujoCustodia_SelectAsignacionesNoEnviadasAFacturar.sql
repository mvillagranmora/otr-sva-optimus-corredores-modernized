/********************************************************** 
Proposito	: Recuperar información de asignaciones no
			  enviadas a facturar para el flujo de custodia
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
	ASIG."IdClasificacionTipoInstrumento",
	ASIG."IdTipoInstrumento",
	
	CASE 
		WHEN ASIG."IdTipoInstrumento" = 'SIM' THEN ASIG."IdInstrumentoSubyacente"
		ELSE ASIG."IdInstrumento"
	END  AS  "IdInstrumento",	
	
	
	ASIG."IdEmisor",
	ASIG."Nemotecnico",
	'' AS "NemoSVS",
	ASIG."IdMonedaAsignacion" AS "IdMonedaInstrumento",
	ASIG."IdTipoReajuste",
	ASIG."FechaEmision" AS "FechaEmisionInstrumento",
	
	
	CASE
		WHEN ASIG."IdTipoInstrumento" = 'SIM' THEN '0001-01-01'
		ELSE ASIG."FechaVencimiento"
	END  AS  "FechaVencimientoInstrumento",
	
	
	
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
			AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR ASIG."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento")
			AND (MZU."IdTipoInstrumento" IS NULL OR ASIG."IdTipoInstrumento" = MZU."IdTipoInstrumento")
			AND (MZU."IdInstrumento" IS NULL OR ASIG."IdInstrumento" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR ASIG."IdEmisor" = MZU."IdEmisor")
			AND (MZU."IdTipoReajuste" IS NULL OR ASIG."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR ASIG."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR ASIG."IdBoveda" = MZU."IdBoveda")
		   )
WHERE ASIG."FechaLiquidacion" <= :FechaMaxima
	AND ASIG."IdEmpresa" = :IdEmpresa
	AND MZU."IdTrabajo" = :IdTrabajo
;
