INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'SocioNegocio_IndicadorPortafolio', 'I', 0,  CURRENT_TIMESTAMP  
FROM "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorPortafolio"
WHERE IdTipoIndicador in   (20102)

SELECT "IdTipoIndicador",count(1)
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
GROUP BY "IdTipoIndicador"
ORDER BY "IdTipoIndicador"

SELECT "Table","Action", max("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-12-10' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT "IdTipoIndicador",count(1)
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
GROUP BY "IdTipoIndicador"
ORDER BY "IdTipoIndicador"

SELECT * FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
WHERE "IdPortafolio" = 338050

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','INDICADORPORTAFOLIO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:SocioNegocio_IndicadorPortafolio;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdPortafolio" || ':' || "IdTipoIndicador" AS varchar(200)),'SocioNegocio_IndicadorPortafolio', 'I', 0,  CURRENT_TIMESTAMP  
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
WHERE "IdTipoIndicador" >= 36 AND "IdTipoIndicador" <= 1050


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdPortafolio" || ':' || "IdTipoIndicador" AS varchar(200)),'SocioNegocio_IndicadorPortafolio', 'I', 0,  CURRENT_TIMESTAMP  
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
WHERE "IdTipoIndicador" >= 1052 AND "IdTipoIndicador" <= 20040


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdPortafolio" || ':' || "IdTipoIndicador" AS varchar(200)),'SocioNegocio_IndicadorPortafolio', 'I', 0,  CURRENT_TIMESTAMP  
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
WHERE "IdTipoIndicador" >= 20041

8842000

1050

SELECT "IdTipoIndicador",count(1) FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" 
GROUP BY "IdTipoIndicador"


--26534388
SELECT COUNT(1) 
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
WHERE "IdTipoIndicador" = 34


SELECT "IdTipoIndicador",COUNT(1) 
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
GROUP BY "IdTipoIndicador"
ORDER BY "IdTipoIndicador"

SELECT "IdPortafolio",COUNT(1) 
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
GROUP BY "IdPortafolio"
ORDER BY "IdPortafolio"

SELECT "IdPortafolio",count(1)
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
WHERE "IdPortafolio" >= 80000 AND "IdPortafolio" <= 80999
GROUP BY "IdPortafolio"
HAVING count(1) != 62
ORDER BY "IdPortafolio"

SELECT "Table","Action", max("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-11-30' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-11-28' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" = 'CuentaInversion_SaldoFechaMovimiento'

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-20' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" IN ('SocioNegocio_IndicadorPortafolio')




SELECT MIN("IdPortafolio")|| '' AS MIN,MAX("IdPortafolio")||'' AS MAX,
FLOOR("IdPortafolio"/10) AS GRUPO,COUNT(1)*1.0 as C,1.0*SUM("IdPortafolio" + CASE WHEN "ValorIndicador" = 'S' 
THEN 13 WHEN "ValorIndicador" = 'N' THEN 5 WHEN "ValorIndicador" IS NULL THEN 0 ELSE LENGTH("ValorIndicador") 
END + LENGTH("CodigoTipoIndicador")) AS HASH 
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" 
WHERE FLOOR("IdPortafolio"/10) = 1 OR 1 = 1 GROUP BY FLOOR("IdPortafolio"/10) 
ORDER BY FLOOR("IdPortafolio"/10)


SELECT 1.0*("IdPortafolio" + CASE WHEN "ValorIndicador" = 'S' 
THEN 13 WHEN "ValorIndicador" = 'N' THEN 5 WHEN "ValorIndicador" IS NULL THEN 0 ELSE LENGTH("ValorIndicador") 
END + LENGTH("CodigoTipoIndicador")) AS HASH ,*
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" 
WHERE FLOOR("IdPortafolio"/10) = 21141 OR 1 = 0 
ORDER BY "IdCliente","IdPortafolio","IdTipoIndicador"

SELECT MIN("IdPortafolio")|| '' AS MIN,MAX("IdPortafolio")||'' AS MAX,FLOOR("IdPortafolio"/10) AS 
GRUPO,COUNT(1)*1.0 as C,1.0*SUM("IdPortafolio" + CASE WHEN "ValorIndicador" = 'S' THEN 13 WHEN 
"ValorIndicador" = 'N' THEN 5 WHEN "ValorIndicador" IS NULL THEN 0 ELSE LENGTH("ValorIndicador") 
END + LENGTH("CodigoTipoIndicador")) AS HASH 
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" 
WHERE FLOOR("IdPortafolio"/10) = 0 OR 1 = 0 GROUP BY FLOOR("IdPortafolio"/10) 

SELECT ("IdPortafolio" + CASE WHEN "ValorIndicador" = 'S' THEN 13 WHEN 
"ValorIndicador" = 'N' THEN 5 WHEN "ValorIndicador" IS NULL THEN 0 ELSE LENGTH("ValorIndicador") 
END + LENGTH("CodigoTipoIndicador")) AS HASH, *
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" 
WHERE FLOOR("IdPortafolio"/10) = 43339 OR 1 = 0
ORDER BY "IdPortafolio","IdTipoIndicador"

SELECT ("IdPortafolio" + CASE WHEN "ValorIndicador" = 'S' THEN 13 WHEN 
"ValorIndicador" = 'N' THEN 5 WHEN "ValorIndicador" IS NULL THEN 0 ELSE LENGTH("ValorIndicador") 
END + LENGTH("CodigoTipoIndicador")) AS HASH ,*
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" 
WHERE FLOOR("IdPortafolio"/10) =0 OR 1 = 0 
ORDER BY "IdPortafolio","IdTipoIndicador"



SELECT TOP 10 *
FROM "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorPortafolio"
WHERE IdStack IS NOT NULL

SELECT count(1) FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" 

SELECT "IdEntidad","IdTipoIndicador",count(1) 
FROM BSCL."BSCL.Plataforma.BD::IND.Indicador" 
WHERE "IdTipoIndicador" = 34
GROUP BY "IdEntidad","IdTipoIndicador"
HAVING count(1) > 1

SELECT *,1.0*("IdPortafolio" + CASE WHEN "ValorIndicador" = 'S' THEN 13 WHEN "ValorIndicador" = 'N' THEN 5 WHEN "ValorIndicador" IS NULL THEN 0 ELSE LENGTH("ValorIndicador") END + LENGTH("CodigoTipoIndicador")) AS HASH 
FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" 
WHERE (FLOOR("IdPortafolio"/10) = 2) ORDER BY "IdCliente" desc,"IdPortafolio" desc,"CodigoTipoIndicador" desc

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','INDICADORPORTAFOLIO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:SocioNegocio_IndicadorPortafolio;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;


SELECT "IdTipoIndicador",count(1) FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica" 
GROUP BY  "IdTipoIndicador"

SELECT * FROM BSCL."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
WHERE  "IdPortafolio" = 412870


SELECT "Table","Action", count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-07-16 18:00:00' AND
"Action" = 'I' and
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"


UPDATE BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack" SET "Stamp" = '2021-07-17 09:50:00'
WHERE "Action" = 'I' AND "Status" = 0 AND "Table" = 'SocioNegocio_IndicadorPortafolio'



SELECT * FROM "BSCL"."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" WHERE "IdOperacion" = 216666


