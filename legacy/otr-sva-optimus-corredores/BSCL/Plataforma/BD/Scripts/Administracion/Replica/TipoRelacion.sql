    INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdTipoRelacion" AS varchar(200))  ,'SocioNegocio_TipoRelacion', 'I', 0,  CURRENT_TIMESTAMP  
FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/TipoRelacion";

