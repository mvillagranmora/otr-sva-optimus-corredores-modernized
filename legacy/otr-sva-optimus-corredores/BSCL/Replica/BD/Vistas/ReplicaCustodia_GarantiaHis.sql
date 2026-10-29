SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_GarantiaHis"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_GarantiaHis" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdGarantia" || ':' || to_char("FechaRespaldo",'YYYY-MM-DD')  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "FechaRespaldo"  AS "FECHARESPALDO", -- VARCHAR(27)
                  "IdGarantia"  AS "IDGARANTIA", -- BIGINT
                  "FolioGarantia"  AS "FOLIOGARANTIA", -- BIGINT
                  "FolioCarteraOrigen"  AS "FOLIOCARTERAORIGEN", -- BIGINT
                  "FolioCarteraGarantizada"  AS "FOLIOCARTERAGARANTIZADA", -- BIGINT
                  "EsEspecifica"  AS "ESESPECIFICA", -- VARCHAR(1)
                  "IdInstrumentoCarteraGarantizada"  AS "IDINSTRUMENTOCARTERAGARANTIZADA", -- BIGINT
                  "NemotecnicoGarantizado"  AS "NEMOTECNICOGARANTIZADO", -- VARCHAR(40)
                  "IdTipoInstrumentoGarantizado"  AS "IDTIPOINSTRUMENTOGARANTIZADO", -- VARCHAR(3)
                  "IdContraparteGarantia"  AS "IDCONTRAPARTEGARANTIA", -- BIGINT
                  "RazonSocialContraparte"  AS "RAZONSOCIALCONTRAPARTE", -- VARCHAR(100)
                  "IdOrigenGarantia"  AS "IDORIGENGARANTIA", -- VARCHAR(3)
                  "NombreOrigenGarantia"  AS "NOMBREORIGENGARANTIA", -- VARCHAR(40)
                  "IdEspecieGarantia"  AS "IDESPECIEGARANTIA", -- BIGINT
                  "DescripcionEspecie"  AS "DESCRIPCIONESPECIE", -- VARCHAR(100)
                  "FechaIngresoRegistro"  AS "FECHAINGRESOREGISTRO", -- VARCHAR(27)
                  "FechaModificacionRegistro"  AS "FECHAMODIFICACIONREGISTRO", -- VARCHAR(27)
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdUsuario"  AS "IDUSUARIO", -- BIGINT
                  "IdModulo"  AS "IDMODULO", -- VARCHAR(3)
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "NombreCliente"  AS "NOMBRECLIENTE", -- VARCHAR(302)
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "IdTipoProductoOperacionOrigen"  AS "IDTIPOPRODUCTOOPERACIONORIGEN", -- VARCHAR(4)
                  "IdTipoCanalIngreso"  AS "IDTIPOCANALINGRESO", -- VARCHAR(3)
                  "IdMonedaSistema"  AS "IDMONEDASISTEMA", -- VARCHAR(3)
                  "NombreMonedaSistema"  AS "NOMBREMONEDASISTEMA", -- VARCHAR(143)
                  "CodigoIFRS"  AS "CODIGOIFRS", -- VARCHAR(40)
                  "IdConcepto"  AS "IDCONCEPTO", -- VARCHAR(6)
                  "NombreConcepto"  AS "NOMBRECONCEPTO", -- VARCHAR(100)
                  "IdTipoOperacionOperacionOrigen"  AS "IDTIPOOPERACIONOPERACIONORIGEN", -- VARCHAR(6)
                  "FechaOperacion"  AS "FECHAOPERACION", -- VARCHAR(10)
                  "FechaVencimientoOperacion"  AS "FECHAVENCIMIENTOOPERACION", -- VARCHAR(10)
                  "PlazoEnDiasOperacion"  AS "PLAZOENDIASOPERACION", -- BIGINT
                  "FolioExternoReferencia"  AS "FOLIOEXTERNOREFERENCIA", -- VARCHAR(40)
                  "IdMesaOperacionOrigen"  AS "IDMESAOPERACIONORIGEN", -- BIGINT
                  "IdBolsaOperacionOrigen"  AS "IDBOLSAOPERACIONORIGEN", -- VARCHAR(3)
                  "IdSucursal"  AS "IDSUCURSAL", -- BIGINT
                  "IdAgente"  AS "IDAGENTE", -- BIGINT
                  "IdCondicionLiquidacion"  AS "IDCONDICIONLIQUIDACION", -- VARCHAR(2)
                  "FechaLiquidacion"  AS "FECHALIQUIDACION", -- VARCHAR(10)
                  "IdClasificacionTipoInstrumento"  AS "IDCLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(3)
                  "ClasificacionTipoInstrumento"  AS "CLASIFICACIONTIPOINSTRUMENTO", -- VARCHAR(100)
                  "IdTipoInstrumento"  AS "IDTIPOINSTRUMENTO", -- VARCHAR(3)
                  "NombreTipoInstrumento"  AS "NOMBRETIPOINSTRUMENTO", -- VARCHAR(100)
                  "IdInstrumento"  AS "IDINSTRUMENTO", -- BIGINT
                  "Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
                  "NombreInstrumento"  AS "NOMBREINSTRUMENTO", -- VARCHAR(100)
                  "IdEmisor"  AS "IDEMISOR", -- VARCHAR(40)
                  "IdTipoReajuste"  AS "IDTIPOREAJUSTE", -- VARCHAR(3)
                  "FechaEmisionInstrumento"  AS "FECHAEMISIONINSTRUMENTO", -- VARCHAR(10)
                  "FechaVencimientoInstrumento"  AS "FECHAVENCIMIENTOINSTRUMENTO", -- VARCHAR(10)
                  "EsFungible"  AS "ESFUNGIBLE", -- VARCHAR(1)
                  "IdBovedaOperacionOrigen"  AS "IDBOVEDAOPERACIONORIGEN", -- VARCHAR(3)
                  "IdMonedaInstrumento"  AS "IDMONEDAINSTRUMENTO", -- VARCHAR(3)
                  "MontoGarantiaMonedaInstrumento"  AS "MONTOGARANTIAMONEDAINSTRUMENTO", -- DECIMAL
                  "NombreMonedaInstrumento"  AS "NOMBREMONEDAINSTRUMENTO", -- VARCHAR(100)
                  "ClasificacionRiesgo"  AS "CLASIFICACIONRIESGO", -- VARCHAR(20)
                  "IdTipoComprobanteOperacionOrigen"  AS "IDTIPOCOMPROBANTEOPERACIONORIGEN", -- VARCHAR(6)
                  "FolioComprobanteOperacionOrigen"  AS "FOLIOCOMPROBANTEOPERACIONORIGEN", -- BIGINT
                  "FechaComprobanteOperacionOrigen"  AS "FECHACOMPROBANTEOPERACIONORIGEN", -- VARCHAR(10)
                  "Cantidad"  AS "CANTIDAD", -- DECIMAL
                  "PrecioGarantia"  AS "PRECIOGARANTIA", -- DECIMAL
                  "MontoGarantia"  AS "MONTOGARANTIA", -- DECIMAL
                  "CorteMinimo"  AS "CORTEMINIMO", -- DECIMAL
                  "ValorRescate"  AS "VALORRESCATE", -- DECIMAL
                  "PrecioCompraOperacionOrigen"  AS "PRECIOCOMPRAOPERACIONORIGEN", -- DECIMAL
                  "TasaCompraOperacionOrigen"  AS "TASACOMPRAOPERACIONORIGEN", -- DECIMAL
                  "PrecioPromedioCompraOperacionOrigen"  AS "PRECIOPROMEDIOCOMPRAOPERACIONORIGEN", -- DECIMAL
                  "MontoPrecioPromedioCompraOperacionOrigen"  AS "MONTOPRECIOPROMEDIOCOMPRAOPERACIONORIGEN", -- DECIMAL
                  "MontoPrecioPromedioMonedaInstrumentoOperacionOrigen"  AS "MONTOPRECIOPROMEDIOMONEDAINSTRUMENTOOPERACIONORIGEN", -- DECIMAL
                  "ValorTipoCambioCierre"  AS "VALORTIPOCAMBIOCIERRE", -- DECIMAL
                  "INGFechaOperacion"  AS "INGFECHAOPERACION", -- VARCHAR(10)
                  "INGFechaLiquidacion"  AS "INGFECHALIQUIDACION", -- VARCHAR(10)
                  "INGCantidad"  AS "INGCANTIDAD", -- DECIMAL
                  "CantidadGarantia"  AS "CANTIDADGARANTIA", -- DECIMAL
                  "INGPrecioGarantia"  AS "INGPRECIOGARANTIA", -- DECIMAL
                  "INGValorRescate"  AS "INGVALORRESCATE", -- DECIMAL
                  "INGMonto"  AS "INGMONTO", -- DECIMAL
                  "INGMontoATasaCompraActual"  AS "INGMONTOATASACOMPRAACTUAL", -- DECIMAL
                  "MontoValorizado"  AS "MONTOVALORIZADO", -- DECIMAL
                  "INGMontoATasaCompraAnterior"  AS "INGMONTOATASACOMPRAANTERIOR", -- DECIMAL
                  "INGPrecioMercadoActual"  AS "INGPRECIOMERCADOACTUAL", -- DECIMAL
                  "INGTasaMercadoActual"  AS "INGTASAMERCADOACTUAL", -- DECIMAL
                  "INGMontoATasaMercadoActual"  AS "INGMONTOATASAMERCADOACTUAL", -- DECIMAL
                  "INGMontoATasaMercadoAnterior"  AS "INGMONTOATASAMERCADOANTERIOR", -- DECIMAL
                  "IdTipoInstrumentoAval"  AS "IDTIPOINSTRUMENTOAVAL", -- VARCHAR(3)
                  "NombreTipoInstrumentoAval"  AS "NOMBRETIPOINSTRUMENTOAVAL", -- VARCHAR(100)
                  "IdClasificacionTipoInstrumentoAval"  AS "IDCLASIFICACIONTIPOINSTRUMENTOAVAL", -- VARCHAR(3)
                  "NombreClasificacionTipoInstrumentoAval"  AS "NOMBRECLASIFICACIONTIPOINSTRUMENTOAVAL", -- VARCHAR(100)
                  "ValorClasificacionRiesgo"  AS "VALORCLASIFICACIONRIESGO", -- VARCHAR(20)
                  "IdEntidadClasificadoraRiesgo"  AS "IDENTIDADCLASIFICADORARIESGO", -- VARCHAR(10)
                  "IdTipoClasificacionRiesgo"  AS "IDTIPOCLASIFICACIONRIESGO", -- VARCHAR(4)
                  "INGDurationCompraActual"  AS "INGDURATIONCOMPRAACTUAL", -- DECIMAL
                  "INGPrecioMercadoProximo"  AS "INGPRECIOMERCADOPROXIMO", -- DECIMAL
                  "INGTasaMercadoProximo"  AS "INGTASAMERCADOPROXIMO", -- DECIMAL
                  "INGMontoATasaCompraProximo"  AS "INGMONTOATASACOMPRAPROXIMO", -- DECIMAL
                  "INGMontoATasaMercadoProximo"  AS "INGMONTOATASAMERCADOPROXIMO", -- DECIMAL
                  "INGMontoInteresCompraProximo"  AS "INGMONTOINTERESCOMPRAPROXIMO", -- DECIMAL
                  "INGDurationCompraProximo"  AS "INGDURATIONCOMPRAPROXIMO", -- DECIMAL
                  "INGMontoReajusteCompraProximo"  AS "INGMONTOREAJUSTECOMPRAPROXIMO", -- DECIMAL
                  "INGFuenteTasaPrecioMercadoActual"  AS "INGFUENTETASAPRECIOMERCADOACTUAL", -- VARCHAR(40)
                  "INGFechaValorizacionGarantia"  AS "INGFECHAVALORIZACIONGARANTIA", -- VARCHAR(10)
                  "INGPrecioMercadoAnterior"  AS "INGPRECIOMERCADOANTERIOR", -- DECIMAL
                  "EGRCantidad"  AS "EGRCANTIDAD", -- DECIMAL
                  "EGRFechaLiquidacion"  AS "EGRFECHALIQUIDACION", -- VARCHAR(10)
                  "EGRFechaOperacion"  AS "EGRFECHAOPERACION", -- VARCHAR(10)
                  "ISIN"  AS "ISIN", -- VARCHAR(40)
                  "INGMontoInteresCorridoCompraActual"  AS "INGMONTOINTERESCORRIDOCOMPRAACTUAL", -- DECIMAL
                  "INGMontoReajusteCorridoCompraActual"  AS "INGMONTOREAJUSTECORRIDOCOMPRAACTUAL", -- DECIMAL
                  "INGMontoInteresCorridoMercadoActual"  AS "INGMONTOINTERESCORRIDOMERCADOACTUAL", -- DECIMAL
                  "INGMontoReajusteCorridoMercadoActual"  AS "INGMONTOREAJUSTECORRIDOMERCADOACTUAL", -- DECIMAL
                  "INGFechaValorizacionAnterior"  AS "INGFECHAVALORIZACIONANTERIOR", -- VARCHAR(10)
                  "INGFechaValorizacionActual"  AS "INGFECHAVALORIZACIONACTUAL", -- VARCHAR(10)
                  "ValorNominal"  AS "VALORNOMINAL", -- DECIMAL
                  "EGRIdConcepto"  AS "EGRIDCONCEPTO", -- VARCHAR(6)
				  "EsGarantiaPrendaria" AS "ESGARANTIAPRENDARIA"	--CHAR(1)


        from "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaHisAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Custodia_GarantiaHis' AND WST."Key" = ("IdGarantia" || ':' || to_char("FechaRespaldo",'YYYY-MM-DD'))

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCustodia_GarantiaHis" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
