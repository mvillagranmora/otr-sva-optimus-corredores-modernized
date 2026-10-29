SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorPortafolio"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorPortafolio" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdPortafolio" || ':' || "IdTipoIndicador"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdTipoCliente"  AS "IDTIPOCLIENTE", -- VARCHAR(40)
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
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
                  "ModeloCliente"  AS "MODELOCLIENTE", -- VARCHAR(40)
                  "ValorIndicador"  AS "VALORINDICADOR", -- VARCHAR(255)
                  "CodigoValorIndicador"  AS "CODIGOVALORINDICADOR", -- VARCHAR(40)
                  "NombreValorIndicador"  AS "NOMBREVALORINDICADOR", -- VARCHAR(255)
                  "IdIndicador"  AS "IDINDICADOR", -- BIGINT
                  "FechaCreacion" AS "FECHACREACION", --VARCHAR(10)
                  "FechaModificacion" AS "FECHAMODIFICACION" --VARCHAR(10)
                  
        from "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
		--from "BSCL"."BSCL.Plataforma.BD::IND.IndicadorPortafolio"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_IndicadorPortafolio' AND WST."Key" = (CAST("IdPortafolio" AS NVARCHAR) || ':' || "IdTipoIndicador")
		--and 1 = 2
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
