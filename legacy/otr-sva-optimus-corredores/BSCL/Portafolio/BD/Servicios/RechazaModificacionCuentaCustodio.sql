SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::RechazaModificacionCuentaCustodio";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para rechazas la modificacion al portafolio de un cliente.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 07-09-2016
--  
"BSCL.Portafolio.BD.Servicios::RechazaModificacionCuentaCustodio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN 	p_SolicitudesCambio							TABLE("IdSecuenciaCambio" BIGINT),	
	IN  p_IdEstadoAutorizacion						NVARCHAR(3),
	
	-- Parametros salida del servicio publico
	--OUT p_out_RefId 								BIGINT,
			
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
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_CountAtributoModificado						BIGINT:=0;
		DECLARE v_countSolicitud								BIGINT:=0;
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	

		--select * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" ;
		BEGIN
			--SELECT "IdSolicitudCambio" INTO v_IdEntidadOrigen FROM :p_SolicitudesCambio;
			--v_IdEntidadOrigen:= CAST (p_secuencia_IdSolicitudCambio AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" csc
			INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  csc."IdSolicitudCambio";
		
			IF (v_countSolicitud = 0) THEN
				p_out_ErroresDetalle =  select 'SOLICITUD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				--p_out_ErroresDetalle =  select cast(v_countSolicitud as nvarchar(40)) as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
			

			--ACTUALIZACION DE VALORES (* ENTIDAD EXISTENTE *)
									
			--DATOS MAESTROS DE LA ENTIDAD 
			v_DatosSolicitudCambio	=SELECT csc.* 
									 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" csc
									 INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  csc."IdSolicitudCambio";
			
			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM :v_DatosSolicitudCambio;
			
			if(v_CountAtributoModificado>0) THEN
				--Se termina el flujo con la actualizacion del registro de solicitud.
				v_entidadOrigen = SELECT "IdEntidadOrigen", "IdTipoEntidad" FROM :v_DatosSolicitudCambio;
				v_solicitudes = select "IdSecuenciaCambio" AS "IdSolicitudCambio" FROM :p_SolicitudesCambio;
				
				CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo" (
					p_IdEmpresa				=>:p_IdEmpresa,
					p_IdUsuario				=>:p_IdUsuario,
					p_IdModulo				=>:p_IdModulo,
					p_SolicitudCambio		=>:v_solicitudes,
					p_EntidadOrigen			=>:v_entidadOrigen,
					p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
					p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
					p_out_ErroresContador			=>:p_out_ErroresContador,
					p_out_ErroresDetalle			=>:p_out_ErroresDetalle	
				);
							
			END IF;
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;