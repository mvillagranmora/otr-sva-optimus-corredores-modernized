SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalCorteCupon";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalCorteCupon" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',
    IN p_FechaCorteCuponDesde				NVARCHAR(10) 	default '0001-01-01',
    IN p_FechaCorteCuponHasta				NVARCHAR(10) 	default '0001-01-01',

	-- Parametros de Salida

	OUT p_out_InformacionContador  				INT,
	OUT p_out_InformacionDetalle   				TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),

	OUT p_out_ErroresContador int ,  
	OUT p_out_ErroresDetalle 					TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
	 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN SEQUENTIAL EXECUTION
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort int default 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 

	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
--								   select v_errorExt,CAST(::SQL_ERROR_MESSAGE AS NVARCHAR(520))from dummy;				
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END; 
	
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" 	FROM dummy WHERE 1=2;

	BEGIN
	
		--variables de proceso
--		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_IdEventoCapital		BIGINT := 0;
		DECLARE in_fechasPagoCorteCupon TABLE ("FechaInicial" NVARCHAR(10),"NumeroDias"	BIGINT);
		DECLARE out_fechasPagoCorteCuponCalculadas TABLE ("FechaInicial" NVARCHAR(10),"NumeroDias"	BIGINT,"FechaCalculada"	NVARCHAR(10));
		DECLARE v_IdPaisSistema 		BIGINT;
		DECLARE vFechaInicial			NVARCHAR(10);

		
--	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);

			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;				
		END;

		-----------------------------------------------------
		--Obtencion de fechas de cupones
		-----------------------------------------------------
		SELECT "IdPais" 
		INTO v_IdPaisSistema
		FROM "BSCL.Plataforma.BD::PLA.Empresa" WHERE "IdEmpresa" = p_IdEmpresa;
	
		in_fechasPagoCorteCupon = SELECT 
			DISTINCT CA."INGFechaPagoCorteCupon" AS "FechaInicial", 0 AS "NumeroDias"
			FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"		CA
			LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
			ON 	(	CA."IdInstrumento" 		= EC."IdInstrumento" 			AND 
					CA."IdTipoInstrumento" 	= EC."IdTipoInstrumento"		AND
					EC."IdTipoEventoCapital"			= 'CC'	)			
			WHERE 	CA."IdClasificacionTipoInstrumento" = 'RF'				AND 
					CA."INGFechaPagoCorteCupon"			BETWEEN p_FechaCorteCuponDesde AND p_FechaCorteCuponHasta AND
					CA."IdEstado" 						= 'VIG'				AND
				  	EC."IdTipoInstrumento" 				IS NULL
			GROUP BY 			
					CA."INGNumeroCupon", 		
					CA."IdClasificacionTipoInstrumento", 
					CA."IdTipoInstrumento", 	
					CA."IdInstrumento", 
					CA."Nemotecnico",
					CA."INGIdMonedaPagoCupon", 	
					CA."INGFechaPagoCorteCupon",
					CA."IdEmisor";
										
		SELECT "FechaInicial" INTO vFechaInicial
		FROM :in_fechasPagoCorteCupon;
		
		IF vFechaInicial = '0001-01-01' THEN
			v_TablaCodigosMensajes =	Select 	1								As "IdInterno", 
												'UI_FECHA_SIN_CORTES_DE_CUPON'	As "Codigo",
												'' 								As "CodigoDato1", 
												'' 								as "ValorDato1",
												'' 								As "CodigoDato2", 
												'' 								As "ValorDato2" 
											From dummy;
					                        
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
			p_out_ErroresContador = 1;
			p_out_ErroresDetalle = 	Select "Codigo", "Descripcion" 
									From :v_out_hash;
			SIGNAL SALIDA_CONTROLADA;
		END IF;

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguienteMasivo" (
			p_IdPais => v_IdPaisSistema,
			p_DiaHabil => 'S',
			p_FechasIN => :in_fechasPagoCorteCupon,
			p_FechasOUTf => :out_fechasPagoCorteCuponCalculadas
		);
		
		-----------------------------------------------------
		-- Inserta registro EventoCapital (Master)
		-----------------------------------------------------			
		
		INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapital"
	    (
			"IdEventoCapital",
			"IdEmpresa",
			"IdTipoEventoCapital",
			"Secuencia",
			"Descripcion",
			"EsInscrito",
			"IdClasificacionTipoInstrumento",
			"IdTipoInstrumento",
			"IdInstrumento",
			"Nemo",
			"Factor",
			"Valor",
			"IdMoneda",
			"IdInstrumentoNuevo",
			"NemoNuevo",
			"FactorNuevo",
			"FechaActualizacion",
			"FechaLimite",
			"FechaPago",
			"NumeroDividendo",
			"IdTipoDividendo",
			"PrecioAnterior",
			"FechaHoraIngresoRegistro",
			"FechaHoraUltimaActualizacion",
			"TipoIngreso",
			"IdUsuario",
			"IdEstado",
			"Origen",
			"IdEmisor"
		)
		SELECT 
			"BSCL.Custodia.BD.Secuencias::EventoCapital".NEXTVAL	-- IdEventoCapital
			,p_IdEmpresa											-- IdEmpresa
			,'CC'													-- IdTipoEventoCapital
			,CA."INGNumeroCupon"									-- Secuencia	
			,CONCAT('CORTE CUPON ' , "Nemotecnico")					-- Descripcion
			,'S'													-- EsInscrito PENDIENTE
			,CA."IdClasificacionTipoInstrumento"					-- IdClasificacionTipoInstrumento
			,CA."IdTipoInstrumento"									-- IdTipoInstrumento
			,CA."IdInstrumento"										-- IdInstrumento
			,CA."Nemotecnico"										-- Nemo
			,0														-- Factor
			,0														-- Valor
			,CA."INGIdMonedaPagoCupon"								-- IdMoneda
			,0														-- IdInstrumentoNuevo
			,''														-- NemoNuevo
			,0														-- FactorNuevo
--			,:v_FechaSistema										-- FechaActualizacion
--			,:v_FechaSistema										-- FechaLimite
			,CA."INGFechaPagoCorteCupon"							-- FechaActualizacion
			,CA."INGFechaPagoCorteCupon"							-- FechaLimite
			,FPC."FechaCalculada"									-- FechaPago
			,0														-- NumeroDividendo
			,'P'													-- IdTipoDividendo
			,0														-- PrecioAnterior
--			,:v_FechaSistema										-- FechaHoraIngresoRegistro
--			,:v_FechaSistema										-- FechaHoraUltimaActualizacion
			, CURRENT_UTCTIMESTAMP									-- FechaHoraIngresoRegistro
			, CURRENT_UTCTIMESTAMP									-- FechaHoraUltimaActualizacion
			,'C'													-- TipoIngreso
			,p_IdUsuario											-- IdUsuario
			,'VIG'													-- IdEstado
			,'SIS'													-- Origen
			, CA."IdEmisor"											-- IdEmisor

		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"		CA
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
		ON 	(	CA."IdInstrumento" 		= EC."IdInstrumento" 			AND 
				CA."IdTipoInstrumento" 	= EC."IdTipoInstrumento"		AND
				EC."IdTipoEventoCapital"			= 'CC'	)			
		INNER JOIN	:out_fechasPagoCorteCuponCalculadas FPC
		ON (FPC."FechaInicial" = CA."INGFechaPagoCorteCupon")		
		WHERE 	CA."IdClasificacionTipoInstrumento" = 'RF'				AND 
--						CA."FechaLiquidacion"				= v_FechaSistema	AND	
--				CA."INGFechaPagoCorteCupon"			= v_FechaSistema	AND
				CA."INGFechaPagoCorteCupon"			BETWEEN p_FechaCorteCuponDesde AND p_FechaCorteCuponHasta	AND
				CA."IdEstado" 						= 'VIG'				AND
--				CA."IdModulo" 						= 'CUS'				AND
			  	EC."IdTipoInstrumento" 				IS NULL				
			  		
		GROUP BY 			
				CA."INGNumeroCupon", 		
				CA."IdClasificacionTipoInstrumento", 
				CA."IdTipoInstrumento", 	
				CA."IdInstrumento", 
				CA."Nemotecnico",
				CA."INGIdMonedaPagoCupon", 	
				CA."INGFechaPagoCorteCupon",
				CA."IdEmisor",
				FPC."FechaCalculada";


--------------------------------------------------------------------------------------------------------
--- 							EVENTO CLIENTES
--------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------
--- 							CARTERA CLIENTES
--------------------------------------------------------------------------------------------------------

		INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
	   	(
			"IdEventoCapitalCliente",			
			"IdEventoCapital",			
			"IdTipoEventoCapital",					
			"Secuencia",							
			"IdSocioNegocio",						
			"TipoSocioNegocio",			   			
			"IdPortafolio",							
			"IdTipoDocumentoIdentidad",				
			"DocumentoIdentidad",					
			"NombreCliente",						
			"IdClasificacionTipoInstrumento",		
			"IdTipoInstrumento",					
			"IdInstrumento",						
			"Nemo",									
			"IdInstrumentoNuevo",					
			"NemoNuevo",							
			"FechaCierre",							
			"FechaPago",							
			"CantidadDerecho",						
			"TipoCartera",							
			"CantidadNueva",						
			"MontoCapital",							
			"MontoInteres",							
			"Total",								
			"IdMoneda",								
			"SecuenciaCupon",						
			"IdEmpleadoEjecutivoPortafolio",					
			"CodigoUsuarioAgente",
			
			"NombreAgente",							
			"CodigoSucursal",						
			"FechaHoraIngresoRegistro",				
			"FechaHoraUltimaActualizacion",			
			"IdUsuario",							
			"IdEstado",								
			"IdBoveda",								
			"IdBolsa",								
			"IdConceptoGarantia",					
			"IdTipoOperacionGarantia",
			"MontoReajuste",
			"Origen",
			"IdTipoReajuste",
			"INGMontoInteresCorteCupon"
			)		
		SELECT 
			"BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL, 	-- "IdEventoCapitalCliente"
			EC."IdEventoCapital",											-- "IdEventoCapital"
			'CC',															-- "IdTipoEventoCapital"
			CA."INGNumeroCupon",											-- "Secuencia"	
			IFNULL(SOC."IdSocioDeNegocio",0),								-- "IdSocioNegocio"
			CASE 
				WHEN SOC."IdCliente" > 0 		THEN 'CLI'
				WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
				WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
				WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
			END	,															--	"TipoSocioNegocio"
			CA."IdPortafolio",												-- "IdPortafolio"
			'R',															-- "IdTipoDocumentoIdentidad"
			CA."DocumentoIdentidad",										-- "DocumentoIdentidad"
			IFNULL(CL."NombreCompleto",''),									-- "NombreCliente"
			CA."IdClasificacionTipoInstrumento",							-- "IdClasificacionTipoInstrumento"
			CA."IdTipoInstrumento",											-- "IdTipoInstrumento"
			CA."IdInstrumento",												-- "IdInstrumento"
			CA."Nemotecnico",												-- "Nemo"
			0,																-- "IdInstrumentoNuevo"
			'',																-- "NemoNuevo"
			CA."INGFechaPagoCorteCupon",									-- "FechaCierre"
			FPC."FechaCalculada",											-- "FechaPago"
			SUM("Cantidad"),												-- "CantidadDerecho"
			'CUS',															-- "TipoCartera"
			0,																-- "CantidadNueva"
			SUM(CA."INGMontoCapitalCorteCupon"),							-- "MontoCapital"	
			SUM(CA."INGInteresesPagoCorteCupon"),							-- "MontoInteres"
			SUM(CA."INGMontoCapitalCorteCupon"+CA."INGInteresesPagoCorteCupon"),		-- "Total"
			CA."INGIdMonedaPagoCupon",										-- "IdMoneda"
			CA."INGNumeroCupon",											-- "SecuenciaCupon"			
--			'',																-- "CodigoUsuarioAgente"	
--			'',																-- "NombreAgente"
--			'',																-- "CodigoSucursal"
			COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),				
			COALESCE(EJE."CodigoEmpleado",''),								--	"CodigoUsuarioAgente"
			COALESCE(EJE."NombreCompletoEjecutivo",''),						--	"NombreAgente"
			COALESCE(EJE."CodigoSucursal",''),							
			CURRENT_UTCTIMESTAMP,											-- "FechaHoraIngresoRegistro"
			CURRENT_UTCTIMESTAMP,											-- "FechaHoraUltimaActualizacion"
			p_IdUsuario,													-- "IdUsuario"			
			'VIG',															-- "IdEstado"
			CA."IdBoveda",													-- "IdBoveda"
			CA."IdBolsa",													-- "IdBolsa"
			'',																-- "IdConceptoGarantia"
			'',																-- "IdTipoOperacionGarantia"
			SUM(CA."INGMontoReajusteCorteCupon"),							-- MontoReajuste
			'SIS',
			CA."IdTipoReajuste",
			CA."INGMontoInteresCorteCupon"
			
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapital"						AS EC
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"					AS CA
				ON (EC."IdTipoInstrumento" 	= CA."IdTipoInstrumento" 		AND
					EC."IdInstrumento"		= CA."IdInstrumento")
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC
				ON (CA."IdCliente" = SOC."IdCliente")
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL
				ON (CA."IdCliente" = CL."IdCliente")						
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
				ON (CA."IdPortafolio" = EJE."IdPortafolio")
			INNER JOIN	:out_fechasPagoCorteCuponCalculadas FPC
				ON (FPC."FechaInicial" = CA."INGFechaPagoCorteCupon")
		WHERE 	EC."IdTipoEventoCapital"			= 'CC'				AND
				CA."IdClasificacionTipoInstrumento" = 'RF'				AND 
--				CA."FechaLiquidacion"				= v_FechaSistema	AND
--				CA."INGFechaPagoCorteCupon"			= v_FechaSistema	AND	
				CA."INGFechaPagoCorteCupon"			BETWEEN p_FechaCorteCuponDesde AND p_FechaCorteCuponHasta	AND	
				CA."IdEstado" 						= 'VIG'				
		GROUP BY 
			EC."IdEventoCapital",
			IFNULL(SOC."IdSocioDeNegocio",0),
			CASE 
				WHEN SOC."IdCliente" > 0 		THEN 'CLI'
				WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
				WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
				WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
			END,
			CA."IdPortafolio",				
			CA."DocumentoIdentidad",
			IFNULL(CL."NombreCompleto",''),				
			CA."IdClasificacionTipoInstrumento",	
			CA."IdTipoInstrumento",		
			CA."IdInstrumento",
			CA."Nemotecnico",
			CA."INGFechaPagoCorteCupon",
			CA."INGFechaPagoCorteCupon",
			CA."INGIdMonedaPagoCupon",						
			CA."INGNumeroCupon",
			COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
			COALESCE(EJE."CodigoEmpleado",''),
			COALESCE(EJE."NombreCompletoEjecutivo",''),
			COALESCE(EJE."CodigoSucursal",''),			
			CA."IdBoveda",
			CA."IdBolsa",
			CA."IdTipoReajuste",
			CA."INGMontoInteresCorteCupon",
			FPC."FechaCalculada";
		
--------------------------------------------------------------------------------------------------------
--- 									GARANTIAS
--------------------------------------------------------------------------------------------------------
 /*
			SELECT 
				"BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL, 	-- "IdEventoCapitalCliente"
				EC."IdEventoCapital",											-- "IdEventoCapital"
				'CC',															-- "IdTipoEventoCapital"
				GA."INGNumeroCupon",											-- "Secuencia"	
				IFNULL(SOC."IdSocioDeNegocio",0),								-- "IdSocioNegocio"
				'',			   													-- "TipoSocioNegocio"
				GA."IdPortafolio",												-- "IdPortafolio"
				'R',															-- "IdTipoDocumentoIdentidad"
				GA."DocumentoIdentidad",										-- "DocumentoIdentidad"
				IFNULL(CL."NombreCompleto",''),									-- "NombreCliente"
				GA."IdClasificacionTipoInstrumento",							-- "IdClasificacionTipoInstrumento"
				GA."IdTipoInstrumento",											-- "IdTipoInstrumento"
				GA."IdInstrumento",												-- "IdInstrumento"
				GA."Nemotecnico",												-- "Nemo"
				0,																-- "IdInstrumentoNuevo"
				'',																-- "NemoNuevo"
				GA."INGFechaPagoCorteCupon",									-- "FechaCierre"
				GA."INGFechaPagoCorteCupon",									-- "FechaPago"
				SUM("Cantidad"),												-- "CantidadDerecho"
				'GAR',															-- "TipoCartera"
				0,																-- "CantidadNueva"
				SUM(GA."INGMontoCapitalCorteCupon"),							-- "MontoCapital"	
				SUM(GA."INGMontoInteresCorteCupon"),							-- "MontoInteres"
				SUM(GA."INGMontoCorteCupon"),									-- "Total"
				GA."INGIdMonedaPagoCupon",										-- "IdMoneda"
				GA."INGNumeroCupon",											-- "SecuenciaCupon"			
				'',																-- "CodigoUsuarioAgente"	
				'',																-- "NombreAgente"
				'',																-- "CodigoSucursal"
				CURRENT_UTCTIMESTAMP,											-- "FechaHoraIngresoRegistro"
				CURRENT_UTCTIMESTAMP,											-- "FechaHoraUltimaActualizacion"
				p_IdUsuario,													-- "IdUsuario"			
				'VIG',															-- "IdEstado"
				GA."IdBoveda",													-- "IdBoveda"
				GA."IdBolsa",													-- "IdBolsa"
				'',																-- "IdConceptoGarantia"
				'',																-- "IdTipoOperacionGarantia"
				SUM(GA."INGMontoReajusteCorteCupon")							-- MontoReajuste
			FROM 
			"BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapital"					AS EC
			
			LEFT JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"				AS GA
			ON (EC."IdTipoInstrumento" 	= GA."IdTipoInstrumento" 		AND
				EC."IdInstrumento"		= GA."IdInstrumento")
				
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 	AS SOC
			ON (GA."IdCliente" = SOC."IdCliente")
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"			AS CL
			ON (GA."IdCliente" = CL."IdCliente")						
		WHERE 	EC."IdTipoEventoCapital"			= 'CC'				AND
				GA."IdClasificacionTipoInstrumento" = 'RF'				AND 
				GA."FechaLiquidacion"				= v_FechaSistema	AND
				GA."IdEstado" 						= 'VIG'				
		GROUP BY 
			EC."IdEventoCapital",
			IFNULL(SOC."IdSocioDeNegocio",0),
			GA."IdPortafolio",				
			GA."DocumentoIdentidad",
			IFNULL(CL."NombreCompleto",''),				
			GA."IdClasificacionTipoInstrumento",	
			GA."IdTipoInstrumento",		
			GA."IdInstrumento",
			GA."Nemotecnico",
			GA."INGFechaPagoCorteCupon",
			GA."INGFechaPagoCorteCupon",
			GA."INGIdMonedaPagoCupon",						
			GA."INGNumeroCupon",					
			GA."IdBoveda",
			GA."IdBolsa";			

 			*/
--------------------------------------------------------------------------------------------------------
--- 									EVENTOS CONTRAPARTE
--------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------
--- EVENTO CONTRAPARTE BOVEDAS (DCV - COR)
--------------------------------------------------------------------------------------------------------

		INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
	   	(
			"IdEventoCapitalCliente",			
			"IdEventoCapital",			
			"IdTipoEventoCapital",					
			"Secuencia",							
			"IdSocioNegocio",						
			"TipoSocioNegocio",			   			
			"IdPortafolio",							
			"IdTipoDocumentoIdentidad",				
			"DocumentoIdentidad",					
			"NombreCliente",						
			"IdClasificacionTipoInstrumento",		
			"IdTipoInstrumento",					
			"IdInstrumento",						
			"Nemo",									
			"IdInstrumentoNuevo",					
			"NemoNuevo",							
			"FechaCierre",							
			"FechaPago",							
			"CantidadDerecho",						
			"TipoCartera",							
			"CantidadNueva",						
			"MontoCapital",							
			"MontoInteres",							
			"Total",								
			"IdMoneda",								
			"SecuenciaCupon",						
			"CodigoUsuarioAgente",					
			"NombreAgente",							
			"CodigoSucursal",						
			"FechaHoraIngresoRegistro",				
			"FechaHoraUltimaActualizacion",			
			"IdUsuario",							
			"IdEstado",								
			"IdBoveda",								
			"IdBolsa",								
			"IdConceptoGarantia",					
			"IdTipoOperacionGarantia",
			"MontoReajuste",
			"Origen")				


		SELECT 
			"BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL, 	-- "IdEventoCapitalCliente"
			EC."IdEventoCapital",											-- "IdEventoCapital"
			'CC',															-- "IdTipoEventoCapital"
			CA."INGNumeroCupon",											-- "Secuencia"	
			0,																-- "IdSocioNegocio"
			'CNT',			   													-- "TipoSocioNegocio"
			0,																-- "IdPortafolio"
			'R',															-- "IdTipoDocumentoIdentidad"
			BO."DocumentoIdentidad",										-- "DocumentoIdentidad"
			IFNULL(PE."RazonSocial",''),									-- "NombreCliente"
			CA."IdClasificacionTipoInstrumento",							-- "IdClasificacionTipoInstrumento"
			CA."IdTipoInstrumento",											-- "IdTipoInstrumento"
			CA."IdInstrumento",												-- "IdInstrumento"
			CA."Nemotecnico",												-- "Nemo"
			0,																-- "IdInstrumentoNuevo"
			'',																-- "NemoNuevo"
			CA."INGFechaPagoCorteCupon",									-- "FechaCierre"
			FPC."FechaCalculada",											-- "FechaPago"
			SUM("Cantidad"),												-- "CantidadDerecho"
			'CUS',															-- "TipoCartera"
			0,																-- "CantidadNueva"
			SUM(CA."INGMontoCapitalCorteCupon")* -1,						-- "MontoCapital"	
			SUM(CA."INGInteresesPagoCorteCupon"),							-- "MontoInteres"
			SUM(CA."INGMontoCapitalCorteCupon" + "INGInteresesPagoCorteCupon") * -1,								-- "Total"
			CA."INGIdMonedaPagoCupon",										-- "IdMoneda"
			CA."INGNumeroCupon",											-- "SecuenciaCupon"			
			'',																-- "CodigoUsuarioAgente"	
			'',																-- "NombreAgente"
			'',																-- "CodigoSucursal"
			CURRENT_UTCTIMESTAMP,											-- "FechaHoraIngresoRegistro"
			CURRENT_UTCTIMESTAMP,											-- "FechaHoraUltimaActualizacion"
			p_IdUsuario,													-- "IdUsuario"			
			'VIG',															-- "IdEstado"
			CA."IdBoveda",													-- "IdBoveda"
			CA."IdBolsa",													-- "IdBolsa"
			'',																-- "IdConceptoGarantia"
			'',																-- "IdTipoOperacionGarantia"
			SUM(CA."INGMontoReajusteCorteCupon"),							-- "MontoReajuste"
			'SIS'															-- "Origen"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapital"				AS EC			
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"				AS CA
				ON (EC."IdTipoInstrumento" 	= CA."IdTipoInstrumento" 		AND
					EC."IdInstrumento"		= CA."IdInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             	AS BO
				ON (CA."IdBoveda" = BO."IdBoveda")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SN
				ON (BO."IdPersona" = SN."IdPersona")
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE
				ON (BO."IdPersona" = PE."IdPersona")
			INNER JOIN	:out_fechasPagoCorteCuponCalculadas FPC
				ON (FPC."FechaInicial" = CA."INGFechaPagoCorteCupon")
		WHERE 	EC."IdTipoEventoCapital"			= 'CC'				AND
				CA."IdClasificacionTipoInstrumento" = 'RF'				AND 
--				CA."FechaLiquidacion"				= v_FechaSistema	AND
				CA."INGFechaPagoCorteCupon"			BETWEEN p_FechaCorteCuponDesde AND p_FechaCorteCuponHasta	AND	
				CA."IdEstado" 						= 'VIG'				AND
				CA."IdBoveda" 					   <> 'CIA'
		GROUP BY 
			EC."IdEventoCapital",
			BO."DocumentoIdentidad",
			IFNULL(PE."RazonSocial",''),
			CA."IdClasificacionTipoInstrumento",	
			CA."IdTipoInstrumento",		
			CA."IdInstrumento",
			CA."Nemotecnico",
			CA."INGFechaPagoCorteCupon",
			CA."INGFechaPagoCorteCupon",
			CA."INGIdMonedaPagoCupon",						
			CA."INGNumeroCupon",
			CA."IdBoveda",					
			CA."IdBolsa",
			FPC."FechaCalculada";
			
--------------------------------------------------------------------------------------------------------
-- 						EVENTO CONTRAPARTE BOVEDAS (CIA)
--------------------------------------------------------------------------------------------------------

		INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
	   	(
			"IdEventoCapitalCliente",			
			"IdEventoCapital",			
			"IdTipoEventoCapital",					
			"Secuencia",							
			"IdSocioNegocio",						
			"TipoSocioNegocio",			   			
			"IdPortafolio",							
			"IdTipoDocumentoIdentidad",				
			"DocumentoIdentidad",					
			"NombreCliente",						
			"IdClasificacionTipoInstrumento",		
			"IdTipoInstrumento",					
			"IdInstrumento",						
			"Nemo",									
			"IdInstrumentoNuevo",					
			"NemoNuevo",							
			"FechaCierre",							
			"FechaPago",							
			"CantidadDerecho",						
			"TipoCartera",							
			"CantidadNueva",						
			"MontoCapital",							
			"MontoInteres",							
			"Total",								
			"IdMoneda",								
			"SecuenciaCupon",						
			"CodigoUsuarioAgente",					
			"NombreAgente",							
			"CodigoSucursal",						
			"FechaHoraIngresoRegistro",				
			"FechaHoraUltimaActualizacion",			
			"IdUsuario",							
			"IdEstado",								
			"IdBoveda",								
			"IdBolsa",								
			"IdConceptoGarantia",					
			"IdTipoOperacionGarantia",
			"MontoReajuste",
			"Origen")				
		SELECT 
			"BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL, 	-- "IdEventoCapitalCliente"
			EvCap."IdEventoCapital",										-- "IdEventoCapital"
			'CC',															-- "IdTipoEventoCapital"
			Carte."INGNumeroCupon",											-- "Secuencia"	
			0,																-- "IdSocioNegocio"
			'CNT',			   													-- "TipoSocioNegocio"
			0,																-- "IdPortafolio"
			'R',															-- "IdTipoDocumentoIdentidad"
			IFNULL(DocIde."DocumentoIdentidad",''),							-- "DocumentoIdentidad"

			CASE IFNULL(DocIde."DocumentoIdentidad",'')
				WHEN '' THEN 'NO EXISTE SOCIO NEGOCIO CONTRAPARTE: '||Instru."RazonSocial"
				ELSE IFNULL(Instru."RazonSocial",'')
			END,															-- "NombreCliente"
			Carte."IdClasificacionTipoInstrumento",							-- "IdClasificacionTipoInstrumento"
			Carte."IdTipoInstrumento",											-- "IdTipoInstrumento"
			Carte."IdInstrumento",												-- "IdInstrumento"
			Carte."Nemotecnico",												-- "Nemo"
			0,																-- "IdInstrumentoNuevo"
			'',																-- "NemoNuevo"
			Carte."INGFechaPagoCorteCupon",									-- "FechaCierre"
			FPC."FechaCalculada",											-- "FechaPago"
			SUM("Cantidad"),												-- "CantidadDerecho"
			'CUS',															-- "TipoCartera"
			0,																-- "CantidadNueva"
			SUM(Carte."INGMontoCapitalCorteCupon") *-1,							-- "MontoCapital"	
			SUM(Carte."INGMontoInteresCorteCupon"),							-- "MontoInteres"
			SUM(Carte."INGMontoCorteCupon" * -1),									-- "Total"
			Carte."INGIdMonedaPagoCupon",										-- "IdMoneda"
			Carte."INGNumeroCupon",											-- "SecuenciaCupon"			
			'',																-- "CodigoUsuarioAgente"	
			'',																-- "NombreAgente"
			'',																-- "CodigoSucursal"
			CURRENT_UTCTIMESTAMP,											-- "FechaHoraIngresoRegistro"
			CURRENT_UTCTIMESTAMP,											-- "FechaHoraUltimaActualizacion"
			p_IdUsuario,													-- "IdUsuario"			
			CASE IFNULL(DocIde."DocumentoIdentidad",'')
				WHEN '' THEN 'PEN'
				ELSE 'VIG'
			END,															-- "IdEstado"
			Carte."IdBoveda",												-- "IdBoveda"
			Carte."IdBolsa",												-- "IdBolsa"
			'',																-- "IdConceptoGarantia"
			'',																-- "IdTipoOperacionGarantia"
			SUM(Carte."INGMontoReajusteCorteCupon"),							-- "MontoReajuste"
			'SIS'															-- "Origen"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapital"							AS EvCap
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"						AS Carte
			ON (EvCap."IdTipoInstrumento" 	= Carte."IdTipoInstrumento" AND
				EvCap."IdInstrumento"		= Carte."IdInstrumento")
			LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 				AS Instru
				ON EvCap."IdInstrumento" = Instru."IdInstrumento"
--			LEFT JOIN 	"BSCL.Mercado.BD.Vistas::MER.ConsultarEmisorInstrumento" 			AS Emisor
--				ON  Instru."IdEmisor" 			= Emisor."IdEmisor" 		AND 
--					EvCap."IdTipoInstrumento" 	= Emisor."IdTipoInstrumento"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"	AS DocIde
				ON Instru."DocumentoIdentidadEmisor" = DocIde."DocumentoIdentidad"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"		AS SocNeg
				ON DocIde."IdPersona" = SocNeg."IdPersona"
			INNER JOIN	:out_fechasPagoCorteCuponCalculadas FPC
				ON (FPC."FechaInicial" = Carte."INGFechaPagoCorteCupon")
		WHERE 	EvCap."IdTipoEventoCapital"				= 'CC'					AND
				Carte."IdClasificacionTipoInstrumento"	= 'RF'					AND 
--				Carte."INGFechaPagoCorteCupon"			= v_FechaSistema		AND
				Carte."INGFechaPagoCorteCupon"			BETWEEN p_FechaCorteCuponDesde AND p_FechaCorteCuponHasta	AND
				Carte."IdEstado" 						= 'VIG'					AND
				Carte."IdBoveda" = 'CIA'
		GROUP BY 
			EvCap."IdEventoCapital",
			DocIde."DocumentoIdentidad",
			Instru."RazonSocial",
			Carte."IdClasificacionTipoInstrumento",	
			Carte."IdTipoInstrumento",		
			Carte."IdInstrumento",
			Carte."Nemotecnico",
			Carte."INGFechaPagoCorteCupon",
			Carte."INGFechaPagoCorteCupon",
			Carte."INGIdMonedaPagoCupon",						
			Carte."INGNumeroCupon",					
			Carte."IdBoveda",
			Carte."IdBolsa",
			FPC."FechaCalculada";		
			
			
--------------------------------------------------------------------------------------------------------
--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago" a partir de resultado 
--- Formas de Pago
--------------------------------------------------------------------------------------------------------
			
		INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"(
			"IdEventoCapitalClienteFormaDePago",
			"IdEventoCapitalCliente",
			"IdTipoMovimiento",
			"IdModalidadPago",
			"IdMedioDePago",
			"NombreMedioDePago",
			"IdBanco",
			"NombreBanco",
			"IdTipoCuentaBancaria",
			"NombreTipoCuentaBancaria",
			"IdCuentaBancaria",
			"NombreCuentaBancaria",
			"NumeroCuentaBancaria",
			"IdTipoTarjetaBancaria",
			"NumeroTarjetaBancaria",
			"NumeroMedioDePago",
			"Sucursal",
			"FechaVctoDocumento",
			"Monto",
			"FechaHoraIngresoRegistro",
			"IdUsuarioIngreso",
			"IdEstado")
	
		SELECT 
		    "BSCL.Custodia.BD.Secuencias::EventoCapitalClienteFormaDePago".NEXTVAL,
			Ecc."IdEventoCapitalCliente",											-- "IdEventoCapitalCliente"
			'I'										as "IdTipoMovimiento",			-- "IdTipoMovimiento"
			IFNULL(MdP."IdModalidadDePago",'')		as "IdModalidadPago",			-- "IdModalidadPago"
			Efp."IdMedioDePago"						as "IdMedioDePago",				-- "IdMedioDePago"
			IFNULL(Efp."MedioDePago",'Efectivo')	as "NombreMedioDePago", 		-- "NombreMedioDePago"
			Efp."IdBancoCuentaBancaria",											--"IdBanco"
			Efp."NombreBancoCuentaBancaria",										--"NombreBanco"
			Efp."IdTipoCuentaBancaria",												--"IdTipoCuentaBancaria",
			Efp."NombreTipoCuentaBancaria",											--"NombreTipoCuentaBancaria",
			Efp."IdCuentaBancaria",													--"IdCuentaBancaria",
			'' as "NombreCuentaBancaria",											--"NombreCuentaBancaria",
			Efp."NumeroCuentaBancaria",												--"NumeroCuentaBancaria"
			Efp."IdTipoTarjetaBancaria",											--"IdTipoTarjetaBancaria",
			Efp."NumeroTarjetaBancaria",											--"NumeroTarjetaBancaria",
			'' as "NumeroMedioDePago",												--"NumeroMedioDePago",
			Ecc."CodigoSucursal",													--"Sucursal",
			Ecc."FechaPago",														--"FechaVctoDocumento",
			Ecc."Total",															--"Monto",
			CURRENT_UTCTIMESTAMP 					as "FechaHoraIngresoRegistro",	--"FechaHoraIngresoRegistro",
			p_IdUsuario 							as "IdUsuarioIngreso",			--"IdUsuarioIngreso",
			'VIG'--p_IdEstado								as "IdEstado"					--"IdEstado"	
			
		FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 						Ecc 
				INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" 	Efp 
					ON (Ecc."IdPortafolio" = Efp."IdPortafolio")
					AND Efp."IdEstado" = 'ACT'
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago" MdP
					ON Efp."IdMedioDePago" = MdP."IdMedioDePago"
	   	WHERE Ecc."IdTipoEventoCapital" = 	Case Efp."IdTipoOperacion" 
	   											When 'CORCUP' Then 'CC' 
	   										End;   
	
	
		
		if (p_out_ErroresContador > 0) then 
			SIGNAL SALIDA_CONTROLADA;
		else
			v_TablaCodigosMensajes =	Select 	1								As "IdInterno", 
												'UI_OPERACION_PROCESAR_EXITO'	As "Codigo",
												'' 								As "CodigoDato1", 
												'' 								as "ValorDato1",
												'' 								As "CodigoDato2", 
												'' 								As "ValorDato2" 
											From dummy;
					                        
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
			p_out_InformacionContador = 1;
			p_out_InformacionDetalle = 	Select "Codigo", "Descripcion" 
										From :v_out_hash;				
		end if;
	

    if p_out_ErroresContador > 0 then 
		SIGNAL SALIDA_CONTROLADA;
	end if;
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END;
GO