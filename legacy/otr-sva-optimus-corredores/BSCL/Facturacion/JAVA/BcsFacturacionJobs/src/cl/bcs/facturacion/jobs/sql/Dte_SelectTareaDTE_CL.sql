WITH TareaDTE AS 
(
	SELECT 
    	TAR."IdTareaDTE" 						AS "idtareadte",
		TAR."IdTareaDTEGrupo" 					AS "idtareadtegrupo",
		TAR."TipoTareaDTE" 						AS "tipotareadte",
		TAR."IdRespuestaDTE" 					AS "idrespuestadte",
		TAR."RespuestaDTEEnvioSII" 				AS "respuestadteenviosii",
		TAR."RespuestaLocationDTE" 				AS "respuestalocationdte",
		TAR."RespuestaPdfDTE" 					AS "respuestapdfdte",
		TAR."RespuestaXmlDTE" 					AS "respuestaxmldte",
		TAR."FolioComprobanteRespuestaDTE" 		AS "foliocomprobanterespuestadte",
		TAR."IdComprobante" 					AS "idcomprobante",		
		TAR."IdEstadoTareaDTE" 					AS "idestadotareadte",		
		TAR."IdEstadoDTE" 						AS "idestadodte",
		TAR."IdEstadoSII" 						AS "idestadosii",
		TAR."IdTipoComprobante" 				AS "idtipocomprobante",
		TAR."TipoComprobanteDTE" 				AS "tipocomprobantedte",
		TAR."AccionTipoOperacion" 				AS "acciontipooperacion",
		TAR."PeriodoContable" 					AS "periodocontable",
		TAR."FechaComprobante" 					AS "fechacomprobante",
		TAR."FechaVencimiento" 					AS "fechavencimiento",
		TAR."CodigoVendedor" 					AS "codigovendedor",
		TAR."CodigoReceptor" 					AS "codigoreceptor",
             		
		TAR."GlosaAjusteComision" 				AS "glosaajustecomision",
		TAR."TotalComisionCmbte" 				AS "totalcomisioncmbte",
 		TAR."GlosaAjusteDerecho" 				AS "glosaajustederecho",
		TAR."TotalDerechoCmbte" 				AS "totalderechocmbte",
		TAR."GlosaAjusteGasto" 					AS "glosaajustegasto",
		TAR."TotalGastoCmbte" 					AS "totalgastocmbte",
		TAR."GlosaAjusteRetencion" 				AS "glosaajusteretencion",
		TAR."TotalRetencionCmbte" 				AS "totalretencioncmbte",                		
		TAR."IdComprobanteMontoNoFacturable" 	AS "idcomprobantemontonofacturable",
		TAR."MontoNeto" 						AS "montoNeto",
		TAR."IdComprobanteMontoExento" 			AS "idcomprobantemontoexento",              		
		TAR."IdComprobanteTasaIva" 				AS "idcomprobantetasaiva",           		
		TAR."MontoIva" 							AS "montoIva",
		TAR."IdComprobanteMontoTotal" 			AS "idcomprobantemontototal",
		TAR."IdComprobanteValorAPagar" 			AS "idcomprobantevalorapagar",
   		
		TAR."TotalIdTipoComprobanteMontoNeto" 			AS "totalidtipocomprobantemontoneto",
		TAR."TotalIdTipoComprobanteMontoExento" 		AS "totalidtipocomprobantemontoexento",
		TAR."TotalIdTipoComprobanteMontoIva" 			AS "totalidtipocomprobantemontoiva",
		TAR."TotalIdTipoComprobanteMontoTotal" 			AS "totalidtipocomprobantemontototal",
		TAR."TotalIdTipoComprobanteCantidadDocumentos" 	AS "totalidtipocomprobantecantidaddocumentos",
		TAR."TotalIdTipoComprobanteCantidadAnulados" 	AS "totalidtipocomprobantecantidadanulados",               		
		TAR."DocumentoIdentidadEmisor" 			AS "documentoidentidademisor",
		TAR."RazonSocialEmisor" 				AS "razonsocialemisor",
		TAR."GiroEmisor" 						AS "giroemisor",
		TAR."DireccionEmisor" 					AS "direccionemisor",
		TAR."ComunaEmisor" 						AS "comunaemisor",
		TAR."CiudadEmisor" 						AS "ciudademisor",
		TAR."EmailEmisor" 						AS "emailemisor",
		TAR."CondicionPago" 					AS "condicionpago",
		TAR."TelefonoEmisor" 					AS "telefonoemisor",
		TAR."EmailReceptor" 					AS "emailreceptor",               		
		TAR."DocumentoIdentidadReceptor" 		AS "documentoidentidadreceptor",
		TAR."CodigoSucursal" 					AS "codigoSucursal",
		TAR."RazonSocialReceptor" 				AS "razonsocialreceptor",
		TAR."GiroReceptor" 						AS "giroreceptor",
		TAR."DireccionReceptor" 				AS "direccionreceptor",
		TAR."ComunaReceptor" 					AS "comunareceptor",
		TAR."CiudadReceptor" 					AS "ciudadreceptor",
		TAR."ContactoReceptor" 					AS "contactoreceptor",
                		
		TAR."TipoReferenciaIdComprobante" 		AS "tiporeferenciaidComprobante",
		TAR."FolioReferenciaIdComprobante" 		AS "folioreferenciaidcomprobante",
		CAST(TAR."FechaReferenciaIdComprobante" AS NVARCHAR(10)) AS "fechareferenciaidComprobante",
		TAR."RazonReferenciaIdComprobante" 		AS "razonreferenciaidComprobante",                		
		TAR."TipoReferenciaFolioComprobante" 	AS "tiporeferenciafoliocomprobante",
		TAR."FolioReferenciaFolioComprobante" 	AS "folioreferenciafoliocomprobante",
		TAR."FechaReferenciaFolioComprobante" 	AS "fechareferenciafoliocomprobante",
		TAR."RazonReferenciaFolioComprobante" 	AS "razonreferenciafoliocomprobante",                		
		TAR."NumeroReferenciaNota" 				AS "numeroreferencianota",
		TAR."TipoReferenciaNota" 				AS "tiporeferencianota",
		TAR."FolioReferenciaNota" 				AS "folioreferencianota",
		TAR."FechaReferenciaNota" 				AS "fechareferencianota",
		TAR."RazonReferenciaNota" 				AS "razonreferencianota",
		TAR."CodigoReferenciaNota" 				AS "codigoreferencianota",

		TAR."TipoReferenciaOtrosConceptos"		AS "tipoReferenciaOtrosConceptos",
        TAR."FolioReferenciaOtrosConceptos"		AS "folioReferenciaOtrosConceptos",
        TAR."FechaReferenciaOtrosConceptos"		AS "fechaReferenciaOtrosConceptos",
        TAR."RazonReferenciaOtrosConceptos"		AS "razonReferenciaOtrosConceptos",
                		
   		-- Si la glosa supera los 80 caracteres se debe continuar en el campo idComprobantedetalledescripcion(despues de COMPRA / VENTA). La plantilla del pdf de factura usa esta logica para armar el detalle 
   		CASE
       		WHEN LENGTH(COALESCE("IdComprobanteDetalleGlosa",'')) > 80 THEN SUBSTRING ("IdComprobanteDetalleGlosa",1,80)
       		ELSE COALESCE("IdComprobanteDetalleGlosa",'')
   		END AS "idcomprobantedetalleglosa",
   		
   		CASE
       		WHEN LENGTH(COALESCE("IdComprobanteDetalleGlosa",'')) > 80 THEN 
       			COALESCE("IdComprobanteDetalleDescripcion",'') || 
       			CASE WHEN (LENGTH(TRIM(TRAILING FROM SUBSTRING ("IdComprobanteDetalleGlosa",1,80))) < LENGTH(SUBSTRING ("IdComprobanteDetalleGlosa",1,80))) THEN ' ' ELSE '' END ||
       			SUBSTRING ("IdComprobanteDetalleGlosa",81,LENGTH(COALESCE("IdComprobanteDetalleGlosa",'')))
       		ELSE COALESCE("IdComprobanteDetalleDescripcion",'')
   		END AS "idComprobantedetalledescripcion",
                		
		TAR."IdComprobanteDetalleCantidad" 		AS "idcomprobantedetallecantidad",
   		TAR."IdComprobanteDetallePrecio" 		AS "idcomprobantedetalleprecio",
   		TAR."IdComprobanteDetallePorcentaje" 	AS "idcomprobantedetalleporcentaje",
   		TAR."IdComprobanteDetalleMontoDescuento" AS "idcomprobantedetallemontodescuento",
   		TAR."IdComprobanteDetalleMonto" 		AS "idcomprobantedetallemonto",
   		CASE 
   			WHEN (TAR."Moneda" IS NOT NULL  AND  TAR."Moneda" <> 'CLP') THEN TAR."PrecioOtraMoneda"
   			ELSE null
   		END AS "precioOtraMoneda",
   		TAR."FactorConversion" AS "factorConversion",
   		CASE 
   			WHEN (TAR."Moneda" IS NOT NULL  AND  TAR."Moneda" <> 'CLP') THEN TAR."Moneda"
   			ELSE null
   		END AS "moneda",
   		TAR."MontoLiquidacionFinal" 		AS "montoLiquidacionFinal",
   		TAR."IdComprobanteDetalleEsExento" 	AS "idcomprobantedetalleesexento",                  		
   		TAR."ParametroKeyProducto" 			AS "parametrokeyproducto",
   		TAR."TipoComprobanteDTEPE" 			AS "tipocomprobantedtepe", 
   		TAR."AccionTipoOperacionCmbtePE" 	AS "acciontipooperacioncmbtepe", 
   		TAR."MonedaTransaccionPE" 			AS "monedatransaccionpe", 
   		TAR."MonedaLiquidacionPE" 			AS "monedaliquidacionpe", 
   		TAR."FechaLiquidacionPE" 			AS "fechaliquidacionpe", 
   		TAR."NacionalidadReceptorPE" 		AS "nacionalidadreceptorpe", 
   		TAR."ProductoOperacionPE" 			AS "productooperacionpe", 
   		TAR."CantidadTotalPE" 				AS "cantidadtotalpe", 
   		TAR."PrecioPromedioPE" 				AS "preciopromediope", 
   		TAR."ImporteTotalPE" 				AS "importetotalpe", 
   		TAR."ImporteLiquidacionPE" 			AS "importeliquidacionpe", 
   		TAR."ImporteLiquidacionPE" 			AS "totalliquidacionpe", 
   		TAR."MonedaImportePE" 				AS "monedaimportepe", 
   		TAR."ImporteAPagarPE" 				AS "importeapagarpe", 
		TAR."GrupoDireccionesCliente"		AS "grupoDireccionesElectronicas",
   		'0' 								AS "nombreInstrumento",
		'0' 								AS "cantidadTotal",
		'0' 								AS "valorPrecioPromedio", 
		--Cmbte."IdBolsa"                     AS "indBolsa",
		
		CASE 
			WHEN  Cmbte."IdBolsa" IS NULL THEN ''
				ELSE Cmbte."IdBolsa" 
			END AS "indBolsa",
		CASE
		  WHEN Cmbte."IdConcepto"= 'COMMEN' THEN 'COBRO MENSUAL POR CUSTODIA NO APV Custodia - ' || (SELECT "MES"
		  																							 FROM (SELECT TO_BIGINT(1) "NUMERO",'Enero' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(2) "NUMERO",'Febrero' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(3) "NUMERO",'Marzo' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(4) "NUMERO",'Abril' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(5) "NUMERO",'Mayo' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(6) "NUMERO",'Junio' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(7) "NUMERO",'Julio' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(8) "NUMERO",'Agosto' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(9) "NUMERO",'Septiembre' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(10) "NUMERO",'Octubre' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(11) "NUMERO",'Noviembre' AS "MES" FROM DUMMY
		  																							       UNION ALL SELECT  TO_BIGINT(12) "NUMERO",'Diciembre' AS "MES" FROM DUMMY
		  																							      )
		  																							 WHERE "NUMERO" = TO_BIGINT(MONTH(Cmbte."FechaLiquidacion"))) || ' ' || YEAR(Cmbte."FechaLiquidacion")
		ELSE ''
		END AS "indMensaje_1",
		IFNULL(Cmbte."IdTipoInstrumento" ,'')	AS "idTipoInstrumento",
		TAR."CodigoMonedaBancoCentral" 			AS "codigoMonedaBancoCentral",
		MON."DescripcionBancoCentral" 			AS "descripcionMonedaBancoCentral",
		TAR."NemoMoneda"						AS "nemoMoneda",
		COALESCE(Cmbte."IdConcepto",'')			AS "idConcepto",
		TAR."FactorConversionSecundario" AS "factorConversionSecundario",
		TAR."PrecioOtraMonedaSecundaria" AS "precioOtraMonedaSecundaria",
		TAR."MonedaSecundaria" AS "monedaSecundaria"		
	FROM "BSCL.Facturacion.BD::FAC.TareaDTE" AS TAR
	  INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte
	     ON Cmbte."IdComprobante" = TAR."IdComprobante"
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON
		ON MON."CodigoBancoCentral" = TAR."CodigoMonedaBancoCentral"
	WHERE TAR."IdTareaDTEGrupo" = :IdTareaDTEGrupo
)
SELECT *
FROM TareaDTE
WHERE ("idestadodte" IN ('ENV', 'REC') AND "tipotareadte" = 'DTEACT')
OR    ("idestadotareadte" = 'PEN'      AND "tipotareadte" <> 'DTEACT');

	
        		
