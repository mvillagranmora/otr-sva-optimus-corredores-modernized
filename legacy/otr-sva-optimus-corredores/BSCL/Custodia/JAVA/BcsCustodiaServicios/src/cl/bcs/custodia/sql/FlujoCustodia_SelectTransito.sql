/********************************************************** 
Proposito	: Recuperar información de tránsito para el flujo de custodia
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
	TRA."IdClasificacionTipoInstrumento",
	TRA."IdTipoInstrumento",
	TRA."IdInstrumento",
	TRA."IdEmisor",
	TRA."Nemotecnico",
	TRA."NemoSVS",
	TRA."CorteMinimo",
	TRA."IdMonedaInstrumento",
	TRA."IdTipoReajuste",
	TRA."FechaEmisionInstrumento",
	TRA."FechaVencimientoInstrumento",
	TRA."CodigoIFRS",
	TRA."EsFungible",
	'' AS "TipoCarteraCorredor",
	TRA."IdBoveda",
	TRA."IdBolsa",
	TRA."IdTipoOperacion",
	TRA."IdMesa",
	TRA."FechaLiquidacion",
	TRA."Cantidad",
	TRA."ValorRescate",
	TRA."PrecioCompra" AS "PrecioCompra",
	TRA."TasaCompra" AS "TasaCompra",
	TRA."MontoCompraMonedaInstrumento" AS "MontoATasaCompra",

	0.0 AS "PrecioMercado",
	0.0 AS "TasaMercado",
	0.0 AS "MontoATasaMercado",

	TRA."IdCliente",
	TRA."IdPortafolio",
--	(CASE WHEN TRA."FechaLiquidacion" >= :v_FechaMinima THEN TRA."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",
	TRA."IdMovimientoCarteraEnTransito" AS "IdRegistro",
	CAST(0 AS BIGINT) AS "FolioRegistro",
	TRA."IdConcepto",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion",
	CMC."IdAccionOperacion",
	TRA."IdRegimenFondoMutuo",	
	TRA."EsVentaDescubierta",
	RFM."GrupoRegimen",
	TRA."FechaOrigenCompra"
	,MC."IdCorredoraContraparte" AS "IdCorredorContraparte" 
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS TRA
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS CMC 
		ON CMC."IdTipoOperacion" = TRA."IdTipoOperacion"
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" AS MZU
		ON (MZU."IdCliente" IS NULL OR TRA."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR TRA."IdPortafolio" = MZU."IdPortafolio")
			AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR TRA."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento")
			AND (MZU."IdTipoInstrumento" IS NULL OR TRA."IdTipoInstrumento" = MZU."IdTipoInstrumento")
			AND (MZU."IdInstrumento" IS NULL OR TRA."IdInstrumento" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR TRA."IdEmisor" = MZU."IdEmisor")
			AND (MZU."FechaVencimiento" IS NULL OR TRA."FechaVencimientoInstrumento" = MZU."FechaVencimiento")
			AND (MZU."IdTipoReajuste" IS NULL OR TRA."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR TRA."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR TRA."IdBoveda" = MZU."IdBoveda")
			AND (MZU."IdRegimenFondoMutuo" IS NULL OR TRA."IdRegimenFondoMutuo" = MZU."IdRegimenFondoMutuo")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		ON INS."IdInstrumento" = TRA."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER
		ON TOPER."IdTipoOperacion" = TRA."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI
		ON TOTI."IdTipoOperacion" = MZU."UsoTipoOperacion"
			AND TOTI."IdTipoInstrumento" = TRA."IdTipoInstrumento"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON TRA."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MC 
		ON TRA."FolioComprobante" = MC."IdComprobante"
		AND TRA."IdInstrumento" = MC."IdInstrumento"
		AND TRA."FolioExternoReferencia" = MC."FolioTransaccion"
WHERE TRA."IdEstado" = 'PEN'
	AND TRA."IdEmpresa" = :IdEmpresa
	AND TRA."FechaLiquidacion" <= :FechaMaxima
	AND MZU."IdTrabajo" = :IdTrabajo;

