SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLGLOS"; 
--- select  *  from "BSCL.Replica.BD.Vistas::ReplicaTBPLGLOS" 
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLGLOS" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	REG."NOM_TAB" || ':' || REG."IDENTIF_REG"  AS TABLE_KEY,
            REG.*

        FROM  "BSCL.Replica.BD.Vistas::Sebra.TBPLGLOS" REG
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBPLGLOS' AND WST."Key" = (REG."NOM_TAB" || ':' || REG."IDENTIF_REG")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBPLGLOS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
