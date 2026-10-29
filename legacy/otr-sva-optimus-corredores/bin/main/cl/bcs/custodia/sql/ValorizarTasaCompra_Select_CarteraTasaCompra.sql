SELECT 
 CAR."IdCartera"
,CAR."TasaCompra"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN  #CarteraTasaCompra AS TEMP ON TEMP."IdCartera" = CAR."IdCartera"   

