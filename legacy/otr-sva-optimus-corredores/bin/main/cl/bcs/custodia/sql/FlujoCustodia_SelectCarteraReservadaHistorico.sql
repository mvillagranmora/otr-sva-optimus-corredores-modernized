/********************************************************** 
Proposito	: Recuperar información de cartera reservada para el flujo de custodia
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
	RSV."IdClasificacionTipoInstrumento",
	RSV."IdTipoInstrumento",
	TO_BIGINT(RSV."IdInstrumento") AS "IdInstrumento",
	RSV."IdEmisor",
	RSV."Nemotecnico",
	RSV."NemoSVS",
	RSV."CorteMinimo",
	RSV."IdMonedaInstrumento",
	RSV."IdTipoReajuste",
	RSV."FechaEmisionInstrumento",
	RSV."FechaVencimientoInstrumento",
	RSV."CodigoIFRS",
	RSV."EsFungible",
	'' AS "TipoCarteraCorredor",
	RSV."IdBoveda",
	RSV."IdBolsa",
	RSV."IdTipoOperacion",
	RSV."IdMesa",
	TO_DATE(RSV."INGFechaLiquidacion") AS "FechaLiquidacion",
	RSV."INGCantidad" as "Cantidad",
	RSV."INGValorRescate" as "ValorRescate",
	RSV."INGPrecioCompra" AS "PrecioCompra",
	RSV."INGTasaCompra" AS "TasaCompra",
	RSV."INGMontoATasaCompraActual" as "MontoATasaCompra",
	
	TO_DECIMAL(RSV."INGPrecioMercadoActual") AS "PrecioMercado",
	TO_DECIMAL(RSV."INGTasaMercadoActual") AS "TasaMercado",
	TO_DECIMAL(RSV."INGMontoATasaMercadoActual") AS "MontoATasaMercado",
	
	RSV."IdCliente",
	RSV."IdPortafolio",
--	(CASE WHEN RSV."INGFechaLiquidacion" >= :v_FechaMinima THEN RSV."INGFechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",	
	RSV."IdRegistro",
	RSV."FolioRegistro",
	RSV."IdConcepto",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion", --CASE WHEN RSV."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
	'' AS "IdRegimenFondoMutuo",
	'' AS "GrupoRegimen",
	TO_DATE('0001-01-01') AS "FechaOrigenCompra"
FROM ( --Las reservas estan guardando "IngMontoValorizado" expresado en la moneda de sistema
		SELECT
			R."IdClasificacionTipoInstrumento",R."IdTipoInstrumento", R."IdInstrumento", R."IdEmisor", C."Nemotecnico",
			C."NemoSVS",C."IdMonedaSistema" AS "IdMonedaInstrumento", C."IdTipoReajuste", C."FechaEmisionInstrumento",
			C."FechaVencimientoInstrumento", R."CodigoIFRS", R."EsFungible", R."IdBoveda", R."IdBolsa", O."IdTipoOperacion",
			O."IdMesaOrigen" AS "IdMesa",
			CASE
				WHEN O."IdTipoOperacion" IN ('COMTER','VENTER') THEN O."FechaCondicionLiquidacion"
				WHEN O."IdTipoOperacion" IN ('COMPAC','VENPAC') THEN O."FechaCreacion"
			END AS "INGFechaLiquidacion",
			R."IngCantidad" AS "INGCantidad", R."IngValorRescate" AS "INGValorRescate", R."Precio" AS "INGPrecioCompra",
			R."Tasa" AS "INGTasaCompra", R."IngMontoValorizado" AS "INGMontoATasaCompraActual", 0 AS "INGPrecioMercadoActual",
			0 AS "INGTasaMercadoActual", R."IdConcepto", 0 AS "INGMontoATasaMercadoActual", O."IdCliente", O."IdPortafolio",
			R."IdReserva" AS "IdRegistro", O."IdOperacion" AS "FolioRegistro",R."CorteMinimo"
		FROM "BSCL.Custodia.BD::HIS.Reserva" as R
			INNER JOIN "BSCL.Mesa.BD::MES.Operacion" as O
				ON O."IdOperacion" = R."IdOperacion"
			INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C
				ON C."FolioCartera" = R."FolioCarteraOrigen" AND R."FechaRespaldo" = C."FechaRespaldo"
		WHERE R."IdEstado" = 'VIG'
			AND C."IdEstado" = 'VIG'
			AND C."IdEmpresa" = :IdEmpresa
			AND R."FechaRespaldo" = :FechaMaxima
	 ) AS RSV
		INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" AS MZU
			ON ((MZU."IdCliente" IS NULL OR RSV."IdCliente" = MZU."IdCliente")
				AND (MZU."IdPortafolio" IS NULL OR RSV."IdPortafolio" = MZU."IdPortafolio")
				AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR RSV."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento")
				AND (MZU."IdTipoInstrumento" IS NULL OR RSV."IdTipoInstrumento" = MZU."IdTipoInstrumento")
				AND (MZU."IdInstrumento" IS NULL OR RSV."IdInstrumento" = MZU."IdInstrumento")
				AND (MZU."IdEmisor" IS NULL OR RSV."IdEmisor" = MZU."IdEmisor")
				AND (MZU."IdTipoReajuste" IS NULL OR RSV."IdTipoReajuste" = MZU."IdTipoReajuste")
				AND (MZU."EsFungible" IS NULL OR RSV."EsFungible" = MZU."EsFungible")
				AND (MZU."IdBoveda" IS NULL OR RSV."IdBoveda" = MZU."IdBoveda")
			   )
WHERE RSV."IdTipoOperacion" IN ('COMPAC','VENPAC','COMTER','VENTER')
	AND RSV."INGFechaLiquidacion" <= :FechaMaxima
	AND MZU."IdTrabajo" = :IdTrabajo;