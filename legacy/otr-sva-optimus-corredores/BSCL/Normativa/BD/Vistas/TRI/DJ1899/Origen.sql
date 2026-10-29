SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.Origen" (
    "IdEmpresa",
    "Numero",
    "IdMoneda",
    "RutCliente",																												--C2
    "IdCliente",
    "ActivoPensionado",																											--C3
    "RutEmpleador",																												--C4
    --"IdEmpleador",
    "FechaOperacion",
    "DepositoEmpleador",--uf																									--C5
    ---Aporte Empleador
    "AporteEmpleador",--uf																										--C7
    "TraspasoEmpleador",--uf																									--C8
    "RetiroEmpleadorActualizado",--($)																							--C9
    "MontoRetencionActualizado",--15%($)																						--C10
    --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    "AhorrosModalidadIndirectaEmpleador",																						--C11
    "AhorrosModalidadDirectaTrabajador",																						--C12
    --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    --del ART. 42 N1 de la LIR en UF
    "CotizacionesModalidadIndirectaEmpleador",--uf																				--C13
    "CotizacionesModalidadDirectaTrabajador",--uf																				--C14
    --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    --del Art.42 N2 de la LIR en UF
    "CotizacionesTrabajadorIndependiente",--uf																					--C15
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    "MontoRetiroPorAhorrosIncisoPrimero",																						--C16
    --Retencin de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    "RetencionImpuestoSobreRetiros",--15%($)																					--C17
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    "MontoRetiroPorAhorrosIncisoSegundo",																						--C18
    --N CERTIFICADO TRABAJADOR
    "NumeroCertificadoTrabajador",																								--C35
    --N CERTIFICADO EMPLEADOR
    "NumeroCertificadoEmpleador",																								--C36
    "EjecutadoComplementoMontos",
    "EjecutadoComplementoIPC"
) AS

	--	1. Aportes Deposito Convenido DC-R
	SELECT 
		SOL."IdEmpresa"												AS "IdEmpresa",
		SOL."IdSolicitudAporteRescate"								AS "Numero",
		SOL."IdMoneda"												AS "IdMoneda",
		TRIM(CLI."DocumentoIdentidad")								AS "RutCliente",												--C2
		/* TRIM(CCAR."DocumentoIdentidad")  AS "RutCliente", */
		SOL."IdCliente"												AS "IdCliente",
		CASE WHEN SAAPV."Pensionado" = 'SI' THEN 2 ELSE 1 END 		AS "ActivoPensionado",											--C3			
		/*'' 														AS "RutEmpleador",												--C4 */
		COALESCE(SAAPV."DocumentoIdentidadEmpleador",'') 			AS "RutEmpleador",												--C4
		
		emp."PeriodoPago" || '-01' 									AS "FechaOperacion",
		
		COALESCE(ROUND(SOL."MontoMonedaSistema"/PAR."Valor",3),0)	AS "DepositoEmpleador",--uf										--C5	
		0															AS "AporteEmpleador",--uf										--C7
		0 															AS "TraspasoEmpleador",--uf										--C8
		0 															AS "RetiroEmpleadorActualizado",--($)							--C9
		0 															AS "MontoRetencionActualizado",--15%($)							--C10
		0 															AS "AhorrosModalidadIndirectaEmpleador",						--C11
		0 															AS "AhorrosModalidadDirectaTrabajador",							--C12
		0 															AS "CotizacionesModalidadIndirectaEmpleador",--uf				--C13
		0 															AS "CotizacionesModalidadDirectaTrabajador",--uf				--C14
		0 															AS "CotizacionesTrabajadorIndependiente",--uf					--C15
		0 															AS "MontoRetiroPorAhorrosIncisoPrimero",						--C16
		0 															AS "RetencionImpuestoSobreRetiros",--15%($)						--C17
		0 															AS "MontoRetiroPorAhorrosIncisoSegundo",						--C18
		0 															AS "NumeroCertificadoTrabajador",								--C35
		0 															AS "NumeroCertificadoEmpleador",								--C36
		'' 															AS "EjecutadoComplementoMontos",
		'' 															AS "EjecutadoComplementoIPC"
	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 			AS SOL 
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS CLI 
		ON CLI."IdCliente" = SOL."IdCliente"
	INNER   JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 					AS SAAPV ON SAAPV."FolioContratoSAAPV" = SOL."FolioContratoOperacion"
	INNER JOIN "BSCL.APV.BD::REC.Cliente" AS clien
     	ON clien."IdRecaudacionCliente" = SOL."IdOrigenRecaCliente"
	INNER JOIN "BSCL.APV.BD::REC.Recaudacion" AS dep
		ON dep."IdRecaudacion"  = clien."IdRecaudacion"
	INNER JOIN "BSCL.APV.BD::REC.Empleador" AS emp
		ON emp."IdRecaudacion" = dep."IdRecaudacion"
		AND emp."IdRecaudacion" = clien."IdRecaudacion" 
		AND emp."IdRecaudacionEmpleador" = clien."IdRecaudacionEmpleador"
	LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS PAR ON ADD_DAYS(ADD_MONTHS (TO_DATE (emp."PeriodoPago" || '-01', 'YYYY-MM-DD' ), +1), -1) = PAR."Fecha" AND PAR."IdParametroFinanciero" = 'UF'	
	WHERE 	SOL."IdTipoMovimientoFondoMutuo" IN ('APONORM')
	AND 	SOL."IdRegimenTributario" IN ('DC-R')
	AND 	SOL."Estado" = 'CRT'
	AND 	SOL."FechaValorCuota" <> '0001-01-01'
	

	UNION ALL
	
	--	2. Aportes Trabajador dependiente via empleador APV-B
		SELECT
		SOL."IdEmpresa"												AS "IdEmpresa",
		SOL."IdSolicitudAporteRescate"								AS "Numero",
		SOL."IdMoneda"												AS "IdMoneda",		
		TRIM(CLI."DocumentoIdentidad")								AS "RutCliente",											--C2
		SOL."IdCliente"												AS "IdCliente",
		CASE WHEN SAAPV."Pensionado" = 'SI' THEN 2 ELSE 1 END 		AS "ActivoPensionado",										--C3
		/* ''												 		AS "RutEmpleador",											--C4 */
		COALESCE(SAAPV."DocumentoIdentidadEmpleador",'') 			AS "RutEmpleador",										    --C4
		
		emp."PeriodoPago" || '-01' 									AS "FechaOperacion",
		
		0 															AS "DepositoEmpleador",--uf									--C5
		0 															AS "AporteEmpleador",--uf									--C7
		0 															AS "TraspasoEmpleador",--uf									--C8
		0 															AS "RetiroEmpleadorActualizado",--($)						--C9
		0 															AS "MontoRetencionActualizado",--15%($)						--C10
		0 															AS "AhorrosModalidadIndirectaEmpleador",					--C11
		0 															AS "AhorrosModalidadDirectaTrabajador",						--C12
		
		COALESCE(ROUND(SOL."MontoMonedaSistema"/PAR."Valor",3),0) AS "CotizacionesModalidadIndirectaEmpleador",--uf			--C13
		
		0 															AS "CotizacionesModalidadDirectaTrabajador",--uf			--C14
		0 															AS "CotizacionesTrabajadorIndependiente",--uf				--C15
		0 															AS "MontoRetiroPorAhorrosIncisoPrimero",					--C16
		0 															AS "RetencionImpuestoSobreRetiros",--15%($)					--C17
		0 															AS "MontoRetiroPorAhorrosIncisoSegundo",					--C18
		0 															AS "NumeroCertificadoTrabajador",							--C35
		0 															AS "NumeroCertificadoEmpleador",							--C36
		'' 															AS "EjecutadoComplementoMontos",
		'' 															AS "EjecutadoComplementoIPC"		

	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 			AS SOL 
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"  AS CLI ON CLI."IdCliente" = SOL."IdCliente" 
	
	INNER   JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 					AS SAAPV ON SAAPV."FolioContratoSAAPV" = SOL."FolioContratoOperacion"
	INNER JOIN "BSCL.APV.BD::REC.Cliente" AS clien
     	ON clien."IdRecaudacionCliente" = SOL."IdOrigenRecaCliente"
	INNER JOIN "BSCL.APV.BD::REC.Recaudacion" AS dep
		ON dep."IdRecaudacion"  = clien."IdRecaudacion"
	INNER JOIN "BSCL.APV.BD::REC.Empleador" AS emp
		ON emp."IdRecaudacion" = dep."IdRecaudacion"
		AND emp."IdRecaudacion" = clien."IdRecaudacion" 
		AND emp."IdRecaudacionEmpleador" = clien."IdRecaudacionEmpleador"
	LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS PAR ON ADD_DAYS(ADD_MONTHS (TO_DATE (emp."PeriodoPago" || '-01', 'YYYY-MM-DD' ), +1), -1) = PAR."Fecha" AND PAR."IdParametroFinanciero" = 'UF'
	WHERE 	SOL."IdTipoMovimientoFondoMutuo" IN ('APONORM')
	AND 	SOL."IdRegimenTributario" IN ('APV-B')
	AND 	SOL."Estado" = 'CRT'
	AND 	SOL."FechaValorCuota" <> '0001-01-01'	
	AND 	SAAPV."IdMedioRecaudacionFormaPago" = 2	
	AND 	TRIM(SAAPV."Trabajador") = 'DEPENDIENTE'
	

	
	UNION ALL 
	
	--	3. Aportes Trabajador dependiente via trabajador APV-B
	SELECT 
		SOL."IdEmpresa"											AS "IdEmpresa",
		SOL."IdSolicitudAporteRescate"							AS "Numero",
		SOL."IdMoneda"											AS "IdMoneda",											
		TRIM(CLI."DocumentoIdentidad")							AS "RutCliente",												--C2
		SOL."IdCliente"											AS "IdCliente",
		CASE WHEN SAAPV."Pensionado" = 'SI' THEN 2 ELSE 1 END 	AS "ActivoPensionado",											--C3		
		/* ''													AS "RutEmpleador",												--C4 */
		COALESCE(SAAPV."DocumentoIdentidadEmpleador",'') 		AS "RutEmpleador",												--C4 
		SUBSTRING(SOL."FechaIngresoRegistro",1,10)				AS "FechaOperacion",	
		0 														AS "DepositoEmpleador",--uf										--C5
		0 														AS "AporteEmpleador",--uf										--C7
		0 														AS "TraspasoEmpleador",--uf										--C8
		0 														AS "RetiroEmpleadorActualizado",--($)							--C9
		0 														AS "MontoRetencionActualizado",--15%($)							--C10
		0 														AS "AhorrosModalidadIndirectaEmpleador",						--C11
		0 														AS "AhorrosModalidadDirectaTrabajador",							--C12
		0 														AS "CotizacionesModalidadIndirectaEmpleador",--uf				--C13
		
		ROUND(SOL."MontoMonedaSistema"/PAR."Valor",3)			AS "CotizacionesModalidadDirectaTrabajador",--uf				--C14
		
		0 														AS "CotizacionesTrabajadorIndependiente",--uf					--C15
		0 														AS "MontoRetiroPorAhorrosIncisoPrimero",						--C16
		0 														AS "RetencionImpuestoSobreRetiros",--15%($)						--C17
		0 														AS "MontoRetiroPorAhorrosIncisoSegundo",						--C18
		0 														AS "NumeroCertificadoTrabajador",								--C35
		0 														AS "NumeroCertificadoEmpleador",								--C36
		'' 														AS "EjecutadoComplementoMontos",
		'' 														AS "EjecutadoComplementoIPC"		


	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 			AS SOL 
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS CLI   ON CLI."IdCliente" = SOL."IdCliente" 
	LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" 	AS PAR 	 ON SUBSTRING(SOL."FechaValorCuota",1,10) = PAR."Fecha"  AND PAR."IdParametroFinanciero" = 'UF'
	INNER   JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 					AS SAAPV ON SAAPV."FolioContratoSAAPV" = SOL."FolioContratoOperacion"
	WHERE 	SOL."IdTipoMovimientoFondoMutuo" IN ('APONORM')
	AND 	SOL."IdRegimenTributario" IN ('APV-B')
	AND 	SOL."Estado" = 'CRT'
	AND 	SAAPV."IdMedioRecaudacionFormaPago" <> 2
	AND 	TRIM(SAAPV."Trabajador") = 'DEPENDIENTE'
	
	
	UNION ALL 
	
	--	4. Aportes Trabajador Independiente via trabajador APV-B
	SELECT 
		SOL."IdEmpresa"											AS "IdEmpresa",
		SOL."IdSolicitudAporteRescate"							AS "Numero",
		SOL."IdMoneda"											AS "IdMoneda",		
		TRIM(CLI."DocumentoIdentidad")							AS "RutCliente",											--C2
		SOL."IdCliente"											AS "IdCliente",
		CASE WHEN SAAPV."Pensionado" = 'SI' THEN 2 ELSE 1 END 	AS "ActivoPensionado",										--C3
		/* ''												 	AS "RutEmpleador",											--C4 */
		COALESCE(SAAPV."DocumentoIdentidadEmpleador",'') 		AS "RutEmpleador",											--C4
		SUBSTRING(SOL."FechaIngresoRegistro",1,10) 					AS "FechaOperacion",
		0 														AS "DepositoEmpleador",--uf									--C5
		0 														AS "AporteEmpleador",--uf									--C7
		0 														AS "TraspasoEmpleador",--uf									--C8
		0 														AS "RetiroEmpleadorActualizado",--($)						--C9
		0 														AS "MontoRetencionActualizado",--15%($)						--C10
		0 														AS "AhorrosModalidadIndirectaEmpleador",					--C11
		0 														AS "AhorrosModalidadDirectaTrabajador",						--C12
		0														AS "CotizacionesModalidadIndirectaEmpleador",--uf			--C13
		0 														AS "CotizacionesModalidadDirectaTrabajador",--uf			--C14
		
		ROUND(SOL."MontoMonedaSistema"/PAR."Valor",3)			AS "CotizacionesTrabajadorIndependiente",--uf				--C15
		
		0 														AS "MontoRetiroPorAhorrosIncisoPrimero",					--C16
		0 														AS "RetencionImpuestoSobreRetiros",--15%($)					--C17
		0 														AS "MontoRetiroPorAhorrosIncisoSegundo",					--C18
		0 														AS "NumeroCertificadoTrabajador",							--C35
		0 														AS "NumeroCertificadoEmpleador",							--C36
		'' 														AS "EjecutadoComplementoMontos",
		'' 														AS "EjecutadoComplementoIPC"		

	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 			AS SOL 
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"  AS CLI 	 ON CLI."IdCliente" = SOL."IdCliente" 
	LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" 	AS PAR 	 ON SUBSTRING(SOL."FechaValorCuota",1,10) = PAR."Fecha"  AND PAR."IdParametroFinanciero" = 'UF'
	INNER   JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 					AS SAAPV ON SAAPV."FolioContratoSAAPV" = SOL."FolioContratoOperacion"
	WHERE 	SOL."IdTipoMovimientoFondoMutuo" IN ('APONORM')
	AND 	SOL."IdRegimenTributario" IN ('APV-B')
	AND 	SOL."Estado" = 'CRT'
	AND 	SAAPV."IdMedioRecaudacionFormaPago" <> 2
	AND 	SOL."FechaValorCuota" <> '0001-01-01'
	AND 	TRIM(SAAPV."Trabajador") = 'INDEPENDIENTE' 
	
	
	UNION ALL 
	
	--	5. Rescates APV-B
	SELECT 
		SOL."IdEmpresa"											AS "IdEmpresa",
		SOL."IdSolicitudAporteRescate"							AS "Numero",
		SOL."IdMoneda"											AS "IdMoneda",		
		TRIM(CLI."DocumentoIdentidad")							AS "RutCliente",											--C2
		SOL."IdCliente"											AS "IdCliente",		

		COALESCE((Select max( CASE WHEN IFNULL("Pensionado",'') = '' THEN 1 ELSE CASE WHEN "Pensionado" = 'SI' THEN 2 ELSE 1 END END ) AS "ActivoPensionado" --C3
				FROM "BSCL.APV.BD::SCS.ContratoSAAPV"  
				WHERE "DocumentoIdentidadCliente" = TRIM(CLI."DocumentoIdentidad") AND "IdEstado" = 'VIG'),1) AS "ActivoPensionado",	--C3
		/*'' 													AS "RutEmpleador",											--C4 */
		COALESCE(SAAPV."DocumentoIdentidadEmpleador",'') 		AS "RutEmpleador",
		SOL."FechaLiquidacion" 									AS "FechaOperacion",
		0 														AS "DepositoEmpleador",--uf									--C5
		0 														AS "AporteEmpleador",--uf									--C7
		0 														AS "TraspasoEmpleador",--uf									--C8
		0 														AS "RetiroEmpleadorActualizado",--($)						--C9
		0 														AS "MontoRetencionActualizado",--15%($)						--C10
		0 														AS "AhorrosModalidadIndirectaEmpleador",					--C11
		0 														AS "AhorrosModalidadDirectaTrabajador",						--C12
		0														AS "CotizacionesModalidadIndirectaEmpleador",--uf			--C13
		0 														AS "CotizacionesModalidadDirectaTrabajador",--uf			--C14
		0 														AS "CotizacionesTrabajadorIndependiente",--uf				--C15
/*		CASE WHEN SOL."FechaValorCuota" >= :FechaInicioCIN AND SOL."IdMedioDePago" = 'CIN' 
		THEN 0 ELSE round(SOL."MontoMonedaSistema" * (IPCACTR."Valor"/IPCINVR."Valor"),0) 
		END 													AS "MontoRetiroPorAhorrosIncisoPrimero",					--C16*/
		 
		COALESCE(SOL."MontoMonedaSistema" * (IPCACTR."Valor"/IPCINVR."Valor"),0)			AS "MontoRetiroPorAhorrosIncisoPrimero",					--C16		
		
/*		CASE WHEN SOL."FechaValorCuota" >= :FechaInicioCIN AND SOL."IdMedioDePago" = 'CIN' 
				 THEN 0 ELSE round((SOL."MontoMonedaSistema" * (IPCACTR."Valor"/IPCINVR."Valor") * 0.15),0) 
		END 													AS "RetencionImpuestoSobreRetiros",--15%($)					--C17*/
	 
		COALESCE((SOL."MontoMonedaSistema" * (IPCACTR."Valor"/IPCINVR."Valor") * 0.15),0)	AS "RetencionImpuestoSobreRetiros",--15%($)					--C17
		
		0 														AS "MontoRetiroPorAhorrosIncisoSegundo",					--C18
		0 														AS "NumeroCertificadoTrabajador",							--C35
		0 														AS "NumeroCertificadoEmpleador",							--C36
		'' 														AS "EjecutadoComplementoMontos",
		'' 														AS "EjecutadoComplementoIPC"		

	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 			AS SOL
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS CLI ON CLI."IdCliente" = SOL."IdCliente" 
	LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" 	AS IPCINVR  ON (SUBSTRING(ADD_MONTHS (TO_DATE (COALESCE(SOL."FechaLiquidacion",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")  
							AND IPCINVR."IdParametroFinanciero" = 'IPC'
	LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR  ON (SUBSTRING(COALESCE(SOL."FechaLiquidacion",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")  		
							AND IPCACTR."IdParametroFinanciero" = 'IPC'	
	LEFT  JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 					AS SAAPV ON SAAPV."FolioContratoSAAPV" = SOL."FolioContratoOperacion"			
	WHERE 	SOL."IdTipoMovimientoFondoMutuo" IN ('RESNORM')
	AND 	SOL."IdRegimenTributario" IN ('APV-B','APV-AN')
	AND 	SOL."FechaLiquidacion" <> '0001-01-01'
	AND 	SOL."Estado" = 'CRT'
	AND 	SOL."IdMedioDePago" <> 'CIN'

	UNION ALL 
	
	--	6. Rescates APV-A
	SELECT 
		SOL."IdEmpresa"											AS "IdEmpresa",
		SOL."IdSolicitudAporteRescate"							AS "Numero",
		SOL."IdMoneda"											AS "IdMoneda",		
		TRIM(CLI."DocumentoIdentidad")							AS "RutCliente",											--C2
		SOL."IdCliente"											AS "IdCliente",		
		COALESCE((Select max( CASE WHEN IFNULL("Pensionado",'') = '' THEN 1 ELSE CASE WHEN "Pensionado" = 'SI' THEN 2 ELSE 1 END END ) AS "ActivoPensionado" 
				FROM "BSCL.APV.BD::SCS.ContratoSAAPV"  
				WHERE "DocumentoIdentidadCliente" = TRIM(CLI."DocumentoIdentidad") AND "IdEstado" = 'VIG'),1) AS "ActivoPensionado",	--C3
				
		/* '' 													AS "RutEmpleador",											--C4 */
		COALESCE(SAAPV."DocumentoIdentidadEmpleador",'') 		AS "RutEmpleador",
		SOL."FechaLiquidacion" 									AS "FechaOperacion",
		0 														AS "DepositoEmpleador",--uf									--C5
		0 														AS "AporteEmpleador",--uf									--C7
		0 														AS "TraspasoEmpleador",--uf									--C8
		0 														AS "RetiroEmpleadorActualizado",--($)						--C9
		0 														AS "MontoRetencionActualizado",--15%($)						--C10
		0 														AS "AhorrosModalidadIndirectaEmpleador",					--C11
		0 														AS "AhorrosModalidadDirectaTrabajador",						--C12
		0														AS "CotizacionesModalidadIndirectaEmpleador",--uf			--C13
		0 														AS "CotizacionesModalidadDirectaTrabajador",--uf			--C14
		0 														AS "CotizacionesTrabajadorIndependiente",--uf				--C15
		0 														AS "MontoRetiroPorAhorrosIncisoPrimero",					--C16
		0 														AS "RetencionImpuestoSobreRetiros",--15%($)					--C17
--		CASE WHEN SOL."FechaValorCuota" >= :FechaInicioCIN AND SOL."IdMedioDePago" = 'CIN' 
--				 THEN 0 ELSE SOL."MontoMonedaSistema" END		AS "MontoRetiroPorAhorrosIncisoSegundo",					--C18
		COALESCE(SOL."MontoMonedaSistema",0)					AS "MontoRetiroPorAhorrosIncisoSegundo",					--C18
		0 														AS "NumeroCertificadoTrabajador",							--C35
		0 														AS "NumeroCertificadoEmpleador",							--C36
		'' 														AS "EjecutadoComplementoMontos",
		'' 														AS "EjecutadoComplementoIPC"		

	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 			AS SOL 
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS CLI ON CLI."IdCliente" = SOL."IdCliente" 
	LEFT  JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" 					AS SAAPV ON SAAPV."FolioContratoSAAPV" = SOL."FolioContratoOperacion"
	WHERE 	SOL."IdTipoMovimientoFondoMutuo" IN ('RESNORM')
	AND 	SOL."IdRegimenTributario" IN ('APV-A')
	AND 	SOL."Estado" = 'CRT'
	AND 	SOL."IdMedioDePago" <> 'CIN'
	ORDER BY "RutCliente","Numero"



WITH read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1899.Origen" to bcs_desarrollador with grant option;