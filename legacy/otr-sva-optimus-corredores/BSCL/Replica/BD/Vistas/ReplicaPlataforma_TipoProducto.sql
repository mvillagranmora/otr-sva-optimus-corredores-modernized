SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoProducto"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoProducto" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdTipoProducto"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "Codigo"  AS "CODIGO", -- VARCHAR(40)
                  "Nombre"  AS "NOMBRE", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO" -- VARCHAR(3)

        from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProductoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_TipoProducto' AND WST."Key" = "IdTipoProducto"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoProducto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
