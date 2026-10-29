SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaAPV_APV"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaAPV_APV" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdMatrizAgrupadorAPV"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdMatrizAgrupadorAPV"  AS "IDMATRIZAGRUPADORAPV", -- BIGINT
                  "IdRegimenTributario"  AS "IDREGIMENTRIBUTARIO", -- BIGINT
                  "IdPlanTipoInversion"  AS "IDPLANTIPOINVERSION", -- INT
                  "NombrePlan"  AS "NOMBREPLAN", -- VARCHAR(10)
                  "IdPropositoPortafolio"  AS "IDPROPOSITOPORTAFOLIO", -- VARCHAR(40)
                  "IdProductoAgrupador"  AS "IDPRODUCTOAGRUPADOR", -- INT
                  "Producto"  AS "PRODUCTO", -- VARCHAR(40)
                  "NombreAgrupador"  AS "NOMBREAGRUPADOR", -- VARCHAR(100)
                  "CodigoAgrupador"  AS "CODIGOAGRUPADOR", -- VARCHAR(100)
                  "Codigo"  AS "CODIGO", -- VARCHAR(50)
                  "IdEstado"  AS "IDESTADO" -- VARCHAR(3)

        from "BSCL.APV.BD.Vistas::ConsultarAPVAnalitica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'APV_APV' AND WST."Key" = "IdMatrizAgrupadorAPV"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaAPV_APV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
