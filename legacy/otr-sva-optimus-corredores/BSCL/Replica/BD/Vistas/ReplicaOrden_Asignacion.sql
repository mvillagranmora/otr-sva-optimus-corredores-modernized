SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaOrden_Asignacion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaOrden_Asignacion" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdAsignacion"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdAsignacion"  AS "IDASIGNACION", -- BIGINT
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "FechaAsignacion"  AS "FECHAASIGNACION", -- VARCHAR(27)
                  "IdEstadoAsignacion"  AS "IDESTADOASIGNACION", -- VARCHAR(3)
                  "CantidadAsignada"  AS "CANTIDADASIGNADA", -- DECIMAL
                  "MontoAsignado"  AS "MONTOASIGNADO", -- DECIMAL
                  "ValorRescateAsignado"  AS "VALORRESCATEASIGNADO", -- DECIMAL
                  "MontoAsignadoLocal"  AS "MONTOASIGNADOLOCAL", -- DECIMAL
                  "TipoCambio"  AS "TIPOCAMBIO", -- DECIMAL
                  "MontoAsignadoLiquidacion"  AS "MONTOASIGNADOLIQUIDACION", -- DECIMAL
                  "IdTransaccion"  AS "IDTRANSACCION", -- BIGINT
                  "FolioTransaccion"  AS "FOLIOTRANSACCION", -- VARCHAR(40)
                  "FechaOperacionTransaccion"  AS "FECHAOPERACIONTRANSACCION", -- VARCHAR(19)
                  "CantidadTransaccion"  AS "CANTIDADTRANSACCION", -- DECIMAL
                  "PrecioTransaccion"  AS "PRECIOTRANSACCION", -- DECIMAL
                  "ValorRescateTransaccion"  AS "VALORRESCATETRANSACCION", -- DECIMAL
                  "TasaTransaccion"  AS "TASATRANSACCION", -- DECIMAL
                  "MontoTransaccion"  AS "MONTOTRANSACCION", -- DECIMAL
                  "IdTransaccionOd"  AS "IDTRANSACCIONOD", -- BIGINT
                  "FolioTransaccionOD"  AS "FOLIOTRANSACCIONOD", -- VARCHAR(40)
                  "IdTransaccionPlazo"  AS "IDTRANSACCIONPLAZO", -- BIGINT
                  "IdEstadoTransaccion"  AS "IDESTADOTRANSACCION", -- VARCHAR(3)
                  "IdOperadorCorredor"  AS "IDOPERADORCORREDOR", -- VARCHAR(40)
                  "IdOperadorContraparte"  AS "IDOPERADORCONTRAPARTE", -- VARCHAR(40)
                  "IdCorredorContraparte"  AS "IDCORREDORCONTRAPARTE", -- BIGINT
                  "CodigoCorredorContraparte"  AS "CODIGOCORREDORCONTRAPARTE", -- VARCHAR(40)
                  "IdBolsaTransaccion"  AS "IDBOLSATRANSACCION", -- VARCHAR(3)
                  "IndicadorOrdenDirecta"  AS "INDICADORORDENDIRECTA", -- VARCHAR(2)
                  "IdOrden"  AS "IDORDEN", -- BIGINT
                  "CantidadOrden"  AS "CANTIDADORDEN", -- DECIMAL
                  "IdTipoPrecioOrden"  AS "IDTIPOPRECIOORDEN", -- VARCHAR(1)
                  "PrecioOrden"  AS "PRECIOORDEN", -- DECIMAL
                  "TasaOrden"  AS "TASAORDEN", -- DECIMAL
                  "ValorRescateOrden"  AS "VALORRESCATEORDEN", -- DECIMAL
                  "IdTipoVigenciaOrden"  AS "IDTIPOVIGENCIAORDEN", -- VARCHAR(2)
                  "FechaVigenciaOrden"  AS "FECHAVIGENCIAORDEN", -- VARCHAR(10)
                  "FechaOperacionOrden"  AS "FECHAOPERACIONORDEN", -- VARCHAR(19)
                  "GlosaOrden"  AS "GLOSAORDEN", -- VARCHAR(520)
                  "IdLibroOrdenes"  AS "IDLIBROORDENES", -- BIGINT
                  "NombreLibroOrdenes"  AS "NOMBRELIBROORDENES", -- VARCHAR(100)
                  "EsAPlazo"  AS "ESAPLAZO", -- VARCHAR(1)
                  "FolioCartera"  AS "FOLIOCARTERA", -- BIGINT
                  "IdMovimientoComprobante"  AS "IDMOVIMIENTOCOMPROBANTE", -- BIGINT
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "IdMoneda"  AS "IDMONEDA", -- VARCHAR(3)
                  "IdTipoReajuste"  AS "IDTIPOREAJUSTE", -- VARCHAR(3)
                  "EsFungible"  AS "ESFUNGIBLE", -- VARCHAR(1)
                  "IdEmisor"  AS "IDEMISOR", -- VARCHAR(40)
                  "IdInstrumentoSubyacente"  AS "IDINSTRUMENTOSUBYACENTE", -- BIGINT
                  "NemotecnicoSubyacente"  AS "NEMOTECNICOSUBYACENTE", -- VARCHAR(40)
                  "IdMonedaSubyacente"  AS "IDMONEDASUBYACENTE", -- VARCHAR(3)
                  "IdCondicionDeLiquidacion"  AS "IDCONDICIONDELIQUIDACION", -- VARCHAR(2)
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "IdMonedaLiquidacion"  AS "IDMONEDALIQUIDACION", -- VARCHAR(3)
                  "FechaVencimiento"  AS "FECHAVENCIMIENTO", -- VARCHAR(10)
                  "Plazo"  AS "PLAZO", -- BIGINT
                  "EsVentaCorta"  AS "ESVENTACORTA", -- VARCHAR(1)
                  "IndicadorDCV"  AS "INDICADORDCV", -- VARCHAR(1)
                  "IdTipoOperacion"  AS "IDTIPOOPERACION", -- VARCHAR(6)
                  "PrecioMercado"  AS "PRECIOMERCADO", -- DECIMAL
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
                  "IdTipoCanalIngreso"  AS "IDTIPOCANALINGRESO", -- VARCHAR(3)
                  "IdProducto"  AS "IDPRODUCTO", -- VARCHAR(4)
                  "IdPorcentajeVisibilidad"  AS "IDPORCENTAJEVISIBILIDAD", -- BIGINT
				  "IdOperacionSpot" AS "IDOPERACIONSPOT" -- BIGINT

        from "BSCL.Orden.BD.Vistas::ConsultarAsignacionAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Orden_Asignacion' AND WST."Key" = "IdAsignacion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaOrden_Asignacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
