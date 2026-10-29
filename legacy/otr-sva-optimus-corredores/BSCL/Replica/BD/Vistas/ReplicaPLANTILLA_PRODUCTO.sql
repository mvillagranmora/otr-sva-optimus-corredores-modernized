SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_PRODUCTO"; 

-- SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_PRODUCTO" where mercado is null  ORDER BY PAR_TPP_ID , PRO_ID , CCA_CODIGO

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_PRODUCTO" AS

	SELECT 
		 WST."IdStack" AS "IDSTACK","Action" AS ACTION,
		 PP."PAR_TPP_ID" || ':' || PP."MERCADO"   || ':' ||  PP."PRO_ID" || ':' ||  PP."CCA_CODIGO"    AS TABLE_KEY,
		 PP.*

	FROM  "BSCL.Replica.BD.Vistas::Sebra.PLANTILLA_PRODUCTO" PP
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'PLANTILLA_PRODUCTO' AND WST."Key" = PP."PAR_TPP_ID" || ':' || PP."MERCADO"   || ':' ||  PP."PRO_ID" || ':' ||  PP."CCA_CODIGO"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_PRODUCTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
