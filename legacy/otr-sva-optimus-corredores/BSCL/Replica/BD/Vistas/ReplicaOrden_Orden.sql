SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaOrden_Orden"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaOrden_Orden" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdOrden"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdOrden"  AS "IDORDEN", -- BIGINT
                  "FechaRecepcion"  AS "FECHARECEPCION", -- VARCHAR(19)
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "FechaIngresoRegistroCorta"  AS "FECHAINGRESOREGISTROCORTA", -- VARCHAR(10)
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "Nombres"  AS "NOMBRES", -- VARCHAR(100)
                  "ApellidoPaterno"  AS "APELLIDOPATERNO", -- VARCHAR(100)
                  "ApellidoMaterno"  AS "APELLIDOMATERNO", -- VARCHAR(100)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "IdPortafolioCliente"  AS "IDPORTAFOLIOCLIENTE", -- BIGINT
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "DescripcionPortafolio"  AS "DESCRIPCIONPORTAFOLIO", -- VARCHAR(100)
                  "PropositoPortafolio"  AS "PROPOSITOPORTAFOLIO", -- VARCHAR(520)
                  "IdProducto"  AS "IDPRODUCTO", -- VARCHAR(4)
                  "NombreProducto"  AS "NOMBREPRODUCTO", -- VARCHAR(100)
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "NombreClasificacionTipoInstrumento"  AS "NOMBRECLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(100)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "NombreTipoInstrumento"  AS "NOMBRETIPOINSTRUMENTO", -- VARCHAR(100)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "ISIN"  AS "ISIN", -- VARCHAR(40)
                  "EsFungible"  AS "ESFUNGIBLE", -- VARCHAR(1)
                  "IdEmisor"  AS "IDEMISOR", -- VARCHAR(40)
                  "EspecificaBolsa"  AS "ESPECIFICABOLSA", -- VARCHAR(1)
                  "IdBolsa"  AS "IDBOLSA", -- VARCHAR(3)
                  "IdBolsaDistribucion"  AS "IDBOLSADISTRIBUCION", -- VARCHAR(3)
                  "FechaOferta"  AS "FECHAOFERTA", -- VARCHAR(19)
                  "Cantidad"  AS "CANTIDAD", -- DECIMAL
                  "ValorRescate"  AS "VALORRESCATE", -- DECIMAL
                  "PrecioOrden"  AS "PRECIOORDEN", -- DECIMAL
                  "PlazoSimultanea"  AS "PLAZOSIMULTANEA", -- BIGINT
                  "TasaSimultanea"  AS "TASASIMULTANEA", -- DECIMAL
                  "PlazoVentaCorta"  AS "PLAZOVENTACORTA", -- BIGINT
                  "TasaVentaCorta"  AS "TASAVENTACORTA", -- DECIMAL
                  "PlazoIF"  AS "PLAZOIF", -- BIGINT
                  "TasaRFIF"  AS "TASARFIF", -- DECIMAL
                  "IdTipoPrecio"  AS "IDTIPOPRECIO", -- VARCHAR(1)
                  "IdTipoTasa"  AS "IDTIPOTASA", -- VARCHAR(1)
                  "PrecioMercado"  AS "PRECIOMERCADO", -- DECIMAL
                  "IdTipoReajuste"  AS "IDTIPOREAJUSTE", -- VARCHAR(3)
                  "FechaEmision"  AS "FECHAEMISION", -- VARCHAR(10)
                  "IdCondicionDeLiquidacion"  AS "IDCONDICIONDELIQUIDACION", -- VARCHAR(2)
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "IdMonedaLiquidacionOrden"  AS "IDMONEDALIQUIDACIONORDEN", -- VARCHAR(3)
                  "MontoLiquidacion"  AS "MONTOLIQUIDACION", -- DECIMAL
                  "IdMonedaLiquidacionCobros"  AS "IDMONEDALIQUIDACIONCOBROS", -- VARCHAR(3)
                  "IdMonedaOrden"  AS "IDMONEDAORDEN", -- VARCHAR(3)
                  "MontoOrden"  AS "MONTOORDEN", -- DECIMAL
                  "IdMonedaLocal"  AS "IDMONEDALOCAL", -- VARCHAR(3)
                  "MontoLocal"  AS "MONTOLOCAL", -- DECIMAL
                  "TipoCambio"  AS "TIPOCAMBIO", -- DECIMAL
                  "FechaVencimiento"  AS "FECHAVENCIMIENTO", -- VARCHAR(10)
                  "IdTipoVigenciaOrden"  AS "IDTIPOVIGENCIAORDEN", -- VARCHAR(2)
                  "FechaVigencia"  AS "FECHAVIGENCIA", -- VARCHAR(10)
                  "FechaSolicitudAnulacion"  AS "FECHASOLICITUDANULACION", -- VARCHAR(19)
                  "FechaAnulacion"  AS "FECHAANULACION", -- VARCHAR(19)
                  "FechaAceptacion"  AS "FECHAACEPTACION", -- VARCHAR(19)
                  "FechaAutorizacion"  AS "FECHAAUTORIZACION", -- VARCHAR(10)
                  "Glosa"  AS "GLOSA", -- VARCHAR(520)
                  "IdEstadoOrden"  AS "IDESTADOORDEN", -- VARCHAR(3)
                  "NombreEstadoOrden"  AS "NOMBREESTADOORDEN", -- VARCHAR(100)
                  "IndicadorFacturacionInmediata"  AS "INDICADORFACTURACIONINMEDIATA", -- VARCHAR(1)
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "FechaOperacion"  AS "FECHAOPERACION", -- VARCHAR(19)
                  "IdTipoCanalIngreso"  AS "IDTIPOCANALINGRESO", -- VARCHAR(3)
                  "IdLibroOrdenes"  AS "IDLIBROORDENES", -- BIGINT
                  "IdEstadoAsignacion"  AS "IDESTADOASIGNACION", -- VARCHAR(3)
                  "IdCarga"  AS "IDCARGA", -- BIGINT
                  "EsVentaCorta"  AS "ESVENTACORTA", -- VARCHAR(1)
                  "SecOrdenMigracion"  AS "SECORDENMIGRACION", -- BIGINT
                  "IdTipoCanalDistribucion"  AS "IDTIPOCANALDISTRIBUCION", -- VARCHAR(3)
                  "IdInstrumentoSubyacente"  AS "IDINSTRUMENTOSUBYACENTE", -- BIGINT
                  "NemotecnicoSubyacente"  AS "NEMOTECNICOSUBYACENTE", -- VARCHAR(40)
                  "ISINSubyacente"  AS "ISINSUBYACENTE", -- VARCHAR(40)
                  "IdPorcentajeVisibilidad"  AS "IDPORCENTAJEVISIBILIDAD", -- BIGINT
                  "IdTipoCustodia"  AS "IDTIPOCUSTODIA", -- VARCHAR(40)
                  "IdFormaCustodia"  AS "IDFORMACUSTODIA", -- VARCHAR(3)
                  "Reservado"  AS "RESERVADO", -- VARCHAR(1)
                  "IdUsuarioReserva"  AS "IDUSUARIORESERVA", -- BIGINT
                  "NombreUsuarioReserva"  AS "NOMBREUSUARIORESERVA", -- VARCHAR(302)
                  "MotivoReserva"  AS "MOTIVORESERVA", -- VARCHAR(40)
                  "Bloqueado"  AS "BLOQUEADO", -- VARCHAR(1)
                  "IdUsuarioBloqueo"  AS "IDUSUARIOBLOQUEO", -- BIGINT
                  "NombreUsuarioBloqueo"  AS "NOMBREUSUARIOBLOQUEO", -- VARCHAR(302)
                  "MotivoBloqueo"  AS "MOTIVOBLOQUEO", -- VARCHAR(40)
                  "Folio"  AS "FOLIO", -- VARCHAR(40)
                  "IdEmpleadoPortafolio"  AS "IDEMPLEADOPORTAFOLIO", -- BIGINT
                  "NombreUsuarioPortafolio"  AS "NOMBREUSUARIOPORTAFOLIO", -- VARCHAR(302)
                  "IdEmpleadoDigitador"  AS "IDEMPLEADODIGITADOR", -- BIGINT
                  "NombreUsuarioDigitador"  AS "NOMBREUSUARIODIGITADOR", -- VARCHAR(302)
                  "CodigoClaseOrden"  AS "CODIGOCLASEORDEN", -- VARCHAR(40)
                  "CodigoProcedenciaOrden"  AS "CODIGOPROCEDENCIAORDEN", -- VARCHAR(3)
                  "IdMedioDePago"  AS "IDMEDIODEPAGO", -- VARCHAR(3)
                  "ComentarioBloqueo"  AS "COMENTARIOBLOQUEO", -- VARCHAR(520)
                  "IdTipoBloqueo"  AS "IDTIPOBLOQUEO", -- VARCHAR(40)
                  "IdPerfilComercialBaseOrigen"  AS "IDPERFILCOMERCIALBASEORIGEN", -- VARCHAR(40)
                  "IdPerfilProductos"  AS "IDPERFILPRODUCTOS", -- VARCHAR(40)
                  "IdPerfilCobros"  AS "IDPERFILCOBROS", -- VARCHAR(40)
                  "IdPerfilLimites"  AS "IDPERFILLIMITES", -- VARCHAR(40)
                  "IdTipoDesbloqueo"  AS "IDTIPODESBLOQUEO", -- VARCHAR(40)
                  "ComentarioDesbloqueo"  AS "COMENTARIODESBLOQUEO", -- VARCHAR(520)
                  "IdTelefonoContacto"  AS "IDTELEFONOCONTACTO", -- BIGINT
                  "IdDireccionEnvioCorrespondencia"  AS "IDDIRECCIONENVIOCORRESPONDENCIA", -- BIGINT
				  "IdInstrumentoNegociacion"  AS "IDINSTRUMENTONEGOCIACION", -- BIGINT
				  "GeneraSpot"  AS "GENERASPOT" -- VARCHAR (1)

        from "BSCL.Orden.BD.Vistas::ConsultarOrdenAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Orden_Orden' AND WST."Key" = "IdOrden"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaOrden_Orden" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
