INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT top 1 table_key,'CuentaInversion_MovimientosCuentaInversion', 'I', 0,  CURRENT_TIMESTAMP  
FROM "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_MovimientosCuentaInversion"

select top 1 * from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" brbvst

select top 1 * from "BSCL.Replica.BD.Vistas::ReplicaOrden_Orden" brbvst

select count(1) from 
"BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_MovimientosCuentaInversion" brbvst

truncate table "BSCL.Plataforma.BD::PLA.Replica.WorkStack" 

SELECT "IdCargoAbonoMovimiento","Descripcion",length("Descripcion") 
from "_SYS_BIC"."BSCL.CuentaInversion.BD.Analytics/MovimientosCuentaInversion" WHERE  (("IdCargoAbonoMovimiento" >= 4219481 and "IdCargoAbonoMovimiento" <= 4219489) OR ("IdCargoAbonoMovimiento" >= 4219810 and "IdCargoAbonoMovimiento" <= 4219819) OR ("IdCargoAbonoMovimiento" >= 4219403 and "IdCargoAbonoMovimiento" <= 4219409) OR ("IdCargoAbonoMovimiento" >= 4219440 and "IdCargoAbonoMovimiento" <= 4219449) OR ("IdCargoAbonoMovimiento" >= 4219610 and "IdCargoAbonoMovimiento" <= 4219619) OR ("IdCargoAbonoMovimiento" >= 4220271 and "IdCargoAbonoMovimiento" <= 4220279) OR ("IdCargoAbonoMovimiento" >= 4218990 and "IdCargoAbonoMovimiento" <= 4218999) OR ("IdCargoAbonoMovimiento" >= 4219320 and "IdCargoAbonoMovimiento" <= 4219329) OR ("IdCargoAbonoMovimiento" >= 4213580 and "IdCargoAbonoMovimiento" <= 4213589) OR ("IdCargoAbonoMovimiento" >= 4213790 and "IdCargoAbonoMovimiento" <= 4213799) OR ("IdCargoAbonoMovimiento" >= 4219577 and "IdCargoAbonoMovimiento" <= 4219579) OR ("IdCargoAbonoMovimiento" >= 4206150 and "IdCargoAbonoMovimiento" <= 4206159) OR ("IdCargoAbonoMovimiento" >= 4219280 and "IdCargoAbonoMovimiento" <= 4219289) OR ("IdCargoAbonoMovimiento" >= 4219450 and "IdCargoAbonoMovimiento" <= 4219459) OR ("IdCargoAbonoMovimiento" >= 4218500 and "IdCargoAbonoMovimiento" <= 4218509) OR ("IdCargoAbonoMovimiento" >= 4219490 and "IdCargoAbonoMovimiento" <= 4219499) OR ("IdCargoAbonoMovimiento" >= 4219820 and "IdCargoAbonoMovimiento" <= 4219828) OR ("IdCargoAbonoMovimiento" >= 4217140 and "IdCargoAbonoMovimiento" <= 4217143) OR ("IdCargoAbonoMovimiento" >= 4219413 and "IdCargoAbonoMovimiento" <= 4219419) OR ("IdCargoAbonoMovimiento" >= 4219290 and "IdCargoAbonoMovimiento" <= 4219299) OR ("IdCargoAbonoMovimiento" >= 4219000 and "IdCargoAbonoMovimiento" <= 4219009) OR ("IdCargoAbonoMovimiento" >= 4219330 and "IdCargoAbonoMovimiento" <= 4219339) OR ("IdCargoAbonoMovimiento" >= 4211980 and "IdCargoAbonoMovimiento" <= 4211989) OR ("IdCargoAbonoMovimiento" >= 4219216 and "IdCargoAbonoMovimiento" <= 4219219) OR ("IdCargoAbonoMovimiento" >= 4211900 and "IdCargoAbonoMovimiento" <= 4211909) OR ("IdCargoAbonoMovimiento" >= 4218300 and "IdCargoAbonoMovimiento" <= 4218309) OR ("IdCargoAbonoMovimiento" >= 4206160 and "IdCargoAbonoMovimiento" <= 4206169) OR ("IdCargoAbonoMovimiento" >= 4217850 and "IdCargoAbonoMovimiento" <= 4217859) OR ("IdCargoAbonoMovimiento" >= 4211780 and "IdCargoAbonoMovimiento" <= 4211789) OR ("IdCargoAbonoMovimiento" >= 4219464 and "IdCargoAbonoMovimiento" <= 4219469) OR ("IdCargoAbonoMovimiento" >= 4218180 and "IdCargoAbonoMovimiento" <= 4218189) OR ("IdCargoAbonoMovimiento" >= 4219790 and "IdCargoAbonoMovimiento" <= 4219799) OR ("IdCargoAbonoMovimiento" >= 4217890 and "IdCargoAbonoMovimiento" <= 4217899) OR ("IdCargoAbonoMovimiento" >= 4219500 and "IdCargoAbonoMovimiento" <= 4219505) OR ("IdCargoAbonoMovimiento" >= 4218220 and "IdCargoAbonoMovimiento" <= 4218229) OR ("IdCargoAbonoMovimiento" >= 4218550 and "IdCargoAbonoMovimiento" <= 4218559) OR ("IdCargoAbonoMovimiento" >= 4218100 and "IdCargoAbonoMovimiento" <= 4218109) OR ("IdCargoAbonoMovimiento" >= 4218430 and "IdCargoAbonoMovimiento" <= 4218439) OR ("IdCargoAbonoMovimiento" >= 4217810 and "IdCargoAbonoMovimiento" <= 4217819) OR ("IdCargoAbonoMovimiento" >= 4219423 and "IdCargoAbonoMovimiento" <= 4219429) OR ("IdCargoAbonoMovimiento" >= 4218140 and "IdCargoAbonoMovimiento" <= 4218149) OR ("IdCargoAbonoMovimiento" >= 4215130 and "IdCargoAbonoMovimiento" <= 4215132) OR ("IdCargoAbonoMovimiento" >= 4219300 and "IdCargoAbonoMovimiento" <= 4219309) OR ("IdCargoAbonoMovimiento" >= 4218020 and "IdCargoAbonoMovimiento" <= 4218029) OR ("IdCargoAbonoMovimiento" >= 4215797 and "IdCargoAbonoMovimiento" <= 4215799) OR ("IdCargoAbonoMovimiento" >= 4218350 and "IdCargoAbonoMovimiento" <= 4218359) OR ("IdCargoAbonoMovimiento" >= 4219010 and "IdCargoAbonoMovimiento" <= 4219019) OR ("IdCargoAbonoMovimiento" >= 4219340 and "IdCargoAbonoMovimiento" <= 4219349) OR ("IdCargoAbonoMovimiento" >= 4218060 and "IdCargoAbonoMovimiento" <= 4218069) OR ("IdCargoAbonoMovimiento" >= 4211990 and "IdCargoAbonoMovimiento" <= 4211999) OR ("IdCargoAbonoMovimiento" >= 4218390 and "IdCargoAbonoMovimiento" <= 4218399) OR ("IdCargoAbonoMovimiento" >= 4219220 and "IdCargoAbonoMovimiento" <= 4219229) OR ("IdCargoAbonoMovimiento" >= 4217940 and "IdCargoAbonoMovimiento" <= 4217949) OR ("IdCargoAbonoMovimiento" >= 4218270 and "IdCargoAbonoMovimiento" <= 4218279) OR ("IdCargoAbonoMovimiento" >= 4217980 and "IdCargoAbonoMovimiento" <= 4217989) OR ("IdCargoAbonoMovimiento" >= 4218310 and "IdCargoAbonoMovimiento" <= 4218319) OR ("IdCargoAbonoMovimiento" >= 4217860 and "IdCargoAbonoMovimiento" <= 4217869) OR ("IdCargoAbonoMovimiento" >= 4219471 and "IdCargoAbonoMovimiento" <= 4219479) OR ("IdCargoAbonoMovimiento" >= 4218190 and "IdCargoAbonoMovimiento" <= 4218199) OR ("IdCargoAbonoMovimiento" >= 4219800 and "IdCargoAbonoMovimiento" <= 4219809) OR ("IdCargoAbonoMovimiento" >= 4218520 and "IdCargoAbonoMovimiento" <= 4218529) OR ("IdCargoAbonoMovimiento" >= 4217900 and "IdCargoAbonoMovimiento" <= 4217909) OR ("IdCargoAbonoMovimiento" >= 4219510 and "IdCargoAbonoMovimiento" <= 4219515) OR ("IdCargoAbonoMovimiento" >= 4218230 and "IdCargoAbonoMovimiento" <= 4218239) OR ("IdCargoAbonoMovimiento" >= 4219392 and "IdCargoAbonoMovimiento" <= 4219398) OR ("IdCargoAbonoMovimiento" >= 4218110 and "IdCargoAbonoMovimiento" <= 4218119) OR ("IdCargoAbonoMovimiento" >= 4218440 and "IdCargoAbonoMovimiento" <= 4218449) OR ("IdCargoAbonoMovimiento" >= 4218771 and "IdCargoAbonoMovimiento" <= 4218773) OR ("IdCargoAbonoMovimiento" >= 4217820 and "IdCargoAbonoMovimiento" <= 4217829) OR ("IdCargoAbonoMovimiento" >= 4219430 and "IdCargoAbonoMovimiento" <= 4219439) OR ("IdCargoAbonoMovimiento" >= 4218150 and "IdCargoAbonoMovimiento" <= 4218159) OR ("IdCargoAbonoMovimiento" >= 4218980 and "IdCargoAbonoMovimiento" <= 4218989) OR ("IdCargoAbonoMovimiento" >= 4219313 and "IdCargoAbonoMovimiento" <= 4219319) OR ("IdCargoAbonoMovimiento" >= 4218030 and "IdCargoAbonoMovimiento" <= 4218039) OR ("IdCargoAbonoMovimiento" >= 4219641 and "IdCargoAbonoMovimiento" <= 4219649) OR ("IdCargoAbonoMovimiento" >= 4218360 and "IdCargoAbonoMovimiento" <= 4218369) OR ("IdCargoAbonoMovimiento" >= 4213570 and "IdCargoAbonoMovimiento" <= 4213579) OR ("IdCargoAbonoMovimiento" >= 4219350 and "IdCargoAbonoMovimiento" <= 4219356) OR ("IdCargoAbonoMovimiento" >= 4218070 and "IdCargoAbonoMovimiento" <= 4218079) OR ("IdCargoAbonoMovimiento" >= 4212000 and "IdCargoAbonoMovimiento" <= 4212009) OR ("IdCargoAbonoMovimiento" >= 4218400 and "IdCargoAbonoMovimiento" <= 4218409) OR ("IdCargoAbonoMovimiento" >= 4213780 and "IdCargoAbonoMovimiento" <= 4213789) OR ("IdCargoAbonoMovimiento" >= 4219230 and "IdCargoAbonoMovimiento" <= 4219236) OR ("IdCargoAbonoMovimiento" >= 4217950 and "IdCargoAbonoMovimiento" <= 4217959) OR ("IdCargoAbonoMovimiento" >= 4218280 and "IdCargoAbonoMovimiento" <= 4218289) OR ("IdCargoAbonoMovimiento" >= 4217990 and "IdCargoAbonoMovimiento" <= 4217999) OR ("IdCargoAbonoMovimiento" >= 4218320 and "IdCargoAbonoMovimiento" <= 4218329))
and length("Descripcion") >= 100

SELECT 
ws."Action" as A,ws."IdStack" as I, ws."Key" as K, ws."Parameters" as P,tab.*
FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" ws
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica" tab 
on ws."Table"  = 'CuentaInversion_MovimientosCuentaInversion' and ws."Key" = tab."IdCargoAbonoMovimiento"

SELECT 
ws."Action" as A,ws."IdStack" as I, ws."Key" as K, ws."Parameters" as P,tab.*
FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" ws
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica" tab 
on ws."Table"  = 'CuentaInversion_MovimientosCuentaInversion' and ws."Key" = tab."IdCargoAbonoMovimiento"
WHERE tab."IdCargoAbonoMovimiento" is not null and
"Action" in ('A','I','M')
and "Job" = 8379062
--and "Status" = 0
--and "Stamp" < ADD_SECONDS(CURRENT_TIMESTAMP,3000)
--and "IdStack" >= ? and "IdStack" <= ?
ORDER BY "Action","Key"


SELECT "Table", count(1) as count,min("IdStack") as "Min",max("IdStack") as "Max"
FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" < ADD_SECONDS(CURRENT_TIMESTAMP,100) AND
"Status" = 0 AND
"Job" = 0
GROUP BY "Table"

SELECT COUNT(1) FROM  "_SYS_BIC"."BSCL.CuentaInversion.BD.Analytics/MovimientosCuentaInversion"

--DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"

SELECT "Table","Action", max("Stamp"),"Status",count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-10-02' 
AND "Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action","Status"

SELECT "Table","Action", *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-09-04 17:45:00' 
--AND "Status" = 0
-- AND "Action" = 'D'
AND "Table" = 'CuentaInversion_MovimientosCuentaInversion'

SELECT "Table","Action",COUNT(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-09-04 17:45:00' 
--AND "Status" = 0
-- AND "Action" = 'D'
AND "Table" = 'CuentaInversion_MovimientosCuentaInversion'
GROUP BY "Table","Action"


SELECT ("IdCargoAbonoMovimiento" + "IdCuentaInversion" + "Monto" + "SaldoPorFechaLiquidacion" + 
"SaldoPorFechaMovimiento") AS HASH,* 
FROM "_SYS_BIC"."BSCL.CuentaInversion.BD.Analytics/MovimientosCuentaInversion" 
WHERE "IdCargoAbonoMovimiento" >= 4219481 and "IdCargoAbonoMovimiento" <= 4219489


SELECT ("IdCargoAbonoMovimiento" + "IdCuentaInversion" + "Monto" + "SaldoPorFechaLiquidacion" + "SaldoPorFechaMovimiento") AS HASH,*
FROM BSCL."BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica" 
WHERE FLOOR("IdCargoAbonoMovimiento"/10) IN (433921)





select ConceptoCargoReplica ,ConceptoAbonoReplica ,* 
from Plataforma_Concepto pc where ConceptoCargoReplica like '%AP%'

select * from TBCTMVTO t where CONCEPTO_MVTO = 'AP' and COD_MOVTO = 'ABDO'

EMPL

select * from SocioNegocio_Empleado ec 


select * from SocioNegocio_Persona snp where IdPersona = 357935



select * from Plataforma_Instrumento pi2 where Nemotecnico = 'CFI-FYPGAE'

