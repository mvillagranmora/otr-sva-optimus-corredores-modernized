SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorPersona"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorPersona" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdPersona" || ':' || "IdTipoIndicador"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdTipoEntidad"  AS "IDTIPOENTIDAD", -- VARCHAR(40)
                  "NombreTipoIndicador"  AS "NOMBRETIPOINDICADOR", -- VARCHAR(255)
                  "GrupoIndicador"  AS "GRUPOINDICADOR", -- VARCHAR(40)
                  "IdUso"  AS "IDUSO", -- VARCHAR(3)
                  "IdTipoIndicador"  AS "IDTIPOINDICADOR", -- BIGINT
                  "CodigoTipoIndicador"  AS "CODIGOTIPOINDICADOR", -- VARCHAR(40)
                  "ClaseDato"  AS "CLASEDATO", -- VARCHAR(40)
                  "CantidadDecimales"  AS "CANTIDADDECIMALES", -- BIGINT
                  "LongitudMinima"  AS "LONGITUDMINIMA", -- BIGINT
                  "LongitudMaxima"  AS "LONGITUDMAXIMA", -- BIGINT
                  "IdTipoPersona"  AS "IDTIPOPERSONA", -- VARCHAR(3)
                  "ValorIndicador"  AS "VALORINDICADOR", -- VARCHAR(255)
                  "CodigoValorIndicador"  AS "CODIGOVALORINDICADOR", -- VARCHAR(40)
                  "NombreValorIndicador"  AS "NOMBREVALORINDICADOR", -- VARCHAR(255)
                  "IdIndicador"  AS "IDINDICADOR" -- BIGINT

        from "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersonaAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_IndicadorPersona' AND WST."Key" = ("IdPersona" || ':' || "IdTipoIndicador")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorPersona" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
