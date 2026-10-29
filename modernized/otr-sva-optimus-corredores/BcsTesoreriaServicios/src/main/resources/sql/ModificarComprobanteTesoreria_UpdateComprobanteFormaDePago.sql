UPDATE ing 
SET ing."Monto"= CASE WHEN pas."Campo"='Monto' THEN pas."Valor" ELSE ing."Monto" END
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ing
INNER JOIN #TES_MOD_COMPROBANTE_COMPROBANTEFORMADEPAGO pas ON (pas."IdComprobanteFormaDePago"=ing."IdComprobanteFormaDePago");