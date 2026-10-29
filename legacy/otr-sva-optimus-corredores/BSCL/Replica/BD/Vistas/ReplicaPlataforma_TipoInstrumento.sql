SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoInstrumento"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoInstrumento" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdTipoInstrumento"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "Codigo"  AS "CODIGO", -- VARCHAR(40)
                  "Nombre"  AS "NOMBRE", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO" -- VARCHAR(3)

        from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_TipoInstrumento' AND WST."Key" = "IdTipoInstrumento"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
