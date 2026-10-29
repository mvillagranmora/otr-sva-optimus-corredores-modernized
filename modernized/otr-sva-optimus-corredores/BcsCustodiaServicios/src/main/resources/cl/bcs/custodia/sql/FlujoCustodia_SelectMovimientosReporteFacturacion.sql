/********************************************************** 
Proposito	: Recuperar informacion de movimientos reporte 
			  de facturacion para el flujo de custodia
autor		: JCASTRO
fecha		: 27/02/2019
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
	MZU."IdClasificacionTipoInstrumento",--MOV."IdClasificacionTipoInstrumento",
	MZU."IdTipoInstrumento",--MOV."IdTipoInstrumento",
	MOV."IdInstrumentoSubyacente" AS "IdInstrumento",--MOV."IdInstrumento",
	MOV."IdEmisor",
	MOV."Nemotecnico",
	MOV."NemoSVS",
	--MOV."IdMoneda" AS "IdMonedaInstrumento",
	INS."IdMoneda" AS "IdMonedaInstrumento",
	MOV."IdTipoReajuste",
	TO_DATE(MOV."FechaEmision") AS "FechaEmisionInstrumento",
	TO_DATE(MOV."FechaVencimiento") AS "FechaVencimientoInstrumento",
	MOV."CodigoIFRS",
	MOV."EsFungible",
	'' AS "TipoCarteraCorredor",
	MOV."IdBoveda",
	MOV."IdBolsa",
	MOV."IdTipoOperacion",
	TO_BIGINT(0) AS "IdMesa",
	MOV."FechaLiquidacion",
	MOV."Cantidad",
	MOV."ValorRescate",
	MOV."Precio" AS "PrecioCompra",
	MOV."Tasa" AS "TasaCompra",
	MOV."Monto" AS "MontoATasaCompra",
	
	0.0 AS "PrecioMercado",
	0.0 AS "TasaMercado",
	0.0 AS "MontoATasaMercado",
	
	MOV."IdCliente",
	MOV."IdPortafolio",
--	(CASE WHEN MOV."FechaLiquidacion" >= :v_FechaMinima THEN MOV."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
	MOV."IdMovimientoComprobante" AS "IdRegistro",
	TO_BIGINT(0) AS "FolioRegistro",
	MOV."IdConceptoMovimiento" AS "IdConcepto",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion", --CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
	COALESCE(MOV."AccionTipoOperacion",'') AS "Accion",
	'' AS "IdRegimenFondoMutuo",
	'' AS "GrupoRegimen",
	TO_DATE('0001-01-01') AS "FechaOrigenCompra"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCustodia" AS MOV
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" AS MZU
		ON ((MZU."IdCliente" IS NULL OR MOV."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR MOV."IdPortafolio" = MZU."IdPortafolio")
			AND (MOV."IdClasificacionTipoInstrumento" = 'RE')
			AND (MOV."IdTipoInstrumento" IN ('RRV','RRF'))
			AND (MZU."IdInstrumento" IS NULL OR MOV."IdInstrumentoSubyacente" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR MOV."IdEmisor" = MZU."IdEmisor")
			AND (MZU."IdTipoReajuste" IS NULL OR MOV."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR MOV."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR MOV."IdBoveda" = MZU."IdBoveda")
		   )
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON (INS."IdInstrumento" = MOV."IdInstrumentoSubyacente")
WHERE MOV."FechaLiquidacion" <= :FechaMaxima
	AND MOV."IdEmpresa" = :IdEmpresa
	AND MZU."IdTrabajo" = :IdTrabajo
;
