SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::LIM.RechazaAutorizacionGrupoEmisorDetalle";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para Rechaza la modificacion del detalle de un grupo emisor.
--  Invocado desde el modulo de autorizaciones. 
--  Autor: mmachmar - BCS
--  Fecha Creacion: 29-06-2016
--  
"BSCL.Plataforma.BD.Servicios::LIM.RechazaAutorizacionGrupoEmisorDetalle" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN 	p_IdGrupoEmisorDetalle						TABLE ("IdGrupoEmisorDetalle"	BIGINT),		
	IN  p_IdConcepto	       						NVARCHAR(6),
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
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
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
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			
			DECLARE v_countSolicitud								BIGINT;
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'GRUPOEMISORDETALLE';
			DECLARE v_IdEstadoAutorizacion							NVARCHAR(3):='RZD';
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.		
		BEGIN

			SELECT COUNT(SOL."IdSolicitudCambio") INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_IdGrupoEmisorDetalle L ON CAST (L."IdGrupoEmisorDetalle" AS NVARCHAR(40)) =  SOL."IdEntidadOrigen" 			
			WHERE SOL."IdTipoEntidad" 		= v_TipoEntidad
  			 AND SOL."IdEstadoAutorizacion"	='PEN';
			
			IF(v_countSolicitud=0 AND p_IdConcepto='MEDLCP') THEN
				p_out_ErroresDetalle =  select 'SOLICITUD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
			--Se termina el flujo con la actualizacion del registro de solicitud.

			IF p_IdConcepto='MEDLCP' THEN		--MODIFICAR 
				v_DatosSolicitudCambio = SELECT SOL.* 
					FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
					JOIN :p_IdGrupoEmisorDetalle L ON  CAST(L."IdGrupoEmisorDetalle" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
					AND SOL."IdTipoEntidad" 		= v_TipoEntidad
	                AND SOL."IdEstadoAutorizacion"	='PEN';
		
				v_SolicitudCambio = SELECT 0 as "IdSolicitudCambio" FROM dummy where 1=2;
				v_EntidadOrigen = SELECT "IdEntidadOrigen", "IdTipoEntidad" FROM :v_DatosSolicitudCambio;
				CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo" (
					p_IdEmpresa				=>:p_IdEmpresa,
					p_IdUsuario				=>:p_IdUsuario,
					p_IdModulo				=>:p_IdModulo,
					p_SolicitudCambio			=>:v_SolicitudCambio,
					p_EntidadOrigen			    =>:v_EntidadOrigen,
					p_IdEstadoAutorizacion	=>:v_IdEstadoAutorizacion,
					p_out_AdvertenciasContador		=>:v_out_AdvertenciasContador,
					p_out_AdvertenciasDetalle		=>:v_out_AdvertenciasDetalle,	
					p_out_ErroresContador			=>:v_out_ErroresContador,
					p_out_ErroresDetalle			=>:v_out_ErroresDetalle		
				);
				
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;
				p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
	                                          From :v_out_AdvertenciasDetalle Union All 
	                                          Select "Codigo","RequiereAutorizacion", "Descripcion"
	                                          From :p_out_AdvertenciasDetalle;
	            
	            p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
	            p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
	                                     From :v_out_ErroresDetalle Union All 
	                                     Select "Codigo", "Descripcion" 
	                                     From :p_out_ErroresDetalle;
	            
	            if p_out_ErroresContador > 0 then
	            	SIGNAL SALIDA_CONTROLADA;
	            end if;		
									
			END IF;

			UPDATE GED
			SET "IdEstadoAutorizacion"=v_IdEstadoAutorizacion,
				"IdEstado"= (CASE WHEN p_IdConcepto='NEDLCP' THEN 'INA' ELSE "IdEstado" END) 
			FROM "BSCL.Plataforma.BD::LIM.GrupoEmisorDetalle" GED	
			JOIN :p_IdGrupoEmisorDetalle L ON L."IdGrupoEmisorDetalle" = GED."IdGrupoEmisorDetalle"; 					
						
		END;

		--------------------
		--FIN SP 
		--------------------
	END;	
END;