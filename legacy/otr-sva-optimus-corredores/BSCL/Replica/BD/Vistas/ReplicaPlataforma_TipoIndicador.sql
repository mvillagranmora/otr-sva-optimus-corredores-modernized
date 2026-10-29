SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoIndicador"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoIndicador" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdTipoIndicador"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdTipoIndicador"  AS "IDTIPOINDICADOR", -- BIGINT
                  "IdTipoEntidad"  AS "IDTIPOENTIDAD", -- VARCHAR(40)
                  "NombreTipoIndicador"  AS "NOMBRETIPOINDICADOR", -- VARCHAR(255)
                  "CodigoTipoIndicador"  AS "CODIGOTIPOINDICADOR", -- VARCHAR(40)
                  "ValorIndicadorDefecto"  AS "VALORINDICADORDEFECTO", -- VARCHAR(255)
                  "CantidadDecimales"  AS "CANTIDADDECIMALES", -- BIGINT
                  "LongitudMinima"  AS "LONGITUDMINIMA", -- BIGINT
                  "LongitudMaxima"  AS "LONGITUDMAXIMA", -- BIGINT
                  "IdUso"  AS "IDUSO", -- VARCHAR(3)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "IdTipoPersona"  AS "IDTIPOPERSONA" -- VARCHAR(10)

        from "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicadorAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_TipoIndicador' AND WST."Key" = "IdTipoIndicador"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_TipoIndicador" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
