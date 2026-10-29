SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ATR.ModificarAtributo";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua informacion de los atributos a modificar, 
--  para ser creado como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 22-01-2016
--  
"BSCL.Plataforma.BD.Servicios::ATR.ModificarAtributo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdTipoEntidad			  NVARCHAR(40),	
	IN  p_IdEntidad				  NVARCHAR(40),
	IN  p_RegistroAtributo		  TABLE("IdAtributo"				BIGINT,	
										"IdTipoAtributo"   		    BIGINT, 
										"ValorAtributo"				NVARCHAR(255)),-- parametro estandar
	
	-- Parametros salida del servicio publico
	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT),
--	OUT p_out_RefId 					BIGINT,	
	-- Salidas para mensajes de advertencias y errores
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
			DECLARE v_countEntidad			   					    BIGINT:=0;
			DECLARE v_estadoAutorizacion							NVARCHAR(3);
			DECLARE v_RequiereAutorizacion							NVARCHAR(1);
			DECLARE v_secuencia_IdSolicitudCambio_Con_AUT			BIGINT:=0;
			DECLARE v_secuencia_IdSolicitudCambio_Sin_AUT			BIGINT:=0;
			DECLARE v_out_IdSecuencias 								table ("IdSecuencia" BIGINT);
			
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			
			
			--varaibles requeridas en la creacion inicla del atributo

			DECLARE	v_IdAtributo 					 BIGINT:=0;
			DECLARE	v_CountAtributo_Con_AUT			 BIGINT:=0;
			DECLARE	v_CountAtributo_Sin_AUT			 BIGINT:=0;

			DECLARE v_out_idAuth 		bigint;
			DECLARE v_out_AuthAdvertenciasContador 	int;
			DECLARE v_out_AuthErroresContador 		int;

			DECLARE v_Tab_Atributos_Nuevos	   table ("IdAtributo" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoAtributo" BIGINT, "ValorAtributo" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1));
			DECLARE v_Tab_Atributos_existentes table ("IdAtributo" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoAtributo" BIGINT, "ValorAtributo" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1));
			DECLARE v_Tab_Atributos			   table ("IdAtributo" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoAtributo" BIGINT, "ValorAtributo" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1));
			DECLARE v_Tab_Atributos_Con_AUT    table ("IdAtributo" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoAtributo" BIGINT, "ValorAtributo" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1));
			DECLARE v_Tab_Atributos_Sin_AUT    table ("IdAtributo" BIGINT ,"IdEntidad" NVARCHAR(40) , "IdTipoEntidad" NVARCHAR(40) ,"IdTipoAtributo" BIGINT, "ValorAtributo" NVARCHAR(255), "RequiereAutorizacion" NVARCHAR(1));

			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 
	/*		
		no se valida existencia puesto que el atributo puede no existir en la entidad		
	*/	
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			--Inicializacion de variables:
		
			v_estadoAutorizacion 			:= 'PEN';
			v_tipoEntidad					:= 'ATRIBUTO';
			v_IdConcepto					:= 'MODATR';
			
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			
			

			--Clasificacion de grupos de atributos
			--1 Obtencion de atributos nuevos y existentes, que requieran o no Autorizacion
			v_Tab_Atributos_Nuevos = SELECT
										("BSCL.Plataforma.BD.Secuencias::Atributo".NEXTVAL) as "IdAtributo",
										p_IdEntidad 				AS "IdEntidad",
										p_IdTipoEntidad 			AS "IdTipoEntidad",
										REG."IdTipoAtributo"		AS "IdTipoAtributo",
										IFNULL(REG."ValorAtributo",'')	AS "ValorAtributo",
										TAT."RequiereAutorizacion"	AS "RequiereAutorizacion"
									FROM :p_RegistroAtributo  REG
										INNER JOIN  "BSCL.Plataforma.BD.Vistas::ATR.ConsultarTipoAtributo" TAT
												 ON (TAT."IdTipoAtributo" = REG."IdTipoAtributo")
									WHERE REG."IdAtributo" =0;
								
			v_Tab_Atributos_existentes = SELECT
										REG."IdAtributo"			AS "IdAtributo",
										p_IdEntidad 				AS "IdEntidad",
										p_IdTipoEntidad 			AS "IdTipoEntidad",
										REG."IdTipoAtributo"		AS "IdTipoAtributo",
										IFNULL(REG."ValorAtributo",'')			AS "ValorAtributo",
										TAT."RequiereAutorizacion"	AS "RequiereAutorizacion"
									FROM :p_RegistroAtributo  REG
										INNER JOIN  "BSCL.Plataforma.BD.Vistas::ATR.ConsultarTipoAtributo" TAT
												 ON (TAT."IdTipoAtributo" = REG."IdTipoAtributo")
									WHERE REG."IdAtributo" >0;	
					
			--ATRIBUTO/ENTIDAD NO EXISTE, SE CREA REGISTRO POR DEFECTO SIN VALOR

			SELECT COUNT(1) INTO v_countEntidad FROM :v_Tab_Atributos_Nuevos;
			
			IF(v_countEntidad>0) THEN

			 ---CREACION DEL ATRIBUTO CON VALORES POR DEFECTO (Para modificar en solicitud)
		 			 SELECT "BSCL.Plataforma.BD.Secuencias::Atributo".NEXTVAL INTO v_IdAtributo FROM DUMMY;
		 			 
		 		
		 			
					 INSERT INTO  "BSCL.Plataforma.BD::PLA.Atributo" 
					  (
					   "IdAtributo",
					   "IdEntidad",			---Identificador inicial(valor requerido)
					   "IdTipoEntidad", 	---Identificador inicial(valor requerido)
					   "IdTipoAtributo", 	---Identificador inicial(valor requerido)
					   "IdEmpresa",
					   "ValorAtributo",
					   "FechaIngreso",
					   --"FechaCreacion",
					   "IdEstadoAutorizacion"
					   )SELECT 
						REG."IdAtributo",
						p_IdEntidad,
						p_IdTipoEntidad,
						REG."IdTipoAtributo",
						p_IdEmpresa,
						'',--Valor inicial del atributo en blanco
						--CURRENT_UTCTIMESTAMP,
						CURRENT_UTCTIMESTAMP,
						v_estadoAutorizacion				
						FROM :v_Tab_Atributos_Nuevos REG;			
					
			END IF;
			
			v_Tab_Atributos= SELECT * FROM :v_Tab_Atributos_Nuevos
							 UNION ALL SELECT * FROM :v_Tab_Atributos_existentes;

			--Para los Atibutos la solicitud de Cambio pasa a ser el ID de Entidad ("Atributo") en el evento de Autorizacion							 

			-- SE GENERAN 2 SOLICITUDES DE CAMBIO (con y sin Autorizacion)
			v_Tab_Atributos_Con_AUT= SELECT * FROM :v_Tab_Atributos where "RequiereAutorizacion"='S';
			v_Tab_Atributos_Sin_AUT= SELECT * FROM :v_Tab_Atributos where "RequiereAutorizacion"='N';
			
			SELECT COUNT(1) INTO v_CountAtributo_Con_AUT FROM :v_Tab_Atributos_Con_AUT;
			SELECT COUNT(1) INTO v_CountAtributo_Sin_AUT FROM :v_Tab_Atributos_Sin_AUT;
			
		
	
		
		
			-- ***	GENERAR SOLICITUD DE CAMBIO ATRIBUTOS SIN AUTORIZACION  ***

 			if v_CountAtributo_Sin_AUT >0  then
				SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio_Sin_AUT from dummy;
					
				V_RegistroSolicitudCambio_Sin_AUT =SELECT	
						v_secuencia_IdSolicitudCambio_Sin_AUT 	AS "IdSolicitudCambio",	
	 				 	0 										AS "IdSecuenciaCambio", --- Se crea interno
						v_tipoEntidad							AS "IdTipoEntidad",	
						MOD."IdAtributo"						AS "IdEntidadOrigen",
						MOD."CampoModificado" 					as "CampoModificado",
						MOD."DATA_TYPE_NAME" 					AS "TipoDato",
						(CASE MOD."COLUMN_NAME" WHEN 'ValorAtributo'		THEN IFNULL(CAST (ATR."ValorAtributo" AS NVARCHAR(255)) ,'')
						END) 									AS "ValorAntiguo",
						MOD."ValorNuevo" 						AS "ValorNuevo",
						CURRENT_UTCTIMESTAMP 									AS "FechaSolicitud",--SE CREAN INTERNOS
						CURRENT_UTCTIMESTAMP 									AS "FechaRevision",--SE CREAN INTERNOS
						v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
				FROM "BSCL.Plataforma.BD::PLA.Atributo" ATR,
								(SELECT COL."DATA_TYPE_NAME",
										COL."COLUMN_NAME",
										CAM."IdTipoAtributo" AS "CampoModificado",
										CAM."ValorAtributo"  AS "ValorNuevo",
										CAM."IdAtributo"
								FROM SYS.TABLE_COLUMNS COL
								INNER JOIN :v_Tab_Atributos_Sin_AUT CAM ON (COL."COLUMN_NAME"= 'ValorAtributo' )
								WHERE COL."TABLE_NAME"='BSCL.Plataforma.BD::PLA.Atributo') MOD
				WHERE  ATR."IdAtributo" = MOD."IdAtributo";

		
				CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
				(
					:V_RegistroSolicitudCambio_Sin_AUT,
					v_out_IdSecuencias
					
				);
				
			
				-------------------------------------------------------------------------
				-- Se confirma la modificacion de atributos que no requieren autorizacion
				-------------------------------------------------------------------------
						
					call  "BSCL.Plataforma.BD.Servicios::ATR.ConfirmarModificarAtributo" (
					:p_IdEmpresa,
					:p_IdUsuario,
					:p_IdModulo,
					:p_UsaReglasNegocio,
					:v_secuencia_IdSolicitudCambio_Sin_AUT,
					'ING',
					V_out_AdvertenciasContador,
					v_out_AdvertenciasDetalle,	
					V_out_ErroresContador,
					V_out_ErroresDetalle
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
	                
	                                     
	                                     
				  if 	p_out_ErroresContador > 0 then
							SIGNAL SALIDA_CONTROLADA;
				  end if;								
				
				
			 end if;
		
								
			-- *** GENERAR SOLICITUD DE CAMBIO ATRIBUTOS CON AUTORIZACION ***
 			if v_CountAtributo_Con_AUT >0  then
				SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio_Con_AUT from dummy;
					
				V_RegistroSolicitudCambio_Con_AUT =SELECT	
						v_secuencia_IdSolicitudCambio_Con_AUT 	AS "IdSolicitudCambio",	
	 				 	0 										AS "IdSecuenciaCambio", --- Se crea interno
						v_tipoEntidad							AS "IdTipoEntidad",	
						MOD."IdAtributo"						AS "IdEntidadOrigen",
						MOD."CampoModificado" 					as "CampoModificado",
						MOD."DATA_TYPE_NAME" 					AS "TipoDato",
						(CASE MOD."COLUMN_NAME" WHEN 'ValorAtributo'		THEN IFNULL(CAST (ATR."ValorAtributo" AS NVARCHAR(255)) ,'')
						END) 									AS "ValorAntiguo",
						MOD."ValorNuevo" 						AS "ValorNuevo",
						CURRENT_UTCTIMESTAMP 									AS "FechaSolicitud",--SE CREAN INTERNOS
						CURRENT_UTCTIMESTAMP 									AS "FechaRevision",--SE CREAN INTERNOS
						v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
				FROM "BSCL.Plataforma.BD::PLA.Atributo" ATR,
								(SELECT COL."DATA_TYPE_NAME",
										COL."COLUMN_NAME",
										CAM."IdTipoAtributo" AS "CampoModificado",
										CAM."ValorAtributo"  AS "ValorNuevo",
										CAM."IdAtributo"
								FROM SYS.TABLE_COLUMNS COL
								INNER JOIN :v_Tab_Atributos_Con_AUT CAM ON (COL."COLUMN_NAME"= 'ValorAtributo' )
								WHERE COL."TABLE_NAME"='BSCL.Plataforma.BD::PLA.Atributo') MOD
				WHERE  ATR."IdAtributo" = MOD."IdAtributo";
		
				CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
				(
					:V_RegistroSolicitudCambio_Con_AUT,
					v_out_IdSecuencias
					
				);
				
					------------------------------------------
					--Genera Evento de Autorizacion
					------------------------------------------

					v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
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
						p_IdCliente => p_IdEntidad, --v_IdCliente,
						p_Glosa => p_IdTipoEntidad,
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