INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'APV_APV', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::ReplicaAPV_APV";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast(cast("IdMatrizAgrupadorNOAPV" AS varchar(200)) || ':' || cast("CodigoPortafolio" AS varchar(200)) AS varchar(200)),'FondoMutuo_NOAPV', 'A', 0,  CURRENT_TIMESTAMP 
FROM DUMMY;


delete FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Table" LIKE '%APV%'

SELECT * FROM BSCL."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" WHERE "IdMovimientoCarteraEnTransito" = 578143