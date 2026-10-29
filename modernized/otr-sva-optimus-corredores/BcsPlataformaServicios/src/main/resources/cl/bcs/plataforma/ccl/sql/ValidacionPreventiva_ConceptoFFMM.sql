SELECT "CodigoConcepto" AS "Valor1" 
FROM "BSCL.FondoMutuo.BD::FMM.Concepto"
WHERE ("NombreConcepto" IS NULL OR "NombreConcepto" = '');