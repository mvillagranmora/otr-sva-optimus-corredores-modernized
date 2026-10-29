INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdRegimenFondoMutuo" AS varchar(200)) || ':' || cast("IdSerieFondo" AS varchar(200)),'FondoMutuo_RegimenSerieFondo', 'I', 0,  CURRENT_TIMESTAMP  
FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/RegimenSerieFondo";
