SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ComprobanteDetalle"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ComprobanteDetalle" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdComprobanteDetalle"  AS TABLE_KEY,
                  "Action" AS ACTION,
                  "IdComprobanteDetalle"  AS "IDCOMPROBANTEDETALLE", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdComprobante"  AS "IDCOMPROBANTE", -- BIGINT
                  "FechaComprobante"  AS "FECHACOMPROBANTE", -- VARCHAR(10)
                  "NumeroLinea"  AS "NUMEROLINEA", -- BIGINT
                  "IdConcepto"  AS "IDCONCEPTO", -- VARCHAR(6)
                  "NombreConcepto"  AS "NOMBRECONCEPTO", -- VARCHAR(100)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "Cantidad"  AS "CANTIDAD", -- DECIMAL
                  "Precio"  AS "PRECIO", -- DECIMAL
                  "IdMonedaOperacion"  AS "IDMONEDAOPERACION", -- VARCHAR(3)
                  "MontoMonedaOperacion"  AS "MONTOMONEDAOPERACION", -- DECIMAL
                  "IdMonedaSistema"  AS "IDMONEDASISTEMA", -- VARCHAR(3)
                  "MontoMonedaSistema"  AS "MONTOMONEDASISTEMA", -- DECIMAL
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "IdMonedaLiquidacion"  AS "IDMONEDALIQUIDACION", -- VARCHAR(3)
                  "MontoMonedaLiquidacion"  AS "MONTOMONEDALIQUIDACION", -- DECIMAL
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "CodigoTipoInstrumento"  AS "CODIGOTIPOINSTRUMENTO", -- VARCHAR(40)
                  "NombreTipoInstrumento"  AS "NOMBRETIPOINSTRUMENTO", -- VARCHAR(100)
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "CodigoTipoOperacion"  AS "CODIGOTIPOOPERACION", -- VARCHAR(40)
                  "NombreTipoOperacion"  AS "NOMBRETIPOOPERACION", -- VARCHAR(100)
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "CodigoTipoProducto"  AS "CODIGOTIPOPRODUCTO", -- VARCHAR(40)
                  "NombreTipoProducto"  AS "NOMBRETIPOPRODUCTO", -- VARCHAR(100)
                  "IdBolsa"  AS "IDBOLSA", -- VARCHAR(3)
                  "IdEstadoComprobanteDetalle"  AS "IDESTADOCOMPROBANTEDETALLE", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "Tasa"  AS "TASA", -- DECIMAL
		  		  "FolioTransaccion" AS "FOLIOTRANSACCION" --VARCHAR(40)

        from "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalleAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Facturacion_ComprobanteDetalle' AND WST."Key" = "IdComprobanteDetalle"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ComprobanteDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
