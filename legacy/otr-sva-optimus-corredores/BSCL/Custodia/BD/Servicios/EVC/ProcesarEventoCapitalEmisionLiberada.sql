SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalEmisionLiberada";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalEmisionLiberada" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			DEFAULT 0,
    IN p_IdUsuario          				BIGINT 			DEFAULT 0,
    IN p_IdModulo           				NVARCHAR(3) 	DEFAULT '',  
	
	IN p_IdEventoCapital					BIGINT     		DEFAULT 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	DEFAULT '',
	IN p_Secuencia							BIGINT 			DEFAULT 0,
	IN p_NumeroDividendo					BIGINT 			DEFAULT 0,
	IN p_IdClasificacionTipoInstrumento		NVARCHAR(3) 	DEFAULT '',
	IN p_IdTipoInstrumento					NVARCHAR(3) 	DEFAULT '',  
	IN p_IdInstrumento						BIGINT 			DEFAULT 0,
	IN p_Nemo								NVARCHAR(40) 	DEFAULT '',
	IN p_Factor								DECIMAL(26,8) 	DEFAULT 0,
	IN p_IdMoneda							NVARCHAR(3) 	DEFAULT '',
	IN p_IdInstrumentoNuevo					BIGINT 			DEFAULT 0,
	IN p_NemoNuevo							NVARCHAR(40) 	DEFAULT '',
	IN p_FactorNuevo						DECIMAL(26,8) 	DEFAULT 0,
	IN p_FechaLimite						NVARCHAR(10) 	DEFAULT '0001-01-01',     
	IN p_FechaPago							NVARCHAR(10) 	DEFAULT '0001-01-01',     
	IN p_IdEstado							NVARCHAR(3) 	DEFAULT '',
	
-- Parametros de Salida
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) default empty,
	
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) default empty,	
	
	OUT p_out_ErroresContador 			INT,   
	OUT p_out_ErroresDetalle 			TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000) )default empty
	 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN SEQUENTIAL EXECUTION
	--------------------
--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort 						INT DEFAULT 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);	        	
	DECLARE v_errorcliente 				INT DEFAULT 0;  
	DECLARE v_Estado 					NVARCHAR(3) DEFAULT 'PRO';
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_AdvertenciasContador 
		FROM :p_out_AdvertenciasDetalle;		 					 	
	END;


	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" 	FROM dummy WHERE 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_ResultadoRegla        NVARCHAR(40);
	    DECLARE	v_Instrumentos 			TABLE ( 
												"IdClasificacionTipoInstrumento"	NVARCHAR(3),
												"IdTipoInstrumento"					NVARCHAR(3),
												"IdInstrumento"						BIGINT ,
												"IdEmisor"							NVARCHAR(40) ,
												"FechaVencimiento"					DATE ,
												"IdTipoReajuste"					NVARCHAR(3),
												"EsFungible"						NVARCHAR(1),
												"IdBoveda"							NVARCHAR(3)
											);

	    DECLARE v_Carteras 				TABLE( 
												"FolioCartera"						NVARCHAR(1),-- BIGINT, --TAMBIEN FOLIO CARTERA RELACIONADA EN OTRAS TABLAS
												"FolioGarantia"						NVARCHAR(1),--BIGINT,
												"FolioSubyacente"					NVARCHAR(1),--BIGINT,
												"IdCliente"							NVARCHAR(1),--BIGINT,
												"IdPortafolio"						NVARCHAR(1)--BIGINT
											);
		
		--variables de proceso
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_RowCount				BIGINT := 0;
		
		
		p_out_TblId = SELECT 0 as "Ids" FROM dummy WHERE 1=2;

		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
		--	 	v_IdEjecucion	:=0;
		p_TblErrores 	= SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores 	= SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			DECLARE v_resultadoRegla nvarchar(40);

			--Si existen errores en las validaciones debe salir
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;				
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN

--		DECLARE v_TipoSocioNegocio			NVARCHAR(1):='';
		DECLARE v_count_errores_custodia 	BIGINT;
		DECLARE v_MontoInteres				DECIMAL(26,8):= 0;
		DECLARE v_NombreAgente				NVARCHAR(100):='';
		DECLARE v_CodigoSucursal			NVARCHAR(40):='';
		DECLARE v_CodigoUsuarioAgente		NVARCHAR(40):='';
		DECLARE v_KeyFlujoCustodia			BIGINT;
		DECLARE v_EventoCapitalCliente 		TABLE (	
													"IdEventoCapital"		 			BIGINT,
													"IdTipoEventoCapital"				NVARCHAR(2),
													"Secuencia"							BIGINT,
													"NumeroDividendo"					BIGINT,
													"IdSocioNegocio"					BIGINT,
													"TipoSocioNegocio"					NVARCHAR(3),
													"IdPortafolio"						BIGINT,
													"IdTipoDocumentoIdentidad"			NVARCHAR(3),
													"DocumentoIdentidad"				NVARCHAR(40),
													"NombreCliente"						NVARCHAR(302),
													"IdClasificacionTipoInstrumento"	NVARCHAR(3),
													"IdTipoInstrumento"					NVARCHAR(3),
													"IdInstrumento"						BIGINT,
													"Nemo"								NVARCHAR(40),
													"IdInstrumentoNuevo"				BIGINT,
													"NemoNuevo"							NVARCHAR(40),
													"FechaCierre"						DATE,
													"FechaPago"							DATE,
													"CantidadDerecho"					DECIMAL(28,8),
													"TipoCartera"						NVARCHAR(3),
													"CantidadNueva"						DECIMAL,
													"MontoCapital"						DECIMAL,		
													"MontoInteres"						DECIMAL(28,8),
													"Total"								DECIMAL,
													"IdMoneda"							NVARCHAR(3),
													"SecuenciaCupon"					BIGINT,
													"IdEmpleadoEjecutivoPortafolio"		BIGINT,
													"CodigoUsuarioAgente"				NVARCHAR(40),
													"NombreAgente"						NVARCHAR(302),
													"CodigoSucursal"					NVARCHAR(40),
													"FechaHoraIngresoRegistro"			DATETIME,
													"FechaHoraUltimaActualizacion"		DATETIME,
													"IdUsuario"							BIGINT,
													"IdEstado"							NVARCHAR(3),
													"IdBoveda"							NVARCHAR(3),
													"IdBolsa"							NVARCHAR(3),
													"IdConcepto"						NVARCHAR(6),
													"Origen"							NVARCHAR(3));


	--------------------------------------------------------------------------------------------------------
	--- EVENTO A FAVOR BOVEDA
	--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado 
	--- obtenido del servicio anterior ("BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia")
	--------------------------------------------------------------------------------------------------------    

		v_EventoCapitalCliente 	= 
			SELECT
			p_IdEventoCapital 											as "IdEventoCapital",							--	"IdEventoCapital"
			p_IdTipoEventoCapital 										as "IdTipoEventoCapital",						--	"IdTipoEventoCapital"
			p_Secuencia 												as "Secuencia",									--	"Secuencia"
			p_NumeroDividendo 											as "NumeroDividendo",							--  "NumeroDividendo"
			IFNULL(SOC."IdSocioDeNegocio",0) 							as "IdSocioNegocio",							--	"IdSocioNegocio"
			CASE 
				WHEN SOC."IdCliente" > 0 		THEN 'CLI'
				WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
				WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
				WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
			END															as "TipoSocioNegocio",							--	"TipoSocioNegocio"
			IFNULL(FT."IdPortafolio",0)									as "IdPortafolio",								--	"IdPortafolio"
			'R'															as "IdTipoDocumentoIdentidad",					--	"IdTipoDocumentoIdentidad"
			IFNULL(CL."DocumentoIdentidad",'')							as "DocumentoIdentidad",						--	"DocumentoIdentidad"
			IFNULL(CL."NombreCompleto",'')								as "NombreCliente",								--	"NombreCliente"
			p_IdClasificacionTipoInstrumento							as "IdClasificacionTipoInstrumento",			--	"IdClasificacionTipoInstrumento"
			p_IdTipoInstrumento											as "IdTipoInstrumento",							--  "IdTipoInstrumento"
			p_IdInstrumento												as "IdInstrumento",								--	"IdInstrumento"
			p_Nemo														as "Nemo",										--	"Nemo"
			IFNULL(p_IdInstrumentoNuevo,0 )								as "IdInstrumentoNuevo",						--	"IdInstrumentoNuevo"
			IFNULL(p_NemoNuevo,0 )										as "NemoNuevo",									--  "NemoNuevo"
			TO_DATE(p_FechaPago, 'YYYY-MM-DD') 							as "FechaCierre",								--	"FechaCierre"
			TO_DATE(p_FechaPago, 'YYYY-MM-DD') 							as "FechaPago",									--	"FechaPago"
			CASE FT."TipoCartera"
				WHEN 'SIC'	THEN IFNULL(ROUND (SUM(IFNULL(FT."Cantidad",0))  * -1)	,0)
				WHEN 'SIV'	THEN IFNULL(ROUND (SUM(IFNULL(FT."Cantidad",0)))		,0)
				WHEN 'PRT'	THEN IFNULL(ROUND (SUM(IFNULL(FT."Cantidad",0))  * -1)	,0)
				WHEN 'GAR'	THEN IFNULL(ROUND (SUM(IFNULL(FT."Cantidad",0)))		,0)
				ELSE 			 IFNULL(ROUND (SUM(IFNULL(FT."Cantidad",0)))		,0)
			END 														as "CantidadDerecho",
			FT."TipoCartera"											as "TipoCartera",								--	"TipoCartera" 														as "TipoCartera",
			IFNULL(FLOOR (SUM(IFNULL(FT."Cantidad",0)) * p_Factor),0)	as "CantidadNueva",								--	"CantidadNueva"
			0 															as "MontoCapital",								--	"MontoCapital"
			v_MontoInteres												as "MontoInteres",								--	"MontoInteres"
			0															as "Total",										--	"Total"								--	"Total"
			p_IdMoneda													as "IdMoneda",									--	"IdMoneda"
			p_Secuencia 												as "SecuenciaCupon",							--	"SecuenciaCupon"
			COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)				as "IdEmpleadoEjecutivoPortafolio",
			COALESCE(EJE."CodigoEmpleado",'')							as "CodigoUsuarioAgente",						--	"CodigoUsuarioAgente"
			COALESCE(EJE."NombreCompletoEjecutivo",'')					as "NombreAgente",								--	"NombreAgente"
			COALESCE(EJE."CodigoSucursal",'')							as "CodigoSucursal",							--	"CodigoSucursal"
			Current_UTCTIMESTAMP 										as "FechaHoraIngresoRegistro",					--	"FechaHoraIngresoRegistro"
			Current_UTCTIMESTAMP 										as "FechaHoraUltimaActualizacion",				--	"FechaHoraUltimaActualizacion"
			p_IdUsuario													as "IdUsuario",									--	"IdUsuario"
			p_IdEstado													as "IdEstado",									--	"IdEstado"
			CASE FT."IdBoveda"
				WHEN '' 	THEN 'CIA'
				WHEN 'COR' 	THEN 'DCV'
				ELSE 		 FT."IdBoveda"
			END															as "IdBoveda",									--  "IdBoveda"
			CASE FT."IdBolsa" 
				WHEN '' THEN 'BCS'
				ELSE 		  IFNULL(FT."IdBolsa",'BCS')
			END 														as "IdBolsa",
			FT."IdConcepto"												as "IdConcepto",
			'SIS'														as "Origen"
		FROM     		"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraEventoCapital"	AS FT
			LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL
				ON (FT."IdCliente" = CL."IdCliente")
			LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC
				ON (CL."IdCliente" = SOC."IdCliente")
			LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
				ON (FT."IdPortafolio" = EJE."IdPortafolio")
		WHERE
				FT."IdEmpresa" 		= p_IdEmpresa  
			AND FT."Nemotecnico"	= p_Nemo
			AND FT."IdCliente" 		> 0
			AND IFNULL(SOC."IdSocioDeNegocio",0) > 0
			AND FT."FechaOperacion" <= p_FechaLimite
			AND FT."IdEstado" = 'VIG'
		GROUP BY 
			p_IdEventoCapital,				
			p_IdTipoEventoCapital,			
			p_Secuencia,					
			IFNULL(SOC."IdSocioDeNegocio",0),
			IFNULL(CL."NombreCompleto",''),
			CASE 
				WHEN SOC."IdCliente" > 0 		THEN 'CLI'
				WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
				WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
				WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
			END			,				
			IFNULL(FT."IdPortafolio",0),	
			IFNULL(CL."DocumentoIdentidad",''),
			CL."NombreCompleto",
			FT."TipoCartera",
			COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
			COALESCE(EJE."CodigoEmpleado",''),
			COALESCE(EJE."NombreCompletoEjecutivo",''),
			COALESCE(EJE."CodigoSucursal",''),
			CASE FT."IdBoveda"
				WHEN '' 	THEN 'CIA'
				WHEN 'COR' 	THEN 'DCV'
				ELSE FT."IdBoveda"
			END,
			CASE FT."IdBolsa" 
				WHEN '' THEN 'BCS'
				ELSE IFNULL(FT."IdBolsa",'BCS')
			END,
			FT."IdConcepto";

		-------------------------------------------------------------------------------------------------------
		--- EVENTO CONTRAPARTE BOVEDAS (DCV - COR)
		--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado 
		--- obtenido del servicio anterior ("BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia")
		--------------------------------------------------------------------------------------------------------    
		-- SUMA DCV

		v_EventoCapitalCliente 	= 
		SELECT
			FT."IdEventoCapital"												as "IdEventoCapital",					-- "IdEventoCapital"
			FT."IdTipoEventoCapital"											as "IdTipoEventoCapital",				-- "IdTipoEventoCapital"
			FT."Secuencia"														as "Secuencia",							-- "Secuencia"
			p_NumeroDividendo 													as "NumeroDividendo",					--  "NumeroDividendo"
			IFNULL(SN."IdSocioDeNegocio",0)										as "IdSocioNegocio",					-- "IdSocioNegocio"
			'CNT'																as "TipoSocioNegocio",					-- "TipoSocioNegocio"
			0 																	as "IdPortafolio",						-- "IdPortafolio"
			'R'																	as "IdTipoDocumentoIdentidad",			-- "IdTipoDocumentoIdentidad"
			BO."DocumentoIdentidad"												as "DocumentoIdentidad",				-- "DocumentoIdentidad"
			IFNULL(PE."RazonSocial",'')											as "NombreCliente",						-- "NombreCliente"
			FT."IdClasificacionTipoInstrumento"									as "IdClasificacionTipoInstrumento",	-- "IdClasificacionTipoInstrumento"
			FT."IdTipoInstrumento"												as "IdTipoInstrumento",					-- "IdTipoInstrumento"
			FT."IdInstrumento"													as "IdInstrumento",						-- "IdInstrumento"
			FT."Nemo"															as "Nemo",								-- "Nemo"
			FT."IdInstrumentoNuevo"												as "IdInstrumentoNuevo",				-- "IdInstrumentoNuevo"
			FT."NemoNuevo"														as "NemoNuevo",							-- "NemoNuevo"
			FT."FechaCierre"													as "FechaCierre",						-- "FechaCierre"
			FT."FechaPago"														as "FechaPago",							-- "FechaPago"
--			SUM(FT."CantidadDerecho")											as "CantidadDerecho",					-- "CantidadDerecho"
/*			CASE FT."IdTipoEventoCapital"
				WHEN 'DV' THEN (SUM(FT."CantidadDerecho") * -1)
				ELSE SUM(FT."CantidadDerecho")
			END 																as "CantidadDerecho",*/
			(SUM(FT."CantidadDerecho") * -1)									as "CantidadDerecho",
			FT."TipoCartera"													as "TipoCartera",						-- "TipoCartera"
			(SUM(FT."CantidadNueva") * -1)										as "CantidadNueva",						-- "CantidadNueva"
			SUM(FT."MontoCapital")												as "MontoCapital",						-- "MontoCapital"
			SUM(FT."MontoInteres")												as "MontoInteres",						-- "MontoInteres"
			0																	as "Total",								-- "Total"
			FT."IdMoneda"														as "IdMoneda",							-- "IdMoneda"
			FT."SecuenciaCupon"													as "SecuenciaCupon",					-- "SecuenciaCupon"
			0																	as "IdEmpleadoEjecutivoPortafolio",
			''																	as "CodigoUsuarioAgente",
			''																	as "NombreAgente",	
			''																	as "CodigoSucursal",
			FT."FechaHoraIngresoRegistro"										as "FechaHoraIngresoRegistro",			-- "FechaHoraIngresoRegistro"
			FT."FechaHoraUltimaActualizacion"									as "FechaHoraUltimaActualizacion",		-- "FechaHoraUltimaActualizacion"
			FT."IdUsuario"														as "IdUsuario",							-- "IdUsuario"
			FT."IdEstado"														as "IdEstado",							-- "IdEstado"
			FT."IdBoveda"														as "IdBoveda",							-- "IdBoveda"
			CASE FT."IdBolsa" 
				WHEN '' THEN 'BCS'
				ELSE IFNULL(FT."IdBolsa",'BCS')										
			END 																as "IdBolsa",
			FT."IdConcepto"														as "IdConcepto",
			'SIS'																as "Origen"
		FROM :v_EventoCapitalCliente						 					AS FT												
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             	AS BO
			ON (FT."IdBoveda" = BO."IdBoveda")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SN
				ON (BO."IdPersona" = SN."IdPersona")
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE
				ON (BO."IdPersona" = PE."IdPersona")
	 	WHERE   
	 			FT."IdEventoCapital"	= p_IdEventoCapital
			AND	FT."Nemo"				= p_Nemo
			AND FT."IdBoveda" 			<> 'CIA'
			AND FT."TipoCartera" 		<> 'GAR'
			AND SN."IdEmpresa" 			= p_IdEmpresa  	
		GROUP BY 
			FT."IdEventoCapital",			
			FT."IdTipoEventoCapital",		
			FT."Secuencia",
			SN."IdSocioDeNegocio",
			BO."DocumentoIdentidad", 
			PE."RazonSocial",
			FT."IdClasificacionTipoInstrumento", 
			FT."IdTipoInstrumento",
			FT."IdInstrumento",
			FT."Nemo",
			FT."IdInstrumentoNuevo",
			FT."NemoNuevo",
			FT."FechaCierre",						
			FT."FechaPago",
			FT."TipoCartera",
			FT."IdMoneda",						
			FT."SecuenciaCupon",
			FT."FechaHoraIngresoRegistro",
			FT."FechaHoraUltimaActualizacion",		
			FT."IdUsuario",						
			FT."IdEstado",
			FT."IdBoveda",
			FT."IdBolsa",
			FT."IdConcepto"
--			FT."FolioCartera",
--			FT."IdCartera"
		UNION ALL
		SELECT * 
		FROM :v_EventoCapitalCliente;
		
		--------------------------------------------------------------------------------------------------------
		--- CONTRAPARTE GARANTIAS BOLSA
		--------------------------------------------------------------------------------------------------------
		
		v_EventoCapitalCliente 	= 
		SELECT
			FT."IdEventoCapital"												as "IdEventoCapital",					-- "IdEventoCapital"
			FT."IdTipoEventoCapital"											as "IdTipoEventoCapital",				-- "IdTipoEventoCapital"
			FT."Secuencia"														as "Secuencia",							-- "Secuencia"
			p_NumeroDividendo 													as "NumeroDividendo",					--  "NumeroDividendo"
			IFNULL(SOC."IdSocioDeNegocio",0)									as "IdSocioNegocio",					-- "IdSocioNegocio"
			'CNT'																as "TipoSocioNegocio",					-- "TipoSocioNegocio"
			0 																	as "IdPortafolio",						-- "IdPortafolio"
			'R'																	as "IdTipoDocumentoIdentidad",			-- "IdTipoDocumentoIdentidad"
			BO."DocumentoIdentidad"												as "DocumentoIdentidad",				-- "DocumentoIdentidad"
			IFNULL(PE."RazonSocial",'')											as "NombreCliente",						-- "NombreCliente"
			FT."IdClasificacionTipoInstrumento"									as "IdClasificacionTipoInstrumento",	-- "IdClasificacionTipoInstrumento"
			FT."IdTipoInstrumento"												as "IdTipoInstrumento",					-- "IdTipoInstrumento"
			FT."IdInstrumento"													as "IdInstrumento",						-- "IdInstrumento"
			FT."Nemo"															as "Nemo",								-- "Nemo"
			FT."IdInstrumentoNuevo"												as "IdInstrumentoNuevo",				-- "IdInstrumentoNuevo"
			FT."NemoNuevo"														as "NemoNuevo",							-- "NemoNuevo"
			FT."FechaCierre"													as "FechaCierre",						-- "FechaCierre"
			FT."FechaPago"														as "FechaPago",							-- "FechaPago"
--			SUM(FT."CantidadDerecho")											as "CantidadDerecho",					-- "CantidadDerecho"
/*			CASE FT."IdTipoEventoCapital"
				WHEN 'DV' THEN (SUM(FT."CantidadDerecho") * -1)
				ELSE SUM(FT."CantidadDerecho")
			END 																as "CantidadDerecho",*/
			(SUM(FT."CantidadDerecho") * -1)									as "CantidadDerecho",
			FT."TipoCartera"													as "TipoCartera",						-- "TipoCartera"
			(SUM(FT."CantidadNueva") * -1)										as "CantidadNueva",						-- "CantidadNueva"
			SUM(FT."MontoCapital")												as "MontoCapital",						-- "MontoCapital"
			SUM(FT."MontoInteres")												as "MontoInteres",						-- "MontoInteres"
			0																	as "Total",								-- "Total"
			FT."IdMoneda"														as "IdMoneda",							-- "IdMoneda"
			FT."SecuenciaCupon"													as "SecuenciaCupon",					-- "SecuenciaCupon"
			0																	as "IdEmpleadoEjecutivoPortafolio",
			''																	as "CodigoUsuarioAgente",
			''																	as "NombreAgente",	
			''																	as "CodigoSucursal",
			FT."FechaHoraIngresoRegistro"										as "FechaHoraIngresoRegistro",			-- "FechaHoraIngresoRegistro"
			FT."FechaHoraUltimaActualizacion"									as "FechaHoraUltimaActualizacion",		-- "FechaHoraUltimaActualizacion"
			FT."IdUsuario"														as "IdUsuario",							-- "IdUsuario"
			FT."IdEstado"														as "IdEstado",							-- "IdEstado"
			FT."IdBoveda"														as "IdBoveda",							-- "IdBoveda"
			CASE FT."IdBolsa" 
				WHEN '' THEN 'BCS'
				ELSE IFNULL(FT."IdBolsa",'BCS')										
			END 																as "IdBolsa",
			FT."IdConcepto"														as "IdConcepto",
			'SIS'																as "Origen"
		FROM :v_EventoCapitalCliente						 					AS FT												
		LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa"             	AS BO
			ON (CASE FT."IdBolsa"
				WHEN '' THEN 'BCS'
				ELSE IFNULL(FT."IdBolsa",'BCS')										
			END = BO."IdBolsa")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS SOC ON 
			SOC."IdTipoDocumentoIdentidad" = 'R' AND 
			SOC."DocumentoIdentidad" = BO."DocumentoIdentidad"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE
				ON (SOC."IdPersona" = PE."IdPersona")
	 	WHERE   
	 			FT."IdEventoCapital"	= p_IdEventoCapital
			AND	FT."Nemo"				= p_Nemo
			AND FT."IdBoveda" 		   <> 'CIA'
			AND FT."TipoCartera"      	= 'GAR'
			AND SOC."IdEmpresa" 			= p_IdEmpresa  	
		GROUP BY 
			FT."IdEventoCapital",			
			FT."IdTipoEventoCapital",		
			FT."Secuencia",
			SOC."IdSocioDeNegocio",
			BO."DocumentoIdentidad", 
			PE."RazonSocial",
			FT."IdClasificacionTipoInstrumento", 
			FT."IdTipoInstrumento",
			FT."IdInstrumento",
			FT."Nemo",
			FT."IdInstrumentoNuevo",
			FT."NemoNuevo",
			FT."FechaCierre",						
			FT."FechaPago",
			FT."TipoCartera",
			FT."IdMoneda",						
			FT."SecuenciaCupon",
			FT."FechaHoraIngresoRegistro",
			FT."FechaHoraUltimaActualizacion",		
			FT."IdUsuario",						
			FT."IdEstado",
			FT."IdBoveda",
			FT."IdBolsa",
			FT."IdConcepto"
		UNION ALL
		SELECT * 
		FROM :v_EventoCapitalCliente;
		

		--------------------------------------------------------------------------------------------------------
		--- EVENTO CONTRAPARTE BOVEDA (CIA)
		--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado 
		--- obtenido del servicio anterior ("BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia")
		--------------------------------------------------------------------------------------------------------    
	
		v_EventoCapitalCliente 	= 
		SELECT 
			EvCapCli."IdEventoCapital"											as "IdEventoCapital",					-- "IdEventoCapital"
			EvCapCli."IdTipoEventoCapital"										as "IdTipoEventoCapital",				-- "IdTipoEventoCapital"
			EvCapCli."Secuencia"												as "Secuencia",							-- "Secuencia"
			p_NumeroDividendo													as "NumeroDividendo",					--  "NumeroDividendo"
			IFNULL(SOCNEG."IdSocioDeNegocio",0)									as "IdSocioNegocio",					-- "IdSocioNegocio"
			'CNT'																as "TipoSocioNegocio",					-- "TipoSocioNegocio"
			0																	as "IdPortafolio",						-- "IdPortafolio"
			'R'																	as "IdTipoDocumentoIdentidad",			-- "IdTipoDocumentoIdentidad"
			IFNULL(DOCIDE."DocumentoIdentidad",'')								as "DocumentoIdentidad",				-- "DocumentoIdentidad"
			CASE IFNULL(DOCIDE."DocumentoIdentidad",'')
				WHEN '' THEN 'NO EXISTE SOCIO NEGOCIO CONTRAPARTE: '||Instru."RazonSocial"
				ELSE IFNULL(Instru."RazonSocial",'')
			END																	as "NombreCliente",						-- "NombreCliente"
			EvCapCli."IdClasificacionTipoInstrumento"							as "IdClasificacionTipoInstrumento",	-- "IdClasificacionTipoInstrumento"
			EvCapCli."IdTipoInstrumento"										as "IdTipoInstrumento",					-- "IdTipoInstrumento"
			EvCapCli."IdInstrumento" 											as "IdInstrumento",						-- "IdInstrumento"
			EvCapCli."Nemo" 													as "Nemo",								-- "Nemo"
			EvCapCli."IdInstrumentoNuevo"										as "IdInstrumentoNuevo",				-- "IdInstrumentoNuevo"
			EvCapCli."NemoNuevo"												as "NemoNuevo",							-- "NemoNuevo"
			EvCapCli."FechaCierre"												as "FechaCierre",						-- "FechaCierre"
			EvCapCli."FechaPago"												as "FechaPago",							-- "FechaPago"
--			SUM(EvCapCli."CantidadDerecho")										as "CantidadDerecho",					-- "CantidadDerecho"
/*			CASE EvCapCli."IdTipoEventoCapital"
				WHEN 'DV' THEN (SUM(EvCapCli."CantidadDerecho") * -1)	
				ELSE SUM(EvCapCli."CantidadDerecho")
			END 																as "CantidadDerecho",*/
			(SUM(EvCapCli."CantidadDerecho") * -1)								as "CantidadDerecho",

			EvCapCli."TipoCartera"												as "TipoCartera",						-- "TipoCartera"
			(SUM(EvCapCli."CantidadNueva") * -1)								as "CantidadNueva",						-- "CantidadNueva"
			SUM(EvCapCli."MontoCapital")										as "MontoCapital",						-- "MontoCapital"
			SUM(EvCapCli."MontoInteres")										as "MontoInteres",						-- "MontoInteres"
			0																	as "Total",								-- "Total"
			EvCapCli."IdMoneda"													as "IdMoneda",							-- "IdMoneda"
			EvCapCli."SecuenciaCupon"											as "SecuenciaCupon",					-- "SecuenciaCupon"
			0																	as "IdEmpleadoEjecutivoPortafolio",
			''																	as "CodigoUsuarioAgente",
			''																	as "NombreAgente",	
			''																	as "CodigoSucursal",
			EvCapCli."FechaHoraIngresoRegistro"									as "FechaHoraIngresoRegistro",			-- "FechaHoraIngresoRegistro"
			EvCapCli."FechaHoraUltimaActualizacion"								as "FechaHoraUltimaActualizacion",		-- "FechaHoraUltimaActualizacion"
			EvCapCli."IdUsuario"												as "IdUsuario",							-- "IdUsuario"
			CASE IFNULL(DOCIDE."DocumentoIdentidad",'')
				WHEN '' THEN 'PEN'
				ELSE EvCapCli."IdEstado"
			END																	as "IdEstado",							-- "IdEstado"
			EvCapCli."IdBoveda"													as "IdBoveda",							-- "IdBoveda"
			CASE EvCapCli."IdBolsa" 
				WHEN '' THEN 'BCS'
				ELSE IFNULL(EvCapCli."IdBolsa",'BCS')
			END																	as "IdBolsa",
			EvCapCli."IdConcepto"												as "IdConcepto",
			'SIS'																as "Origen"
		FROM	
			:v_EventoCapitalCliente						 									AS EvCapCli		
			LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 				AS Instru
				ON EvCapCli."IdInstrumento" = Instru."IdInstrumento"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"	AS DOCIDE
				ON Instru."DocumentoIdentidadEmisor" = DOCIDE."DocumentoIdentidad"  
				and DOCIDE."DocumentoIdentidad" <> ''
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"		AS SOCNEG
				ON (DOCIDE."IdPersona" = SOCNEG."IdPersona"	AND
					DOCIDE."IdEmpresa" = SOCNEG."IdEmpresa")
		WHERE 
			EvCapCli."IdBoveda" 		= 'CIA'				AND	
			EvCapCli."IdEventoCapital" 	= p_IdEventoCapital AND
			DOCIDE."IdEmpresa"			= p_IdEmpresa  
			
		GROUP BY 
			EvCapCli."IdEventoCapital",			
			EvCapCli."IdTipoEventoCapital",		
			EvCapCli."Secuencia",
			SOCNEG."IdSocioDeNegocio",
			DOCIDE."DocumentoIdentidad",
			Instru."RazonSocial",
			EvCapCli."IdClasificacionTipoInstrumento", 
			EvCapCli."IdTipoInstrumento",
			EvCapCli."IdInstrumento",
			EvCapCli."Nemo",
			EvCapCli."IdInstrumentoNuevo",
			EvCapCli."NemoNuevo",
			EvCapCli."FechaCierre",						
			EvCapCli."FechaPago",
			EvCapCli."TipoCartera",
			EvCapCli."IdMoneda",						
			EvCapCli."SecuenciaCupon",	
			EvCapCli."FechaHoraIngresoRegistro",
			EvCapCli."FechaHoraUltimaActualizacion",		
			EvCapCli."IdUsuario",						
			EvCapCli."IdEstado",
			EvCapCli."IdBoveda",
			EvCapCli."IdBolsa",
			EvCapCli."IdConcepto"
		UNION ALL
		SELECT * 
		FROM :v_EventoCapitalCliente;
		
		--------------------------------------------------------------------------------------------------------
		--- OPERACIONES EN TRÁNSITO DE VENTA
		--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente"  de operaciones
		--- que se encuentran en tránsito, por ejemplo, ventas lo liquidadas.
		--------------------------------------------------------------------------------------------------------    
	
		v_EventoCapitalCliente 	= 
		SELECT 	
				p_IdEventoCapital 														as "IdEventoCapital",							--	"IdEventoCapital"
				p_IdTipoEventoCapital 													as "IdTipoEventoCapital",						--	"IdTipoEventoCapital"
				p_Secuencia 															as "Secuencia",									--	"Secuencia"
				p_NumeroDividendo 														as "NumeroDividendo",							--  "NumeroDividendo"
				w."IdSocioDeNegocio"													as "IdSocioNegocio",
				w."Tipo"																as "TipoSocioNegocio",
				0																		as "IdPortafolio",
				w."TipoDocumentoIdentidad"												as "IdTipoDocumentoIdentidad",
				w."DocumentoIdentidad"													as "DocumentoIdentidad",
				w."NombreCompleto"														as "NombreCliente",
				z."IdClasificacionTipoInstrumento"										as "IdClasificacionTipoInstrumento",
				z."IdTipoInstrumento"													as "IdTipoInstrumento",
				z."IdInstrumento"														as "IdInstrumento",
				z."Nemotecnico"															as "Nemo",
				0																		as "IdInstrumentoNuevo",
				''																		as "NemoNuevo",
				TO_DATE(p_FechaPago, 'YYYY-MM-DD') 										as "FechaCierre",								--	"FechaCierre"
				TO_DATE(p_FechaPago, 'YYYY-MM-DD') 										as "FechaPago",									--	"FechaPago"
				SUM(z."EGRCantidad")													as "CantidadDerecho",							-- es la sumatorioa de este campo
				'TRA'																	as "TipoCartera",								-- tipo de cartera
				IFNULL(FLOOR(SUM(IFNULL(z."EGRCantidad",0)) * p_Factor),0)				as "CantidadNueva",								-- "CantidadNueva"
				0 																		as "MontoCapital",								--	"MontoCapital"
				0																		as "MontoInteres",								--	"MontoInteres"
				0																		as "Total",										-- se multiplica por el factor
				p_IdMoneda																as "IdMoneda",									--	"IdMoneda"
				p_Secuencia 															as "SecuenciaCupon",							--	"SecuenciaCupon"
				COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)							as "IdEmpleadoEjecutivoPortafolio",
				COALESCE(EJE."CodigoEmpleado",'')										as "CodigoUsuarioAgente",						--	"CodigoUsuarioAgente"
				COALESCE(EJE."NombreCompletoEjecutivo",'')								as "NombreAgente",								--	"NombreAgente"
				COALESCE(EJE."CodigoSucursal",'')										as "CodigoSucursal",							--	"CodigoSucursal"
				Current_UTCTIMESTAMP 													as "FechaHoraIngresoRegistro",					--	"FechaHoraIngresoRegistro"
				Current_UTCTIMESTAMP 													as "FechaHoraUltimaActualizacion",				--	"FechaHoraUltimaActualizacion"
				p_IdUsuario																as "IdUsuario",									--	"IdUsuario"
				p_IdEstado																as "IdEstado",									--	"IdEstado"
				z."IdBoveda"															as "IdBoveda",
				z."IdBolsa"																as "IdBolsa",
				''																		as "IdConcepto",
				'SIS'																	as "Origen"
		FROM "BSCL.Custodia.BD::CUS.Cartera" z
			INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion" x
			 	ON z."EGRIdOperacion" = x."IdOrden"
			INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" y
			 	ON x."IdTransaccion" = y."IdTransaccion"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" w
			 	ON (y."IdEmpresa" 				= w."IdEmpresa"	AND
			 		y."IdCorredorContraparte" 	= w."IdContraparte"	)
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
				ON (z."IdPortafolio" = EJE."IdPortafolio")
		WHERE 	z."IdEmpresa"		= p_IdEmpresa
		AND		y."IdEmpresa"		= z."IdEmpresa"
		AND		z."IdInstrumento" 	= p_IdInstrumento
		AND 	z."IdEstado" 		!= 'ANU'
		AND 	z."FechaOperacion"	<= p_FechaLimite
	  	AND 	x."IndicadorOrdenDirecta" = ''

		GROUP BY	z."IdBolsa",
					w."IdSocioDeNegocio",
					w."Tipo",
					w."TipoDocumentoIdentidad",
					w."DocumentoIdentidad",
					w."NombreCompleto",
					z."IdClasificacionTipoInstrumento",
					z."IdTipoInstrumento",
					z."IdInstrumento",
					z."Nemotecnico",
					COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
					COALESCE(EJE."CodigoEmpleado",''),
					COALESCE(EJE."NombreCompletoEjecutivo",''),
					COALESCE(EJE."CodigoSucursal",''),
					z."IdBoveda"
		UNION ALL
		SELECT * 
		FROM :v_EventoCapitalCliente;
		
		--------------------------------------------------------------------------------------------------------
		--- OPERACIONES EN TRÁNSITO DE COMPRA
		--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente"  de operaciones
		--- que se encuentran en tránsito, por ejemplo, ventas lo liquidadas.
		--------------------------------------------------------------------------------------------------------    
	
		v_EventoCapitalCliente 	= 
		SELECT 	
				p_IdEventoCapital 														as "IdEventoCapital",							--	"IdEventoCapital"
				p_IdTipoEventoCapital 													as "IdTipoEventoCapital",						--	"IdTipoEventoCapital"
				p_Secuencia 															as "Secuencia",									--	"Secuencia"
				p_NumeroDividendo 														as "NumeroDividendo",							--  "NumeroDividendo"
				w."IdSocioDeNegocio"													as "IdSocioNegocio",
				w."Tipo"																as "TipoSocioNegocio",
				0																		as "IdPortafolio",
				w."TipoDocumentoIdentidad"												as "IdTipoDocumentoIdentidad",
				w."DocumentoIdentidad"													as "DocumentoIdentidad",
				w."NombreCompleto"														as "NombreCliente",
				z."IdClasificacionTipoInstrumento"										as "IdClasificacionTipoInstrumento",
				z."IdTipoInstrumento"													as "IdTipoInstrumento",
				z."IdInstrumento"														as "IdInstrumento",
				z."Nemotecnico"															as "Nemo",
				0																		as "IdInstrumentoNuevo",
				''																		as "NemoNuevo",
				TO_DATE(p_FechaPago, 'YYYY-MM-DD') 										as "FechaCierre",								--	"FechaCierre"
				TO_DATE(p_FechaPago, 'YYYY-MM-DD') 										as "FechaPago",									--	"FechaPago"
				SUM(z."INGCantidad")													as "CantidadDerecho",							-- es la sumatorioa de este campo
				'TRA'																	as "TipoCartera",								-- tipo de cartera
				(IFNULL(FLOOR(SUM(IFNULL(z."INGCantidad",0)) * p_Factor),0) * -1)		as "CantidadNueva",								-- "CantidadNueva"
				0 																		as "MontoCapital",								--	"MontoCapital"
				0																		as "MontoInteres",								--	"MontoInteres"
				0																		as "Total",										-- se multiplica por el factor
				p_IdMoneda																as "IdMoneda",									--	"IdMoneda"
				p_Secuencia 															as "SecuenciaCupon",							--	"SecuenciaCupon"
				COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)							as "IdEmpleadoEjecutivoPortafolio",
				COALESCE(EJE."CodigoEmpleado",'')										as "CodigoUsuarioAgente",						--	"CodigoUsuarioAgente"
				COALESCE(EJE."NombreCompletoEjecutivo",'')								as "NombreAgente",								--	"NombreAgente"
				COALESCE(EJE."CodigoSucursal",'')										as "CodigoSucursal",							--	"CodigoSucursal"
				Current_UTCTIMESTAMP 													as "FechaHoraIngresoRegistro",					--	"FechaHoraIngresoRegistro"
				Current_UTCTIMESTAMP 													as "FechaHoraUltimaActualizacion",				--	"FechaHoraUltimaActualizacion"
				p_IdUsuario																as "IdUsuario",									--	"IdUsuario"
				p_IdEstado																as "IdEstado",									--	"IdEstado"
				z."IdBoveda"															as "IdBoveda",
				z."IdBolsa"																as "IdBolsa",
				''																		as "IdConcepto",
				'SIS'																	as "Origen"
		FROM "BSCL.Custodia.BD::CUS.Cartera" z
			 INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion" x
			 	ON z."IdOperacion" = x."IdOrden"
			 INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" y
			 	ON x."IdTransaccion" = y."IdTransaccion"
			 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" w
 			 	ON (y."IdEmpresa" 				= w."IdEmpresa"	AND
 					y."IdCorredorContraparte" 	= w."IdContraparte"	)
			 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
				ON (z."IdPortafolio" = EJE."IdPortafolio")
		WHERE z."IdEmpresa"			= p_IdEmpresa
		AND		y."IdEmpresa"		= z."IdEmpresa"
		AND		z."IdInstrumento" 	= p_IdInstrumento
		AND 	z."IdEstado" 		!= 'ANU'
		AND 	z."FechaOperacion"	<= p_FechaLimite
		AND 	x."IndicadorOrdenDirecta" = ''
		GROUP BY	z."IdBolsa",
					w."IdSocioDeNegocio",
					w."Tipo",
					w."TipoDocumentoIdentidad",
					w."DocumentoIdentidad",
					w."NombreCompleto",
					z."IdClasificacionTipoInstrumento",
					z."IdTipoInstrumento",
					z."IdInstrumento",
					z."Nemotecnico",
					COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
					COALESCE(EJE."CodigoEmpleado",''),
					COALESCE(EJE."NombreCompletoEjecutivo",''),
					COALESCE(EJE."CodigoSucursal",''),
					z."IdBoveda"
		UNION ALL
		SELECT * 
		FROM :v_EventoCapitalCliente;
		
		--------------------------------------------------------------------------------------------------------
		--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
		--------------------------------------------------------------------------------------------------------  
	
	
		INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
	   	(
			"IdEventoCapitalCliente",			
			"IdEventoCapital",			
			"IdTipoEventoCapital",		
			"Secuencia",			
			"NumeroDividendo",
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
--			"FolioCartera",
--			"IdCartera",
			"Origen"
			)
		SELECT  "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL,
				"IdEventoCapital"		 		,
				"IdTipoEventoCapital"			,
				"Secuencia"						,
				"NumeroDividendo"				,
				"IdSocioNegocio"				,
				"TipoSocioNegocio"				,
				"IdPortafolio"					,
				"IdTipoDocumentoIdentidad"		,
				"DocumentoIdentidad"			,
				"NombreCliente"					,
				"IdClasificacionTipoInstrumento",
				"IdTipoInstrumento"				,
				"IdInstrumento"					,
				"Nemo"							,
				"IdInstrumentoNuevo"			,
				"NemoNuevo"						,
				"FechaCierre"					,
				"FechaPago"						,
				"CantidadDerecho"				,
				"TipoCartera"					,
				"CantidadNueva"					,
				"MontoCapital"					,
				"MontoInteres"					,
				"Total"							,
				"IdMoneda"						,
				"SecuenciaCupon"				,
				"IdEmpleadoEjecutivoPortafolio" ,
				"CodigoUsuarioAgente"			,
				"NombreAgente"					,
				"CodigoSucursal"				,
				"FechaHoraIngresoRegistro"		,
				"FechaHoraUltimaActualizacion"	,
				"IdUsuario"						,
				"IdEstado"						,
				"IdBoveda"						,
				"IdBolsa"						,
				"IdConcepto"					,
--				"FolioCartera"					,
--				"IdCartera"						,
				"Origen"						
		FROM :v_EventoCapitalCliente;
	
		-- Valida si existe eventos clientes contrapartes pendientes
		SELECT count(1) INTO v_errorcliente
		FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 
		WHERE  	"IdEventoCapital" 	= p_IdEventoCapital	AND
				"IdEstado"			= 'PEN';
	
		IF (v_errorcliente > 0) THEN				
			v_Estado = 'PEN';
		END IF;
	
		UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
		SET "IdEstado" = v_Estado
		WHERE "IdEventoCapital" = p_IdEventoCapital;
		
		
		IF v_Estado = 'PEN' THEN
			v_TablaCodigosMensajes =	Select 	p_NumeroDividendo 								As "IdInterno", 
													'UI_EVENTO_CAPITAL_PROCESAR_CON_PENDIENTE' 	As "Codigo",
													'v_errorcliente' 							As "CodigoDato1", 
													Cast(v_errorcliente As NVARCHAR(520)) 		as "ValorDato1",
													'' 											As "CodigoDato2", 
													'' 											As "ValorDato2" 
											From dummy;
				                        
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
			
			p_out_AdvertenciasContador = 1;
			p_out_AdvertenciasDetalle = Select "Codigo",'' as "RequiereAutorizacion", "Descripcion" 
										From :v_out_hash;	
		ELSE
			v_TablaCodigosMensajes =	Select 	p_NumeroDividendo 					As "IdInterno", 
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
		END IF;
	
	    IF p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL  "BSCL.Custodia.BD.Servicios::EVC.NotificarVariacionCapital"(p_IdEmpresa, p_IdUsuario, p_IdModulo, p_IdEventoCapital); 
    
	END;
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END; 
GO