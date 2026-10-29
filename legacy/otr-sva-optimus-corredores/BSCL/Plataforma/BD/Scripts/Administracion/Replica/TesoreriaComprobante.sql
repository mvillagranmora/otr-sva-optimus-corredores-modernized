

SELECT DISTINCT "NombreEstado"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteTesoreriaAnalitica"

SELECT * FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteTesoreriaAnalitica"
WHERE "IdComprobante" = 50955

SELECT * FROM "BSCL.Tesoreria.BD::TES.Comprobante"
WHERE "IdComprobante" = 50955

SELECT * FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
WHERE "IdComprobante" = 50955

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-09-07'
 AND "Status" = 0
-- AND "Action" = 'D'
AND "Table" LIKE '%Tesoreria_Comprobante%'


SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-09-08' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT MIN("IdComprobante")|| '' AS MIN,MAX("IdComprobante")||'' AS MAX,
FLOOR("IdComprobante"/1000) AS GRUPO,COUNT(1)*1.0 as C,
1.0*SUM("IdComprobante" + "Monto" + "MontoIngreso" + "MontoEgreso" + CASE "NombreEstado" 
WHEN 'ANULADO' THEN 98 WHEN 'LIQUIDADO' THEN 777 ELSE 3 END) AS HASH 
FROM "_SYS_BIC"."BSCL.Tesoreria.BD.Analytics/Comprobante" 
WHERE FLOOR("IdComprobante"/1000) = 0 OR 1 = 1 GROUP BY FLOOR("IdComprobante"/1000) 


SELECT * FROM 
_sys_bic."BSCL.Tesoreria.BD.Analytics/Comprobante"
WHERE "IdComprobante" = 52890

SELECT * FROM 
_sys_bic."BSCL.Tesoreria.BD.Analytics/ComprobanteFormaDePago"
WHERE "IdComprobante" = 52890