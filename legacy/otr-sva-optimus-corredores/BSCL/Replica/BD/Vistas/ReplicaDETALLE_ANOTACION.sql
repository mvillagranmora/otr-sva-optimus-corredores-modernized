SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaDETALLE_ANOTACION"; 
-- select * from "BSCL.Replica.BD.Vistas::ReplicaDETALLE_ANOTACION"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaDETALLE_ANOTACION" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	ANOT."Tipo" || CAST(DAN_ID AS varchar) AS TABLE_KEY,
            "DAN_ID", -- INT
            "PAR_TAN_ID", -- VARCHAR(10)		
            "EMP_CODIGO", -- CHAR(4)
            "PER_ID", -- INT
            "CUENTA", -- VARCHAR(100)
            "FIL_ID", -- INT
            "DAN_TITULO", -- VARCHAR(50)
            "DAN_NOTA", -- VARCHAR(200)
            "DAN_FEC_INGRESO", -- DATETIME
            "DAN_FEC_INICIO", -- DATETIME
            "DAN_FEC_VENCE", -- DATETIME
            "DAN_IND_AMBITO", -- CHAR(3)
            "DAN_IND_RELEVANCIA", -- CHAR(1)
            "DAN_USO_CARTOLA" -- CHAR(1)

        FROM "BSCL.Replica.BD.Vistas::Sebra.DETALLE_ANOTACION" ANOT
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'DETALLE_ANOTACION' AND WST."Key" = ANOT."Tipo" || DAN_ID
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaDETALLE_ANOTACION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
