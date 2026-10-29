SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMERCADO"; 
-- select * from "BSCL.Replica.BD.Vistas::ReplicaMERCADO" 
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMERCADO" AS


	SELECT 
		WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
		MERCADO  AS TABLE_KEY, 
		MERCADO,
		MER_NOMBRE,
		PAR_GRU_MERCADO,
		PAR_SUBGRU_MERCADO,
		PAR_TIPO_MERCADO,
		PAR_MERCADO_NEG,
		PAR_TIPO_VAL
 
	FROM "BSCL.Replica.BD.Vistas::Sebra.MERCADO" tb1
	LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'MERCADO' AND WST."Key" = MERCADO

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMERCADO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
