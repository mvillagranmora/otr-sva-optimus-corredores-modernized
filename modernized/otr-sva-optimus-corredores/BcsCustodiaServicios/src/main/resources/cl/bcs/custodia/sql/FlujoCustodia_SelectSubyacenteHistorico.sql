/********************************************************** 
Proposito	: Recuperar información movimientos subyacentes para el flujo de custodia
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
	SUB."IdClasificacionTipoInstrumento",
	SUB."IdTipoInstrumento",
	SUB."IdInstrumento",
	SUB."IdEmisor",
	SUB."Nemotecnico",
	SUB."NemoSVS",
	SUB."CorteMinimo",
	SUB."IdMonedaInstrumento",
	SUB."IdTipoReajuste",
	SUB."FechaEmisionInstrumento",
	SUB."FechaVencimientoInstrumento",
	CAR."CodigoIFRS",
	SUB."EsFungible",
	'' AS "TipoCarteraCorredor",
	CAR."IdBoveda",
	CAR."IdBolsa",
	CAR."IdTipoOperacion",
	CAR."IdMesa",
	CAR."FechaOperacion" AS "FechaLiquidacion",
	
	SUB."INGCantidad" as "Cantidad",
	SUB."INGValorRescate" as "ValorRescate",
	SUB."INGPrecioSubyacente" as "PrecioCompra",
	SUB."INGTasaSubyacente" as "TasaCompra",
	SUB."INGMontoCompraActualMonedaInstrumento" as "MontoATasaCompra", 
	
	SUB."INGPrecioMercadoActual" as "PrecioMercado",
	SUB."INGTasaMercadoActual" as "TasaMercado",
	SUB."INGMontoMercadoActualMonedaInstrumento" as "MontoATasaMercado",
	SUB."IdMonedaEmision",
	CAR."IdCliente",
	CAR."IdPortafolio",
--	(CASE WHEN CAR."FechaLiquidacion" >= :v_FechaMinima THEN CAR."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",
	SUB."IdSubyacente" as "IdRegistro",
	SUB."FolioSubyacente" as "FolioRegistro",
	CAR."IdConcepto",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion", --CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
	CAR."IdRegimenFondoMutuo",
	RFM."GrupoRegimen",
	CAR."FechaOrigenCompra",
	CASE
		WHEN CompDet."IndicadorOrdenDirecta" = 'OD'
			THEN TranLocal."IdCorredorLocal"
		ELSE
			Tran."IdCorredorContraparte"
	END AS "IdCorredorContraparte"
FROM "BSCL.Custodia.BD::HIS.Subyacente" AS SUB
	INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR
		ON (CAR."FolioCartera" = SUB."FolioCarteraRelacionada" AND CAR."FechaRespaldo" = SUB."FechaRespaldo")
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" as MZU
		ON ((MZU."IdCliente" IS NULL OR CAR."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR CAR."IdPortafolio" = MZU."IdPortafolio")
			AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR SUB."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento")
			AND (MZU."IdTipoInstrumento" IS NULL OR SUB."IdTipoInstrumento" = MZU."IdTipoInstrumento")
			AND (MZU."IdInstrumento" IS NULL OR SUB."IdInstrumento" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR SUB."IdEmisor" = MZU."IdEmisor")
			AND (MZU."FechaVencimiento" IS NULL OR SUB."FechaVencimientoInstrumento" = MZU."FechaVencimiento")
			AND (MZU."IdTipoReajuste" IS NULL OR SUB."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR SUB."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR CAR."IdBoveda" = MZU."IdBoveda")
			AND (MZU."IdRegimenFondoMutuo" IS NULL OR CAR."IdRegimenFondoMutuo" = MZU."IdRegimenFondoMutuo")
		   )
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON CAR."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
			LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
				ON CAR."IdTipoComprobante" = CompDet."IdTipoComprobante"
				AND CAR."FolioComprobante" = CompDet."IdComprobante"
				
			LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
				ON CompDet."IdTransaccion" = Tran."IdTransaccion"
				AND Tran."IdCorredorLocal" <> Tran."IdCorredorContraparte"
			LEFT JOIN "BSCL.Orden.BD::HIS.Transaccion" TranLocal
				ON CompDet."IdTransaccion" = TranLocal."IdTransaccion"
				AND TranLocal."IdCorredorLocal" = TranLocal."IdCorredorContraparte"
			
WHERE CAR."IdTipoOperacion" IN (:idsTipoOperacion)
	AND CAR."IdEstado" = 'VIG'
	AND SUB."IdEstado" = 'VIG'
	AND CAR."IdEmpresa" = :IdEmpresa
	--AND CAR."FechaLiquidacion" <= :FechaMaxima
	AND CAR."FechaRespaldo" = :FechaMaxima
	AND MZU."IdTrabajo" = :IdTrabajo;
