SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPRODUCTO";

-- select  * from "BSCL.Replica.BD.Vistas::ReplicaPRODUCTO" 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPRODUCTO" AS

	SELECT 
		WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
		TP."PRO_ID"  AS TABLE_KEY, 
		TP."MERCADO",
		TP."PRO_ID",
		TP."PRO_NOMBRE",
		TP."PRO_MESA",
		TP."PRO_IND_HABILITADO",
		TP."PRO_ID_NCG380"
		
	FROM  "BSCL.Replica.BD.Vistas::Sebra.PRODUCTO" TP
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'PRODUCTO' AND WST."Key" = TP."PRO_ID"


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPRODUCTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
