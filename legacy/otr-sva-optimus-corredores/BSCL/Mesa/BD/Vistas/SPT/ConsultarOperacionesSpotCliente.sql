SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperacionesSpotCliente";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperacionesSpotCliente" AS
SELECT
OPE."IdEmpresa",
OPE."IdCliente",
OPE."IdUsuario",
OPE."IdOperacionSpot",
OPE."IdPortafolio",
CASE
WHEN OPE."IdTipoOperacion" = 'COMSPT' THEN 'VENSPT'
WHEN OPE."IdTipoOperacion" = 'VENSPT' THEN 'COMSPT'
END AS "IdTipoOperacion",
CASE
WHEN TIP."Nombre" = 'COMPRA SPOT' THEN 'VENTA SPOT'
WHEN TIP."Nombre" = 'VENTA SPOT' THEN 'COMPRA SPOT'
END AS "NombreTipoOperacion",
OPE."FechaOperacion",
OPE."IdMonedaPrincipal",
OPE."IdMonedaSecundaria",
OPE."MontoMonedaPrincipal",
OPE."MontoMonedaSecundaria",
OPE."TipoCambioCierre",
CAN."Nombre" AS "IdTipoCanalIngreso",
CASE 
WHEN OPE."Estado" = 'ANU' THEN OPE."Estado" 
ELSE
COALESCE (MOV."IdEstadoFacturacion", OPE."EstadoConfirmacion")
END AS "Estado",
CASE
WHEN COALESCE (MOV."IdEstadoFacturacion", OPE."EstadoConfirmacion") = 'PEN' AND OPE."Estado" != 'ANU' THEN 'Pendiente de Autorización'
WHEN MOV."IdEstadoFacturacion" = 'ING' THEN 'Pendiente de Liquidación'
WHEN MOV."IdEstadoFacturacion" = 'FAC' THEN 'Pendiente de Facturación'
WHEN COALESCE (MOV."IdEstadoFacturacion",OPE."Estado") = 'ANU' THEN 'Anulado'
END AS "NombreEstado",
OPE."IdSolicitudAporteRescate"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" OPE
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIP ON OPE."IdTipoOperacion" = TIP."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" CAN ON OPE."IdTipoCanalIngreso" = CAN."IdTipoCanalIngreso"
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV ON OPE."IdOperacionSpot" = MOV."IdTransaccion" AND OPE."IdTipoOperacion" = MOV."IdTipoOperacion" 
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" COD ON OPE."IdOperacionSpot" = COD."IdTransaccion" AND OPE."IdTipoOperacion" = COD."IdTipoOperacion"
WHERE (MOV."IdEstadoFacturacion" IN ('ING','FAC','ANU') AND COALESCE(OPE."Estado",'') NOT IN ('MOD') OR OPE."EstadoConfirmacion" IN ('PEN') AND COALESCE(OPE."Estado",'') NOT IN ('MOD'))
AND COALESCE(COD."IdEstadoComprobanteDetalle",'') != 'ENV';


GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperacionesSpotCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
