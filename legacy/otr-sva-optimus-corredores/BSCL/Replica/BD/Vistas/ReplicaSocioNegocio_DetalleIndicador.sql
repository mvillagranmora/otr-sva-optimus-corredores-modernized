SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_DetalleIndicador"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_DetalleIndicador" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdDetalleIndicador" || ':' || "IdTipoIndicador"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdDetalleIndicador"  AS "IDDETALLEINDICADOR", -- BIGINT
                  "IdTipoIndicador"  AS "IDTIPOINDICADOR", -- BIGINT
                  "ValorIndicador"  AS "VALORINDICADOR", -- VARCHAR(255)
                  "Codigo"  AS "CODIGO" -- VARCHAR(40)

        from "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarDetalleIndicadorAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_DetalleIndicador' AND WST."Key" = ("IdDetalleIndicador" || ':' || "IdTipoIndicador")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_DetalleIndicador" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
