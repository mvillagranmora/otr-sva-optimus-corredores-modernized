SELECT DISTINCT 
"Ejercicio" AS EJERCICIO
,"Nemo" AS NEMO
,TO_VARCHAR("FechaPago") AS FEC_PAGO
,"Mercado" AS MERCADO
--,"IdOrigen" AS IDORIGEN
FROM "BSCL.Normativa.BD.Vistas::TRI.ConsultarCalificacionTributaria"
WHERE "Ejercicio"= :EJERCICIO ORDER BY NEMO;