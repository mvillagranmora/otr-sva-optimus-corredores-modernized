SELECT ("IdFolioCSC" + "MontoPesos" + "MontoUF" + "IdPortafolio"),*
FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/ContratoSuscripcionCuotas"
WHERE "IdFolioCSC" >= 1448000 AND "IdFolioCSC" <= 1448099
ORDER BY "IdFolioCSC"

SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-09-10' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"


SELECT ("IdFolioCSC" + "MontoPesos" + "MontoUF" + "IdPortafolio" + CASE "IdEstado" WHEN 'RVC' THEN 11 WHEN 'ANU' THEN 12 WHEN 'ING' THEN 13 WHEN 'RZD' THEN 14 ELSE 99 END) AS HASH,* 
FROM _SYS_BIC."BSCL.FondoMutuo.BD.Analytics/ContratoSuscripcionCuotas" 
WHERE FLOOR("IdFolioCSC"/10) = 137087