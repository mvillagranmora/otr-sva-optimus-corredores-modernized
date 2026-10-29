SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ClasificacionTipoInstrumento"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ClasificacionTipoInstrumento" AS

        Select WST."IdStack" AS "IDSTACK", 
		  		  "IdClasificacionTipoInstrumento"  AS TABLE_KEY,
		  		  "Action" AS ACTION,
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "Codigo"  AS "CODIGO", -- VARCHAR(40)
                  "Descripcion"  AS "DESCRIPCION", -- VARCHAR(520)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "Nombre"  AS "NOMBRE" -- VARCHAR(100)

        from "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumentoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_ClasificacionTipoInstrumento' AND WST."Key" = "IdClasificacionTipoInstrumento"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ClasificacionTipoInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
