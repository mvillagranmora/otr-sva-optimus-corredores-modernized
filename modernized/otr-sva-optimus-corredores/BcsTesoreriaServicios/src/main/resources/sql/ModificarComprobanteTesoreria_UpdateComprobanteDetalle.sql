UPDATE ing 
SET ing."Monto"= CASE WHEN pas."Campo"='Monto' THEN pas."Valor" ELSE ing."Monto" END
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ing
INNER JOIN #TES_MOD_COMPROBANTE_COMPROBANTEDETALLE pas ON (pas."IdComprobanteDetalle"=ing."IdComprobanteDetalle");