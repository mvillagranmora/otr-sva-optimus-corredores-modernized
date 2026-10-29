SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CrearEventoCapitalCliente";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CrearEventoCapitalCliente" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	IN p_UsaReglasNegocio 					NVARCHAR(1) 	default 'N',
		
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',
	IN p_Clasificacion						NVARCHAR(3) 	default '',
	IN p_IdTipoInstrumento					NVARCHAR(3) 	default '',  
	IN p_IdInstrumento						BIGINT 			default 0,
	IN p_Nemo								NVARCHAR(40) 	default '',
	IN p_FechaPago							NVARCHAR(10) 	default '0001-01-01',     
	IN p_NumeroDividendo					BIGINT 			default 0,
	IN p_Secuencia							BIGINT 			default 0,
	IN p_ValorUnitario						BIGINT 			default 0,		
	IN p_Factor								DECIMAL(26,8) 	default 0,
	IN p_Descripcion						NVARCHAR(100) 	default '',			
	IN p_IdCliente							BIGINT			default 0,
	IN p_IdPortafolio						BIGINT			default 0,
	IN p_IdMoneda							NVARCHAR(3) 	default '',
	IN p_CantidadDerecho					BIGINT			default 0,
	IN p_CantidadNueva						BIGINT			default 0,
	IN p_MontoCapital						BIGINT			default 0,
	IN p_MontoInteres						BIGINT			default 0,
	IN p_TipoCartera						NVARCHAR(3)		default '',
	IN P_IdBoveda							NVARCHAR(3)		default '',
--	IN p_EsInscrito							NVARCHAR(1)		default '',

/*	IN p_FechaActualizacion					NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaLimite						NVARCHAR(10) 	default '0001-01-01',     
	IN p_NumeroDividendo					BIGINT 			default 0,
	IN p_IdTipoDividendo					NVARCHAR(2) 	default '',
	IN p_PrecioAnterior						DECIMAL(26,8) 	default 0,
	IN p_FechaHoraIngresoRegistro			NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaHoraUltimaActualizacion		NVARCHAR(10) 	default '0001-01-01',     
	IN p_TipoIngreso						BIGINT 			default 0,
	IN p_IdEstado							NVARCHAR(3) 	default '',

*/
	
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,   
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_Secuencia bigint
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
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_MiExiste 				NVARCHAR(1);
		DECLARE v_MensajeRegla          NVARCHAR(520);
		DECLARE v_ResultadoRegla        NVARCHAR(40);
		DECLARE v_IdEjecucion 			BIGINT;
		DECLARE v_IdEventoCapital		BIGINT;
		DECLARE v_out_REF_ID			BIGINT;
		DECLARE v_NombreAgente			NVARCHAR(100):='';
		DECLARE v_CodigoSucursal		NVARCHAR(40):='';
		DECLARE v_CodigoUsuarioAgente	NVARCHAR(40):='';
		
		DECLARE v_IdSocioNegocio		BIGINT := 0;
		DECLARE v_TipoSocioNegocio		NVARCHAR(1):='';
		DECLARE v_DocumentoIdentidad	NVARCHAR(40):='';
		DECLARE v_NombreCompleto	NVARCHAR(100):='';
		--variables de proceso
/*		DECLARE v_EstadoCarteraVigente 	NVARCHAR(3);*/
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

			
			-- Ontengo secuencia del evento de capital
			SELECT "IdEventoCapital" INTO v_IdEventoCapital
			FROM "BSCL.Custodia.BD::CUS.EventoCapital" 
			WHERE "Secuencia" = :p_Secuencia
			  AND "Nemo" = :p_Nemo;

			SELECT 	"DocumentoIdentidad" ,"NombreCompleto"  INTO v_DocumentoIdentidad, v_NombreCompleto
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
			WHERE "IdCliente" = :p_IdCliente;

			-- Recuperamos numero de secuencia que usaremos para en el evento
	    	SELECT "BSCL.Custodia.BD.Secuencias::EventoCapital".NEXTVAL INTO p_out_Secuencia FROM DUMMY;
	
	
/*			SELECT IFNULL(MAX(A."Secuencia"),0) + 1 INTO v_Secuencia 
			FROM "BSCL.Custodia.BD::CUS.EventoCapital" A
			WHERE 
				A."IdEmpresa"						= p_IdEmpresa						AND
				A."IdTipoEventoCapital" 			= p_IdTipoEventoCapital				AND
				A."IdClasificacionTipoInstrumento"	= p_IdClasificacionTipoInstrumento	AND
				A."IdInstrumento"					= p_IdInstrumento					AND
				A."Nemo" 							= p_Nemo;  */
		
				v_MiExiste 				 := '';  	
			

			INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
		   	(
				"IdEventoCapitalCliente",			
				"IdEventoCapital",			
				"IdTipoEventoCapital",		
				"NumeroDividendo",
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
				"Origen")
			VALUES(
			    "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL, 	--	IdEventoCapitalCliente
				v_IdEventoCapital,												--	IdEventoCapital
				p_IdTipoEventoCapital,											--	IdTipoEventoCapital
				p_NumeroDividendo,												--	NumeroDividendo
				p_Secuencia,													--	Secuencia
				v_IdSocioNegocio,												--	IdSocioNegocio
				v_TipoSocioNegocio,												--	TipoSocioNegocio
				p_IdPortafolio,													--	IdPortafolio
				'R',															--	IdTipoDocumentoIdentidad
				v_DocumentoIdentidad,											--	DocumentoIdentidad			
				v_NombreCompleto,												--	NombreCliente
				p_Clasificacion,												--	IdClasificacionTipoInstrumento
				p_IdTipoInstrumento,											--  IdTipoInstrumento
				p_IdInstrumento, 												--	IdInstrumento
				p_Nemo,															--	Nemo
				0,																--	IdInstrumentoNuevo
				'',																--  NemoNuevo
				TO_DATE(p_FechaPago, 'YYYY-MM-DD'),								--	FechaCierre
				TO_DATE(p_FechaPago, 'YYYY-MM-DD'),								--	FechaPago
				IFNULL(p_CantidadDerecho,0),									--	CantidadDerecho
				p_TipoCartera,													--	TipoCartera
				IFNULL(p_CantidadNueva,0),										--	CantidadNueva
				IFNULL(p_MontoCapital,0) * p_Factor,							--	MontoCapital
				p_MontoInteres,													--	MontoInteres
				Case p_IdTipoEventoCapital
					When 'DV' THEN IFNULL(IFNULL(p_CantidadDerecho,0) * p_Factor,0)
					Else 0
				End,															--  Total
--				IFNULL(p_CantidadNueva,0) * p_Factor,							--	Total
				p_IdMoneda,														--	IdMoneda
				p_Secuencia,													--	SecuenciaCupon
				v_CodigoUsuarioAgente,											--	CodigoUsuarioAgente
				v_NombreAgente,													--	NombreAgente
				v_CodigoSucursal,												--	CodigoSucursal
				CURRENT_UTCTIMESTAMP,											--	FechaHoraIngresoRegistro
				CURRENT_UTCTIMESTAMP,											--	FechaHoraUltimaActualizacion
				p_IdUsuario,													--	IdUsuario
				'VIG',															--	IdEstado
				p_IdBoveda,
				'MAN');													--  IdBoveda
		
	
		    
		    	if p_out_ErroresContador > 0 then 
						SIGNAL SALIDA_CONTROLADA;
				end if;
    
		END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

	END;
END; 
GO