SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaDETALLE_INDICADOR";
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaDETALLE_INDICADOR" AS

	SELECT 
		"IdStack" as IDSTACK,
		"Action" AS ACTION, 
		llave  AS TABLE_KEY, 
		 "IND_ID",
		 "DIN_ID", 
		 LEFT("DIN_VALOR",30) AS DIN_VALOR, 
		 "DIN_REQ_CUMPLE",
		 "DIN_ORDEN"
		 FROM "BSCL.Replica.BD.Vistas::Sebra.DETALLE_INDICADOR" i
		 LEFT JOIN  "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion"   AS s
		 			ON (s."Table" ='DETALLE_INDICADOR' AND cast(i.llave AS varchar(200)) = s."Key")
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaDETALLE_INDICADOR" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		
		
