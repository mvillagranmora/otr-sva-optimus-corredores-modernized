SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_ComprobanteDetalle"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_ComprobanteDetalle" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdComprobanteDetalle"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdComprobanteDetalle"  AS "IDCOMPROBANTEDETALLE", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "IdComprobante"  AS "IDCOMPROBANTE", -- BIGINT
                  "FechaComprobante"  AS "FECHACOMPROBANTE", -- VARCHAR(10)
                  "TipoMovimiento"  AS "TIPOMOVIMIENTO", -- VARCHAR(10)
                  "NumeroLinea"  AS "NUMEROLINEA", -- INT
                  "IdConceptoDetalle"  AS "IDCONCEPTODETALLE", -- VARCHAR(6)
                  "NombreConceptoDetalle"  AS "NOMBRECONCEPTODETALLE", -- VARCHAR(100)
                  "IdMonedaDetalle"  AS "IDMONEDADETALLE", -- VARCHAR(3)
                  "NombreMonedaDetalle"  AS "NOMBREMONEDADETALLE", -- VARCHAR(100)
                  "MontoDetalle"  AS "MONTODETALLE", -- DECIMAL
                  "IdConceptoComprobante"  AS "IDCONCEPTOCOMPROBANTE", -- VARCHAR(6)
                  "NombreConceptoComprobante"  AS "NOMBRECONCEPTOCOMPROBANTE", -- VARCHAR(100)
                  "IdMonedaComprobante"  AS "IDMONEDACOMPROBANTE", -- VARCHAR(3)
                  "NombreMonedaComprobante"  AS "NOMBREMONEDACOMPROBANTE", -- VARCHAR(100)
                  "MontoComprobante"  AS "MONTOCOMPROBANTE", -- DECIMAL
                  "FechaLiquidacionComprobante"  AS "FECHALIQUIDACIONCOMPROBANTE", -- VARCHAR(10)
                  "IdCaja"  AS "IDCAJA", -- BIGINT
                  "SecuenciaAgrupacion"  AS "SECUENCIAAGRUPACION", -- VARCHAR(40)
                  "IdMovimientoRelacionado"  AS "IDMOVIMIENTORELACIONADO", -- BIGINT
                  "IdEmpleadoDigitador"  AS "IDEMPLEADODIGITADOR", -- BIGINT
                  "IdSucursalEmpleadoDigitador"  AS "IDSUCURSALEMPLEADODIGITADOR", -- BIGINT
                  "IdTipoComprobanteOrigen"  AS "IDTIPOCOMPROBANTEORIGEN", -- VARCHAR(3)
                  "IdComprobanteOrigen"  AS "IDCOMPROBANTEORIGEN", -- BIGINT
                  "FechaComprobanteOrigen"  AS "FECHACOMPROBANTEORIGEN", -- VARCHAR(10)
                  "FechaLiquidacionComprobanteOrigen"  AS "FECHALIQUIDACIONCOMPROBANTEORIGEN", -- VARCHAR(10)
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "NombreTipoProducto"  AS "NOMBRETIPOPRODUCTO", -- VARCHAR(100)
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "NombreTipoOperacion"  AS "NOMBRETIPOOPERACION", -- VARCHAR(100)
                  "IdMonedaOperacion"  AS "IDMONEDAOPERACION", -- VARCHAR(3)
                  "MontoOperacion"  AS "MONTOOPERACION", -- DECIMAL
                  "IdBolsa"  AS "IDBOLSA", -- VARCHAR(3)
                  "FechaParidad"  AS "FECHAPARIDAD", -- VARCHAR(10)
                  "ValorParidad"  AS "VALORPARIDAD", -- DECIMAL
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "FolioCartera"  AS "FOLIOCARTERA", -- BIGINT
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "IdTipoReajuste"  AS "IDTIPOREAJUSTE", -- VARCHAR(3)
                  "IdCarga"  AS "IDCARGA", -- BIGINT
                  "EstadoComprobanteDetalle"  AS "ESTADOCOMPROBANTEDETALLE", -- VARCHAR(3)
                  "NombreEstadoComprobanteDetalle"  AS "NOMBREESTADOCOMPROBANTEDETALLE", -- VARCHAR(100)
                  "IdTipoPersonaTitularPago"  AS "IDTIPOPERSONATITULARPAGO", -- VARCHAR(3)
                  "IdPersonaTitularPago"  AS "IDPERSONATITULARPAGO", -- BIGINT
                  "NombrePersonaTitularPago"  AS "NOMBREPERSONATITULARPAGO", -- VARCHAR(300)
                  "IdPortafolioTitularPago"  AS "IDPORTAFOLIOTITULARPAGO", -- BIGINT
                  "IdTipoPersona"  AS "IDTIPOPERSONA", -- VARCHAR(3)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "NombrePersona"  AS "NOMBREPERSONA", -- VARCHAR(300)
                  "IdTipoDocumentoIdentidad"  AS "IDTIPODOCUMENTOIDENTIDAD", -- VARCHAR(3)
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "EsBilateral"  AS "ESBILATERAL", -- VARCHAR(1)
                  "ComprobanteNeteado"  AS "COMPROBANTENETEADO", -- VARCHAR(1)
                  "SeparadoPorConcepto"  AS "SEPARADOPORCONCEPTO", -- VARCHAR(1)
                  "IdEstadoComprobante"  AS "IDESTADOCOMPROBANTE", -- VARCHAR(3)
                  "NombreEstadoComprobante"  AS "NOMBREESTADOCOMPROBANTE", -- VARCHAR(100)
                  "IdUsuario"  AS "IDUSUARIO" -- BIGINT

        from "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalleAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Tesoreria_ComprobanteDetalle' AND WST."Key" = "IdComprobanteDetalle"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTesoreria_ComprobanteDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
