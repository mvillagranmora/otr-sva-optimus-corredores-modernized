SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Moneda"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Moneda" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdMoneda"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "Codigo"  AS "CODIGO", -- VARCHAR(40)
                  "CodigoNumerico"  AS "CODIGONUMERICO", -- INT
                  "Nombre"  AS "NOMBRE", -- VARCHAR(100)
                  "Simbolo"  AS "SIMBOLO" -- VARCHAR(40)

        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMonedaAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Moneda' AND WST."Key" = "IdMoneda"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Moneda" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
