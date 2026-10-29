/********************************************************** 
Proposito	: Recuperar información de eventos de 
			  capital para el flujo de custodia
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
	EVC."IdClasificacionTipoInstrumento",
	EVC."IdTipoInstrumento",
	EVC."IdInstrumento",
	'' AS "IdEmisor",
	EVC."NemoNuevo" AS "Nemotecnico",
	'' AS "NemoSVS",
	EVC."IdMoneda" AS "IdMonedaInstrumento",
	'' AS "IdTipoReajuste",
	TO_DATE('0001-01-01') AS "FechaEmisionInstrumento",
	TO_DATE('0001-01-01') AS "FechaVencimientoInstrumento",
	'' AS "CodigoIFRS",
	'' AS "EsFungible",
	'' AS "TipoCarteraCorredor",
	EVC."IdBoveda",
	'' as "IdBolsa",
	'' AS "IdTipoOperacion",
	TO_BIGINT(0) AS "IdMesa",
	EVC."FechaLiquidacion",
	TO_DECIMAL(EVC."Cantidad") AS "Cantidad",
	0.0 AS "ValorRescate",
	EVC."Precio" AS "PrecioCompra",
	0.0 AS "TasaCompra",
	EVC."Monto" AS "MontoATasaCompra",
	
	0.0 AS "PrecioMercado",
	0.0 AS "TasaMercado",
	0.0 AS "MontoATasaMercado",
	
	EVC."IdCliente",
	EVC."IdPortafolio",
--	(CASE WHEN EVC."FechaLiquidacion" >= :v_FechaMinima THEN EVC."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
	EVC."IdRegistro",
	EVC."FolioRegistro",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion", --CASE WHEN EVC."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
	EVC."IdTipoEventoCapital" AS "Accion",
	EVC."IdRegimenFondoMutuo",
	RFM."GrupoRegimen",
	TO_DATE('0001-01-01') AS "FechaOrigenCompra"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCustodia" AS EVC
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" AS MZU 
		ON ((MZU."IdCliente" IS NULL OR EVC."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR EVC."IdPortafolio" = MZU."IdPortafolio")
			AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR EVC."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento")
			AND (MZU."IdTipoInstrumento" IS NULL OR EVC."IdTipoInstrumento" = MZU."IdTipoInstrumento")
			AND (MZU."IdInstrumento" IS NULL OR EVC."IdInstrumento" = MZU."IdInstrumento")
			AND (MZU."IdBoveda" IS NULL OR EVC."IdBoveda" = MZU."IdBoveda")
			AND (MZU."IdRegimenFondoMutuo" IS NULL OR EVC."IdRegimenFondoMutuo" = MZU."IdRegimenFondoMutuo")
		   )
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON EVC."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
WHERE EVC."IdTipoEventoCapital" IN ('SU','SL','PB')
	AND EVC."IdEstado" IN ('ING','VIG','NOT')
	AND EVC."FechaLiquidacion" <= :FechaMaxima
	AND EVC."IdEmpresa" = :IdEmpresa
	AND MZU."IdTrabajo" = :IdTrabajo;

