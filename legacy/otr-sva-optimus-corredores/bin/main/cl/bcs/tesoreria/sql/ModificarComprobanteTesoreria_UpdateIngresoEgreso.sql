UPDATE ing 
SET ing."Monto"= CASE WHEN pas."Campo"='Monto' THEN pas."Valor" ELSE ing."Monto" END
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" ing
INNER JOIN #TES_MOD_COMPROBANTE_INGRESOEGRESO pas ON (pas."IdIngresoEgreso"=ing."IdIngresoEgreso");