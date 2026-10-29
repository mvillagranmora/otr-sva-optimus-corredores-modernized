SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_IngresoEgreso"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTesoreria_IngresoEgreso" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdIngresoEgreso"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdIngresoEgreso"  AS "IDINGRESOEGRESO", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "FechaMovimiento"  AS "FECHAMOVIMIENTO", -- VARCHAR(10)
                  "TipoMovimiento"  AS "TIPOMOVIMIENTO", -- VARCHAR(30)
                  "IdConcepto"  AS "IDCONCEPTO", -- VARCHAR(6)
                  "NombreConcepto"  AS "NOMBRECONCEPTO", -- VARCHAR(100)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "DecimalesMoneda"  AS "DECIMALESMONEDA", -- BIGINT
                  "NombreMoneda"  AS "NOMBREMONEDA", -- VARCHAR(100)
                  "Monto"  AS "MONTO", -- DECIMAL
                  "MontoEgreso"  AS "MONTOEGRESO", -- DECIMAL
                  "MontoIngreso"  AS "MONTOINGRESO", -- DECIMAL
                  "MontoConSigno"  AS "MONTOCONSIGNO", -- DECIMAL
                  "IdCondicionLiquidacion"  AS "IDCONDICIONLIQUIDACION", -- VARCHAR(2)
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "IdCaja"  AS "IDCAJA", -- BIGINT
                  "NombreCaja"  AS "NOMBRECAJA", -- VARCHAR(100)
                  "IdPersonaTitular"  AS "IDPERSONATITULAR", -- BIGINT
                  "IdTipoPersonaTitular"  AS "IDTIPOPERSONATITULAR", -- VARCHAR(3)
                  "IdTipoDocumentoIdentidadTitular"  AS "IDTIPODOCUMENTOIDENTIDADTITULAR", -- VARCHAR(3)
                  "DocumentoIdentidadTitular"  AS "DOCUMENTOIDENTIDADTITULAR", -- VARCHAR(40)
                  "NombrePersonaTitular"  AS "NOMBREPERSONATITULAR", -- VARCHAR(302)
                  "IdPortafolioTitular"  AS "IDPORTAFOLIOTITULAR", -- BIGINT
                  "CodigoPortafolioTitular"  AS "CODIGOPORTAFOLIOTITULAR", -- VARCHAR(100)
                  "NombrePortafolioTitular"  AS "NOMBREPORTAFOLIOTITULAR", -- VARCHAR(100)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdTipoPersona"  AS "IDTIPOPERSONA", -- VARCHAR(3)
                  "IdTipoDocumentoIdentidadPersona"  AS "IDTIPODOCUMENTOIDENTIDADPERSONA", -- VARCHAR(3)
                  "DocumentoIdentidadPersona"  AS "DOCUMENTOIDENTIDADPERSONA", -- VARCHAR(40)
                  "TipoPersona"  AS "TIPOPERSONA", -- VARCHAR(40)
                  "IdClientePersona"  AS "IDCLIENTEPERSONA", -- BIGINT
                  "IdSocioDeNegocioPersona"  AS "IDSOCIODENEGOCIOPERSONA", -- BIGINT
                  "NombrePersona"  AS "NOMBREPERSONA", -- VARCHAR(302)
                  "IdPortafolioPersona"  AS "IDPORTAFOLIOPERSONA", -- BIGINT
                  "NombrePortafolioPersona"  AS "NOMBREPORTAFOLIOPERSONA", -- VARCHAR(100)
                  "CodigoPortafolioPersona"  AS "CODIGOPORTAFOLIOPERSONA", -- VARCHAR(100)
                  "Glosa"  AS "GLOSA", -- VARCHAR(260)
                  "IdModuloEnviaMovimiento"  AS "IDMODULOENVIAMOVIMIENTO", -- VARCHAR(3)
                  "IdUsuarioEnviaMovimiento"  AS "IDUSUARIOENVIAMOVIMIENTO", -- BIGINT
                  "IdMovimientoRelacionado"  AS "IDMOVIMIENTORELACIONADO", -- BIGINT
                  "SecuenciaAgrupacion"  AS "SECUENCIAAGRUPACION", -- VARCHAR(40)
                  "IdEmpleadoDigitador"  AS "IDEMPLEADODIGITADOR", -- BIGINT
                  "IdSucursalEmpleadoDigitador"  AS "IDSUCURSALEMPLEADODIGITADOR", -- BIGINT
                  "IdTipoComprobanteOrigen"  AS "IDTIPOCOMPROBANTEORIGEN", -- VARCHAR(6)
                  "IdComprobanteOrigen"  AS "IDCOMPROBANTEORIGEN", -- BIGINT
                  "FechaComprobanteOrigen"  AS "FECHACOMPROBANTEORIGEN", -- VARCHAR(10)
                  "FechaLiquidacionComprobanteOrigen"  AS "FECHALIQUIDACIONCOMPROBANTEORIGEN", -- VARCHAR(10)
                  "IdTipoProducto"  AS "IDTIPOPRODUCTO", -- VARCHAR(4)
                  "NombreTipoProducto"  AS "NOMBRETIPOPRODUCTO", -- VARCHAR(100)
                  "IdOperacion"  AS "IDOPERACION", -- BIGINT
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "NombreTipoOperacion"  AS "NOMBRETIPOOPERACION", -- VARCHAR(100)
                  "FechaOperacion"  AS "FECHAOPERACION", -- VARCHAR(10)
                  "IdMonedaOperacion"  AS "IDMONEDAOPERACION", -- VARCHAR(3)
                  "MontoOperacion"  AS "MONTOOPERACION", -- DECIMAL
                  "IdBolsa"  AS "IDBOLSA", -- VARCHAR(3)
                  "FechaParidad"  AS "FECHAPARIDAD", -- VARCHAR(10)
                  "ValorParidad"  AS "VALORPARIDAD", -- DECIMAL
                  "FolioCartera"  AS "FOLIOCARTERA", -- BIGINT
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdTipoReajuste"  AS "IDTIPOREAJUSTE", -- VARCHAR(3)
                  "EsBilateral"  AS "ESBILATERAL", -- VARCHAR(1)
                  "IdUsuarioEmisionComprobante"  AS "IDUSUARIOEMISIONCOMPROBANTE", -- BIGINT
                  "NombreUsuarioEmisionComprobante"  AS "NOMBREUSUARIOEMISIONCOMPROBANTE", -- VARCHAR(302)
                  "FechaHoraEmisionComprobante"  AS "FECHAHORAEMISIONCOMPROBANTE", -- VARCHAR(19)
                  "IdCarga"  AS "IDCARGA", -- BIGINT
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "IdComprobante"  AS "IDCOMPROBANTE", -- BIGINT
                  "IdMedioDePago"  AS "IDMEDIODEPAGO", -- VARCHAR(3)
                  "NombreMedioDePago"  AS "NOMBREMEDIODEPAGO", -- VARCHAR(100)
                  "IdModalidadDePago"  AS "IDMODALIDADDEPAGO", -- VARCHAR(6)
                  "NombreModalidadDePago"  AS "NOMBREMODALIDADDEPAGO", -- VARCHAR(100)
                  "IdUsuarioAnulacion"  AS "IDUSUARIOANULACION", -- BIGINT
                  "FechaHoraAnulacion"  AS "FECHAHORAANULACION", -- VARCHAR(19)
                  "NombreEmpresa"  AS "NOMBREEMPRESA", -- VARCHAR(100)
                  "IdMatrizAgrupador"  AS "IDMATRIZAGRUPADOR", -- BIGINT
                  "EsADC"  AS "ESADC" -- VARCHAR(1)

        from "BSCL.Tesoreria.BD.Vistas::IngresoEgresoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Tesoreria_IngresoEgreso' AND WST."Key" = "IdIngresoEgreso"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTesoreria_IngresoEgreso" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
