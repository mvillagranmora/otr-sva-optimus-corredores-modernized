SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ComprobanteCobro"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ComprobanteCobro" AS

        Select WST."IdStack" AS "IDSTACK", 
				  "IdComprobanteCobro"  AS TABLE_KEY,
                  "Action" AS ACTION,
                  "IdComprobanteCobro"  AS "IDCOMPROBANTECOBRO", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdComprobante"  AS "IDCOMPROBANTE", -- BIGINT
                  "FechaComprobante"  AS "FECHACOMPROBANTE", -- VARCHAR(10)
                  "FechaSistema"  AS "FECHASISTEMA", -- VARCHAR(10)
                  "IdCobroClasificacion"  AS "IDCOBROCLASIFICACION", -- BIGINT
                  "CodigoCobroClasificacion"  AS "CODIGOCOBROCLASIFICACION", -- VARCHAR(40)
                  "IdCobro"  AS "IDCOBRO", -- VARCHAR(10)
                  "NombreCobro"  AS "NOMBRECOBRO", -- VARCHAR(100)
                  "FormaCobro"  AS "FORMACOBRO", -- VARCHAR(100)
                  "Porcentaje"  AS "PORCENTAJE", -- DECIMAL
                  "IdMonedaCobroOperacion"  AS "IDMONEDACOBROOPERACION", -- VARCHAR(3)
                  "MontoCobroMonedaOperacion"  AS "MONTOCOBROMONEDAOPERACION", -- DECIMAL
                  "IdMonedaCobroSistema"  AS "IDMONEDACOBROSISTEMA", -- VARCHAR(3)
                  "NombreMonedaSistema"  AS "NOMBREMONEDASISTEMA", -- VARCHAR(100)
                  "DecimalesMonedaSistema"  AS "DECIMALESMONEDASISTEMA", -- INT
                  "MontoCobroMonedaSistema"  AS "MONTOCOBROMONEDASISTEMA", -- DECIMAL
                  "IdMonedaCobroLiquidacion"  AS "IDMONEDACOBROLIQUIDACION", -- VARCHAR(3)
                  "MontoCobroMonedaLiquidacion"  AS "MONTOCOBROMONEDALIQUIDACION", -- DECIMAL
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO" -- VARCHAR(100)

        from "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Facturacion_ComprobanteCobro' AND WST."Key" = "IdComprobanteCobro"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ComprobanteCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
