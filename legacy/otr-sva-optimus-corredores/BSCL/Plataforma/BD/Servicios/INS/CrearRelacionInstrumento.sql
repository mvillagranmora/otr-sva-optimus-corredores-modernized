SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::INS.CrearRelacionInstrumento";
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::INS.CrearRelacionInstrumento" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          						BIGINT 			default 0,
    IN p_IdUsuario          						BIGINT 			default 0,
    IN p_IdModulo           						NVARCHAR(3) 	default '',
	IN p_IdRelacionInstrumento 						BIGINT 			default 0,
	IN p_IdTipoRelacionInstrumento					NVARCHAR(6) 	default '',	
	IN p_IdClasificacionTipoInstrumento				NVARCHAR(6) 	default '',	
	IN p_IdTipoInstrumento  		 				NVARCHAR(3)   	default '',	
	IN p_IdInstrumento								BIGINT			default 0,
	IN p_Nemo 										NVARCHAR(40)	default '',	
	IN p_IdClasificacionTipoInstrumentoRelacionado	NVARCHAR(6) 	default '',	
	IN p_IdTipoInstrumentoRelacionado  				NVARCHAR(3)   	default '',	
	IN p_IdInstrumentoRelacionado					BIGINT			default 0,
	IN p_NemoRelacionado							NVARCHAR(40)	default '',	
	IN p_Descripcion								NVARCHAR(100)	default '',
	IN p_FechaInicioRelacion						NVARCHAR(10) 	default '0001-01-01',   
	IN p_FechaFinRelacion							NVARCHAR(10) 	default '0001-01-01',
	IN p_IdEstado 									NVARCHAR(3) 	default '',
	IN p_UsaReglasNegocio 							NVARCHAR(1) 	default 'N',
	
	-- Parametros de Salida
	OUT p_out_ErroresContador int,   
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_IdRelacionInstrumento bigint
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
--		select count(1) into p_out_AdvertenciasContador 
--		from :p_out_AdvertenciasDetalle;		 					 	
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

	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_Secuencia				BIGINT := 0;
		
		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
 	
			SELECT count(1) INTO p_out_ErroresContador
		    FROM "BSCL.Plataforma.BD::INS.RelacionInstrumento"
			WHERE 
				"IdTipoRelacionInstrumento" 	= :p_IdTipoRelacionInstrumento	and 
				"IdInstrumento"					= :p_IdInstrumento				and 
				"IdInstrumentoRelacionado"		= :p_IdInstrumentoRelacionado;



			IF (p_out_ErroresContador >= 1) THEN				
				p_out_ErroresDetalle = SELECT 'EVENTO_CAPITAL' as "Codigo", 'Error Relacion ya Existe' as "Descripcion" FROM DUMMY;
		 	END IF;
		 	
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

			
		-- Recuperamos numero de secuencia que usaremos para la Relación Instrumento
		Select  "BSCL.Plataforma.BD.Secuencias::RelacionInstrumento".NEXTVAL INTO p_out_IdRelacionInstrumento FROM DUMMY;
	
			
		INSERT INTO "BSCL.Plataforma.BD::INS.RelacionInstrumento"
			    (
				"IdRelacionInstrumento", 		
				"IdTipoRelacionInstrumento",				
				"IdClasificacionTipoInstrumento",
				"IdTipoInstrumento",  		 	
				"IdInstrumento",					
				"Nemo", 							
				"IdClasificacionTipoInstrumentoRelacionado",
				"IdTipoInstrumentoRelacionado",  
				"IdInstrumentoRelacionado",		
				"NemoRelacionado",				
				"Descripcion",					
				"FechaInicioRelacion",			
				"FechaFinRelacion",				
				"IdUsuario", 					
				"IdEstado" 						

				)
	   	VALUES (	
				:p_out_IdRelacionInstrumento,
				p_IdTipoRelacionInstrumento,
				p_IdClasificacionTipoInstrumento,
				p_IdTipoInstrumento,
				p_IdInstrumento,
				p_Nemo,
				p_IdClasificacionTipoInstrumentoRelacionado,
				p_IdTipoInstrumentoRelacionado,
				p_IdInstrumentoRelacionado,
				p_NemoRelacionado,
				p_Descripcion,
				p_FechaInicioRelacion,
				p_FechaFinRelacion,
				p_IdUsuario,
				p_IdEstado
		    );
		    
		    if p_out_ErroresContador > 0 then 
				SIGNAL SALIDA_CONTROLADA;
			end if;
    
		END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

	END;
END; 
