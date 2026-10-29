SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoOperacion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoOperacion" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdTipoOperacion"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "Codigo"  AS "CODIGO", -- VARCHAR(40)
                  "Nombre"  AS "NOMBRE", -- VARCHAR(100)
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "IdTipoOperacionRelacionada"  AS "IDTIPOOPERACIONRELACIONADA", -- VARCHAR(6)
                  "Accion"  AS "ACCION" -- VARCHAR(40)

        from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacionAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_TipoOperacion' AND WST."Key" = "IdTipoOperacion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoOperacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
