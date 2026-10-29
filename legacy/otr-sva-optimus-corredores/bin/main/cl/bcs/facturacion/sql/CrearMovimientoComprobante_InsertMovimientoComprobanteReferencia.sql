INSERT INTO "BSCL.Facturacion.BD::FAC.MovimientoComprobanteReferencia" (
SELECT "BSCL.Facturacion.BD.Secuencias::IdMovimientoComprobanteReferencia".Nextval,
:Referencia,
:TipoReferencia,
:IdMovimientoComprobante,
''
FROM DUMMY
)