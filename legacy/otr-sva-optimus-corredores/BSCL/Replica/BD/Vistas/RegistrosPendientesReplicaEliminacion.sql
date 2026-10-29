SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaEliminacion";

--SELECT * FROM "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaEliminacion"

CREATE VIEW  "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaEliminacion" AS
	SELECT max("IdStack") AS IDSTACK,"Key" AS KEY_TABLE, "Table" AS TABLE_NAME
	from "BSCL.Plataforma.BD::PLA.Replica.WorkStack" s1
	WHERE  "Status" = 0 AND "Action" = 'D' AND  "Table" != ''
	GROUP BY "Key", "Table"
	ORDER BY IDSTACK
	
	
--	SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" WHERE "CONFIG_GROUP" like '%SocioNegocio_PersonaDireccionFisica%'

	
	
	--SELECT  * FROM BSCL."BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaEliminacion" 
--	where TABLE_NAME LIKE 'SocioNegocio_PersonaDireccionFisica'


