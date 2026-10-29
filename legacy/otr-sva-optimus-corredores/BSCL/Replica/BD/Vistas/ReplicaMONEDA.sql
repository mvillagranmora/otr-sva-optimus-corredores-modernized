SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMONEDA"; 
--SELECT  *  FROM "BSCL.Replica.BD.Vistas::ReplicaMONEDA"
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMONEDA" AS

	SELECT 
		WST."IdStack" AS "IDSTACK","Action" AS ACTION, 
		 MON_ID  AS TABLE_KEY, 

		 MON_ID
	  ,  MON_NOMBRE --- JK  aplicar left al nombre 
	  ,  MON_SIMBOLO
	  , MON_NEMO
	  , MON_USO_OPERATIVO
	FROM "BSCL.Replica.BD.Vistas::Sebra.MONEDA" M
	LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'MONEDA' AND WST."Key" = M.MON_ID

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMONEDA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
