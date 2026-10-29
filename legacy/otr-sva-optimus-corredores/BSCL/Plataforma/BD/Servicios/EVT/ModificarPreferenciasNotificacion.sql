SET SCHEMA "BSCL";
go
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::EVT.ModificarPreferenciasNotificacion";
go
CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua informacion de los indicadores a modificar, 
--  para ser creado como una solicitud de Cambio.
--  Autor:  
--  Fecha Creacion:  
--  
"BSCL.Plataforma.BD.Servicios::EVT.ModificarPreferenciasNotificacion" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdEntidad					BIGINT,	
	IN  p_TipoEntidad				NVARCHAR(3),
	IN	p_DireccionEvento			NVARCHAR(6),
	IN  p_RegistroAtributo		  TABLE("IdConfiguracionEvento"		BIGINT,	
										"IdEstado"					NVARCHAR(3),
										"FechaVencimiento"  		SECONDDATE),-- parametro estandar
	IN SuspensionTemporal			  NVARCHAR(1),   
	IN FechaInicioSuspension		  SECONDDATE,
	IN FechaTerminoSuspension		  SECONDDATE,
	-- Parametros salida del servicio publico
	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT),
--	OUT p_out_RefId 					BIGINT,	
	-- Salidas para mensajes de advertencias y errores
	OUT p_salida 						NVARCHAR(520),
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
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
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
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
			DECLARE v_tipoEntidad									NVARCHAR(40);--para la autorizacion
			DECLARE v_IdConcepto	       							NVARCHAR(6);
			DECLARE v_Evento      									BIGINT:=1;
			DECLARE v_TolalEventos						      		BIGINT:=0;	
			DECLARE v_count			   							    BIGINT:=0;
			DECLARE v_countnot			   							BIGINT:=0;
			DECLARE v_estadoAutorizacion							NVARCHAR(3);
			DECLARE v_RequiereAutorizacion							NVARCHAR(1);
			DECLARE v_secuencia_IdSolicitudCambio_Con_AUT			BIGINT:=0;
			DECLARE v_secuencia_IdSolicitudCambio_Sin_AUT			BIGINT:=0;
			DECLARE v_out_IdSecuencias 								table ("IdSecuencia" BIGINT);
			DECLARE v_IdEntidad										BIGINT;	
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			
			
			--varaibles requeridas en la creacion inicla del indicador

			DECLARE	v_IdIndicador 					 BIGINT:=0;
			DECLARE	v_CountIndicador_Con_AUT			 BIGINT:=0;
			DECLARE	v_CountIndicador_Sin_AUT			 BIGINT:=0;

			DECLARE v_out_idAuth 		bigint;
			DECLARE v_out_AuthAdvertenciasContador 	int;
			DECLARE v_out_AuthErroresContador 		int;

			DECLARE v_Tab_indicadores_Nuevos	   	table ("IdIndicador" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoIndicador" BIGINT, "ValorIndicador" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1), "FechaVencimiento" SECONDDATE);
			DECLARE v_Tab_Indicadores_existentes 	table ("IdIndicador" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoIndicador" BIGINT, "ValorIndicador" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1), "FechaVencimiento" SECONDDATE);
			DECLARE v_Tab_Indicadores			   	table ("IdIndicador" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoIndicador" BIGINT, "ValorIndicador" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1), "FechaVencimiento" SECONDDATE);
			DECLARE v_Tab_Indicadores_Con_AUT    	table ("IdIndicador" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoIndicador" BIGINT, "ValorIndicador" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1), "FechaVencimiento" SECONDDATE);
			DECLARE v_Tab_Indicadores_Sin_AUT    	table ("IdIndicador" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoIndicador" BIGINT, "ValorIndicador" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1), "FechaVencimiento" SECONDDATE);

			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 
	/*		
		no se valida existencia puesto que el indicador puede no existir en la entidad		
	*/	
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			--Inicializacion de variables:
			-- select top 1 "IdEntidad" into v_IdEntidad from "BSCL.Plataforma.BD::ACC.Usuario"  where "IdUsuario" = p_IdUsuario;
			v_IdEntidad						:= p_IdEntidad;
			v_estadoAutorizacion 			:= 'PEN';
			v_tipoEntidad					:= 'NOTIFICADOR';
			v_IdConcepto					:= 'MODNOT';
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);

			SELECT COUNT(1) INTO v_count FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" WHERE "IdConcepto" = v_IdConcepto and "RequiereAutorizacion" = 'S';
 
	
 		   
 		   --Ingresar presonalizaciones si no existen, con default 'N'
 		   	INSERT INTO 
		   		"BSCL.Plataforma.BD::EVT.PersonalizacionEvento"
		   		
			   
			   	    
			   	SELECT DISTINCT
				   		 p_IdEntidad AS "IdEntidad",
				   		 p_TipoEntidad AS "TipoEntidad",
				   		cep."IdConfiguracionEvento", 	   	
				   		'ACT',
	 			   		CURRENT_TIMESTAMP,
				   		p_IdUsuario,
				   		'N',
				   		'0001-01-01',
				   		'0001-01-01' 
			   	FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos"  cep
				WHERE "IdConfiguracionEvento" not in (select "IdConfiguracionEvento" from "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad" where "IdEntidad" =  p_IdEntidad AND "TipoEntidad" = p_TipoEntidad) AND "DirEvt" = p_DireccionEvento;

						   		
		   
 		   
		 
			
			
			-- ***	GENERAR SOLICITUD DE CAMBIO ATRIBUTOS SIN AUTORIZACION  ***

 			if v_count = 0  then
				SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio_Sin_AUT from dummy;
					
				  
				
				
					t_IdEstado =
							SELECT	
								v_secuencia_IdSolicitudCambio_Sin_AUT 	AS "IdSolicitudCambio",	
								0 										AS "IdSecuenciaCambio", --- Se crea interno
								v_tipoEntidad							AS "IdTipoEntidad",	
								pevt."IdConfiguracionEvento"			AS "IdEntidadOrigen",
								'IdEstado'								as "CampoModificado",
								'NVARCHAR' 								AS "TipoDato",
								'' AS "ValorAntiguo",
								reg."IdEstado" 							AS "ValorNuevo",
								CURRENT_UTCTIMESTAMP 					AS "FechaSolicitud",--SE CREAN INTERNOS
								CURRENT_UTCTIMESTAMP 					AS "FechaRevision",--SE CREAN INTERNOS
								v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
							FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pevt
							INNER JOIN :p_RegistroAtributo reg
							ON pevt."IdEntidad" = p_IdEntidad AND pevt."TipoEntidad" = p_TipoEntidad
							WHERE  reg."IdConfiguracionEvento" = pevt."IdConfiguracionEvento";
				
				suspension =
							SELECT	
								v_secuencia_IdSolicitudCambio_Sin_AUT 	AS "IdSolicitudCambio",	
								0 										AS "IdSecuenciaCambio", --- Se crea interno
								v_tipoEntidad							AS "IdTipoEntidad",	
								pevt."IdEntidad"						AS "IdEntidadOrigen",
								'SuspensionTemporal' 					as "CampoModificado",
								'NVARCHAR' 								AS "TipoDato",
								''										AS "ValorAntiguo",
								SuspensionTemporal 						AS "ValorNuevo",
								CURRENT_UTCTIMESTAMP 					AS "FechaSolicitud",--SE CREAN INTERNOS
								CURRENT_UTCTIMESTAMP 					AS "FechaRevision",--SE CREAN INTERNOS
								v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
							FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pevt
 							WHERE pevt."IdEntidad" = p_IdEntidad AND pevt."TipoEntidad" = p_TipoEntidad;
 				
 				fechaInicio =
							SELECT	
								v_secuencia_IdSolicitudCambio_Sin_AUT 	AS "IdSolicitudCambio",	
								0 										AS "IdSecuenciaCambio", --- Se crea interno
								v_tipoEntidad							AS "IdTipoEntidad",	
								pevt."IdEntidad"						AS "IdEntidadOrigen",
								'FechaInicioSuspension' 				as "CampoModificado",
								'SECONDDATE' 							AS "TipoDato",
								CASE 
								 WHEN pevt."FechaInicioSuspension" IS NULL THEN '0001-01-01'
								 ELSE to_char(pevt."FechaInicioSuspension")
								 END
								 										AS "ValorAntiguo",
								to_char(FechaInicioSuspension) 			AS "ValorNuevo",
								CURRENT_UTCTIMESTAMP 					AS "FechaSolicitud",--SE CREAN INTERNOS
								CURRENT_UTCTIMESTAMP 					AS "FechaRevision",--SE CREAN INTERNOS
								v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
							FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pevt
 							WHERE pevt."IdEntidad" = p_IdEntidad AND pevt."TipoEntidad" = p_TipoEntidad;
 							
 							
 				fechaTermino =
 				
							SELECT	
								v_secuencia_IdSolicitudCambio_Sin_AUT 	AS "IdSolicitudCambio",	
								0 										AS "IdSecuenciaCambio", --- Se crea interno
								v_tipoEntidad							AS "IdTipoEntidad",	
								pevt."IdEntidad"						AS "IdEntidadOrigen",
								'FechaTerminoSuspension' 				as "CampoModificado",
								'SECONDDATE' 								AS "TipoDato",
								CASE 
								 WHEN pevt."FechaTerminoSuspension" IS NULL THEN '0001-01-01' 
								 ELSE to_char(pevt."FechaTerminoSuspension")
								 END
								 										AS "ValorAntiguo",
								to_char(FechaTerminoSuspension)			AS "ValorNuevo",
								CURRENT_UTCTIMESTAMP 					AS "FechaSolicitud",--SE CREAN INTERNOS
								CURRENT_UTCTIMESTAMP 					AS "FechaRevision",--SE CREAN INTERNOS
								v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
							FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pevt
 							WHERE pevt."IdEntidad" = p_IdEntidad AND pevt."TipoEntidad" = p_TipoEntidad;
 							
 				V_RegistroSolicitudCambio_Sin_AUT =
 							SELECT  DISTINCT * FROM (
 							SELECT 
 				 				"IdSolicitudCambio",	
								"IdSecuenciaCambio", 
								"IdTipoEntidad",	
								"IdEntidadOrigen",
								"CampoModificado",
								"TipoDato",
								"ValorAntiguo",
								"ValorNuevo",
								"FechaSolicitud",
								"FechaRevision",
								"IdEstadoAutorizacion" FROM :t_IdEstado
							UNION ALL 
							SELECT 
 				 				"IdSolicitudCambio",	
								"IdSecuenciaCambio", 
								"IdTipoEntidad",	
								"IdEntidadOrigen",
								"CampoModificado",
								"TipoDato",
								"ValorAntiguo",
								"ValorNuevo",
								"FechaSolicitud",
								"FechaRevision",
								"IdEstadoAutorizacion" FROM :suspension
							UNION ALL 
							SELECT 
 				 				"IdSolicitudCambio",	
								"IdSecuenciaCambio", 
								"IdTipoEntidad",	
								"IdEntidadOrigen",
								"CampoModificado",
								"TipoDato",
								"ValorAntiguo",
								"ValorNuevo",
								"FechaSolicitud",
								"FechaRevision",
								"IdEstadoAutorizacion" FROM :fechaInicio
							UNION ALL 
							SELECT 
 				 				"IdSolicitudCambio",	
								"IdSecuenciaCambio", 
								"IdTipoEntidad",	
								"IdEntidadOrigen",
								"CampoModificado",
								"TipoDato",
								"ValorAntiguo",
								"ValorNuevo",
								"FechaSolicitud",
								"FechaRevision",
								"IdEstadoAutorizacion" FROM :fechaTermino
							UNION ALL	
							SELECT 
								v_secuencia_IdSolicitudCambio_Sin_AUT,	
								0, 
								'NOTIFICADOR',	
								'0',
								'IdEntidad',
								'BIGINT',
							    '0',
								p_IdEntidad||'',
								CURRENT_UTCTIMESTAMP,
								CURRENT_UTCTIMESTAMP,
								'PEN' FROM dummy
							UNION ALL	
							SELECT 
								v_secuencia_IdSolicitudCambio_Sin_AUT,	
								0, 
								'NOTIFICADOR',	
								'0',
								'TipoEntidad',
								'BIGINT',
							    '0',
								p_TipoEntidad||'',
								CURRENT_UTCTIMESTAMP,
								CURRENT_UTCTIMESTAMP,
								'PEN' FROM dummy) ;

		
				CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
				(
					:V_RegistroSolicitudCambio_Sin_AUT,
					v_out_IdSecuencias
					
				);
				
					
				
				-------------------------------------------------------------------------
				-- Se confirma la modificacion de indicadores que no requieren autorizacion
				-------------------------------------------------------------------------
					v_solicitudes = SELECT v_secuencia_IdSolicitudCambio_Sin_AUT AS "IdSolicitudCambio" FROM DUMMY;
					
					call  "BSCL.Plataforma.BD.Servicios::EVT.ConfirmarModificacionPreferencias" (
					:p_IdEmpresa,
					:p_IdUsuario,
					:p_IdModulo,
					:p_UsaReglasNegocio,
					:v_solicitudes,
					'ING',
					V_out_AdvertenciasContador,
					v_out_AdvertenciasDetalle,	
					V_out_ErroresContador,
					V_out_ErroresDetalle
					);
					
					
					
				 p_Salida :='Se confirman las modificaciones';
	  			 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;	
	  			 
				 p_out_AdvertenciasDetalle = Select "Codigo",'Requiere autorización' as "RequiereAutorizacion",'Cambios realizados'  as "Descripcion" 
	                                          From :v_out_AdvertenciasDetalle Union All 
	                                          Select "Codigo","RequiereAutorizacion", "Descripcion"
	                                          From :p_out_AdvertenciasDetalle;  
	                                          
	                                          
	              p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
	                                         
	              p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
	                                     From :v_out_ErroresDetalle Union All 
	                                     Select "Codigo", "Descripcion" 
	                                     From :p_out_ErroresDetalle; 
	                
	                                     
	                                     
				  if 	p_out_ErroresContador > 0 then
							SIGNAL SALIDA_CONTROLADA;
				  end if;								
				
				
			 end if;
		
								
			-- *** GENERAR SOLICITUD DE CAMBIO ATRIBUTOS CON AUTORIZACION ***
 			if v_count >0  then
				SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio_Con_AUT from dummy;
					
					
				t_IdEstado =
							SELECT	
								v_secuencia_IdSolicitudCambio_Con_AUT 	AS "IdSolicitudCambio",	
								0 										AS "IdSecuenciaCambio", --- Se crea interno
								v_tipoEntidad							AS "IdTipoEntidad",	
								pevt."IdConfiguracionEvento"			AS "IdEntidadOrigen",
								'IdEstado'								as "CampoModificado",
								'NVARCHAR' 								AS "TipoDato",
								'' AS "ValorAntiguo",
								reg."IdEstado" 							AS "ValorNuevo",
								CURRENT_UTCTIMESTAMP 					AS "FechaSolicitud",--SE CREAN INTERNOS
								CURRENT_UTCTIMESTAMP 					AS "FechaRevision",--SE CREAN INTERNOS
								v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
							FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pevt
							INNER JOIN :p_RegistroAtributo reg
							ON pevt."IdEntidad" = p_IdEntidad AND pevt."TipoEntidad" = p_TipoEntidad
							WHERE  reg."IdConfiguracionEvento" = pevt."IdConfiguracionEvento";
				
				suspension =
							SELECT	
								v_secuencia_IdSolicitudCambio_Con_AUT 	AS "IdSolicitudCambio",	
								0 										AS "IdSecuenciaCambio", --- Se crea interno
								v_tipoEntidad							AS "IdTipoEntidad",	
								pevt."IdEntidad"						AS "IdEntidadOrigen",
								'SuspensionTemporal' 					as "CampoModificado",
								'NVARCHAR' 								AS "TipoDato",
								''										AS "ValorAntiguo",
								SuspensionTemporal 						AS "ValorNuevo",
								CURRENT_UTCTIMESTAMP 					AS "FechaSolicitud",--SE CREAN INTERNOS
								CURRENT_UTCTIMESTAMP 					AS "FechaRevision",--SE CREAN INTERNOS
								v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
							FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pevt
 							WHERE pevt."IdEntidad" = p_IdEntidad AND pevt."TipoEntidad" = p_TipoEntidad;
 				
 				fechaInicio =
							SELECT	
								v_secuencia_IdSolicitudCambio_Con_AUT 	AS "IdSolicitudCambio",	
								0 										AS "IdSecuenciaCambio", --- Se crea interno
								v_tipoEntidad							AS "IdTipoEntidad",	
								pevt."IdEntidad"						AS "IdEntidadOrigen",
								'FechaInicioSuspension' 				as "CampoModificado",
								'SECONDDATE' 							AS "TipoDato",
								CASE 
								 WHEN pevt."FechaInicioSuspension" IS NULL THEN '0001-01-01'
								 ELSE to_char(pevt."FechaInicioSuspension")
								 END
								 										AS "ValorAntiguo",
								to_char(FechaInicioSuspension) 			AS "ValorNuevo",
								CURRENT_UTCTIMESTAMP 					AS "FechaSolicitud",--SE CREAN INTERNOS
								CURRENT_UTCTIMESTAMP 					AS "FechaRevision",--SE CREAN INTERNOS
								v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
							FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pevt
 							WHERE pevt."IdEntidad" = p_IdEntidad AND pevt."TipoEntidad" = p_TipoEntidad;
 							
 							
 				fechaTermino =
 				
							SELECT	
								v_secuencia_IdSolicitudCambio_Con_AUT 	AS "IdSolicitudCambio",	
								0 										AS "IdSecuenciaCambio", --- Se crea interno
								v_tipoEntidad							AS "IdTipoEntidad",	
								pevt."IdEntidad"						AS "IdEntidadOrigen",
								'FechaTerminoSuspension' 				as "CampoModificado",
								'SECONDDATE' 								AS "TipoDato",
								CASE 
								 WHEN pevt."FechaTerminoSuspension" IS NULL THEN '0001-01-01' 
								 ELSE to_char(pevt."FechaTerminoSuspension")
								 END
								 										AS "ValorAntiguo",
								to_char(FechaTerminoSuspension)			AS "ValorNuevo",
								CURRENT_UTCTIMESTAMP 					AS "FechaSolicitud",--SE CREAN INTERNOS
								CURRENT_UTCTIMESTAMP 					AS "FechaRevision",--SE CREAN INTERNOS
								v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
							FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pevt
 							WHERE pevt."IdEntidad" = p_IdEntidad AND pevt."TipoEntidad" = p_TipoEntidad;
 							
 				V_RegistroSolicitudCambio_Con_AUT =
 							SELECT  DISTINCT * FROM (
 							SELECT 
 				 				"IdSolicitudCambio",	
								"IdSecuenciaCambio", 
								"IdTipoEntidad",	
								"IdEntidadOrigen",
								"CampoModificado",
								"TipoDato",
								"ValorAntiguo",
								"ValorNuevo",
								"FechaSolicitud",
								"FechaRevision",
								"IdEstadoAutorizacion" FROM :t_IdEstado
							UNION ALL 
							SELECT 
 				 				"IdSolicitudCambio",	
								"IdSecuenciaCambio", 
								"IdTipoEntidad",	
								"IdEntidadOrigen",
								"CampoModificado",
								"TipoDato",
								"ValorAntiguo",
								"ValorNuevo",
								"FechaSolicitud",
								"FechaRevision",
								"IdEstadoAutorizacion" FROM :suspension
							UNION ALL 
							SELECT 
 				 				"IdSolicitudCambio",	
								"IdSecuenciaCambio", 
								"IdTipoEntidad",	
								"IdEntidadOrigen",
								"CampoModificado",
								"TipoDato",
								"ValorAntiguo",
								"ValorNuevo",
								"FechaSolicitud",
								"FechaRevision",
								"IdEstadoAutorizacion" FROM :fechaInicio
							UNION ALL 
							SELECT 
 				 				"IdSolicitudCambio",	
								"IdSecuenciaCambio", 
								"IdTipoEntidad",	
								"IdEntidadOrigen",
								"CampoModificado",
								"TipoDato",
								"ValorAntiguo",
								"ValorNuevo",
								"FechaSolicitud",
								"FechaRevision",
								"IdEstadoAutorizacion" FROM :fechaTermino
							UNION ALL	
							SELECT 
								v_secuencia_IdSolicitudCambio_Con_AUT,	
								0, 
								'NOTIFICADOR',	
								'0',
								'IdEntidad',
								'BIGINT',
							    '0',
								p_IdEntidad||'',
								CURRENT_UTCTIMESTAMP,
								CURRENT_UTCTIMESTAMP,
								'PEN' FROM dummy
							UNION ALL	
							SELECT 
								v_secuencia_IdSolicitudCambio_Con_AUT,	
								0, 
								'NOTIFICADOR',	
								'0',
								'TipoEntidad',
								'BIGINT',
							    '0',
								p_TipoEntidad||'',
								CURRENT_UTCTIMESTAMP,
								CURRENT_UTCTIMESTAMP,
								'PEN' FROM dummy) ;
 							
		
				CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
				(
					:V_RegistroSolicitudCambio_Con_AUT,
					v_out_IdSecuencias
					
				);
				
					------------------------------------------
					--Genera Evento de Autorizacion
					------------------------------------------

					v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'Requiere Autorización' as "RequiereAutorizacion", 'Un agente debe autorizar estos cambios' as "Descripcion" from dummy where 1=1;
					v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
					
					
					CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
						p_IdEmpresa => p_IdEmpresa,
						p_IdUsuario => p_IdUsuario,
						p_IdModulo => p_IdModulo,
						p_UsaReglasNegocio => 'N',								
						p_IdTipoEvento => v_IdConcepto,--'MODDOC',
						p_IdModuloSolicita => p_IdModulo,
						p_IdUsuarioSolicita => p_IdUsuario,
						p_Fecha => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
						p_IdConcepto => v_IdConcepto,
						p_NumeroOperacionConcepto =>  CAST (v_secuencia_IdSolicitudCambio_Con_AUT AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
						p_FechaOperacionConcepto => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
						p_IdCliente => v_IdEntidad, --v_IdCliente,
						p_Glosa => 'CONFIRMACION CAMBIO PREFERENCIAS DE NOTIFICACION',
						--salida
						p_out_IdEvento => v_out_idAuth,
						p_out_AdvertenciasContador => v_out_AuthAdvertenciasContador,
						p_out_AdvertenciasDetalle =>  v_out_AuthAdvertenciasDetalle,
						p_out_ErroresContador =>  v_out_AuthErroresContador,
						p_out_ErroresDetalle =>  v_out_AuthErroresDetalle);		
			
					 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AuthAdvertenciasContador;	
		  			 
					 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
		                                          From :v_out_AuthAdvertenciasDetalle Union All 
		                                          Select "Codigo","RequiereAutorizacion", "Descripcion"
		                                          From :p_out_AdvertenciasDetalle;  
		                                          
		              p_Salida :='Un agente debe confirmar las modificaciones';                            
		              p_out_ErroresContador := p_out_ErroresContador + v_out_AuthErroresContador;
		                                         
		              p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
		                                     From :v_out_AuthErroresDetalle Union All 
		                                     Select "Codigo", "Descripcion" 
		                                     From :p_out_ErroresDetalle; 
		                    
                                     
					  if 	p_out_ErroresContador > 0 then
								SIGNAL SALIDA_CONTROLADA;
								
							else
								--Mensaje de Salida
		
								v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
								CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
								p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
														     select * from :p_out_AdvertenciasDetalle;
								SIGNAL SALIDA_CONTROLADA;
					  end if;								
							
					
							
			 end if;
			
			
			
			
		p_out_IdSecuencias= SELECT v_secuencia_IdSolicitudCambio_Sin_AUT AS "IdSecuencia" FROM dummy
							UNION ALL  SELECT v_secuencia_IdSolicitudCambio_Con_AUT AS "IdSecuencia" FROM dummy ;

	END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
