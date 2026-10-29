SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaMODELO_FACTURACION"; 
 --select * from "BSCL.Replica.BD.Vistas::ReplicaMODELO_FACTURACION"
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaMODELO_FACTURACION" AS

	SELECT WST."IdStack" AS "IDSTACK", "Action" AS ACTION, 'MFOPTIMUS1'  AS TABLE_KEY,
	MFAC.*
	FROM  "BSCL.Replica.BD.Vistas::Sebra.MODELO_FACTURACION" MFAC
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'MODELO_FACTURACION' AND MFAC."PAR_MFA_ID" = WST."Key"
	WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaMODELO_FACTURACION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
