SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteUI";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteUI" As 
SELECT	 
			comprobante."IdComprobante",
			comprobante."IdConcepto",
			ConcPla."Nombre" AS "NombreConcepto",
		 	comprobante."IdPortafolio" ,
 	   		COALESCE(comprobante."ModPrefFact",'') AS "ModPrefFact",
 	   		COALESCE("DetalleTipoIndicador"."DetalleIndicador",'') AS "ModPrefFactDescripcion",
		 	port."CodigoPortafolio",
		 	port."Proposito",
		 	(port."CodigoPortafolio" ||' '|| port."Proposito") As "NombrePortafolio",
		 	comprobante."IdCliente" ,
		 	comprobante."DocumentoIdentidad" ,
		 	comprobante."TipoDocumentoIdentidad" ,
		 	comprobante."NombreLargoCliente" ,
		 	comprobante."IdEmpleadoEjecutivoPortafolio",
		 	comprobante."IdSucursalEmpleadoEjecutivoPortafolio",
		 	comprobante."FolioComprobante" ,
		 	comprobante."IdTipoComprobante" ,
		 	tipocomprobante."Codigo" AS "CodigoTipoComprobante" ,
		 	tipocomprobante."Nombre" AS "NombreTipoComprobante" ,
		 	INDIC."ValorIndicador" AS "SeparaPorInstrumento",
		 	--Operacion
		 	comprobante."IdMoneda",
		 	moneda."Nombre" AS "NombreMoneda",
		 	--Local
		 	comprobante."IdMonedaComprobante",
		 	monedaLocal."Nombre" AS "NombreMonedaComprobante",
		 	--Liquidacion
		 	comprobante."IdMonedaLiquidacion",		 	
		 	monedaLiq."Nombre" AS "NombreMonedaLiquidacion",
		 	comprobante."IdTipoOperacion" ,		 	
		 	tipooperacion."Nombre" AS "NombreTipoOperacion",
		 	tipoproducto."Codigo" AS "CodigoTipoProducto",
		 	tipoproducto."Nombre" AS "NombreTipoProducto",
		 	comprobante."IdClasificacionTipoInstrumento" ,
		 	comprobante."IdTipoInstrumento",
		 	comprobante."Cantidad" AS "CantidadTotal",
		 	--Montos Comprobantes
		 	comprobante."Monto",
		 	comprobante."MontoComprobante",
		 	comprobante."MontoLiquidacion",
		 	--Montos Cobros
		 	comprobante."MontoCobro",
		 	"MontoComprobanteCobro",		 	
		 	comprobante."MontoLiquidacionCobro",
		 	--Montos Comprobantes Finales
		 	comprobante."MontoFinal",
    	 	comprobante."MontoComprobanteFinal",
		 	comprobante."MontoLiquidacionFinal",
		 	IFNULL(comprobante."MontoPagado",0) as "MontoPagado" ,
            comprobante."NombreEmpleadoEjecutivoPortafolio" ,
			CASE
				WHEN comprobante."IdTipoComprobante" LIKE 'N%' AND comprobante."IdTipoOperacion" NOT IN ('COMSPT','VENSPT','ANCSPT','ANVSPT') THEN medioent."MedioDePagoEntregamos"
				ELSE mediorec."MedioDePagoRecibimos"
			END AS "MedioDePagoRecibimos",
			CASE
				WHEN comprobante."IdTipoComprobante" LIKE 'N%' AND comprobante."IdTipoOperacion" NOT IN ('COMSPT','VENSPT','ANCSPT','ANVSPT')  THEN mediorec."MedioDePagoRecibimos"
				ELSE medioent."MedioDePagoEntregamos"
			END AS "MedioDePagoEntregamos",
            comprobante."NombreSucursalEmpleadoEjecutivoPortafolio",
		 	comprobante."IdCondicionLiquidacion",
		 	comprobante."FechaLiquidacion" ,
		 	comprobante."FechaEnvio" ,	
		 	comprobante."IdRespuestaDTE" ,	
		 	comprobante."RespuestaDTEEnvioSII",
		 	comprobante."RespuestaLocationDTE",
		 	comprobante."RespuestaPdfDTE",
		 	comprobante."RespuestaXmlDTE",
		 	comprobante."IdEstadoDTE" ,	
		 	comprobante."IdEstadoComprobante",
		 	estadodte."Nombre" AS "NombreEstadoDTE" ,
		 	comprobante."IdEstadoSII" ,
		 	estadosii."Nombre" AS "NombreEstadoSII" ,
		 	comprobante."IdComprobanteReferencia",
		 	comprobante."IdTipoComprobanteReferencia",
		 	comprobante."FechaComprobante" ,	 	
		 	comprobante."IdEmpresa" ,
		 	estado."Nombre" AS "NombreEstado" ,
		 	comprobante."IdBolsa",
		 	comprobante."EsExento",
		 	comprobante."EsVentaCorta" As "EsVentaCorta",
            Case 
	          When comprobante."IdUsuario" <> 0 Then Usuario."CodigoUsuario"
	        Else ''
	        End As "CodigoUsuario", 
			Case 
	          When comprobante."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then comprobante."NemotecnicoSubyacente" 
	        Else comprobante."NemotecnicoInstrumento"
	        End As "NemotecnicoInstrumento",
			comprobante."Precio",
			comprobante."ValorTipoCambio",			
			ConsFormCusOrd."Nombre" AS "NombreFormaCustodia",
	        CuentaCustodioPortafolio."Codigo" As "CodigoBoveda",
	        BOVEDA."Nombre" As "NombreBoveda",
			comprobante."CodigoBancoCustodio", 
			comprobante."CodigoBancoLiquidador",
			CuentaCustodioPortafolio."NombreCompletoBancoCustodio",
			CuentaCustodioPortafolio."NombreCompletoBancoLiquidador",			
			comprobante."Tasa",
			comprobante."FechaVencimiento",
			comprobante."DiasPlazo",
			comprobante."ModalidadSMV",
			comprobante."MercadoSMV",
			comprobante."Intereses",
			comprobante."ReajustesVAC",
			comprobante."CodigoBroker", 
			comprobante."NombreTipoCliente", 
			comprobante."EsClienteFinal",
			comprobante."EsAPlazo",
			case 
				when INDIC."ValorIndicador" = 'S' then 
					case 
						when comprobante."Cantidad" = 0 then 0
						else (comprobante."Monto"/comprobante."Cantidad")
					end
				else 0
			end AS "PPP",
			DocInd."DocumentoIdentidad" AS "DocumentoIdentidadCAVALI"
			
			,IFNULL(tblCobros."TotalComis",0) as "TotalComis"
		 	,IFNULL(tblCobros."TotalImpto",0) as "TotalImpto"
		 	,IFNULL(tblCobros."TotalDere",0) as "TotalDere"
		 	,IFNULL(tblCobros."TotalGasto",0) as "TotalGasto"
		 	,IFNULL(tblCobros."TotalReten",0) as "TotalRetencion"
		 	,IFNULL(tblCobros."IdMonedaCobro",'') AS "IdMonedaCobro"
		 	
		 	,IFNULL(tblCobrosCmbte."TotalComisCmbte",0) as "TotalComisCmbte"
		 	,IFNULL(tblCobrosCmbte."TotalImptoCmbte",0) as "TotalImptoCmbte"
		 	,IFNULL(tblCobrosCmbte."TotalDereCmbte",0) as "TotalDereCmbte"
		 	,IFNULL(tblCobrosCmbte."TotalGastoCmbte",0) as "TotalGastoCmbte"
		 	,IFNULL(tblCobrosCmbte."TotalRetenCmbte",0) as "TotalRetencionCmbte"
		 	,IFNULL(tblCobrosCmbte."IdMonedaComprobanteCobro",'') AS "IdMonedaComprobanteCobro"
		 	
		 	,IFNULL(tblCobrosLiq."TotalComisLiq",0) as "TotalComisLiq"
		 	,IFNULL(tblCobrosLiq."TotalImptoLiq",0) as "TotalImptoLiq"
		 	,IFNULL(tblCobrosLiq."TotalDereLiq",0) as "TotalDereLiq"
		 	,IFNULL(tblCobrosLiq."TotalGastoLiq",0) as "TotalGastoLiq"
		 	,IFNULL(tblCobrosLiq."TotalRetenLiq",0) as "TotalRetencionLiq"
		 	,IFNULL(tblCobrosLiq."IdMonedaLiquidacionCobro",'') AS "IdMonedaLiquidacionCobro"
		 	,IFNULL(comprobante."CodigoIFRS",'')   AS "CodigoIFRS"
		 	
		 	,IFNULL(tblComisiones."ComisionBVL",0) AS "ComisionBVL"
		 	,IFNULL(tblComisiones."ComisionSAB",0) AS "ComisionSAB"
		 	,IFNULL(tblComisiones."ComisionSMV",0) AS "ComisionSMV"
		 	,IFNULL(tblComisiones."ComisionCAVALI",0) AS "ComisionCAVALI"
		 	,IFNULL(tblComisiones."FondoGarantia",0) AS "FondoGarantia"
		 	,IFNULL(tblComisiones."FondoLiquidacion",0) AS "FondoLiquidacion"
		 	,IFNULL(tblComisiones."OtrasComisiones",0) AS "OtrasComisiones"
			,IFNULL(tblComisiones."ImpuestoIGV",0) AS "ImpuestoIGV"
			
			,IFNULL(tblComisionSabDis."MontoDisponible",0) AS "ComisionSABDisponible"
			,CCPBCC."IdCorredorCorresponsal"
 	   		,COALESCE(CCPBCC."Codigo", '') AS "CodigoCorredorCorresponsal"
 	   		,comprobante."IdTipoCanalIngreso"
 	   		,COALESCE(comprobante."IdTipoLiquidacionComisionFactura",'') AS "IdTipoLiquidacionComisionFactura"
 	   		,COALESCE(CTLF."TipoLiquidacion",'') AS "TipoLiquidacion"
			,ConcFac."Contrapartida"
			,comprobante."FechaIngresoRegistro"
			,comprobante."MayorMenorValor"
			,comprobante."MontoRetencionGananciaCap"
			,comprobante."CargoGananciaCap" AS "IdTipoRetencion"
			,tipooperacion."EsOtrosConceptos"
			,COALESCE(comprobante."EstadoCaja", 'NRE') AS "EstadoCaja"
	  FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS comprobante
	  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = comprobante."IdTipoComprobante"	  
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS moneda ON moneda."IdMoneda" = comprobante."IdMoneda"
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS monedaLocal ON monedaLocal."IdMoneda" = comprobante."IdMonedaComprobante"
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS monedaLiq ON monedaLiq."IdMoneda" = comprobante."IdMonedaLiquidacion"	  
	  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS estadofac ON estadofac."IdEstado" = comprobante."IdEstadoComprobante" and Upper(estadofac."NombreEntidad") = Upper('FAC.Comprobante') 	               
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estado  ON estado."IdEstado" = estadofac."IdEstado"
	  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS estadofacdte  ON estadofacdte."IdEstado" = comprobante."IdEstadoDTE" and Upper(estadofacdte."NombreEntidad") = Upper('FAC.Comprobante')
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estadodte  ON estadodte."IdEstado" = estadofacdte."IdEstado"
	  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS estadofacsii ON estadofacsii."IdEstado" = comprobante."IdEstadoSII" and Upper(estadofacsii."NombreEntidad") = Upper('FAC.Comprobante')
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estadosii ON estadosii."IdEstado" = estadofacsii."IdEstado" 
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS BOVEDA  ON BOVEDA."IdBoveda" = comprobante."IdBoveda"            
	  LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as port ON port."IdPortafolio" = comprobante."IdPortafolio"
	  LEFT OUTER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" As ConsCli ON ConsCli."IdCliente" = comprobante."IdCliente"
	  LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocInd ON DocInd."IdPersona" = ConsCli."IdPersona" AND DocInd."IdTipoDocumentoIdentidad" = 'RUT' 
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooperacion ON tipooperacion."IdTipoOperacion" = comprobante."IdTipoOperacion"
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS tipoproducto ON tipoproducto."IdTipoProducto" = comprobante."IdTipoProducto"
	  LEFT JOIN "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" AS ConsFormCusOrd ON ConsFormCusOrd."IdTipoFormaCustodiaOrden" = comprobante."IdFormaCustodia"
	  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolioUI" AS CuentaCustodioPortafolio ON CuentaCustodioPortafolio."IdCuentaCustodioPortafolio" = comprobante."IdCuentaCustodioPortafolio"
	  LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" As Usuario On Usuario."IdUsuario" = comprobante."IdUsuario" 
	  LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS ConcFac ON ConcFac."IdConcepto" = comprobante."IdConcepto"
      LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConcPla ON ConcPla."IdConcepto" = ConcFac."IdConcepto"   
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIPINDIC ON (TIPINDIC."IdEmpresa" =comprobante."IdEmpresa" AND TIPINDIC."CodigoTipoIndicador"='FAC_SFPINS')
      LEFT OUTER JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDIC ON(INDIC."IdTipoIndicador"=TIPINDIC."IdTipoIndicador" AND INDIC."IdTipoEntidad"='PORTAFOLIO' AND INDIC."IdEntidad"=comprobante."IdPortafolio")
      
	  LEFT OUTER JOIN (
    	   SELECT  "IdComprobante",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoCobro" ELSE 0 END) AS "TotalComis",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoCobro" ELSE 0 END) AS "TotalDere",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoCobro" ELSE 0 END) AS "TotalGasto",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoCobro" ELSE 0 END) AS "TotalImpto",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoCobro" ELSE 0 END) AS "TotalReten",
                    MAX("IdMonedaCobro") AS "IdMonedaCobro"
             FROM (
					SELECT  CmbteCobro."IdComprobante",
							Cobro."IdCobroClasificacion", 
							CobroClasificacion."CodigoCobroClasificacion",
							IFNULL(SUM(CmbteCobro."MontoCobro"),0) as "MontoCobro",
							CmbteCobro."IdMonedaCobro"
					  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
					GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaCobro"
					ORDER BY Cobro."IdCobroClasificacion"
             )
             GROUP BY "IdComprobante"
       	) AS tblCobros on tblCobros."IdComprobante" = comprobante."IdComprobante"
       	
       	LEFT OUTER JOIN (
    	   SELECT  "IdComprobante",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalComisCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalDereCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalGastoCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalImptoCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalRetenCmbte",
                    MAX("IdMonedaComprobanteCobro") AS "IdMonedaComprobanteCobro"
             FROM (
					SELECT  CmbteCobro."IdComprobante",
							Cobro."IdCobroClasificacion", 
							CobroClasificacion."CodigoCobroClasificacion",
							IFNULL(SUM(CmbteCobro."MontoComprobanteCobro"),0) as "MontoLocalCobro",
							CmbteCobro."IdMonedaComprobanteCobro"
					  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
					GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaComprobanteCobro"
					ORDER BY Cobro."IdCobroClasificacion"
             )
             group by "IdComprobante"
       	) AS tblCobrosCmbte on tblCobrosCmbte."IdComprobante" = comprobante."IdComprobante"
       	       	
       	Left Join (
    	   Select "IdComprobante",
                   Max(Case When "CodigoCobroClasificacion" = 'COMISION' Then "MontoLiquidacionCobro" Else 0 End) As "TotalComisLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'DERECHO' Then "MontoLiquidacionCobro" Else 0 End) As "TotalDereLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'GASTO' Then "MontoLiquidacionCobro" Else 0 End) As "TotalGastoLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'IMPUESTO' Then "MontoLiquidacionCobro" Else 0 End) As "TotalImptoLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'RETENCION' Then "MontoLiquidacionCobro" Else 0 End) As "TotalRetenLiq",
                   MAX("IdMonedaLiquidacionCobro") AS "IdMonedaLiquidacionCobro"
           From (
					SELECT  CmbteCobro."IdComprobante",
							Cobro."IdCobroClasificacion", 
							CobroClasificacion."CodigoCobroClasificacion",
							IFNULL(SUM(CmbteCobro."MontoLiquidacionCobro"),0) as "MontoLiquidacionCobro",
							CmbteCobro."IdMonedaLiquidacionCobro"
					  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
					  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
					GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaLiquidacionCobro"
					ORDER BY Cobro."IdCobroClasificacion"
                  )
           Group by "IdComprobante"
       	) As tblCobrosLiq on tblCobrosLiq."IdComprobante" = comprobante."IdComprobante"
       	
       	LEFT JOIN (
       		SELECT "IdComprobante",
					MAX(CASE WHEN "IdClaseCobro" = 'COMISION_BVL' THEN "MontoLiquidacionCobro" ELSE 0 END) AS "ComisionBVL",
					MAX(CASE WHEN "IdClaseCobro" = 'COMISION_SAB' THEN "MontoLiquidacionCobro" ELSE 0 END) AS "ComisionSAB",
					MAX(CASE WHEN "IdClaseCobro" = 'COMISION_SMV' THEN "MontoLiquidacionCobro" ELSE 0 END) AS "ComisionSMV",
					MAX(CASE WHEN "IdClaseCobro" = 'COMISION_CAVALI' THEN "MontoLiquidacionCobro" ELSE 0 END) AS "ComisionCAVALI",
					MAX(CASE WHEN "IdClaseCobro" = 'FONDO_GARANTIA' THEN "MontoLiquidacionCobro" ELSE 0 END) AS "FondoGarantia",
					MAX(CASE WHEN "IdClaseCobro" = 'FONDO_LIQUIDACION' THEN "MontoLiquidacionCobro" ELSE 0 END) AS "FondoLiquidacion",
					MAX(CASE WHEN "IdClaseCobro" = 'COMISION_GENERICA' THEN "MontoLiquidacionCobro" ELSE 0 END) AS "OtrasComisiones",
					MAX(CASE WHEN "IdClaseCobro" = 'IMPUESTO_IGV' THEN "MontoLiquidacionCobro" ELSE 0 END) AS "ImpuestoIGV"
			FROM (
				SELECT "IdComprobante",
						"IdClaseCobro",
						IFNULL(SUM("MontoLiquidacionCobro"),0) AS "MontoLiquidacionCobro"
				FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" AS ComprobanteCobros
				INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" AS DefinicionCobro ON DefinicionCobro."CodigoDefinicionCobro" = ComprobanteCobros."IdCobro"
				GROUP BY "IdComprobante","IdClaseCobro"
			)
			GROUP BY "IdComprobante"
       	) AS tblComisiones ON tblComisiones."IdComprobante" = comprobante."IdComprobante"
       
       	LEFT JOIN (
       	    SELECT  CmbteCobro."IdComprobante", 
       	            IFNULL(SUM("MontoLiquidacionCobro"),0) - IFNULL((SELECT Sum("MontoLiquidacion")
       	                                                             FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte 
        	                                                         WHERE Cmbte."IdComprobanteReferencia" = CmbteCobro."IdComprobante" 
       	                                                               AND Cmbte."IdEstadoComprobante" <> 'ANU'),0) AS "MontoDisponible"
       	    FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" AS CmbteCobro                                                            
       	     INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" As Cobro 
       	        On Cobro."CodigoDefinicionCobro" = CmbteCobro."IdCobro"
       	       AND Cobro."IdClaseCobro" = 'COMISION_SAB'
       	    WHERE CmbteCobro."IdEstado" = 'VIG'   
       	    GROUP BY  CmbteCobro."IdComprobante" 
       	) AS tblComisionSabDis ON tblComisionSabDis."IdComprobante" = comprobante."IdComprobante"
       	 LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarProductoBovedaCorredorCorresponsal" AS CCPBCC
   			ON CCPBCC."IdTipoProducto"  = comprobante."IdTipoProducto"
   			AND CCPBCC."IdBoveda" = comprobante."IdBoveda"
	   LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFactura" AS CTLF
	  	ON CTLF."IdTipoLiquidacionComisionFactura" = comprobante."IdTipoLiquidacionComisionFactura"
	  		AND CTLF."IdEstado" = 'ACT'
	   LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS "DetalleTipoIndicador" 
	    ON "DetalleTipoIndicador"."CodigoTipoIndicador" = 'FAC_MODFAC' 
	    	AND "DetalleTipoIndicador"."Codigo" = comprobante."ModPrefFact" 
	    	AND "DetalleTipoIndicador"."IdEmpresa" = comprobante."IdEmpresa"
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMedioDePagoRecibimos" mediorec
	ON mediorec."IdComprobante"=comprobante."IdComprobante"
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMedioDePagoEntregamos" medioent
	ON medioent."IdComprobante"=comprobante."IdComprobante";
      	
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 