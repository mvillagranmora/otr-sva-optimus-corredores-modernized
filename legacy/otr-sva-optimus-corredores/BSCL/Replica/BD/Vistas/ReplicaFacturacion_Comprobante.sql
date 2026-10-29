SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_Comprobante"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_Comprobante" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdComprobante"  AS TABLE_KEY,
                  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdComprobante"  AS "IDCOMPROBANTE", -- BIGINT
                  "FechaComprobante"  AS "FECHACOMPROBANTE", -- VARCHAR(10)
                  "FechaSistema"  AS "FECHASISTEMA", -- VARCHAR(10)
                  "IdTipoComprobante"  AS "IDTIPOCOMPROBANTE", -- VARCHAR(6)
                  "CodigoTipoComprobante"  AS "CODIGOTIPOCOMPROBANTE", -- VARCHAR(3)
                  "NombreTipoComprobante"  AS "NOMBRETIPOCOMPROBANTE", -- VARCHAR(100)
                  "IdEstadoComprobante"  AS "IDESTADOCOMPROBANTE", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "Proposito"  AS "PROPOSITO", -- VARCHAR(520)
                  "DescripcionPortafolio"  AS "DESCRIPCIONPORTAFOLIO", -- VARCHAR(100)
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "TipoDocumentoIdentidad"  AS "TIPODOCUMENTOIDENTIDAD", -- VARCHAR(1)
                  "NombreLargoCliente"  AS "NOMBRELARGOCLIENTE", -- VARCHAR(302)
                  "IdConcepto"  AS "IDCONCEPTO", -- VARCHAR(6)
                  "NombreConcepto"  AS "NOMBRECONCEPTO", -- VARCHAR(100)
                  "EsExento"  AS "ESEXENTO", -- VARCHAR(1)
                  "MonedaOperacion"  AS "MONEDAOPERACION", -- VARCHAR(3)
                  "MontoMonedaOperacion"  AS "MONTOMONEDAOPERACION", -- DECIMAL
                  "TotalCobrosMonedaOperacion"  AS "TOTALCOBROSMONEDAOPERACION", -- DECIMAL
                  "TotalComisionMonedaOperacion"  AS "TOTALCOMISIONMONEDAOPERACION", -- DECIMAL
                  "TotalImpuestoMonedaOperacion"  AS "TOTALIMPUESTOMONEDAOPERACION", -- DECIMAL
                  "TotalDerechoMonedaOperacion"  AS "TOTALDERECHOMONEDAOPERACION", -- DECIMAL
                  "TotalGastoMonedaOperacion"  AS "TOTALGASTOMONEDAOPERACION", -- DECIMAL
                  "TotalRetencionMonedaOperacion"  AS "TOTALRETENCIONMONEDAOPERACION", -- DECIMAL
                  "MonedaSistema"  AS "MONEDASISTEMA", -- VARCHAR(3)
                  "CodigoMonedaSistema"  AS "CODIGOMONEDASISTEMA", -- VARCHAR(40)
                  "NombreMonedaSistema"  AS "NOMBREMONEDASISTEMA", -- VARCHAR(100)
                  "SimboloMonedaSistema"  AS "SIMBOLOMONEDASISTEMA", -- VARCHAR(3)
                  "DecimalesMonedaSistema"  AS "DECIMALESMONEDASISTEMA", -- INT
                  "MontoMonedaSistema"  AS "MONTOMONEDASISTEMA", -- DECIMAL
                  "TotalCobrosMonedaSistema"  AS "TOTALCOBROSMONEDASISTEMA", -- DECIMAL
                  "MontoFinalMonedaSistema"  AS "MONTOFINALMONEDASISTEMA", -- DECIMAL
                  "IdCondicionLiquidacion"  AS "IDCONDICIONLIQUIDACION", -- VARCHAR(2)
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "IdMonedaLiquidacion"  AS "IDMONEDALIQUIDACION", -- VARCHAR(3)
                  "NombreMonedaLiquidacion"  AS "NOMBREMONEDALIQUIDACION", -- VARCHAR(100)
                  "MontoMonedaLiquidacion"  AS "MONTOMONEDALIQUIDACION", -- DECIMAL
                  "TotalCobrosMonedaLiquidacion"  AS "TOTALCOBROSMONEDALIQUIDACION", -- DECIMAL
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "AccionTipoOperacion"  AS "ACCIONTIPOOPERACION", -- VARCHAR(40)
                  "CodigoTipoOperacion"  AS "CODIGOTIPOOPERACION", -- VARCHAR(40)
                  "NombreTipoOperacion"  AS "NOMBRETIPOOPERACION", -- VARCHAR(100)
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "CodigoTipoProducto"  AS "CODIGOTIPOPRODUCTO", -- VARCHAR(40)
                  "NombreTipoProducto"  AS "NOMBRETIPOPRODUCTO", -- VARCHAR(100)
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "NombreTipoInstrumento"  AS "NOMBRETIPOINSTRUMENTO", -- VARCHAR(100)
                  "IdBolsa"  AS "IDBOLSA", -- VARCHAR(3)
                  "NombreBolsa"  AS "NOMBREBOLSA", -- VARCHAR(100)
                  "IdEmpleadoEjecutivoPortafolio"  AS "IDEMPLEADOEJECUTIVOPORTAFOLIO", -- BIGINT
                  "NombreEmpleadoEjecutivoPortafolio"  AS "NOMBREEMPLEADOEJECUTIVOPORTAFOLIO", -- VARCHAR(302)
                  "IdSucursalEmpleadoEjecutivoPortafolio"  AS "IDSUCURSALEMPLEADOEJECUTIVOPORTAFOLIO", -- BIGINT
                  "NombreSucursalEmpleadoEjecutivoPortafolio"  AS "NOMBRESUCURSALEMPLEADOEJECUTIVOPORTAFOLIO", -- VARCHAR(100)
                  "FechaEnvioDTE"  AS "FECHAENVIODTE", -- VARCHAR(19)
                  "IdEstadoDTE"  AS "IDESTADODTE", -- VARCHAR(3)
                  "NombreEstadoDTE"  AS "NOMBREESTADODTE", -- VARCHAR(100)
                  "IdEstadoSII"  AS "IDESTADOSII", -- VARCHAR(3)
                  "NombreEstadoSII"  AS "NOMBREESTADOSII", -- VARCHAR(100)
                  "FolioComprobante"  AS "FOLIOCOMPROBANTE", -- BIGINT
                  "FechaEmision"  AS "FECHAEMISION", -- VARCHAR(10)
                  "IdComprobanteReferencia"  AS "IDCOMPROBANTEREFERENCIA", -- BIGINT
                  "IdTipoComprobanteReferencia"  AS "IDTIPOCOMPROBANTEREFERENCIA", -- VARCHAR(6)
                  "FolioComprobanteReferencia"  AS "FOLIOCOMPROBANTEREFERENCIA", -- BIGINT
                  "FechaComprobanteReferencia"  AS "FECHACOMPROBANTEREFERENCIA", -- VARCHAR(10)
                  "CodigoEmpresa"  AS "CODIGOEMPRESA", -- VARCHAR(40)
                  "NombreEmpresa"  AS "NOMBREEMPRESA", -- VARCHAR(100)
                  "Glosa"  AS "GLOSA", -- VARCHAR(260)
                  "DireccionFacturacion"  AS "DIRECCIONFACTURACION", -- VARCHAR(100)
				  "IdMonedaNegociacion" AS "IDMONEDANEGOCIACION" -- VARCHAR(10)

        from "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Facturacion_Comprobante' AND WST."Key" = "IdComprobante"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFacturacion_Comprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
