SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_MovimientosCuentaInversion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_MovimientosCuentaInversion" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdCargoAbonoMovimiento"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdCargoAbonoMovimiento"  AS "IDCARGOABONOMOVIMIENTO", -- BIGINT
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "FechaMovimiento"  AS "FECHAMOVIMIENTO", -- VARCHAR(10)
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "EsIngresoManual"  AS "ESINGRESOMANUAL", -- VARCHAR(1)
                  "IdCuentaInversion"  AS "IDCUENTAINVERSION", -- BIGINT
                  "CodigoCuentaInversion"  AS "CODIGOCUENTAINVERSION", -- VARCHAR(40)
                  "NombreCuentaInversion"  AS "NOMBRECUENTAINVERSION", -- VARCHAR(100)
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "NombreCliente"  AS "NOMBRECLIENTE", -- VARCHAR(302)
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "DescripcionPortafolio"  AS "DESCRIPCIONPORTAFOLIO", -- VARCHAR(100)
                  "Proposito"  AS "PROPOSITO", -- VARCHAR(520)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "CodigoMoneda"  AS "CODIGOMONEDA", -- VARCHAR(40)
                  "NombreMoneda"  AS "NOMBREMONEDA", -- VARCHAR(100)
                  "SimboloMoneda"  AS "SIMBOLOMONEDA", -- VARCHAR(3)
                  "DecimalesMoneda"  AS "DECIMALESMONEDA", -- INT
                  "TipoMovimiento"  AS "TIPOMOVIMIENTO", -- VARCHAR(6)
                  "Monto"  AS "MONTO", -- DECIMAL
                  "IdConcepto"  AS "IDCONCEPTO", -- VARCHAR(6)
                  "NombreConcepto"  AS "NOMBRECONCEPTO", -- VARCHAR(100)
                  "Descripcion"  AS "DESCRIPCION", -- VARCHAR(100)
                  "IdCondicionDeLiquidacion"  AS "IDCONDICIONDELIQUIDACION", -- VARCHAR(2)
                  "CodigoCondicionDeLiquidacion"  AS "CODIGOCONDICIONDELIQUIDACION", -- VARCHAR(2)
                  "NombreCondicionDeLiquidacion"  AS "NOMBRECONDICIONDELIQUIDACION", -- VARCHAR(40)
                  "PlazoCondicionDeLiquidacion"  AS "PLAZOCONDICIONDELIQUIDACION", -- DECIMAL
                  "FechaEfectivaMovimiento"  AS "FECHAEFECTIVAMOVIMIENTO", -- VARCHAR(10)
                  "Cargo"  AS "CARGO", -- DECIMAL
                  "Abono"  AS "ABONO", -- DECIMAL
                  "SaldoPorFechaLiquidacion"  AS "SALDOPORFECHALIQUIDACION", -- DECIMAL
                  "SaldoPorFechaMovimiento"  AS "SALDOPORFECHAMOVIMIENTO", -- DECIMAL
                  "IdTipoComprobante"  AS "IDTIPOCOMPROBANTE", -- VARCHAR(6)
                  "CodigoTipoComprobante"  AS "CODIGOTIPOCOMPROBANTE", -- VARCHAR(3)
                  "NombreTipoComprobante"  AS "NOMBRETIPOCOMPROBANTE", -- VARCHAR(100)
                  "IdComprobante"  AS "IDCOMPROBANTE", -- BIGINT
                  "FolioComprobante"  AS "FOLIOCOMPROBANTE", -- VARCHAR(40)
                  "IdComprobanteDetalle"  AS "IDCOMPROBANTEDETALLE", -- BIGINT
                  "IdComprobanteFormaDePago"  AS "IDCOMPROBANTEFORMADEPAGO", -- BIGINT
                  "IdTipoMovimientoAsociado"  AS "IDTIPOMOVIMIENTOASOCIADO", -- VARCHAR(6)
                  "IdMovimientoAsociado"  AS "IDMOVIMIENTOASOCIADO", -- BIGINT
                  "FechaComprobante"  AS "FECHACOMPROBANTE", -- VARCHAR(10)
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "CodigoTipoOperacion"  AS "CODIGOTIPOOPERACION", -- VARCHAR(40)
                  "NombreTipoOperacion"  AS "NOMBRETIPOOPERACION", -- VARCHAR(100)
                  "IdOperacion"  AS "IDOPERACION", -- BIGINT
                  "FechaOperacion"  AS "FECHAOPERACION", -- VARCHAR(10)
                  "FechaLiquidacionOperacion"  AS "FECHALIQUIDACIONOPERACION", -- VARCHAR(10)
                  "FechaVencimientoOperacion"  AS "FECHAVENCIMIENTOOPERACION", -- VARCHAR(10)
                  "IdMonedaOperacion"  AS "IDMONEDAOPERACION", -- VARCHAR(3)
                  "MontoOperacion"  AS "MONTOOPERACION", -- DECIMAL
                  "TipoCambioOperacion"  AS "TIPOCAMBIOOPERACION", -- DECIMAL
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "CodigoTipoProducto"  AS "CODIGOTIPOPRODUCTO", -- VARCHAR(40)
                  "NombreTipoProducto"  AS "NOMBRETIPOPRODUCTO", -- VARCHAR(100)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "NombreTipoInstrumento"  AS "NOMBRETIPOINSTRUMENTO", -- VARCHAR(100)
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "NombreClasificacionTipoInstrumento"  AS "NOMBRECLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(100)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "EsCargoAbonoPorTraspaso"  AS "ESCARGOABONOPORTRASPASO", -- VARCHAR(1)
                  "IdCargoAbonoRelacionadoTraspaso"  AS "IDCARGOABONORELACIONADOTRASPASO", -- BIGINT
                  "NombreClienteRelacionado"  AS "NOMBRECLIENTERELACIONADO", -- VARCHAR(302)
                  "CodigoPortafolioRelacionado"  AS "CODIGOPORTAFOLIORELACIONADO", -- VARCHAR(100)
                  "EsCertificadoAutomatico"  AS "ESCERTIFICADOAUTOMATICO", -- VARCHAR(1)
                  "IdUsuarioCertificador"  AS "IDUSUARIOCERTIFICADOR", -- BIGINT
                  "CodigoUsuarioCertificador"  AS "CODIGOUSUARIOCERTIFICADOR", -- VARCHAR(40)
                  "FechaCertificacion"  AS "FECHACERTIFICACION", -- VARCHAR(10)
                  "IdPersonaPagoCobro"  AS "IDPERSONAPAGOCOBRO", -- BIGINT
                  "IdContraparte"  AS "IDCONTRAPARTE", -- BIGINT
                  "EsIndicadorBilateral"  AS "ESINDICADORBILATERAL", -- VARCHAR(1)
                  "IdEmpleadoDigitador"  AS "IDEMPLEADODIGITADOR", -- BIGINT
                  "IdEjecutivo"  AS "IDEJECUTIVO", -- BIGINT
                  "IdSucursal"  AS "IDSUCURSAL", -- BIGINT
                  "IdModulo"  AS "IDMODULO", -- VARCHAR(3)
                  "IdUsuario"  AS "IDUSUARIO", -- BIGINT
                  "CodigoUsuarioCreador"  AS "CODIGOUSUARIOCREADOR", -- VARCHAR(40)
                  "NombreUsuario"  AS "NOMBREUSUARIO", -- VARCHAR(302)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "IdCarga"  AS "IDCARGA", -- BIGINT
                  "SecuenciaMovimientoMigracion"  AS "SECUENCIAMOVIMIENTOMIGRACION", -- BIGINT
                  "IdTipoMovimientoFondoMutuo"  AS "IDTIPOMOVIMIENTOFONDOMUTUO", -- VARCHAR(10)
                  "IdRegimenTributario"  AS "IDREGIMENTRIBUTARIO", -- VARCHAR(40)
                  "IdMatrizAgrupador"  AS "IDMATRIZAGRUPADOR", -- BIGINT
                  "EsADC"  AS "ESADC", -- VARCHAR(1)
                  "ExcluyeMovimientoEnCartola" AS "EXCLUYEMOVIMIENTOENCARTOLA" -- NVARCHAR(1)

        from "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'CuentaInversion_MovimientosCuentaInversion' AND WST."Key" = "IdCargoAbonoMovimiento"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_MovimientosCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

                                    
