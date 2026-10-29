--GCBSXNEC-1: Suscripcion OSAS 2da instancia.
WITH PreferenciaPagoWith as (SELECT COUNT(1),PP."IdTipoOperacion",PPP."IdPortafolio",PP."IdMoneda", MP."Nombre",MP."UsaCuentaBancaria", BANCBANC."NombreBanco",CBANC."NumeroCuentaBancaria"  FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" PPP
							INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as PP ON PPP."IdPreferenciaPago" = PP."IdPreferenciaPago"
							 LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as POR ON PPP."IdPortafolio" = POR."IdPortafolio"
  							LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON POR."IdCliente" = CLI."IdCliente"
							 LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" as MP ON pp."IdMedioDePago" = mp."IdMedioDePago"
							 LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CBANC ON CLI."IdPersona"= CBANC."IdPersona" AND PPP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
    					  	 LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CUENTABANCO  ON PPP."IdCuentaBancaria" = CUENTABANCO."IdCuentaBanco" AND CUENTABANCO."Default" = 'S'
					  		 LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANCBANC ON BANCBANC."IdBanco" = CBANC."IdBanco"
		  					 WHERE PP."IdTipoOperacion" IN ('DIVDIN','SORLET','CORCUP','REPDIN') AND PPP."IdEstado" = 'ACT' AND PPP."PorDefecto" = 'S' 
		   					GROUP BY PP."IdTipoOperacion",PPP."IdPortafolio",PP."IdMoneda",  MP."Nombre",MP."UsaCuentaBancaria",BANCBANC."NombreBanco",CBANC."NumeroCuentaBancaria")
	SELECT
	    EvCli."IdEventoCapitalCliente"					AS "IdEventoCapitalCliente",
		EvCli."IdEventoCapital"							AS "IdEventoCapital",
		EvCli."IdTipoEventoCapital"						AS "IdTipoEventoCapital",          					
		TipEvCap."Nombre" 								AS "TipoEvento",
		EvCli."Secuencia"								AS "Secuencia",
		EvCli."SecuenciaRelacionada"					AS "SecuenciaRelacionada",
		EvCli."SuscripcionComplementaria"				AS "SuscripcionComplementaria",
		EvCli."NumeroDividendo"							AS "NumeroDividendo",
		EvCli."IdSocioNegocio"							AS "IdSocioNegocio",
		EvCli."TipoSocioNegocio"						AS "TipoSocioNegocio",
		COALESCE(Portaf."IdCliente",0)        			AS "IdCliente",
		COALESCE(EvCli."IdPortafolio",0)				AS "IdPortafolio",
		Portaf."CodigoPortafolio"						AS "CodigoPortafolio",
		Portaf."Proposito"        						AS "Portafolio",
		EvCli."IdTipoDocumentoIdentidad"				AS "IdTipoDocumentoIdentidad",
		EvCli."DocumentoIdentidad"						AS "DocumentoIdentidad",
		CPDI."DocumentoIdentidad"						AS "RUTCavali", 
	    EvCli."NombreCliente"                           AS "NombreCliente",		
		EvCli."IdClasificacionTipoInstrumento"			AS "IdClasificacionTipoInstrumento",
		EvCli."IdTipoInstrumento"						AS "IdTipoInstrumento",
		TipInstru."Descripcion"       					AS "TipoInstrumento",
		EvCli."IdInstrumento"							AS "IdInstrumento",
		EvCli."Nemo"									AS "Nemo",
		ConsuInstru."ISIN"								AS "ISIN", 
		EvCli."IdInstrumentoNuevo"						AS "IdInstrumentoNuevo",
		EvCli."NemoNuevo"								AS "NemoNuevo",
		EvCli."IdConceptoGarantia"						AS "IdConceptoGarantia",
		TO_NVARCHAR(EvCli."FechaCierre",'YYYY-MM-DD')	AS "FechaCierre",		
		TO_NVARCHAR(EvCli."FechaPago",'YYYY-MM-DD')		AS "FechaPago",	
		EvCli."CantidadDerecho"							AS "CantidadDerecho",
		EvCli."TipoCartera" 							AS "IdTipoCartera",
		CASE EvCli."TipoCartera"    
			WHEN 'CUS'  THEN 'CUSTODIA'
			WHEN 'GAR'  THEN 'GARANTIAS' 
			WHEN 'SIM'  THEN 'SIMULTANEAS'
			WHEN 'EMP'  THEN 'EMPRESTITO'
			WHEN 'TRA'  THEN 'TRANSITO'
			WHEN 'CMB'  THEN 'COMPROBANTE'
			WHEN 'PRE'  THEN 'PRESTATARIO'
			ELSE EvCli."TipoCartera"  
		END  											AS "TipoCartera",
		EvCli."CantidadNueva"							AS "CantidadNueva",
		EvCli."MontoCapital"							AS "MontoCapital",
		EvCli."Precio"									AS "Precio",
		EvCli."PrecioNuevo"								AS "PrecioNuevo",
		EvCli."MontoInteres"							AS "MontoInteres",
		EvCli."MontoReajuste"							AS "MontoReajuste",
		CASE WHEN EvCli."IdMoneda" = 'CLP' THEN ROUND(EvCli."Total", 0)
			ELSE EvCli."Total"
		END												AS "Total",
		EvCli."MontoRetencion"							AS "MontoRetencion",
		EvCli."CantidadDerechoArchivo"					AS "CantidadDerechoArchivo",
		EvCli."AccionesDisponiblesArchivo"				AS "AccionesDisponiblesArchivo",
		EvCli."DiferenciaCantidadDerecho"				AS "DiferenciaCantidadDerecho",
		EvCli."DiferenciaAccionesCantidadNueva"			AS "DiferenciaAccionesCantidadNueva",
		EvCli."MontoBeneficioArchivo"					AS "MontoBeneficioArchivo",
		EvCli."DiferenciaMontoBeneficio"				AS "DiferenciaMontoBeneficio",
		EvCli."MontoInteresArchivo"						AS "MontoInteresArchivo",
		EvCli."MontoCapitalArchivo"						AS "MontoCapitalArchivo",
		EvCli."DiferenciaMontoInteres"					AS "DiferenciaMontoInteres",
		EvCli."DiferenciaMontoCapital"					AS "DiferenciaMontoCapital",
		EvCli."MontoRetencionArchivo"					AS "MontoRetencionArchivo",
		EvCli."IdMoneda"								AS "IdMoneda",
		IFNULL(Moneda."Nombre",'')           			AS "NombreMoneda",
		EvCli."SecuenciaCupon"							AS "SecuenciaCupon",
		EvCli."IdEmpleadoEjecutivoPortafolio"			AS "IdEmpleadoEjecutivoPortafolio",
		EvCli."CodigoUsuarioAgente"						AS "CodigoUsuarioAgente",
		EvCli."NombreAgente"							AS "NombreAgente",
		EvCli."CodigoSucursal"							AS "CodigoSucursal",
		EvCli."FechaHoraIngresoRegistro"				AS "FechaHoraIngresoRegistro",
		EvCli."FechaHoraUltimaActualizacion"			AS "FechaHoraUltimaActualizacion",
		EvCli."IdUsuario"								AS "IdUsuario",
		EvCli."IdEstado"								AS "IdEstado",
		COALESCE(EvCptal."FactorNuevo", 0)	            AS "FactorNuevo",
		CASE 
			WHEN EvCli."IdMoneda" <> EvCptal."IdMoneda" AND EvCli."FactorOtraMoneda" = EvCptal."Factor" THEN (SELECT "Factor" 
																											  FROM "BSCL.Custodia.BD.Vistas::EVC.ConsultarFactorPorMoneda" 
																											  WHERE "Secuencia" = EvCptal."Secuencia" AND "IdMoneda" = EvCli."IdMoneda" AND "Prioridad" = 1 and "Nemotecnico" = EvCptal."Nemo")			
			WHEN EvCli."IdMoneda" <> EvCptal."IdMoneda" AND EvCli."FactorOtraMoneda" <> EvCptal."Factor" THEN EvCli."FactorOtraMoneda"
			ELSE EvCptal."Factor"
		END												AS "Factor",				
		EvCptal."Valor"									AS "Valor",
		EvCptal."Descripcion"         					AS "DescripcionEvento",
		EvCptal."IdEmpresa"								AS "IdEmpresa",
		EvCli."IdBoveda"          						AS "IdBoveda",
		Case EvCli."Origen"          			
			WHEN 'SIS'	THEN 'SISTEMA'
			ELSE 'MANUAL'
		End  											AS "Origen",
		IFNULL(ConsultaIndicador."ValorIndicador",'N')	AS "ConfiguracionNotificacion",
		CASE WHEN EvCli."TipoSocioNegocio" = 'CNT' THEN 'S'
			ELSE
				CASE WHEN PreferenciaPago."IdPortafolio" > 0
					THEN 'S'
					ELSE 'N'
				END
		END												AS "TienePreferenciaDePago",
		CASE 
			WHEN Portaf."IdPersona" = EM."IdPersona" 
			THEN 'S' 
			ELSE EvCli."EstadoNotificacion" 
		END 											AS "EstadoNotificacion",
		IFNULL(MedioPago."NombreMedioDePago",'')		AS "NombreMedioDePago",
/*		CASE EvCli."TipoCuentaBoveda"
			WHEN 'D' THEN 'DEPOSITANTE'
			WHEN 'M' then 'MANDANTE'	
			ELSE ''	
		END												AS "TipoCuentaBoveda",*/
		EvCli."IdTipoCuentaCustodio"					AS "TipoCuentaBoveda",
		EvCli."NumeroCuentaBoveda"						AS "NumeroCuentaBoveda",
		CASE EvCli."PagaClienteCorredor"						
			WHEN 'CL' THEN 'CLIENTE' 
			WHEN 'CO' THEN 'CORREDOR' 
			ELSE ''	END 								AS "PagaClienteCorredor",
		EvCli."IdTipoReajuste"							AS "IdTipoReajuste",
		EvCli."INGMontoInteresCorteCupon"				AS "INGMontoInteresCorteCupon",
		EvCli."Amortizacion"							AS "Amortizacion",
		EvCli."AfectoaClearing"							AS "AfectoaClearing",
		EvCli."ProcesadoClearing"						AS "ProcesadoClearing",
		EvCli."IdCorredorContraparte"					AS "IdCorredorContraparte",
		EvCli."CodigoCorredorContraparte"				AS "CodigoCorredorContraparte",
		EvCli."IdComprobanteTesoreria"					AS "IdComprobanteTesoreria",
		EvCli."TipoOperacion"							AS "TipoOperacion",
		EvCli."IdBolsa"									AS "IdBolsa",
  		EvCli."IdRegimenFondoMutuo"						AS "IdRegimenFondoMutuo",
  		COALESCE(EvCli."OperacionesVigentes",'')		AS "OperacionesVigentes",
  		EvCli."ISFUT"									AS "ISFUT",
  		EvCLI."RetencionISFUT"							AS "RetencionISFUT",
  		EvCli."MontoRetencionISFUT"						AS "MontoRetencionISFUT",
  		EvCptal."ISFUT"									AS "ISFUTEventoCapital",
  		EvCptal."TasaEfectiva"							AS "TasaEfectiva",
	CASE WHEN EvCli."IdTipoEventoCapital" = 'DV' OR EvCli."IdTipoEventoCapital" = 'RC' THEN 
  		PreferenciaPago."Nombre" ELSE '' END  as "Modalidad" ,
    CASE WHEN EvCli."IdTipoEventoCapital" = 'DV' OR EvCli."IdTipoEventoCapital" = 'RC' THEN	(CASE WHEN PreferenciaPago."UsaCuentaBancaria" = 'S' AND PreferenciaPago."NombreBanco" IS NOT NULL AND PreferenciaPago."NumeroCuentaBancaria" IS NOT NULL
		THEN (COALESCE(PreferenciaPago."Nombre",'')||'-'||COALESCE(PreferenciaPago."NombreBanco",'')||'-'||COALESCE(PreferenciaPago."NumeroCuentaBancaria",'')) 
		ELSE PreferenciaPago."Nombre" END) ELSE '' END AS "MedioDePago" ,
	IFNULL(ConsultaIndicadorCuentaInversion."ValorIndicador",'N') as  "IndicadorCuentaInversion",
	EvCli."ImpuestoDivExtranjero",
	EvCli."MontoImpuestoExtranjero",
	EvCli."MontoGastoTransferencia" AS "GastoTransferencia"
		
	FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"  												AS EvCli
  		INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 											AS EvCptal
    		ON EvCli."IdEventoCapital" = EvCptal."IdEventoCapital" 
    	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"									AS Portaf
    		ON ( Portaf."IdPortafolio" = EvCli."IdPortafolio") 
    	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"				AS CPDI
    		ON EvCli."IdSocioNegocio" = CPDI."IdSocioDeNegocio"
    	   AND CPDI."IdTipoDocumentoIdentidad" = 'RUT'
  		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"							AS MedioPago
		  	ON (	EvCli."IdEventoCapitalCliente" = MedioPago."IdEventoCapitalCliente"	and 
		  			EvCli."IdEventoCapitalClienteFormaDePago" = MedioPago."IdEventoCapitalClienteFormaDePago")
--  		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 								AS Portafolio
--    		ON EvCli."IdPortafolio" = Portafolio."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" 									AS Moneda
		    ON EvCli."IdMoneda" = Moneda."IdMoneda"
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital" 						AS TipEvCap
		    ON EvCli."IdTipoEventoCapital" = TipEvCap."IdTipoEventoCapital"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento" 			AS TipInstru
		    ON EvCli."IdTipoInstrumento" = TipInstru."IdTipoInstrumento"
		   AND EvCli."IdClasificacionTipoInstrumento" = TipInstru."IdClasificacionTipoInstrumento"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"							AS ConsuInstru
			ON EvCptal."IdInstrumento" = ConsuInstru."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" 					AS ConsultaIndicador
		    ON ConsultaIndicador."IdUso" = 'PRE' 
		   AND ConsultaIndicador."GrupoIndicador" = 'GRUPONOTIFICACIONES' 
		   AND ConsultaIndicador."CodigoTipoIndicador" = 'NOT_ENVROP'
		   AND ConsultaIndicador."IdPortafolio" = EvCli."IdPortafolio"

		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" 					AS ConsultaIndicadorCuentaInversion
		 ON ConsultaIndicadorCuentaInversion."IdUso" = 'PRE' 
		   AND ConsultaIndicadorCuentaInversion."GrupoIndicador" = 'GRUPOGENERAL' 
		   AND ConsultaIndicadorCuentaInversion."CodigoTipoIndicador" = 'GEN_OCCINV'
		   AND ConsultaIndicadorCuentaInversion."IdPortafolio" = EvCli."IdPortafolio"

		LEFT JOIN PreferenciaPagoWith AS PreferenciaPago 
		   ON PreferenciaPago."IdPortafolio" = EvCli."IdPortafolio"
		   AND CASE EvCli."IdTipoEventoCapital"
		   		WHEN 'DV' THEN 'DIVDIN'
		   		WHEN 'SL' THEN 'SORLET'
		   		WHEN 'CC' THEN 'CORCUP'
		   		WHEN 'RC' THEN 'REPDIN'
		   	   END							= PreferenciaPago."IdTipoOperacion"
		   AND PreferenciaPago."IdMoneda" = EvCli."IdMoneda"
		LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EM ON EM."IdPersona" = Portaf."IdPersona" AND ConsuInstru."IdEmisor"=EM."IdEmisor" 	 
	WHERE (EvCli."IdTipoEventoCapital" 	IN (:p_TipoEvento))
	AND (EvCli."IdInstrumento" 			IN (:p_Instrumento))
	AND (EvCli."Nemo" 					IN (:p_Nemo) OR :p_Nemo = '')
    AND (EvCli."Secuencia"				IN (:p_Secuencia))
	AND  EvCli."IdEstado" <> 'ANU';     -- Los anulados fueron borrados por el usuario(no deben verse, solo quedan respaldados en BD)
