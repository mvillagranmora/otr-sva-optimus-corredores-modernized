SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorCliente"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorCliente" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdCliente" || ':' || "IdTipoIndicador"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdTipoCliente"  AS "IDTIPOCLIENTE", -- VARCHAR(40)
                  "IdTipoEntidad"  AS "IDTIPOENTIDAD", -- VARCHAR(40)
                  "NombreTipoIndicador"  AS "NOMBRETIPOINDICADOR", -- VARCHAR(255)
                  "GrupoIndicador"  AS "GRUPOINDICADOR", -- VARCHAR(40)
                  "IdUso"  AS "IDUSO", -- VARCHAR(3)
                  "IdTipoIndicador"  AS "IDTIPOINDICADOR", -- BIGINT
                  "CodigoTipoIndicador"  AS "CODIGOTIPOINDICADOR", -- VARCHAR(10)
                  "ClaseDato"  AS "CLASEDATO", -- VARCHAR(40)
                  "CantidadDecimales"  AS "CANTIDADDECIMALES", -- BIGINT
                  "LongitudMinima"  AS "LONGITUDMINIMA", -- BIGINT
                  "LongitudMaxima"  AS "LONGITUDMAXIMA", -- BIGINT
                  "IdTipoPersona"  AS "IDTIPOPERSONA", -- VARCHAR(3)
                  "ModeloCliente"  AS "MODELOCLIENTE", -- VARCHAR(40)
                  "ValorIndicador"  AS "VALORINDICADOR", -- VARCHAR(255)
                  "CodigoValorIndicador"  AS "CODIGOVALORINDICADOR", -- VARCHAR(40)
                  "NombreValorIndicador"  AS "NOMBREVALORINDICADOR", -- VARCHAR(255)
                  "IdIndicador"  AS "IDINDICADOR" -- BIGINT

        from "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_IndicadorCliente' AND WST."Key" = ("IdCliente" || ':' || "IdTipoIndicador")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
