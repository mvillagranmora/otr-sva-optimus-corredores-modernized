SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePago"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePago" As
(SELECT		
    "CmbteFPag"."IdComprobanteFormaDePago",
    "CmbteFPag"."IdComprobante",
    "CmbteFPag"."IdMedioDePago",
    "CmbteFPag"."NombreMedioDePago",
    "CmbteFPag"."NumeroSerie",
    "CmbteFPag"."NumeroDocumento",
    "CmbteFPag"."IdCuentaBancaria",
    "CmbteFPag"."NombreCuentaBancaria",
    "CmbteFPag"."IdTipoCuentaBancaria",
    "CmbteFPag"."NombreTipoCuentaBancaria",
    "CmbteFPag"."NumeroCuentaBancaria",
    "CmbteFPag"."IdTarjetaBancaria",
    "CmbteFPag"."IdTipoTarjetaBancaria",
    "CmbteFPag"."NombreTipoTarjetaBancaria",
    "CmbteFPag"."NumeroTarjetaBancaria",
    "CmbteFPag"."FechaVctoTarjetaBancaria",
    "CmbteFPag"."IdModalidadDePago",
    "CmbteFPag"."NombreModalidadDePago",
    "CmbteFPag"."IdBanco",
    "CmbteFPag"."CodigoBanco",
    "CmbteFPag"."NombreBanco",
    "CmbteFPag"."IdMoneda",
    "CmbteFPag"."FechaDocumentoPago",      
    "CmbteFPag"."MontoLiquidacionFormaDePago",
    "CmbteFPag"."IdEstado", 
    "CmbteFPag"."FechaIngresoRegistro"
        
From "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago" As "CmbteFPag"
    LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioDePago" ON ("MedioDePago"."IdMedioDePago" = "CmbteFPag"."IdMedioDePago")
    LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS "EstadoFacturacion_$2" ON ("EstadoFacturacion_$2"."IdEstado" = "CmbteFPag"."IdEstado")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$3" ON ("Estado_$3"."IdEstado" = "EstadoFacturacion_$2"."IdEstado")  
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda_$4" ON ("Moneda_$4"."IdMoneda" = "CmbteFPag"."IdMoneda")
WHERE Upper("EstadoFacturacion_$2"."NombreEntidad") = Upper('FAC.ComprobanteFormaDePago')
	UNION
(SELECT	
    "CmbteFPag"."IdComprobanteFormaDePago",
    com."IdComprobante",
    CASE WHEN "MovimientoComprobante"."ActualizarTesoreria" = 'N' AND "MovimientoComprobante"."ActualizarCuentaInversion" = 'S' THEN 'CIN' 
    	 WHEN "MovimientoComprobante"."ActualizarTesoreria" = 'S' AND MvtoCmbteFPago."NombreMedioDePago" IS NOT NULL THEN MvtoCmbteFPago."IdMedioDePago" 
    	 ELSE  'SIN DEFINIR' END AS "IdMedioDePago",
    CASE WHEN "MovimientoComprobante"."ActualizarTesoreria" = 'N' AND "MovimientoComprobante"."ActualizarCuentaInversion" = 'S' THEN 'CUENTA INVERSION'
    	 WHEN "MovimientoComprobante"."ActualizarTesoreria" = 'S' AND MvtoCmbteFPago."NombreMedioDePago" IS NOT NULL THEN MvtoCmbteFPago."NombreMedioDePago"    	 
    	 ELSE  'SIN DEFINIR' END AS "NombreMedioDePago", 
    "CmbteFPag"."NumeroSerie",
    "CmbteFPag"."NumeroDocumento",
    "CmbteFPag"."IdCuentaBancaria",
    "CmbteFPag"."NombreCuentaBancaria",
    "CmbteFPag"."IdTipoCuentaBancaria",
    "CmbteFPag"."NombreTipoCuentaBancaria",
    "CmbteFPag"."NumeroCuentaBancaria",
    "CmbteFPag"."IdTarjetaBancaria",
    "CmbteFPag"."IdTipoTarjetaBancaria",
    "CmbteFPag"."NombreTipoTarjetaBancaria",
    "CmbteFPag"."NumeroTarjetaBancaria",
    "CmbteFPag"."FechaVctoTarjetaBancaria",
    "CmbteFPag"."IdModalidadDePago",
    "CmbteFPag"."NombreModalidadDePago",
    "CmbteFPag"."IdBanco",
    "CmbteFPag"."CodigoBanco",
    "CmbteFPag"."NombreBanco",
    "CmbteFPag"."IdMoneda",
    "CmbteFPag"."FechaDocumentoPago",      
    "CmbteFPag"."MontoLiquidacionFormaDePago",
    "CmbteFPag"."IdEstado", 
    "CmbteFPag"."FechaIngresoRegistro"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" com
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago" AS "CmbteFPag" ON com."IdComprobante"="CmbteFPag"."IdComprobante"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" AS "MovimientoComprobante" ON ("MovimientoComprobante"."IdComprobante" = com."IdComprobante")
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" AS MvtoCmbteFPago ON MvtoCmbteFPago."IdMovimientoComprobante" = "MovimientoComprobante"."IdMovimientoComprobante"
WHERE "CmbteFPag"."IdComprobanteFormaDePago" IS NULL));   
    
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;     
