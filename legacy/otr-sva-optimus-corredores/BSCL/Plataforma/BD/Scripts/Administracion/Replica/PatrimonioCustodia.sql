INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Custodia_PatrimonioCustodiaFechaLiquidacion', 'I', 0,  CURRENT_TIMESTAMP
FROM bscl."BSCL.Replica.BD.Vistas::ReplicaCustodia_PatrimonioCustodiaFechaLiquidacion"
WHERE FechaPatrimonio in ('2026-05-11');

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Custodia_PatrimonioCustodiaFechaOperacion', 'I', 0,  CURRENT_TIMESTAMP
FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_PatrimonioCustodiaFechaOperacion"
WHERE FechaPatrimonio in ('2026-05-11');

SELECT "FechaPatrimonio" ,count(1)
FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioCustodiaFechaLiquidacionAnalitica"
GROUP BY "FechaPatrimonio"
order by "FechaPatrimonio" desc


SELECT "FechaPatrimonio" ,count(1)
FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioCustodiaFechaOperacionAnalitica"
GROUP BY "FechaPatrimonio"
order by "FechaPatrimonio" desc

SELECT "Table","Action", max("Stamp") AS stamp,count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2021-01-05' AND
"Status" = 1-- AND "Action" = 'D'
GROUP BY "Table","Action"
ORDER BY stamp



select FECHAINGRESOCOBRO , count(1) from "BSCL.Replica.BD.Vistas::ReplicaOrden_CobroOrden" brbvroco 
group by FECHAINGRESOCOBRO 
order by FECHAINGRESOCOBRO desc

select year(FechaIngresoCobro) y,month(FechaIngresoCobro) m,count(1) from "BSCL.Replica.BD.Vistas::ReplicaOrden_CobroOrden" brbvroco 
group by year(FechaIngresoCobro) ,month(FechaIngresoCobro)
order by y desc,m desc



select * from bscl."BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate"