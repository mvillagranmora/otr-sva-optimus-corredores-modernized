SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaRAZONES_SOCIALES"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaRAZONES_SOCIALES" AS

        SELECT	WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	    CAST(NF."RSO_ID"   AS varchar)  AS TABLE_KEY,
        		"RSO_ID", -- INT
       			"PER_ID",--int] NOT NULL,***	
	            "GIR_ID", -- INT
	            "RSO_PATERNO", -- VARCHAR(60)
	            "RSO_MATERNO", -- VARCHAR(50)
	            "RSO_NOMBRES", -- VARCHAR(30)
	            "RSO_RAZ_SOCIAL" -- VARCHAR(80)
        FROM "BSCL.Replica.BD.Vistas::Sebra.RAZONES_SOCIALES" NF
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'RAZONES_SOCIALES' AND WST."Key" = NF."RSO_ID"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaRAZONES_SOCIALES" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
