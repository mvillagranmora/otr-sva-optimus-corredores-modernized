SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ComprobanteFacturacionFormaPago"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ComprobanteFacturacionFormaPago" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdComprobanteFormaDePago"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdComprobanteFormaDePago"  AS "IDCOMPROBANTEFORMADEPAGO", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdComprobante"  AS "IDCOMPROBANTE", -- BIGINT
                  "FechaComprobante"  AS "FECHACOMPROBANTE", -- VARCHAR(10)
                  "IdMedioDePago"  AS "IDMEDIODEPAGO", -- VARCHAR(3)
                  "NombreMedioDePago"  AS "NOMBREMEDIODEPAGO", -- VARCHAR(100)
                  "NumeroSerie"  AS "NUMEROSERIE", -- VARCHAR(40)
                  "NumeroDocumento"  AS "NUMERODOCUMENTO", -- BIGINT
                  "IdCuentaBancaria"  AS "IDCUENTABANCARIA", -- BIGINT
                  "NombreCuentaBancaria"  AS "NOMBRECUENTABANCARIA", -- VARCHAR(100)
                  "IdTipoCuentaBancaria"  AS "IDTIPOCUENTABANCARIA", -- VARCHAR(4)
                  "NombreTipoCuentaBancaria"  AS "NOMBRETIPOCUENTABANCARIA", -- VARCHAR(100)
                  "NumeroCuentaBancaria"  AS "NUMEROCUENTABANCARIA", -- VARCHAR(100)
                  "IdTarjetaBancaria"  AS "IDTARJETABANCARIA", -- BIGINT
                  "IdTipoTarjetaBancaria"  AS "IDTIPOTARJETABANCARIA", -- VARCHAR(40)
                  "NombreTipoTarjetaBancaria"  AS "NOMBRETIPOTARJETABANCARIA", -- VARCHAR(100)
                  "NumeroTarjetaBancaria"  AS "NUMEROTARJETABANCARIA", -- VARCHAR(100)
                  "FechaVctoTarjetaBancaria"  AS "FECHAVCTOTARJETABANCARIA", -- VARCHAR(10)
                  "IdModalidadDePago"  AS "IDMODALIDADDEPAGO", -- VARCHAR(6)
                  "NombreModalidadDePago"  AS "NOMBREMODALIDADDEPAGO", -- VARCHAR(100)
                  "IdBanco"  AS "IDBANCO", -- BIGINT
                  "CodigoBanco"  AS "CODIGOBANCO", -- VARCHAR(40)
                  "NombreBanco"  AS "NOMBREBANCO", -- VARCHAR(100)
                  "FechaDocumentoPago"  AS "FECHADOCUMENTOPAGO", -- VARCHAR(10)
                  "IdMonedaLiquidacion"  AS "IDMONEDALIQUIDACION", -- VARCHAR(3)
                  "MontoMonedaLiquidacion"  AS "MONTOMONEDALIQUIDACION", -- DECIMAL
                  "IdEstado"  AS "IDESTADO" -- VARCHAR(3)

        from "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaPagoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Facturacion_ComprobanteFacturacionFormaPago' AND WST."Key" = "IdComprobanteFormaDePago"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ComprobanteFacturacionFormaPago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
