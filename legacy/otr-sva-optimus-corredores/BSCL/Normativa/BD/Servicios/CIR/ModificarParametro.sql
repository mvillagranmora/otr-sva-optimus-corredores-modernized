SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR.ModificarParametro";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua informacion de parametros para la Circularizacion, 
--  para ser creado como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 26-09-2016
--  
"BSCL.Normativa.BD.Servicios::CIR.ModificarParametro" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_Periodo									NVARCHAR(6),
	IN  p_RegistroParametro		  TABLE("Codigo" 					NVARCHAR(40), 
										"Valor" 					NVARCHAR(100)),-- parametro estandar

	-- Parametros salida del servicio publico
--	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
	OUT p_out_RefId 					BIGINT,	
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
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA


	BEGIN	
	
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
	
		--salida/error/advertencia controlado por este servicio
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;	

	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
			DECLARE v_tipoEntidad									NVARCHAR(40):='';
			DECLARE v_IdConcepto	       							NVARCHAR(6):='';
			--DECLARE v_Evento      									BIGINT:=1;
			--DECLARE v_TolalEventos						      		BIGINT:=0;	
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_IdEntidadBase									BIGINT:=0;
			DECLARE v_countEntidad,v_count			   					    BIGINT:=0;
			DECLARE v_estadoAutorizacion							NVARCHAR(3):='';
			DECLARE v_RequiereAutorizacion							NVARCHAR(1):='';
			DECLARE v_secuencia_IdSolicitudCambio					BIGINT:=0;
			DECLARE v_out_IdSecuencias 								table ("IdSecuencia" BIGINT);
			
			DECLARE v_IdTipoDocumento 							    NVARCHAR(10):='';
			DECLARE v_IdNormativa 									NVARCHAR(100) DEFAULT '';
	 		DECLARE v_Esquema,v_Version, v_FechaVersion				NVARCHAR(10) DEFAULT '';
			
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			DECLARE v_fechaHoraSistema NVARCHAR(40):='';
			--DECLARE v_NombrePlantilla	NVARCHAR(100):='';
			--DECLARE v_IdCliente		   bigint:=0;
			
			DECLARE v_Tab_Parametros_Nuevos	   		table ("IdParametro" BIGINT, "Codigo" NVARCHAR(40),	"Valor"	NVARCHAR(100));
			DECLARE v_Tab_Parametros_existentes 	table ("IdParametro" BIGINT, "Codigo" NVARCHAR(40),	"Valor"	NVARCHAR(100));
			
			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 		
		
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			--Inicializacion de variables:
			v_secuencia_IdSolicitudCambio 	:= -1;
			
			v_estadoAutorizacion 			:= 'PEN';
			v_tipoEntidad					:= 'CIRCULARIZACION-PARAMETROS';
			v_IdConcepto					:= 'MCIPAR';
			p_out_RefId						:=	0;
			v_IdTipoDocumento 				:= 'CIRNOMINA';
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);		

		    v_tabParametros= SELECT * FROM  "BSCL.Normativa.BD::CIR.Parametro" WHERE 1=2;
		    v_tabParametros= SELECT * FROM  "BSCL.Normativa.BD::CIR.Parametro"
		    				 WHERE "Periodo"=p_Periodo;--  AND "Version"=v_Version;
		    
		 	--Clasificacion de grupos de indicadors
			--1 Obtencion de indicadors nuevos y existentes, que requieran o no Autorizacion
			v_Tab_Parametros_Nuevos=SELECT 	0 AS "IdParametro",
											'' AS "Codigo",
											'' AS "Valor"
											FROM DUMMY WHERE 1=2;	
			v_Tab_Parametros_existentes=SELECT * FROM  :v_Tab_Parametros_Nuevos;

			v_Tab_Parametros_Nuevos = SELECT
										("BSCL.Normativa.BD.Secuencias::CIR.Parametro".NEXTVAL) as "IdParametro",
										REG."Codigo" ,
										IFNULL(REG."Valor",'')	AS "Valor"
									FROM :p_RegistroParametro  REG
										LEFT OUTER JOIN :v_tabParametros  PAR
										 ON (PAR."Codigo"=REG."Codigo" AND PAR."Valor"=REG."Valor" )
									WHERE PAR."Codigo" IS NULL;
								
			v_Tab_Parametros_existentes = SELECT
										PAR."IdParametro"			AS "IdParametro",
										REG."Codigo" ,
										IFNULL(REG."Valor",'')	AS "Valor"
									FROM :p_RegistroParametro  REG
										INNER JOIN :v_tabParametros  PAR
										 ON (PAR."Codigo"=REG."Codigo" AND PAR."Valor"=REG."Valor" )
									WHERE REG."Codigo" <>'RUTMAS';	
			
			--Identifica la existencia de parametros 

			SELECT COUNT(1) INTO v_countEntidad FROM :v_Tab_Parametros_Nuevos;			
									   		
			IF(v_countEntidad>0) THEN

			 ---CREACION DEL ATRIBUTO CON VALORES POR DEFECTO (Para modificar en solicitud)

					 INSERT INTO  "BSCL.Normativa.BD::CIR.Parametro"
					  (
					   "IdParametro",
					   --"Esquema",
					   --"Version",
					   "Periodo",
					   "Codigo",
					   "Descripcion",
					   "Valor",
					   "FechaIngreso",
					   "FechaCreacion",
					   "FechaModificacion",
					   "IdEstadoAutorizacion",
					   "IdUsuarioCreador"
					   )SELECT 
  					    REG."IdParametro",

						p_Periodo,
						REG."Codigo",
						(CASE REG."Codigo"  WHEN 'RUTMAS' THEN 'RUT DESDE ARCHIVO'
							  				WHEN 'RUT' 	THEN 'RUT'
							 				WHEN 'MERCADO' 	THEN 'MERCADO'
							 				WHEN 'MONTO' 	THEN 'MONTO'
							 				WHEN 'TIPOOPER' 	THEN 'TIPO OPERACION'
											WHEN 'CLIENTE' 	THEN 'CONFIGURACION CLIENTE' ELSE '' END) AS "Descripcion",
						REG."Valor",
					    v_fechaHoraSistema,
						CURRENT_UTCTIMESTAMP,
						CURRENT_UTCTIMESTAMP,
						v_estadoAutorizacion,
					    p_IdUsuario						
						FROM :v_Tab_Parametros_Nuevos REG;			
					
			END IF;




			v_Tab_Parametros= SELECT * FROM :v_Tab_Parametros_Nuevos WHERE "Codigo" <>'RUTMAS'
						     UNION ALL SELECT TOP 1 * FROM :v_Tab_Parametros_Nuevos WHERE "Codigo" ='RUTMAS' --SOLO UN RUT DESDE ARCHIVO
							 UNION ALL SELECT * FROM :v_Tab_Parametros_existentes;

			
		-- 1) OBTENCION DE VALORES SEGUN CAMPOS ENVIADOS DESDE UI
			-- (Se obtiene: Tipo de Dato y Valor Anterior del campo modificado




		SELECT COUNT(1) INTO v_count FROM :v_Tab_Parametros;		
			
		IF v_count >0  THEN
			
				SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
				
				V_RegistroSolicitudCambio =SELECT	
						v_secuencia_IdSolicitudCambio 		    AS "IdSolicitudCambio",	
	 				 	0 										AS "IdSecuenciaCambio", --- Se crea interno
						v_tipoEntidad							AS "IdTipoEntidad",	
						MOD."IdParametro"						AS "IdEntidadOrigen",
						MOD."CampoModificado" 					as "CampoModificado",
						MOD."DATA_TYPE_NAME" 					AS "TipoDato",
						(CASE MOD."COLUMN_NAME" WHEN 'Valor'		THEN IFNULL(CAST (ATR."Valor" AS NVARCHAR(255)) ,'')
						END) 									AS "ValorAntiguo",
						MOD."ValorNuevo" 						AS "ValorNuevo",
						CURRENT_UTCTIMESTAMP 									AS "FechaSolicitud",--SE CREAN INTERNOS
						CURRENT_UTCTIMESTAMP 									AS "FechaRevision",--SE CREAN INTERNOS
						v_estadoAutorizacion 					AS "IdEstadoAutorizacion"
				FROM "BSCL.Normativa.BD::CIR.Parametro" ATR,
								(SELECT COL."DATA_TYPE_NAME",
										COL."COLUMN_NAME",
										CAM."Codigo" AS "CampoModificado",
										CASE COL."COLUMN_NAME" WHEN 'Valor' THEN CAM."Valor"
										END AS "ValorNuevo",
										CAM."IdParametro"
								FROM SYS.TABLE_COLUMNS COL
								INNER JOIN :v_Tab_Parametros CAM ON (COL."COLUMN_NAME" IN ('Valor') )
								WHERE COL."TABLE_NAME"='BSCL.Normativa.BD::CIR.Parametro') MOD
				WHERE  ATR."IdParametro" = MOD."IdParametro";
					
				-- 2) ENVIO A SERVICIO CREACION SOLICITUD DE CAMBIO
	
				CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
				(
					:V_RegistroSolicitudCambio,
					v_out_IdSecuencias
					
				);
												
							
								
				-- 2) DETERMINA SI LA MODIFICACION  REQUIERE DE UNA AUTORIZACION PREVIA
						
				------------------------------------------
				--Pregunta Si requiere autorizacion
				------------------------------------------
				
				call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
					:p_IdEmpresa,
					:p_IdUsuario,
					:p_IdModulo,
					:V_RegistroSolicitudCambio,
					:v_IdConcepto,
					:v_RequiereAutorizacion
				);	

					
		
				if v_RequiereAutorizacion='S' then
				------------------------------------------
				--Genera Evento de Autorizacion
				------------------------------------------
					DECLARE	 v_Glosa		NVARCHAR(520);
					declare v_out_idAuth 		bigint;
					declare v_out_AuthAdvertenciasContador 	int;
					declare v_out_AuthErroresContador 		int;
					v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
					v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
					
					SELECT ' PARAMETROS CIRCULARIZACION: '  into  v_Glosa 
					FROM DUMMY;
								
					v_tablaEventos = SELECT
										v_IdConcepto AS "IdTipoEvento",              		 
									    p_IdModulo 	 AS "IdModuloSolicita",
									    p_IdUsuario	 AS "IdUsuarioSolicita",
									    TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "Fecha",
									    '' AS "IdTipoOperacion",
									    v_IdConcepto AS	"IdConcepto",
									    CAST (v_secuencia_IdSolicitudCambio AS NVARCHAR(40)) AS "NumeroOperacionConcepto",
									    TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaOperacionConcepto",
									    v_IdEntidadBase  AS "IdCliente",
									    0  AS "IdFuncionario",
									    0  AS "IdProveedor",
									    '' AS "IdTipoProducto",
										'' AS "IdClasificacionTipoInstrumento",
										'' AS "IdTipoInstrumento",
										0  AS "IdInstrumento",
										'' AS "Nemo",
									    0  AS "Cantidad",
									    0  AS "Monto",
									    0  AS "Precio",
									    0  AS "Tasa",
									    '' AS "IdMoneda",
									    v_Glosa AS "Glosa",
									    0 as "IdUnion" 
									FROM DUMMY;
				
				----------------------------------------------------------------------------------------------------------------							
        			v_tablaEventosDetalle = select  "IdEventoDetalle" as "IdUnion","NombreCampo","TipoDato","Valor","Clasificacion"
   											from "BSCL.Plataforma.BD::AUT.EventoDetalle" 
   											where "IdEventoDetalle" = 0;
   				-----------------------------------------------------------------------------------------------------------------									
					CALL"BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo"(
							p_IdEmpresa => p_IdEmpresa,
							p_IdUsuario => p_IdUsuario,
							p_IdModulo => p_IdModulo,
							p_UsaReglasNegocio=> :p_UsaReglasNegocio,
						    p_simularOperacion=> 'N',
							p_tablaEventos=> :v_tablaEventos,	
							p_tablaDetalle => :v_tablaEventosDetalle,
							--salida
							p_out_IdEvento => v_out_idAuthtable,
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
								p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
								v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
								CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
								p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
														     select * from :p_out_AdvertenciasDetalle;
								SIGNAL SALIDA_CONTROLADA;
					  end if;								
					                            
						
						
				else
				
				
					------------------------------------------
					-- Se confirma la modificacion y se realizan los cambios
					------------------------------------------

					V_TabSolicitudCambio= SELECT v_secuencia_IdSolicitudCambio AS "IdSolicitudCambio" FROM  DUMMY;	
					call  "BSCL.Normativa.BD.Servicios::CIR.ConfirmarModificarParametro" (
					:p_IdEmpresa,
					:p_IdUsuario,
					:p_IdModulo,
					:p_UsaReglasNegocio,
					:V_TabSolicitudCambio,
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
				
		END IF;		
		
	
		p_out_RefId:=v_secuencia_IdSolicitudCambio;

	END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;