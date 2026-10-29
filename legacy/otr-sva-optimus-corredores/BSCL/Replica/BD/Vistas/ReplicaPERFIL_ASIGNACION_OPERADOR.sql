SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPERFIL_ASIGNACION_OPERADOR"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPERFIL_ASIGNACION_OPERADOR" AS

        Select WST."IdStack" AS "IDSTACK", ''  AS TABLE_KEY,
            PAO.*

        FROM  "BSCL.Replica.BD.Vistas::Sebra.PERFIL_ASIGNACION_OPERADOR" PAO
        INNER JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'PERFIL_ASIGNACION_OPERADOR' --AND WST."Key" = DUM."Key"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPERFIL_ASIGNACION_OPERADOR" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    