INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'FondoMutuo_SolicitudAporteRescateFondoMutuo', 'A', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SolicitudAporteRescateFondoMutuo"
WHERE FECHAVALORCUOTA in ('2022-04-02','2022-04-03')

SELECT FECHAVALORCUOTA ,FECHAEMISION,*
FROM "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SolicitudAporteRescateFondoMutuo"
WHERE table_key = 13129470

SELECT FECHAVALORCUOTA ,FECHAEMISION,*
FROM "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SolicitudAporteRescateFondoMutuo"
WHERE FECHAVALORCUOTA  = '2022-04-03'

SELECT *
FROM "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SolicitudAporteRescateFondoMutuo" fs  WHERE FECHAVALORCUOTA  = '2022-04-02'

--46 minutos prueba desde hana 1.0


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','solicitud', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:FondoMutuo_SolicitudAporteRescateFondoMutuo;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;

12495819
12495841
12495836
12495835
12495834

DELETE FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"

SELECT count(1)
FROM bscl."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateAnalitica" 

SELECT *
FROM bscl."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateAnalitica" 
WHERE "MontoMaximo" >= 90000000000000.000000


SELECT "IdSolicitudAporteRescate","Estado","SolicitudAporteRescate"."ValorCuota" ,"ValorCuota"."ValorCuota"
FROM bscl."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
LEFT OUTER  JOIN bscl."BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo" )
LEFT OUTER  JOIN bscl."BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON ("ValorCuota"."IdSerieFondo" = "SerieDeFondo"."IdSerieFondo")
WHERE "IdSolicitudAporteRescate" >= 11337712 AND "IdSolicitudAporteRescate" < 11337996


SELECT "Table","Action", max("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2022-07-23 00:00:00' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"

UPDATE  BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
SET "Stamp" = '2020-08-30 09:32:11'
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
AND "Table" = 'FondoMutuo_SolicitudAporteRescateFondoMutuo'

SELECT "FechaRespaldo",count(1)
FROM bscl."BSCL.Custodia.BD::HIS.Cartera"
GROUP BY "FechaRespaldo"
ORDER BY "FechaRespaldo"


SELECT MIN("IdTraspaso")|| '' AS MIN,MAX("IdTraspaso")||'' AS MAX,FLOOR("IdTraspaso"/10000) AS GRUPO,COUNT(1)*1.0 as C,1.0*SUM("IdTraspaso" + "IdSolicitudAporteRescate" + LENGTH("IdTipoMovimientoTraspaso")) AS HASH FROM BSCL."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica" WHERE FLOOR("IdTraspaso"/10000) = 0 OR 1 = 1 GROUP BY FLOOR("IdTraspaso"/10000) 

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SolicitudAporteRescateFondoMutuo"



SELECT COUNT(1)
FROM bscl."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateAnalitica"
WHERE "MontoMonedaSistema" = 0
AND "FechaAceptacion" >= '2020-09-09'






SELECT "IdEstado","INGCantidad",* FROM 
"BSCL"."BSCL.Custodia.BD::HIS.Cartera"
WHERE "FechaRespaldo" = '2020-09-30'
AND "IdPortafolio" = 69051
AND "IdInstrumento" = 252790


SELECT "IdEstado","INGCantidad","INGFechaOperacion","INGFechaLiquidacion",* FROM 
"BSCL"."BSCL.Custodia.BD::HIS.Garantia"
WHERE "FechaRespaldo" = '2020-09-30'
AND "IdPortafolio" = 69051
AND "IdInstrumento" = 252790

    SELECT * FROM "BSCL.Orden.BD::MRV.INDICES_RV"


   
    
    

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','FondoMutuo_SolicitudAporteRescateFondoMutuo', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:FondoMutuo_SolicitudAporteRescateFondoMutuo;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;