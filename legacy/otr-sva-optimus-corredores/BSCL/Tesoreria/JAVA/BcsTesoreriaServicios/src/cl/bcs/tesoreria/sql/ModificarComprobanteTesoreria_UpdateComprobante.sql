UPDATE ing 
SET ing."Monto"= CASE WHEN pas."Campo"='Monto' THEN pas."Valor" ELSE ing."Monto" END
FROM "BSCL.Tesoreria.BD::TES.Comprobante"  ing
INNER JOIN #TES_MOD_COMPROBANTE_COMPROBANTE pas ON (pas."IdComprobante"=ing."IdComprobante");