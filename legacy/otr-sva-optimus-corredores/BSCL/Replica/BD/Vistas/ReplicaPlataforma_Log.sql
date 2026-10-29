SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Log"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Log" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		  	"IdLog" AS TABLE_KEY, 
				  	COALESCE("Action",'') AS ACTION, 
				  	"IdLog" AS "IDLOG", -- BIGINT, 
					COALESCE("IdModulo",'') AS "IDMODULO", -- NVARCHAR(3), 
					COALESCE("IdTipoLog",'') AS "IDTIPOLOG", -- NVARCHAR(40), 
					COALESCE("IdTipoRegistro",'') AS "IDTIPOREGISTRO", -- NVARCHAR(40), 
					COALESCE("NombreProceso",'') AS "NOMBREPROCESO", -- NVARCHAR(100), 
					COALESCE("FechaRegistro",'1900-01-01 00:00:00') AS "FECHAREGISTRO", -- TIMESTAMP, 
					COALESCE("TimeZone",'') AS "TIMEZONE", --NVARCHAR(3), 
					COALESCE("Nemo",'') AS "NEMO", --NVARCHAR(260), 
					COALESCE("IdDetalleLog",0) AS "IDDETALLELOG", --BIGINT 
					COALESCE("SubRegistro",'') AS "SUBREGISTRO", --NVARCHAR(40), 
					COALESCE("CampoRegistro",'') AS "CAMPOREGISTRO",--NVARCHAR(500), 
					RIGHT(COALESCE("ValorAntiguo",''),4000) AS "VALORANTIGUO",--NVARCHAR(5000), 
					RIGHT(COALESCE("ValorNuevo",''),4000) AS "VALORNUEVO",--NVARCHAR(5000), 
					COALESCE("IdUsuario",0) AS "IDUSUARIO", --BIGINT, 
					COALESCE("CodigoUsuario",'') AS "CODIGOUSUARIO" , --NVARCHAR(100), 
					COALESCE("IdUsuarioAtendido",0) AS "IDUSUARIOATENDIDO" --BIGINT 
        FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogAnalitica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Log' AND WST."Key" = "IdLog"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Log" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
