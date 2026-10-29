SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CertificaSuscripcionOSA";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CertificaSuscripcionOSA" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	IN p_IdEventoCapital 		 			TABLE ("IdEventoCapitalCliente" BIGINT),

-- Parametros de Salida
-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  INT,
	OUT p_out_AdvertenciasDetalle   TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador       INT,
	OUT p_out_ErroresDetalle        TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	 )   
	 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN 
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

		DECLARE v_count_EventoCapital 			BIGINT;
		DECLARE v_count_EventoCapitalCliente 	BIGINT;
		--variables de proceso
/*		DECLARE v_EstadoCarteraVigente 	NVARCHAR(3);*/
		DECLARE v_FechaSistema 					NVARCHAR(10);
		DECLARE v_Secuencia						BIGINT := 0;
		
		p_out_TblId = SELECT 0 as "Ids" from dummy where 1=2;

		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
/* 		
			-- Se valida que el evento capital debe encontrarse "ACT" para poder realizar el cierre.
			SELECT count(1) INTO p_out_ErroresContador
		    FROM "BSCL.Custodia.BD::CUS.EventoCapital"
			WHERE "IdEventoCapital" = p_IdEventoCapital
			  and "IdEstado" != 'ACT';
*/
			IF (p_out_ErroresContador > 0) THEN				
				p_out_ErroresDetalle = SELECT 'EVENTO_CAPITAL' as "Codigo", 'ERROR' as "Descripcion" FROM DUMMY;
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

			p_out_AdvertenciasContador		:= 0;
			p_out_ErroresContador			:= 0;
			p_out_AdvertenciasDetalle		= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			p_out_ErroresDetalle			= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;


/*
			SELECT COUNT(1) into v_count_EventoCapital
			FROM "BSCL.Custodia.BD::CUS.EventoCapital"
			WHERE "IdEventoCapital" = p_IdEventoCapital
			  AND "IdTipoEventoCapital" = 'SU';
			
			IF v_count_EventoCapital>0 THEN
			  			
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
				SET "IdEstado" = 'CER'
				WHERE "IdEventoCapital" = p_IdEventoCapital
				  AND "IdTipoEventoCapital" = 'SU';
				  
			ELSE
			
				p_out_ErroresContador		:= 1;
				p_out_ErroresDetalle = SELECT '11' AS "Codigo", 'No existe registro Evento Capital' AS "Descripcion" FROM dummy;
				
			END IF;
*/
			SELECT COUNT(1) into v_count_EventoCapitalCliente
			FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
			WHERE "IdEventoCapitalCliente" IN (SELECT "IdEventoCapitalCliente" FROM :p_IdEventoCapital)
			  AND "IdTipoEventoCapital" = 'SU';
			
			IF v_count_EventoCapitalCliente>0 THEN
			  			
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				SET "IdEstado" = 'CRT'
				WHERE "IdEventoCapitalCliente" IN (SELECT "IdEventoCapitalCliente" FROM :p_IdEventoCapital)
				  AND "IdTipoEventoCapital" = 'SU';
				  
			ELSE
			
				p_out_ErroresContador		:= 1;
				p_out_ErroresDetalle = SELECT '12' AS "Codigo", 'No existe registro Evento Capital Cliente' AS "Descripcion" FROM dummy;
				
			END IF;
			
	--------------------------------------------------------------------------------------------------------
	--- 
	--------------------------------------------------------------------------------------------------------
			if p_out_ErroresContador > 0 then 
				SIGNAL SALIDA_CONTROLADA;
			end if;
		END;
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END;

