SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLRECL"; 

-- SELECT  *  FROM  "BSCL.Replica.BD.Vistas::ReplicaTBPLRECL"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLRECL" AS

	SELECT 
		"IdStack" AS "IDSTACK","Action" AS ACTION,
		"RUT_CLI" || ':' || "RUT_PERSONA" || ':' || "TIPO_REL_CLI"  AS TABLE_KEY, -- jk COMPLETAR LLAVE
		"RUT_CLI",
		"SEC_RUT_CLI",
		"TIPO_REL_CLI", --  relaciones validas
		"RUT_PERSONA",
		"SEC_RUT_PERSONA",
		"CARGO_PERSONA", -- 
		'P' AS  "IND_PERSONA_CLI" -- 
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBPLRECL" AS TB2
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
			ON WST."Table" = 'TBPLRECL' AND WST."Key" = "RUT_CLI" || ':' || "RUT_PERSONA" || ':' || "TIPO_REL_CLI"
		WHERE "RUT_CLI" <> '' AND "RUT_PERSONA" <> ''

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBPLRECL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
