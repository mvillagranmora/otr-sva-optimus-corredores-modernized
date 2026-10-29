SET SCHEMA "BSCL";

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMantenedorIgk";

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMantenedorIgk" AS
SELECT	 
			comprobante."IdComprobante",
			comprobante."IdConcepto",
			ConcPla."Nombre" AS "NombreConcepto",
			ConcFac."EsReembolso" AS "ConcepEsReembolso",
		 	comprobante."IdPortafolio" ,
		 	comprobante."ModPrefFact",
		 	viewportalio."CodigoPortafolio",
		 	viewportalio."Proposito",
		 	(viewportalio."CodigoPortafolio" ||' '|| viewportalio."Proposito") As "NombrePortafolio",
		 	comprobante."IdCliente" ,
		 	comprobante."DocumentoIdentidad" ,
		 	comprobante."TipoDocumentoIdentidad" ,
		 	comprobante."NombreLargoCliente" ,
		 	comprobante."DireccionFacturacion" ,
		 	comprobante."FolioComprobante" ,
		 	comprobante."IdTipoComprobante" ,
		 	tipocomprobante."Codigo" AS "CodigoTipoComprobante" ,
		 	tipocomprobante."Nombre" AS "NombreTipoComprobante" ,
		 	comprobante."IdMonedaSpot",
		 	comprobante."IdMoneda",
		 	moneda."Nombre" AS "NombreMoneda",
		 	moneda."Codigo" AS "CodigoMoneda" ,
		 	moneda."Simbolo" AS "SimboloMoneda" ,
		 	moneda."Decimales" AS "DecimalesMoneda" ,
		 	comprobante."IdMonedaComprobante",
		 	monedaLocal."Nombre" AS "NombreMonedaComprobante",
		 	monedaLocal."Codigo" AS "CodigoMonedaComprobante",
		 	monedaLocal."Simbolo" AS "SimboloMonedaComprobante",
		 	monedaLocal."Decimales" AS "DecimalesMonedaComprobante",
		 	comprobante."IdMonedaLiquidacion",		 	
		 	monedaLiq."Nombre" AS "NombreMonedaLiquidacion",
		 	monedaLiq."Codigo" AS "CodigoMonedaLiquidacion",
		 	monedaLiq."Simbolo" AS "SimboloMonedaLiquidacion",
		 	monedaLiq."Decimales" AS "DecimalesMonedaLiquidacion",		 	
		 	comprobante."IdTipoOperacion" ,		 	
		 	Case
		 	  When comprobante."EsVentaCorta" = 'S' THEN tipooperacion."Accion" || ' CORTA' 
		 	Else tipooperacion."Accion" 
		 	End As "AccionTipoOperacion",
		 	tipooperacion."Codigo" AS "CodigoTipoOperacion",
		 	tipooperacion."Nombre" AS "NombreTipoOperacion",
		 	comprobante."IdTipoProducto",
		 	tipoproducto."Codigo" AS "CodigoTipoProducto",
		 	tipoproducto."Nombre" AS "NombreTipoProducto",
		 	comprobante."IdClasificacionTipoInstrumento" ,
		 	comprobante."IdTipoInstrumento" ,
		 	TipIns."Nombre" As "NombreTipoInstrumento",
		 	comprobante."Cantidad" AS "CantidadTotal",
		 	Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(comprobante."Monto", FormCmbte."MontoDecimales", FormCmbte."MontoTrucaRedondea"), comprobante."Monto") As "Monto",
		 	Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(comprobante."MontoComprobante", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), comprobante."MontoComprobante") As "MontoComprobante",
		 	Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(comprobante."MontoLiquidacion", FormCmbteLiq."MontoDecimales", FormCmbteLiq."MontoTrucaRedondea"), comprobante."MontoLiquidacion") As "MontoLiquidacion",
		 	"MontoCobro",
		 	"MontoComprobanteCobro",		 	
		 	"MontoLiquidacionCobro",
		 	Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(comprobante."MontoFinal", FormCmbte."MontoDecimales", FormCmbte."MontoTrucaRedondea"), comprobante."MontoFinal") As "MontoFinal",
    	 	Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(comprobante."MontoComprobanteFinal", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), comprobante."MontoComprobanteFinal") As "MontoComprobanteFinal",
		 	Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(comprobante."MontoLiquidacionFinal", FormCmbteLiq."MontoDecimales", FormCmbteLiq."MontoTrucaRedondea"), comprobante."MontoLiquidacionFinal") As "MontoLiquidacionFinal",
		 	IFNULL(comprobante."MontoPagado",0) as "MontoPagado" ,
            comprobante."IdEmpleadoEjecutivoPortafolio" ,
            comprobante."NombreEmpleadoEjecutivoPortafolio" ,
            comprobante."IdSucursalEmpleadoEjecutivoPortafolio",
            comprobante."NombreSucursalEmpleadoEjecutivoPortafolio",
		 	comprobante."IdCondicionLiquidacion",
		    CondLiqu."Codigo" As "CodigoCondicionDeLiquidacion",
		    CondLiqu."Nombre" As "NombreCondicionDeLiquidacion",
		 	CASE comprobante."FechaLiquidacion" 
		 		WHEN '0001-01-01' THEN '' 
				ELSE TO_NVARCHAR ( comprobante."FechaLiquidacion" ) 
			END AS "FechaLiquidacionString" ,
		 	comprobante."FechaLiquidacion" ,
		 	CASE comprobante."FechaEmision" 
		 		WHEN '0001-01-01' THEN '' 
				ELSE TO_NVARCHAR ( comprobante."FechaEmision" ) 
			END AS "FechaEmisionString" ,
		 	comprobante."FechaEmision" ,
		 	CASE comprobante."FechaEnvio" 
		 		WHEN '0001-01-01' THEN '' 
				ELSE TO_NVARCHAR ( comprobante."FechaEnvio" ) 
			END AS "FechaEnvioString" ,
		 	comprobante."FechaEnvio" ,	
		 	comprobante."IdRespuestaDTE" ,	
		 	comprobante."RespuestaDTEEnvioSII",
		 	comprobante."RespuestaLocationDTE",
		 	comprobante."RespuestaPdfDTE",
		 	comprobante."RespuestaXmlDTE",
		 	comprobante."IdEstadoDTE" ,		 	
		 	estadodte."Nombre" AS "NombreEstadoDTE" ,
		 	comprobante."IdEstadoSII" ,
		 	estadosii."Nombre" AS "NombreEstadoSII" ,
		 	comprobante."FechaRespuestaEnvio" AS "FechaRespuestaEnvio",		 			 	
		 	comprobante."IdComprobanteReferencia",
		 	comprobante."IdTipoComprobanteReferencia",
		 	comprobante."FolioComprobanteReferencia",
		 	comprobante."FechaComprobanteReferencia",	
		 	comprobante."FechaComprobante" ,	 	
		 	comprobante."FechaIngresoRegistro" ,
		 	comprobante."FechaSistema" ,
		 	comprobante."IdEmpresa" ,
		 	empresa."Codigo" AS "CodigoEmpresa" ,
		 	empresa."Nombre" AS "NombreEmpresa" ,
		 	comprobante."IdEstadoComprobante" ,
		 	estado."Nombre" AS "NombreEstado" ,
		 	comprobante."IdBolsa",
		 	Bolsa."Nombre" As "NombreBolsa",
		 	comprobante."Glosa",
		 	comprobante."EsExento",
		 	CASE comprobante."EsVentaCorta" WHEN 'N'THEN 'NO' ELSE 'SI' end As "EsVentaCorta",
            comprobante."IdUsuario",
            Case 
	          When comprobante."IdUsuario" <> 0 Then Usuario."CodigoUsuario"
	        Else ''
	        End As "CodigoUsuario", 
			"IdInstrumento",			
			Case 
	          When comprobante."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then comprobante."NemotecnicoSubyacente" 
	        Else comprobante."NemotecnicoInstrumento"
	        End As "NemotecnicoInstrumento",
			"Precio",
			"ValorTipoCambio",			
			comprobante."IdBoveda",
			comprobante."IdFormaCustodia", 
			ConsFormCusOrd."Nombre" AS "NombreFormaCustodia",	        
            CuentaCustodioPortafolio."NombreBoveda" AS "NombreBovedaCuentaCustodioPortafolio",            
	        BOVEDA."Nombre" As "NombreBoveda",
	        BOVEDA."Codigo" AS "CodigoBoveda",	        
			comprobante."IdCuentaCustodioPortafolio", 
		   	comprobante."LiquidaBancoCustodio", 
			comprobante."IdPersonaBancoCustodio",
			comprobante."CodigoBancoCustodio", 
			comprobante."IdPersonaBancoLiquidador", 
			comprobante."CodigoBancoLiquidador",
			CuentaCustodioPortafolio."NombreModeloCustodia",
			CuentaCustodioPortafolio."NombreTipoCustodia",
			CuentaCustodioPortafolio."NombreCompletoBancoCustodio",
			CuentaCustodioPortafolio."NombreCompletoBancoLiquidador",			
			"Tasa",
			comprobante."FechaVencimiento",
			"DiasPlazo",
			"ModalidadSMV",
			"MercadoSMV",
			"Intereses",
			"ReajustesVAC",
			"IdPersonaBroker", 
			"CodigoBroker", 
			comprobante."IdTipoCliente", 
			"NombreTipoCliente", 
			comprobante."EsClienteFinal",
			comprobante."EsAPlazo",
			comprobante."ValorParidad",
			DocInd."IdTipoDocumentoIdentidad" AS "TipoDocumentoIdentidadCAVALI", 
			DocInd."DocumentoIdentidad" AS "DocumentoIdentidadCAVALI",
			comprobante."IdEstadoModPrefFact"			
			,IFNULL(tblCobros."TotalComis",0) as "TotalComis"
		 	,IFNULL(tblCobros."TotalImpto",0) as "TotalImpto"
		 	,IFNULL(tblCobros."TotalDere",0) as "TotalDere"
		 	,IFNULL(tblCobros."TotalGasto",0) as "TotalGasto"
		 	,IFNULL(tblCobros."TotalReten",0) as "TotalRetencion"		 	
		 	,IFNULL(tblCobrosCmbte."TotalComisCmbte",0) as "TotalComisCmbte"
		 	,IFNULL(tblCobrosCmbte."TotalImptoCmbte",0) as "TotalImptoCmbte"
		 	,IFNULL(tblCobrosCmbte."TotalDereCmbte",0) as "TotalDereCmbte"
		 	,IFNULL(tblCobrosCmbte."TotalGastoCmbte",0) as "TotalGastoCmbte"
		 	,IFNULL(tblCobrosCmbte."TotalRetenCmbte",0) as "TotalRetencionCmbte"		 	
		 	,IFNULL(tblCobrosLiq."TotalComisLiq",0) as "TotalComisLiq"
		 	,IFNULL(tblCobrosLiq."TotalImptoLiq",0) as "TotalImptoLiq"
		 	,IFNULL(tblCobrosLiq."TotalDereLiq",0) as "TotalDereLiq"
		 	,IFNULL(tblCobrosLiq."TotalGastoLiq",0) as "TotalGastoLiq"
		 	,IFNULL(tblCobrosLiq."TotalRetenLiq",0) as "TotalRetencionLiq"
		 	,IFNULL(comprobante."CodigoIFRS",'')   AS "CodigoIFRS"		 	
		 	,IFNULL(tblComisiones."ComisionBVL",0) AS "ComisionBVL"
		 	,IFNULL(tblComisiones."ComisionSAB",0) AS "ComisionSAB"
		 	,IFNULL(tblComisiones."ComisionSMV",0) AS "ComisionSMV"
		 	,IFNULL(tblComisiones."ComisionCAVALI",0) AS "ComisionCAVALI"
		 	,IFNULL(tblComisiones."FondoGarantia",0) AS "FondoGarantia"
		 	,IFNULL(tblComisiones."FondoLiquidacion",0) AS "FondoLiquidacion"
		 	,IFNULL(tblComisiones."OtrasComisiones",0) AS "OtrasComisiones"
			,IFNULL(tblComisiones."ImpuestoIGV",0) AS "ImpuestoIGV"
			,comprobante."MayorMenorValor" AS "MayorMenorValor"
			,comprobante."MontoRetencionGananciaCap" AS "MontoRetencionGananciaCap"
			,CASE ConsCli."IdTipoCliente" WHEN 'INSTITUCIONAL' THEN ConsCli."IdTipoCliente" ELSE 'NO INSTITUCIONAL' END AS "TipoCliente"
			,CASE WHEN (Cic."CodigoTipoIndicador"='NRESNODOM' OR Cic."CodigoTipoIndicador"='NRESNODOMPJ') AND Cic."ValorIndicador" = 'S'  THEN 'NO RESIDENTE' ELSE 'RESIDENTE'END AS "Residente"
	   		,CASE WHEN(Cic."CodigoTipoIndicador"='NRESNODOM' OR Cic."CodigoTipoIndicador"='NRESNODOMPJ') AND Cic."ValorIndicador" = 'S' 
	   		THEN  (SELECT CF."Valor" FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" CF where CF."CodigoParametro" = 'IGK_POR_RETEN_NORESIDENTE' AND CF."IdEmpresa" = comprobante."IdEmpresa")
	   		ELSE (SELECT CF."Valor" FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" CF where CF."CodigoParametro" = 'IGK_POR_RETEN_RESIDENTE' AND CF."IdEmpresa" = comprobante."IdEmpresa") END 
	   		AS "PorcentajeRetencionIgk"
	   		,CASE comprobante."IdFormaCustodia" WHEN 'NOM'THEN 'SI' ELSE 'NO' END AS "EsVentaNominal"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS comprobante
	  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante 
	     ON tipocomprobante."IdTipoComprobante" = comprobante."IdTipoComprobante"	  
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS empresa ON  empresa."IdEmpresa" = comprobante."IdEmpresa" 
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS moneda ON moneda."IdMoneda" = comprobante."IdMoneda"
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS monedaLocal ON monedaLocal."IdMoneda" = comprobante."IdMonedaComprobante"
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS monedaLiq ON monedaLiq."IdMoneda" = comprobante."IdMonedaLiquidacion"	  
	  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS estadofac 
	               ON estadofac."IdEstado" = comprobante."IdEstadoComprobante"
	               		and Upper(estadofac."NombreEntidad") = Upper('FAC.Comprobante') 	               
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estado 
	               ON estado."IdEstado" = estadofac."IdEstado"	               
	  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS estadofacdte 
	               ON estadofacdte."IdEstado" = comprobante."IdEstadoDTE" 	               
	               	and Upper(estadofacdte."NombreEntidad") = Upper('FAC.Comprobante')
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estadodte 
	               ON estadodte."IdEstado" = estadofacdte."IdEstado"
	  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS estadofacsii 
	               ON estadofacsii."IdEstado" = comprobante."IdEstadoSII" 	               
	               	and Upper(estadofacsii."NombreEntidad") = Upper('FAC.Comprobante')
	  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estadosii 
	               ON estadosii."IdEstado" = estadofacsii."IdEstado"
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS BOVEDA
	    ON BOVEDA."IdBoveda" = comprobante."IdBoveda"            
	  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as viewportalio 
	     ON viewportalio."IdPortafolio" = comprobante."IdPortafolio"
	  LEFT OUTER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" As ConsCli 
	    ON ConsCli."IdCliente" = comprobante."IdCliente"
	  LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocInd 
	    ON DocInd."IdPersona" = ConsCli."IdPersona"   
	   AND DocInd."IdTipoDocumentoIdentidad" = 'RUT' 
	   LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = ConsCli."IdPersona"
	   LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.TipoPersona" TIPER ON TIPER."IdTipoPersona" = PER."Tipo"
	   LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS CIC ON (Cic."IdCliente" = comprobante."IdCliente" AND CIC."CodigoTipoIndicador" IN ('NRESNODOM','NRESNODOMPJ') AND CIC."IdTipoPersona" = TIPER."IdTipoPersona")
	  	LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooperacion ON tipooperacion."IdTipoOperacion" = comprobante."IdTipoOperacion"
	  	LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS tipoproducto ON tipoproducto."IdTipoProducto" = comprobante."IdTipoProducto"
	  	LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Bolsa" As Bolsa ON Bolsa."IdBolsa" = comprobante."IdBolsa"
	 	LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" As TipIns On TipIns."IdTipoInstrumento" = comprobante."IdTipoInstrumento"
		LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarFormaCustodiaOrden" AS ConsFormCusOrd ON ConsFormCusOrd."IdFormaCustodiaOrden" = comprobante."IdFormaCustodia"
	  	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS CuentaCustodioPortafolio 
	  	ON CuentaCustodioPortafolio."IdCuentaCustodioPortafolio" = comprobante."IdCuentaCustodioPortafolio"
	  INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CondLiqu
	     ON CondLiqu."IdCondicionDeLiquidacion" = comprobante."IdCondicionLiquidacion"
	  Left Join "BSCL.Plataforma.BD::ACC.Usuario" As Usuario
         On Usuario."IdUsuario" = comprobante."IdUsuario"      
      LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS ConcFac
         ON ConcFac."IdConcepto" = comprobante."IdConcepto"
      LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConcPla
         ON ConcPla."IdConcepto" = ConcFac."IdConcepto" 
      LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbte
        On FormCmbte."IdMoneda" = comprobante."IdMoneda"
       And FormCmbte."IdTipoOperacion" = ''
       And FormCmbte."IdTipoInstrumento" = ''
       And FormCmbte."IdUsoMonedaFormato" = 'GEN' 
      LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbteLoc
        On FormCmbteLoc."IdMoneda" = comprobante."IdMonedaComprobante"
       And FormCmbteLoc."IdTipoOperacion" = ''
       And FormCmbteLoc."IdTipoInstrumento" = ''
       And FormCmbteLoc."IdUsoMonedaFormato" = 'GEN' 
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbteLiq
        On FormCmbteLiq."IdMoneda" = comprobante."IdMonedaLiquidacion"
       And FormCmbteLiq."IdTipoOperacion" = ''
       And FormCmbteLiq."IdTipoInstrumento" = ''
       And FormCmbteLiq."IdUsoMonedaFormato" = 'GEN'       
	  LEFT OUTER JOIN (
    	   SELECT  "IdComprobante",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoCobro" ELSE 0 END) AS "TotalComis",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoCobro" ELSE 0 END) AS "TotalDere",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoCobro" ELSE 0 END) AS "TotalGasto",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoCobro" ELSE 0 END) AS "TotalImpto",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoCobro" ELSE 0 END) AS "TotalReten"
             FROM (
                    SELECT       "IdComprobante",
                                  "IdCobroClasificacion", 
                                  "CodigoCobroClasificacion",
                                  IFNULL(SUM("MontoCobro"),0) as "MontoCobro"
                      FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"
                    GROUP BY "IdComprobante", "IdCobroClasificacion","CodigoCobroClasificacion"
                    ORDER BY "IdCobroClasificacion"
             )
             group by "IdComprobante"
       	) AS tblCobros on tblCobros."IdComprobante" = comprobante."IdComprobante"       	
       	LEFT OUTER JOIN (
    	   SELECT  "IdComprobante",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalComisCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalDereCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalGastoCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalImptoCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalRetenCmbte"
             FROM (
                    SELECT       "IdComprobante",
                                  "IdCobroClasificacion", 
                                  "CodigoCobroClasificacion",
                                  IFNULL(SUM("MontoLocalCobro"),0) as "MontoLocalCobro"
                      FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"
                    GROUP BY "IdComprobante", "IdCobroClasificacion","CodigoCobroClasificacion"
                    ORDER BY "IdCobroClasificacion"
             )
             group by "IdComprobante"
       	) AS tblCobrosCmbte on tblCobrosCmbte."IdComprobante" = comprobante."IdComprobante"       	       	
       	Left Join (
    	   Select "IdComprobante",
                   Max(Case When "CodigoCobroClasificacion" = 'COMISION' Then "MontoLiquidacionCobro" Else 0 End) As "TotalComisLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'DERECHO' Then "MontoLiquidacionCobro" Else 0 End) As "TotalDereLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'GASTO' Then "MontoLiquidacionCobro" Else 0 End) As "TotalGastoLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'IMPUESTO' Then "MontoLiquidacionCobro" Else 0 End) As "TotalImptoLiq",
                   Max(Case When "CodigoCobroClasificacion" = 'RETENCION' Then "MontoLiquidacionCobro" Else 0 End) As "TotalRetenLiq"
           From (
                   Select "IdComprobante",
                          "IdCobroClasificacion", 
                          "CodigoCobroClasificacion",
                          Ifnull(Sum("MontoLiquidacionCobro"),0) As "MontoLiquidacionCobro"
                   From "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"
                   Group By "IdComprobante", "IdCobroClasificacion","CodigoCobroClasificacion"
                   Order By "IdCobroClasificacion"
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
				FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" AS ComprobanteCobros
				INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" AS DefinicionCobro ON DefinicionCobro."CodigoDefinicionCobro" = ComprobanteCobros."IdCobro"
				GROUP BY "IdComprobante","IdClaseCobro"
			)
			GROUP BY "IdComprobante"
       	) AS tblComisiones ON tblComisiones."IdComprobante" = comprobante."IdComprobante";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMantenedorIgk"  
TO  BCS_DESARROLLADOR WITH GRANT OPTION;