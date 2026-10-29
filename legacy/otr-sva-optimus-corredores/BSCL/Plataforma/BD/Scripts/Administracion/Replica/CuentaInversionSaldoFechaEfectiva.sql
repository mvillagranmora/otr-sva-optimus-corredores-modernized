INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'CuentaInversion_SaldoFechaMovimiento', 'I', 0,  CURRENT_TIMESTAMP  
FROM "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaMovimiento"
WHERE  FECHASALDO = '2022-10-18';

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'CuentaInversion_SaldoFechaEfectiva', 'I', 0,  CURRENT_TIMESTAMP  
FROM bscl."BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaEfectiva"
WHERE  FECHASALDO = '2026-05-23';

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'CuentaInversion_SaldoFechaMovimientoHist', 'I', 0,  CURRENT_TIMESTAMP  
FROM bscl."BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaMovimientoHist"
WHERE  FECHASALDO IN ('2025-12-31')

INSERT 	INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key",
	"Table",	"Action" ,	"Status",	"Stamp" )
	SELECT table_key,	'CuentaInversion_SaldoFechaEfectivaHist',	'I',	0,	CURRENT_TIMESTAMP
FROM bscl."BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaEfectivaHist"
WHERE FECHASALDO IN ('2026-05-23')

SELECT "FechaSaldo" ,COUNT(1) 
FROM "BSCL.CuentaInversion.BD::CTA.SaldoFechaEfectiva" 
--WHERE TO_DATE("FechaSaldo",'YYYY-MM-DD') BETWEEN ADD_DAYS(CURRENT_DATE,- (5 + 1)) AND ADD_DAYS(CURRENT_DATE, -1)
group by "FechaSaldo" 

delete
FROM "BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento" 
where "FechaSaldo"  = '2024-07-02'

SELECT "FechaSaldo" ,COUNT(1) 
FROM "BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento" WHERE TO_DATE("FechaSaldo",'YYYY-MM-DD') >= ADD_DAYS(CURRENT_DATE,- 6)
group by "FechaSaldo" order by "FechaSaldo" desc 


SELECT "FechaSaldo" ,count(1) 
FROM bscl."BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento"  GROUP BY "FechaSaldo" 

SELECT "FechaSaldo" ,count(1) 
FROM bscl."BSCL.CuentaInversion.BD::CTA.SaldoFechaEfectiva"  GROUP BY "FechaSaldo" 

SELECT "FechaSaldo","DocumentoIdentidad" ,"IdPortafolio" ,"SaldoDisponible" 
FROM bscl."BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento"  WHERE "FechaSaldo"  >= '2022-11-24' AND "FechaSaldo" <= '2022-11-27' 
AND "SaldoDisponible" != 0
ORDER BY "NombreCliente","IdPortafolio" 


SELECT TOP 10
    count(1),
    "FechaSaldo",
    sum("SaldoDisponible") AS "SaldoDisponible"
FROM BSCL."BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento"
GROUP BY "FechaSaldo"
ORDER BY "FechaSaldo" DESC;

SELECT "FechaSaldo","DocumentoIdentidad" ,"IdPortafolio" ,"SaldoDisponible",f.*
FROM bscl."BSCL.CuentaInversion.BD::CTA.SaldoFechaEfectiva" f WHERE "DocumentoIdentidad" = '4902242-5' AND "FechaSaldo"  >= '2022-11-24'
ORDER BY f."IdPortafolio" ,f."FechaSaldo" 


-- Drop table

SELECT count(1) FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" WHERE "Table" LIKE 'CuentaInversion_SaldoFechaEfectiva'

SELECT "Table" ,"Status" ,count(1)
FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"
group by "Table" ,"Status" 

truncate table "BSCL.Plataforma.BD::PLA.Replica.WorkStack" 

SELECT *
FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"
where "Parameters" like '%table%'
order by "Stamp" desc


UPDATE "BSCL.Plataforma.BD::PLA.Replica.WorkStack" SET "Stamp" = CURRENT_TIMESTAMP WHERE "Table" LIKE 'CuentaInversion_SaldoFechaMovimiento'


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'CuentaInversion_SaldoFechaMovimiento', 'I', 0,  CURRENT_TIMESTAMP  
FROM "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaMovimiento"
WHERE "FECHASALDO" IN ( '2022-02-01','2022-02-21')

SELECT count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaEfectiva"
WHERE idstack IS NOT NULL

1294260

SELECT FECHASALDO ,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_SaldoFechaMovimientoHist"
WHERE FECHASALDO  >= '2022-11-25'
GROUP BY FECHASALDO 


SELECT count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_MovimientosCuentaInversion"
WHERE idstack IS NOT NULL

SELECT "Table","Action", MAX("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2022-04-11 12:40:00' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2022-04-11' AND
"Table" = 'CuentaInversion_SaldoFechaMovimiento'

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Table" = 'CuentaInversion_SaldoFechaMovimiento'

SELECT *
FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"
where "Parameters" like '%table%'
and "Stamp" >= '2023-09-13'
order by "Stamp" desc

delete from "BSCL.Plataforma.BD::PLA.Replica.WorkStack"




SELECT PARAMETER_NAME,PARAMETER_TYPE,POSITION,DATA_TYPE_NAME,HAS_DEFAULT_VALUE,TABLE_TYPE_NAME 
FROM SYS.PROCEDURE_PARAMETERS WHERE SCHEMA_NAME = 'BSCL' AND PROCEDURE_NAME = 'BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema'
ORDER BY POSITION

select count(1) from "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica" bcibvcmcia




SELECT 'SPOTS',COUNT(1) FROM "BSCL.Mesa.BD::SPT.OperacionSpot" spot
union
select 'TRANSACCIONES',count(1) from "BSCL.Orden.BD::ORD.Transaccion" bobht 
union 
select 'MOVIMIENTOS',count(1) from "BSCL.Facturacion.BD::FAC.MovimientoComprobante" b
union 
select 'ASIGNACIONES',count(1) from "BSCL.Orden.BD::ORD.Asignacion" bo
union
select 'ORDENES',count(1) from "BSCL.Orden.BD::ORD.Orden" bo
union
select 'FFMM',count(1) from "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" bfmbfsar 


select * from "BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialCorredoraAnalitica" bpbvccmcca 
where "IdPerfilComercialBase" = 'MC ARRIESGADO'
and "IdTipoProducto" = 'INAC'






update bscl."BSCL.Plataforma.BD::PLA.Configuracion"
set "Valor" = '2026-05-16'
where "CodigoParametro" = 'PLA.FechaSistema'

