SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.SuscripcionEmisionPago";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.SuscripcionEmisionPago" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',
	IN p_Secuencia							BIGINT 			default 0,
	IN p_IdSocioNegocio 					BIGINT 			default 0,
	IN p_TipoSocioNegocio					NVARCHAR(3) 	default '',
	IN p_IdPortafolio						BIGINT 			default 0,
	IN p_IdTipoDocumentoIdentidad			NVARCHAR(3) 	default '',
	IN p_DocumentoIdentidad					NVARCHAR(40) 	default '',
	IN p_NombreCliente						NVARCHAR(100) 	default '',
	IN p_IdClasificacionTipoInstrumento		NVARCHAR(3) 	default '',
	IN p_IdTipoInstrumento					NVARCHAR(3) 	default '',  
	IN p_IdInstrumento						BIGINT 			default 0,	
	IN p_Nemo								NVARCHAR(40) 	default '',	
	IN p_IdInstrumentoNuevo					BIGINT 			default 0,
	IN p_NemoNuevo							NVARCHAR(40) 	default '',
	IN p_FechaPago							NVARCHAR(10) 	default '0001-01-01',

	IN p_FechaCierre                        NVARCHAR(10) 	default '0001-01-01',	
	IN p_CantidadDerecho    				BIGINT 			default 0,
	IN p_TipoCartera						NVARCHAR(3) 	default '',
	IN p_CantidadNueva						BIGINT 			default 0,
	IN p_MontoCapital						DECIMAL(26,8) 	default 0,
	IN p_MontoInteres						DECIMAL(26,8) 	default 0,
	IN p_Total								DECIMAL(26,8) 	default 0,
	IN p_IdMoneda							NVARCHAR(3) 	default '',	
	IN p_SecuenciaCupon						BIGINT 			default 0,
	IN p_IdEmpleadoEjecutivoPortafolio		BIGINT		 	default 0,
	IN p_CodigoUsuarioAgente				NVARCHAR(40) 	default '',
	IN p_NombreAgente						NVARCHAR(100) 	default '',
	IN p_CodigoSucursal						NVARCHAR(40) 	default '',
	IN p_IdBoveda							NVARCHAR(3) 	default '',
	IN p_UsaReglasNegocio 					NVARCHAR(1) 	default 'N',
	IN p_IdBolsa							NVARCHAR(3) 	default '',
	IN p_NumeroDividendo					BIGINT 			default 0,
	IN p_Precio			    				DECIMAL(26,8)	default 0,
	
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
	
		--variables de proceso
		DECLARE v_FechaSistema 				TIMESTAMP;
		DECLARE v_IdEventoCapital			BIGINT := 0;
		DECLARE v_NumeroDividendo			BIGINT := 0;
		DECLARE v_Master					BIGINT := 0;
		DECLARE v_IdInstrumentoRelacionado	BIGINT := 0;
		DECLARE V_NemoRelacionado			NVARCHAR(40);
		DECLARE v_Precio					DECIMAL(26,8);
		
		v_Master			:= 0;
		v_IdEventoCapital 	:= 0;
		v_NumeroDividendo	:= 0;
		
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

		--obtengo datos del instumento relacionado.
		SELECT "IdInstrumentoRelacionado", "NemoRelacionado" INTO v_IdInstrumentoRelacionado, v_NemoRelacionado
		FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarRelacionInstrumento"
		WHERE 
		  "IdInstrumento" 		= p_IdInstrumento
		  AND "IdEstado"			= 'VIG';
		-- Valida si existe Evento Capital Suscripción
		SELECT COUNT(1) INTO v_Master 
		FROM "BSCL.Custodia.BD::CUS.EventoCapital" 		
		WHERE 
			"IdEmpresa" 			= p_IdEmpresa		AND
			"NumeroDividendo"		= p_NumeroDividendo AND
			"Secuencia"				= p_Secuencia		AND
			"IdTipoEventoCapital"	= 'SU';
			
		SELECT "Valor" INTO v_Precio 
		FROM "BSCL.Custodia.BD::CUS.EventoCapital" 		
		WHERE 
			"IdEmpresa" 			= p_IdEmpresa		AND
			"NumeroDividendo"		= p_NumeroDividendo AND
			"Secuencia"				= p_Secuencia		AND
			"IdTipoEventoCapital"	= 'EP';
	
	

		IF (v_Master = 0) THEN

		-- Recuperamos numero de secuencia que usaremos para en el evento
	   		SELECT "BSCL.Custodia.BD.Secuencias::EventoCapital".NEXTVAL INTO v_IdEventoCapital FROM DUMMY;

		-- Inserta Evento Capital SU

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
				"IdEstado"
			)
			SELECT 
				:v_IdEventoCapital,
				"IdEmpresa",
				'SU',								
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
				:v_IdInstrumentoRelacionado,
				:v_NemoRelacionado,
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
				'VIG'
			FROM 
				"BSCL.Custodia.BD::CUS.EventoCapital"
			WHERE
				"IdEventoCapital" = p_IdEventoCapital;
		END IF;
	   	 	

		--------------------------------------------------------------------------------------------------------
		--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado 
		--------------------------------------------------------------------------------------------------------

		IF (v_Master != 0) THEN
			SELECT "IdEventoCapital"  INTO v_IdEventoCapital 
			FROM "BSCL.Custodia.BD::CUS.EventoCapital" 		
			WHERE 
				"IdEmpresa" 			= p_IdEmpresa		AND
				"NumeroDividendo"		= p_NumeroDividendo AND					
				"Secuencia"				= p_Secuencia		AND
				"IdTipoEventoCapital"	= 'SU';		
		END IF;

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
			"Precio",
			"NumeroDividendo",
			"IdTipoReajuste",
			"CodigoCorredorContraparte",
			"TipoOperacion")
		VALUES
			(							
			"BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL, 
			:v_IdEventoCapital,
			'SU',		
			p_Secuencia,			
			p_IdSocioNegocio,					
			p_TipoSocioNegocio,			   		
			p_IdPortafolio,				
			p_IdTipoDocumentoIdentidad,	
			p_DocumentoIdentidad,	
			p_NombreCliente,				
			p_IdClasificacionTipoInstrumento,	
			p_IdTipoInstrumento,		
			p_IdInstrumento,			
			p_Nemo,					
			:v_IdInstrumentoRelacionado,
			:v_NemoRelacionado,						

			TO_DATE(p_FechaCierre, 'YYYY-MM-DD'),--p_FechaCierre,									
			TO_DATE(p_FechaPago, 'YYYY-MM-DD'),--p_FechaPago,				
			p_CantidadDerecho,		
			p_TipoCartera,						
			p_CantidadNueva,					
			(p_CantidadNueva * v_Precio),						
			p_MontoInteres,				
			p_Total,				
			p_IdMoneda,						
			p_SecuenciaCupon,
			p_IdEmpleadoEjecutivoPortafolio,
			p_CodigoUsuarioAgente,				
			p_NombreAgente,				
			p_CodigoSucursal,		
			CURRENT_UTCTIMESTAMP,	--p_FechaHoraIngresoRegistro,
			CURRENT_UTCTIMESTAMP,	--p_FechaHoraUltimaActualizacion,		
			p_IdUsuario,						
			'VIG',
			p_IdBoveda,
			p_IdBolsa,
			'',
			'',
			v_Precio,
			p_NumeroDividendo,
			'',
			'',
			'');
			
			
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
					ON Efp."IdMedioDePago" 	= MdP."IdMedioDePago"
	   	WHERE   Ecc."IdEventoCapital" 		= :v_IdEventoCapital 	AND 
	   			Ecc."IdTipoEventoCapital" 	= 	'SU' 				AND 
	   			Ecc."IdPortafolio" 			= :p_IdPortafolio		AND 
	   			Efp."IdTipoOperacion" 		= 'INGSUS'				;
	   			
			
				
	select 1 into P_OUT_SECUENCIA from  DUMMY;

    if p_out_ErroresContador > 0 then 
		SIGNAL SALIDA_CONTROLADA;
	end if;
    
--	END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

	END;
END; 


