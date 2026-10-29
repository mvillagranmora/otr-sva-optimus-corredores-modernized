INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT TABLE_KEY,'Tesoreria_ComprobanteFormaDePago', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL"."BSCL.Replica.BD.Vistas::ReplicaTesoreria_ComprobanteFormaDePago";


SELECT "Table","Action", max("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-10-19' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"
ORDER BY max("Stamp") desc


SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-10-15' AND
"Status" = 0 AND "Table" = 'Custodia_CustodiaFechaOperacion'
-- AND "Action" = 'D'


DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Table" = 'Custodia_CustodiaFechaLiquidacion' AND "Status" = 0

Custodia_CustodiaFechaLiquidacion	I	2020-10-13 09:29:59	553609


SELECT * FROM 
"BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoAnalitica"
WHERE "FechaLiquidacion" = '2020-10-13'




SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Stamp" >= '2020-10-13'
ORDER BY "Stamp" DESC


SELECT * FROM BSCL."BSCL."
