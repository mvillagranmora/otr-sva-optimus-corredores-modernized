 INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'FondoMutuo_SerieDeFondo', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_SerieDeFondo" ;


SELECT * FROM  "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/SerieDeFondo"
ORDER BY "IdInstrumento" desc;

SELECT "IdSerieFondo" AS GRUPO,COUNT(1)*1.0 as C,1.0*SUM("IdInstrumento" + length("IdSerieFondo") + length("MedioPagoCargo") + "ValorCuota") AS HASH 
FROM _SYS_BIC."BSCL.FondoMutuo.BD.Analytics/SerieDeFondo" GROUP BY "IdSerieFondo" 


select * from "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondoAnalitica" bfmbvfcsdfa 