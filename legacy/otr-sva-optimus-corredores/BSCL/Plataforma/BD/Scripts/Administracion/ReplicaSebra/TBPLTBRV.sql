INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBPLTBRV', 'I', 0,  CURRENT_TIMESTAMP , ''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV" 
WHERE year(fecha) = '2022';

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBTECMBT', 'I', 0,  CURRENT_TIMESTAMP , ''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBTECMBT" ;

select year(fecha) as y,count(1)
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV" 
group by year(FECHA) order by y desc 

2024	135605
2023	163403
2022	183641
2021	95432

WHERE fecha >= '2021-04-01';

select count(1) from "BSCL.Replica.BD.Vistas::Sebra.TBTECMBT" brbvst

select * from "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV" brbvrt 
where IDSTACK is not null



select MERCADO,FEC_TRANS,FOLIO_TRANS,IND_BOLSA,TIPO_OPERAC,count(1)
from "BSCL.Replica.BD.Vistas::Sebra.TBPLTBRV" brbvst
group by MERCADO,FEC_TRANS,FOLIO_TRANS,IND_BOLSA,TIPO_OPERAC
having count(1) > 1