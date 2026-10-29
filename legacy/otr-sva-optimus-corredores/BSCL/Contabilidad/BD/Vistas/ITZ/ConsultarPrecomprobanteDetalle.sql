SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarPrecomprobanteDetalle";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarPrecomprobanteDetalle"
AS 
SELECT
ASI."IdPreComprobante"
,ASI."IdPreComprobanteAsiento"
,ASI."Linea"
,ASI."IdCuentaContable"
,CTA."NombreCuenta"	AS "NombreCuenta"
,CASE WHEN ASI."MontoHaber" > 0 THEN ABS(EV2."Monto") ELSE 0 END AS "MontoHaberDetalle"
,CASE WHEN ASI."MontoDebe" > 0 THEN ABS(EV2."Monto") ELSE 0 END AS "MontoDebeDetalle"
,EV2."CodigoMoneda" AS "IdMoneda"
,EVT."Nombre" AS "Evento"
,EV1."GlosaDetalle"
,MOV."DocumentoIdentidad"
,PORT."CodigoPortafolio"
FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" EV1			
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.Evento" EV2 ON EV1."IdEvento" = EV2."IdEvento"		
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipo" AS EVT ON EVT."IdTipoEvento" = EV2."IdTipoEvento"
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" ASI ON EV1."IdPreComprobanteAsiento" = ASI."IdPreComprobanteAsiento"
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS MOV ON MOV."IdMovimiento" = EV2."IdMovimiento"	
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT ON PORT."IdPortafolio" = MOV."Cuenta"
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS CTA ON CTA."CuentaContabilidad" = ASI."IdCuentaContable"
WHERE EV2."Monto" != 0
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarPrecomprobanteDetalle" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO	
