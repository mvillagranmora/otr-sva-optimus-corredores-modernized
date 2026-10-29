SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL"."BSCL.Portafolio.BD.Servicios::ConfirmarCrearPortafolio";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion del portafolio.
--  Autor: JGARCES - BCS
--  Fecha Creacion: 28-11-2015
--  
"BSCL"."BSCL.Portafolio.BD.Servicios::ConfirmarCrearPortafolio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	--IN 	p_IdSolicitudCambio							BIGINT,	
	IN 	p_IdPortafolio								BIGINT,
	IN 	p_IdEstadoAutorizacion						NVARCHAR(3),	
	
	-- Parametros salida del servicio publico
	OUT p_out_RefId 								BIGINT,
			
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
	DECLARE v_IdCliente				BIGINT;
	DECLARE v_IdMatrizAgrupadorAPV	BIGINT;
	DECLARE v_CountIdMatrizAgrupadorAPV	BIGINT;
	DECLARE v_ProductoAgrupador		BIGINT;
	DECLARE v_CountProductoAgrupador BIGINT;
	
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
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	        	
	v_IdCliente				:=0;
	v_IdMatrizAgrupadorAPV	:=0;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
			DECLARE v_countIdPortafolio		BIGINT;
						
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.		
		BEGIN
		
			SELECT COUNT("IdPortafolio") INTO v_countIdPortafolio
			FROM "BSCL.Portafolio.BD::POR.Portafolio"
			WHERE "IdPortafolio" = :p_IdPortafolio AND "IdEstadoAutorizacion" = 'PEN';
			
			IF(v_countIdPortafolio=0) THEN
				p_out_ErroresDetalle =  select 'NO_EXISTE_PORTAFOLIO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
		END;
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN	
			SELECT COUNT("IdMatrizAgrupadorAPV") INTO  v_CountIdMatrizAgrupadorAPV
			FROM "BSCL.Portafolio.BD::POR.Portafolio"
			WHERE "IdPortafolio" = :p_IdPortafolio
			AND "Proposito" = 'PROPOSITOAPV'
			AND "IdEstadoAutorizacion" = 'PEN';
			
			IF(:v_CountIdMatrizAgrupadorAPV > 0 ) THEN
				
				SELECT "IdCliente", "IdMatrizAgrupadorAPV" INTO v_IdCliente, v_IdMatrizAgrupadorAPV
				FROM "BSCL.Portafolio.BD::POR.Portafolio"
				WHERE "IdPortafolio" = :p_IdPortafolio
				AND "Proposito" = 'PROPOSITOAPV'
				AND "IdEstadoAutorizacion" = 'PEN';
				
				SELECT COUNT(1) INTO v_CountProductoAgrupador
				FROM "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador"
				WHERE "IdMatrizAgrupadorAPV" = :v_IdMatrizAgrupadorAPV; 
			
				IF(:v_IdMatrizAgrupadorAPV > 0 AND v_CountProductoAgrupador > 0) THEN  
 			       SELECT "IdProductoAgrupador" INTO v_ProductoAgrupador
				   FROM "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador"
				   WHERE "IdMatrizAgrupadorAPV" = :v_IdMatrizAgrupadorAPV; 
			    ELSE 			      
			       p_out_ErroresContador := p_out_ErroresContador + 1;
			       p_out_ErroresDetalle  =  select 'NO_EXISTE_MATRIZ_AGRUPADOR_APV' as "Codigo", :v_IdMatrizAgrupadorAPV as "Descripcion" from dummy;
			      
			       SIGNAL SALIDA_CONTROLADA;			      
			    END IF;
				
				UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
				SET "IdEstadoAutorizacion"	= :p_IdEstadoAutorizacion
				WHERE "IdCliente" = v_IdCliente
				AND "Proposito" = 'PROPOSITOAPV'
				AND "IdMatrizAgrupadorAPV" IN (SELECT "IdMatrizAgrupadorAPV" 
											   FROM "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" 
											   WHERE "IdProductoAgrupador" = :v_ProductoAgrupador);
			ELSE
				-- HACER UPDATE EN PORTAFOLIO.
				UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
				SET "IdEstadoAutorizacion"	= :p_IdEstadoAutorizacion
				WHERE "IdPortafolio" = :p_IdPortafolio;-- AND "IdEstadoAutorizacion" = :p_IdEstadoAutorizacion;
				
				SELECT ::ROWCOUNT into p_out_RefId FROM DUMMY;
				
				--FALTA ACTUALIZAR AUTORIZACION.	
			END IF;
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
