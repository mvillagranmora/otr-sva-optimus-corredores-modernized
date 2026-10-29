SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalDivisionSociedad";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalDivisionSociedad" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',
	IN p_Secuencia							BIGINT 			default 0,
	IN p_IdClasificacionTipoInstrumento		NVARCHAR(3) 	default '',
	IN p_IdTipoInstrumento					NVARCHAR(3) 	default '',  
	IN p_IdInstrumento						BIGINT 			default 0,
	IN p_Nemo								NVARCHAR(40) 	default '',
	IN p_Factor								DECIMAL(26,8) 	default 0,
	IN p_IdMoneda							NVARCHAR(3) 	default '',
	IN p_IdInstrumentoNuevo					BIGINT 			default 0,
	IN p_NemoNuevo							NVARCHAR(40) 	default '',
	IN p_FactorNuevo						DECIMAL(26,8) 	default 0,
	IN p_FechaLimite						NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaPago							NVARCHAR(10) 	default '0001-01-01',     
	IN p_IdEstado							NVARCHAR(3) 	default '',
	IN p_NumeroDividendo					BIGINT			default 0, 
-- Parametros de Salida
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),
	
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	
	OUT p_out_ErroresContador 			int,   
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;


	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
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
		DECLARE v_MiExiste 				NVARCHAR(1);
		DECLARE v_MensajeRegla          NVARCHAR(520);
		DECLARE v_ResultadoRegla        NVARCHAR(40);
		DECLARE v_IdEjecucion 			BIGINT;
		DECLARE v_out_REF_ID			BIGINT;

	
		--variables de proceso
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_Secuencia				BIGINT := 0;
		
		p_out_TblId = SELECT 0 as "Ids" from dummy where 1=2;

		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
	 	v_IdEjecucion:=0;
		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
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
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN

		DECLARE v_IdSocioNegocio			BIGINT := 0;
		DECLARE v_IdTipoDocumentoIdentidad	NVARCHAR(1):='';
		DECLARE v_CantidadDerecho			BIGINT := 0;
		DECLARE v_TipoCartera				NVARCHAR(3):='';
		DECLARE v_CantidadNueva				BIGINT := 0;
		DECLARE v_MontoCapital				DECIMAL(26,8):= 0;
		DECLARE v_MontoInteres				DECIMAL(26,8):= 0;
		DECLARE v_Total						DECIMAL(26,8):= 0;
		DECLARE v_NombreAgente				NVARCHAR(100):='';
		DECLARE v_CodigoSucursal			NVARCHAR(40):='';
		DECLARE v_CodigoUsuarioAgente		NVARCHAR(40):='';
		DECLARE v_count_errores_custodia 	BIGINT;
		DECLARE v_KeyFlujoCustodia			BIGINT;
		
	-- CONSULTA CUSTODIA-------------------------------------------------------------
	


--------------------------------------------------------------------------------------------------------
--- EVENTO A FAVOR BOVEDA
--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado 
--- obtenido del servicio anterior ("BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia")
--------------------------------------------------------------------------------------------------------    

--------------------------------------------------------------------------------------------------------
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
			"Precio",						
			"PrecioNuevo",						
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
			"NumeroDividendo",
			"Origen")
	
		SELECT
		    "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL,--	IdEventoCapitalCliente
			p_IdEventoCapital,											--	IdEventoCapital
			p_IdTipoEventoCapital,										--	IdTipoEventoCapital
			p_Secuencia,												--	Secuencia
			IFNULL(SOC."IdSocioDeNegocio",0),							--	IdSocioNegocio
			CASE 
				WHEN SOC."IdCliente" > 0 		THEN 'CLI'
				WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
				WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
				WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
			END	,														--	TipoSocioNegocio
			IFNULL(FT."IdPortafolio",0),								--	IdPortafolio
			'R',--														--	IdTipoDocumentoIdentidad
			IFNULL(CL."DocumentoIdentidad",''),							--	DocumentoIdentidad			
			IFNULL(CL."NombreCompleto",''),								--	NombreCliente
			p_IdClasificacionTipoInstrumento,							--	IdClasificacionTipoInstrumento
			p_IdTipoInstrumento,										--  IdTipoInstrumento
			p_IdInstrumento, 											--	IdInstrumento
			p_Nemo,														--	Nemo
			p_IdInstrumentoNuevo,										--	IdInstrumentoNuevo		
			p_NemoNuevo,												--  NemoNuevo
			TO_DATE(p_FechaLimite, 'YYYY-MM-DD'),						--	FechaCierre
			TO_DATE(p_FechaPago, 'YYYY-MM-DD'),							--	FechaPago
			SUM(FT."Cantidad"),											--	CantidadDerecho
			FT."TipoCartera",
			SUM(FT."Cantidad" * p_FactorNuevo),									--	CantidadNueva		
			0 ,																	--	MontoCapital
			FT."PrecioCompra" ,													-- 	Precio
			FT."PrecioCompra" * p_FactorNuevo,									-- 	Precio Nuevo
			0,																				--	MontoInteres
			(SUM(FT."Cantidad") * p_FactorNuevo) * (FT."PrecioCompra" * p_FactorNuevo),		--	Total
			p_IdMoneda,																		--	IdMoneda
			p_Secuencia,																	--	SecuenciaCupon
			COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),						
			COALESCE(EJE."CodigoEmpleado",''),										--	"CodigoUsuarioAgente"
			COALESCE(EJE."NombreCompletoEjecutivo",''),								--	"NombreAgente"
			COALESCE(EJE."CodigoSucursal",''),
			Current_UTCTIMESTAMP,															--	FechaHoraIngresoRegistro
			Current_UTCTIMESTAMP,															--	FechaHoraUltimaActualizacion
			p_IdUsuario,																	--	IdUsuario
			p_IdEstado,																		--	IdEstado
			CASE FT."IdBoveda"
				WHEN '' THEN 'COR'
				ELSE FT."IdBoveda"
			END,																			--  IdBoveda
			Case FT."IdBolsa" 
				WHEN '' THEN 'BCS'
				ELSE IFNULL(FT."IdBolsa",'BCS')												--  IdBolsa
			END,
			p_NumeroDividendo,
			'SIS' 
		FROM 
				"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraEventoCapital"			AS FT	
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL
				ON (FT."IdCliente" = CL."IdCliente")
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC
				ON (CL."IdCliente" = SOC."IdCliente")
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
				ON (FT."IdPortafolio" = EJE."IdPortafolio")
		WHERE  
			FT."Nemotecnico"	= p_Nemo
			AND FT."IdCliente" > 0
			AND FT."FechaOperacion" <= p_FechaLimite
			AND FT."IdEstado" = 'VIG'
			AND IFNULL(SOC."IdSocioDeNegocio",0) > 0
			AND "IdBoveda"		<> ''
		GROUP BY 
			FT."TipoCartera",
			SOC."IdSocioDeNegocio",
			CASE 
				WHEN SOC."IdCliente" > 0 		THEN 'CLI'
				WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
				WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
				WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
			END	,
			FT."IdPortafolio",
			CL."DocumentoIdentidad",
			CL."NombreCompleto",
			FT."PrecioCompra",
			COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
			COALESCE(EJE."CodigoEmpleado",''),
			COALESCE(EJE."NombreCompletoEjecutivo",''),
			COALESCE(EJE."CodigoSucursal",''),
			CASE FT."IdBoveda"
				WHEN '' THEN 'COR'
				ELSE FT."IdBoveda"
			END,
			Case FT."IdBolsa" 
				when '' then 'BCS'
				ELSE IFNULL(FT."IdBolsa",'BCS')										
			END;		
	
		UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
		SET "IdEstado" = 'ACT'
		WHERE "IdEventoCapital" = p_IdEventoCapital;
	
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
										
	    if p_out_ErroresContador > 0 then 
			SIGNAL SALIDA_CONTROLADA;
		end if;
	    
		END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

	END;
END; 

