
SELECT count(1) FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws 

SELECT DISTINCT "Table" FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws WHERE "Status"  = 1

SELECT * FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws;

--DELETE FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" WHERE "Table"  = 'TBCTMVTO'

--encolados
SELECT "Table","Status",count(1) FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws 
WHERE "Status" = 0 AND BPBPRWS ."Stamp" >= '2022-09-23 18:00:00'
GROUP BY "Table" ,"Status" ORDER BY "Table","Status"


UPDATE "BSCL.Plataforma.BD::PLA.Replica.WorkStack" SET "Stamp"  = current_timestamp , "Status"  = 0 WHERE "Status"  = 1;


SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Instrumento" WHERE 

SELECT * FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws 
WHERE "Stamp" <= ADD_DAYS(CURRENT_DATE,-1)

SELECT * FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws WHERE "Action"  = 'M'

SELECT month(fec_mvto),count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE FEC_MVTO >= '2022-01-17' GROUP BY month(FEC_MVTO)

DELETE FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws WHERE "Stamp" <= ADD_DAYS(CURRENT_DATE,-1);


DELETE FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws WHERE "Table" = 'TBCTMVTO'

SELECT count(1) FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws WHERE "Table" = 'TBCTMVTO'

SELECT * FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws WHERE "Table" = 'TBCTMVTO'

update "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws SET "Status"  = 0 WHERE "Table" = 'TBCTMVTO'



DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Table" IN ('Custodia_CustodiaFechaLiquidacion','Custodia_CustodiaFechaOperacion')
AND "Stamp" >= '2020-12-17' AND "Status" = 0 AND "Action" = 'A';






INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PRECIOS', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Plataforma_PrecioInstrumento;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;



