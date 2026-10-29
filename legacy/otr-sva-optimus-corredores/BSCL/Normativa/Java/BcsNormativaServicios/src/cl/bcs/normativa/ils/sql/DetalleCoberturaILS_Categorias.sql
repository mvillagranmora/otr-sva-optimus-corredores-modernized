SELECT 
 "CatRie" AS "Categoria"
,"TipoEmisor" 
,MIN("PorcentajeCobertura") AS  "MinPorcentajeCobertura"
,MAX("PorcentajeCobertura") AS  "MaxPorcentajeCobertura"
,SUM("ValorMercado")*1.00   AS  "TotalValorMercado"
FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
WHERE "Fecha" = :FechaContable  AND "TipoValorizacion"='A MERCADO'
AND  "IdEmpresa" = :IdEmpresa AND "ValorMercado" > 0
GROUP BY "TipoEmisor" ,"CatRie"