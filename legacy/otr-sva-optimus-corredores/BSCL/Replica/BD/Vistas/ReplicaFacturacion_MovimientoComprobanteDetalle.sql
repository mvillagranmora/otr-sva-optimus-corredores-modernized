SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_MovimientoComprobanteDetalle";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_MovimientoComprobanteDetalle"  AS
SELECT
	WST."IdStack" AS "IDSTACK",
	"IdMovimientoComprobanteDetalle" AS TABLE_KEY,
	"Action" AS ACTION,
	"IdMovimientoComprobanteDetalle" AS "IDMOVIMIENTOCOMPROBANTEDETALLE",
	"IdComprobanteDetalle" AS "IDCOMPROBANTEDETALLE",
	"IdMovimientoComprobante" AS "IDMOVIMIENTOCOMPROBANTE",
	"IdTransaccion" AS "IDTRANSACCION",
	"IdInstrumento" AS "IDINSTRUMENTO",
	"NemotecnicoInstrumento" AS "NEMOTECNICOINSTRUMENTO",
	"IdConceptoMovimiento" AS "IDCONCEPTOMOVIMIENTO",
	"Cantidad" AS "CANTIDAD",
	"Precio" AS "PRECIO",
	"PrecioPromedio" AS "PRECIOPROMEDIO",
	"Monto" AS "MONTO",
	"Tasa" AS "TASA",
	"ValorRescate" AS "VALORRESCATE",
	"IdEstadoMovimientoComprobanteDetalle" AS "IDESTADOMOVIMIENTOCOMPROBANTEDETALLE",
	"MontoMovimientoComprobanteDetalle" AS "MONTOMOVIMIENTOCOMPROBANTEDETALLE",
	"MontoLiquidacion" AS "MONTOLIQUIDACION",
	"IdMoneda" AS "IDMONEDA",
	"IdMonedaMovimientoComprobanteDetalle" AS "IDMONEDAMOVIMIENTOCOMPROBANTEDETALLE",
	"IdMonedaLiquidacion" AS "IDMONEDALIQUIDACION",
	"IdTipoInstrumentoSubyacente" AS "IDTIPOINSTRUMENTOSUBYACENTE",
	"NemotecnicoSubyacente" AS "NEMOTECNICOSUBYACENTE",
	"IdInstrumentoSubyacente" AS "IDINSTRUMENTOSUBYACENTE",
	"IdTipoInstrumento" AS "IDTIPOINSTRUMENTO",
	"NombreTipoInstrumento" AS "NOMBRETIPOINSTRUMENTO",
	"NombreClasificacionTipoInstrumento" AS "NOMBRECLASIFICACIONTIPOINSTRUMENTO",
	"IdClasificacionTipoInstrumento" AS "IDCLASIFICACIONTIPOINSTRUMENTO",
	"NombreTipoInstrumentoSubyacente" AS "NOMBRETIPOINSTRUMENTOSUBYACENTE",
	"IdMonedaSubyacente" AS "IDMONEDASUBYACENTE",
	"IdTipoReajuste" AS "IDTIPOREAJUSTE",
	"FolioTransaccion" AS "FOLIOTRANSACCION",
	"CodigoIFRS" AS "CODIGOIFRS",
	"EsEnRueda" AS "ESENRUEDA",
	"IdSucursalEmpleadoEjecutivoPortafolio" AS "IDSUCURSALEMPLEADOEJECUTIVOPORTAFOLIO",
	"IdEmpleadoEjecutivoPortafolio" AS "IDEMPLEADOEJECUTIVOPORTAFOLIO",
	"IdMesa" AS "IDMESA",
	"EsVentaCorta" AS "ESVENTACORTA",
	"ValorTipoCambio" AS "VALORTIPOCAMBIO",
	"IndicadorOrdenDirecta" AS "INDICADORORDENDIRECTA",
	"IdFormaCustodia" AS "IDFORMACUSTODIA",
	"NemoSVS" AS "NEMOSVS",
	"Glosa" AS "GLOSA",
	"IdMonedaEmision" AS "IDMONEDAEMISION",
	"IdMonedaSpot" AS "IDMONEDASPOT",
	"FechaTransaccion" AS "FECHATRANSACCION"
FROM "_SYS_BIC"."BSCL.Facturacion.BD.Analytics/MovimientoComprobanteDetalle"
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
ON  WST."Table" = 'Facturacion_MovimientoComprobanteDetalle' AND WST."Key" = "IdMovimientoComprobanteDetalle"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFacturacion_MovimientoComprobanteDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
