  select "IdComprobante"  from "BSCL.Facturacion.BD::FAC.Comprobante" 
where "IdComprobante" in ( SELECT "IdComprobanteReferencia" FROM "BSCL.Facturacion.BD::FAC.Comprobante" 
WHERE "IdComprobante"=:IdComprobante ) and "IdTipoComprobante" = 'NDE'; 


