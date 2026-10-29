INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Custodia_PatrimonioConsolidadoFechaLiquidacion', 'I', 0,  CURRENT_TIMESTAMP
FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_PatrimonioConsolidadoFechaLiquidacion"
WHERE FechaPatrimonio in ('2026-05-11');

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Custodia_PatrimonioConsolidadoFechaOperacion', 'I', 0,  CURRENT_TIMESTAMP
FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_PatrimonioConsolidadoFechaOperacion"
WHERE FechaPatrimonio in ('2026-05-11');

SELECT "Table","Action", max("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"-- 12:32
WHERE
"Stamp" >= '2020-11-04' AND
"Status" = 0
--AND "Action" = 'M'
GROUP BY "Table","Action"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-20' 
AND "Status" = 0
-- AND "Action" = 'D'
AND "Table" = 'Custodia_PatrimonioConsolidadoFechaOperacion'
ORDER BY "Stamp" desc

SELECT "FechaPatrimonio",count(1) 
FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica" 
GROUP BY "FechaPatrimonio"
ORDER BY "FechaPatrimonio" DESC


SELECT *
FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica" 
where "DocumentoIdentidad"  = '76106169-0'
and "FechaPatrimonio"  = '2024-06-24'
ORDER BY "FechaPatrimonio" DESC


SELECT "FechaPatrimonio",count(1) 
FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaOperacionAnalitica" 
GROUP BY "FechaPatrimonio"
ORDER BY "FechaPatrimonio" desc

SELECT "FechaPatrimonio",count(1) 
FROM "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica"
GROUP BY "FechaPatrimonio"
ORDER BY "FechaPatrimonio" desc

SELECT ("IdPortafolio" + "IdInstrumento" + "CantidadPatrimonio" + "PrecioMercadoCustodia" + "ValorCuota" + "IdMatrizAgrupadorAPV" + "MontoPatrimonio"),*
FROM "_SYS_BIC"."BSCL.Custodia.BD.Analytics/PatrimonioConsolidadoFechaLiquidacion" 
WHERE "IdPortafolio" >= 2 AND "IdPortafolio" <= 99
ORDER BY "FechaPatrimonio","IdCliente","IdPortafolio","IdInstrumento","IdRegimenTributario" desc






SELECT "FolioCartera","IdCartera","INGFechaOperacion","EGRFechaOperacion","INGIdTipoOperacion","EGRIdTipoOperacion","INGCantidad","EGRCantidad" FROM BSCL."BSCL.Custodia.BD::CUS.Cartera"
WHERE "IdPortafolio" = 214273 and "IdInstrumento" = 255408
ORDER BY "FolioCartera","IdCartera"







SELECT * FROM BSCL."BSCL.Custodia.BD::CUS.PatrimonioCustodia"
WHERE "FechaPatrimonio" = '2020-09-30' and "TipoFechaConsulta" = 'LIQ' and "IdPortafolio" = 125971 and "Nemotecnico" = 'CFIBTGFGIA'
ORDER BY "IdPortafolio"


UPDATE BSCL."BSCL.Custodia.BD::CUS.PatrimonioCustodia"
SET "MontoPatrimonio" = ROUND("PrecioMercadoCustodia" * "CantidadPatrimonio",0)
WHERE "IdPatrimonioCustodia" IN (
SELECT "IdPatrimonioCustodia" FROM BSCL."BSCL.Custodia.BD::CUS.PatrimonioCustodia" CUS
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CUS."IdInstrumento"
WHERE "FechaPatrimonio" = '2020-09-30'
AND "IdClasificacionTipoInstrumento" = 'RV'
AND "TipoFechaConsulta" = 'LIQ'
AND ROUND("PrecioMercadoCustodia" * "CantidadPatrimonio",0) != "MontoPatrimonio"
)

SELECT "IdPatrimonioCustodia" FROM BSCL."BSCL.Custodia.BD::CUS.PatrimonioCustodia" CUS
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CUS."IdInstrumento"
WHERE "FechaPatrimonio" = '2020-09-30'
AND "IdClasificacionTipoInstrumento" = 'RV'
AND "TipoFechaConsulta" = 'LIQ'
AND ROUND("PrecioMercadoCustodia" * "CantidadGarantia",0) != "MontoGarantia"



SELECT "IdPortafolio" , CASE WHEN "IdRegimenTributario" IS NULL THEN 0 ELSE LENGTH("IdRegimenTributario") END , "IdInstrumento" , "CantidadPatrimonio" , ROUND("PrecioMercadoCustodia",4) , "ValorCuota" , "IdMatrizAgrupadorAPV" , ROUND("MontoPatrimonio") AS HASH
FROM BSCL."BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica" 
WHERE (FLOOR("IdPortafolio"/10) = 20141)



SELECT * FROM  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteAnalitica"
WHERE "IdCliente" = 45562

SELECT * FROM "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa"




SELECT DISTINCT c."IdMonedaInstrumento",c."Nemotecnico",i."IdMoneda" FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" c 
LEFT JOIN bscl."BSCL.Plataforma.BD::INS.Instrumento" I ON c."IdInstrumento" = i."IdInstrumento"
WHERE c."IdMonedaInstrumento" != i."IdMoneda"



SELECT * FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws 

where "Table" = 'CLIENTE'

truncate table "BSCL.Plataforma.BD::PLA.Replica.WorkStack" 

select * from "BSCL.Plataforma.BD::PLA.Replica.Cleaner" bpbprc 



select * from sys.TRIGGERS t 
where TRIGGER_NAME like '%Car%'





select COD_PER  from "BSCL.Replica.BD::TBCTCIRV" brbt 


select "FechaRespaldo", count(1)
from "BSCL.Custodia.BD::HIS.Cartera" bcbhc 
group by "FechaRespaldo"