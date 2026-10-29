UPDATE ing 
SET ing."Monto"= CASE WHEN pas."Campo"='Monto' THEN pas."Valor" ELSE ing."Monto" END
FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" ing
INNER JOIN #TES_MOD_COMPROBANTE_INGRESOEGRESOFORMADEPAGO pas ON (pas."IdIngresoEgresoFormaDePago"=ing."IdIngresoEgresoFormaDePago");