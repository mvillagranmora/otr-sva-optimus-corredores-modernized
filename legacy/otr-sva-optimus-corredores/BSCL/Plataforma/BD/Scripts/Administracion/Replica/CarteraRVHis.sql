INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key ,'Custodia_CarteraRVHis', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraRVHis"
where FechaRespaldo = '2023-12-05'

SELECT FECHARESPALDO ,count(1)  
FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraRVHis"
WHERE FechaRespaldo >= '2023-11-15'
group by FECHARESPALDO 
order by FECHARESPALDO 

select count(1) from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraRVAnalitica" bcbvcccr 

SELECT "Table","Action", min("Stamp"),count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-09-04' AND
"Status" = 0-- AND "Action" = 'D'
GROUP BY "Table","Action"

select count(1) from "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraRV" brbvrccr 

delete FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
where "Table" = 'Custodia_CarteraRV'

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-27' AND
"Status" = 0-- AND "Action" = 'D'
and "Table" = 'Custodia_CarteraRV'

SELECT count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCustodia_CarteraRVHis"
WHERE  "FECHARESPALDO" = '2020-09-07'
WHERE idstack IS NOT NULL


delete from Custodia_CarteraRVHis where FechaRespaldo = '2023-11-16' and IdCartera != 10213

select * from Custodia_CarteraRVHis ccr 
