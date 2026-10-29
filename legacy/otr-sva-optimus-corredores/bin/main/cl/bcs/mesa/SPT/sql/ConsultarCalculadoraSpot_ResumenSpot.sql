SELECT 
"Nemotecnico"
,SUM ("MontoMonedaPrincipal") AS "TotalUSD"
,SUM ("MontoMonedaSecundaria") AS "TotalClp"
,AVG("TipoCambioCierre")AS "PromedioUtilizado"
,AVG(("PuntaCompra"+"PuntaVenta")/2) AS "PromedioCosto"
,AVG("TipoCambioCierre" -(("PuntaCompra"+"PuntaVenta")/2) )AS "Diferencia"
,SUM(("TipoCambioCierre" -(("PuntaCompra"+"PuntaVenta")/2) )*"MontoMonedaPrincipal"* (CASE 
  WHEN "IdTipoOperacion" = 'COMSPT'AND "ParidadCosto"<1 AND "ParidadCosto">=1  THEN 1 
  WHEN "IdTipoOperacion" = 'COMSPT'AND "ParidadCosto" <1 AND "ParidadCosto"<1  THEN -1
  WHEN "IdTipoOperacion" = 'VENSPT'AND "ParidadCosto"<1 AND "ParidadCosto">=1  THEN -1
  WHEN "IdTipoOperacion" = 'VENSPT'AND "ParidadCosto" <1 AND "ParidadCosto"<1  THEN 1
  WHEN "IdTipoOperacion" = 'VENSPT'AND "ParidadCosto">=1 AND "ParidadCosto">=1  THEN 1
  ELSE -1 END ) ) AS "ValorTotal" 
,SUM ("Margen") AS "MargenFinal"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" 
WHERE "IdMonedaPrincipal" ='USD'
AND "Estado" IN ('VIG','PEL','PDA')
AND TO_DATE("FechaOperacion") = :FechaOperacion
AND "Nemotecnico"=:Nemotecnico 
GROUP BY "Nemotecnico";