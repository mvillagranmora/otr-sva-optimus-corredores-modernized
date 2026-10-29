SELECT COUNT(*) AS "Validacion"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS f_com
INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS f_det ON f_det."IdComprobante" = f_com."IdComprobante"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS por ON f_com."IdPortafolio" = por."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS ind ON ind."IdEntidad" = por."IdPortafolio"
WHERE f_com."IdPortafolio" = :IdPortafolio
AND f_com."IdComprobante" = :IdComprobante	
AND f_det."IdClasificacionTipoInstrumento" = 'RV'
AND ind."IdTipoIndicador" = 19
AND ind."ValorIndicador" = 'S';
