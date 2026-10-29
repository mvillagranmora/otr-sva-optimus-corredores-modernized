SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteOrigen";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteOrigen" AS
SELECT  C."IdComprobante",
		CDO."IdComprobanteDetalle",
		CDO."IdIngresoEgreso",
		IE."IdTipoMovimiento" AS "IdTipoMovimientoIngresoEgreso",
		CD."IdConcepto",
		IE."Monto" AS "MontoIngresoEgreso",
		IE."EsIngresoAutomatico",
		CDO."IdComprobante" AS "IdComprobanteAsociado"
  FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" as CDO
 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" as CD on CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle"
 INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as C on C."IdComprobante" = CD."IdComprobante"
  LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" as IE on IE."IdIngresoEgreso" = CDO."IdIngresoEgreso"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteOrigen" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
