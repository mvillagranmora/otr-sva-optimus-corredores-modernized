SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::LIM.ModificarLimite";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua informacion de los limites a modificar, 
--  para ser creado como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 25-04-2016
--  
"BSCL.Plataforma.BD.Servicios::LIM.ModificarLimite" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdTipoEntidad				 		NVARCHAR(40),--PORTAFOLIO, CLIENTE, AGENTE
	IN  p_IdEntidadOrigen			 		BIGINT,
	IN  p_IdLimite						   	BIGINT,	
	IN  p_RegistroCambio		  TABLE("CampoModificado"		NVARCHAR(255), -- parametro estandar
											"ValorNuevo"			NVARCHAR(255)),-- parametro estandar
	
	-- Parametros salida del servicio publico
--	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
	OUT p_out_RefId 					BIGINT,	
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
			DECLARE v_tipoEntidad									NVARCHAR(40):='';
			DECLARE v_IdConcepto	       							NVARCHAR(6):='';
			DECLARE v_Evento      									BIGINT:=1;
			DECLARE v_TolalEventos						      		BIGINT:=0;	
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_IdEntidadBase									BIGINT:=0;
			DECLARE v_countEntidad			   					    BIGINT:=0;
			DECLARE v_estadoAutorizacion							NVARCHAR(3):='';
			DECLARE v_RequiereAutorizacion							NVARCHAR(1):='';
			DECLARE v_secuencia_IdSolicitudCambio					BIGINT:=0;
			DECLARE v_out_IdSecuencias 								table ("IdSecuencia" BIGINT);
			
			
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			DECLARE v_fechaHoraSistema NVARCHAR(40):='';
			DECLARE v_NombrePlantilla	NVARCHAR(100):='';
			DECLARE v_IdCliente		   bigint:=0;
			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 		
		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdLimite AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countEntidad FROM "BSCL.Plataforma.BD::LIM.Limite" WHERE "IdLimite"=p_IdLimite;
			
			IF(v_countEntidad=0) THEN
				p_out_ErroresDetalle =  select 'ENTIDAD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			ELSE
				SELECT "Nombre"  into  v_NombrePlantilla FROM "BSCL.Plataforma.BD::LIM.Limite" WHERE "IdLimite"=p_IdLimite;
				v_IdEntidadBase:=p_IdLimite;
			END IF;
			
			IF p_IdTipoEntidad='CLIENTE' THEN
				SELECT "IdPersona" INTO v_IdEntidadBase  FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
				WHERE "IdCliente"=p_IdEntidadOrigen;
			END IF;


			IF p_IdTipoEntidad='PORTAFOLIO' THEN
				SELECT "IdCliente" INTO v_IdCliente FROM"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"
				WHERE "IdPortafolio"=p_IdEntidadOrigen;
				
				SELECT "IdPersona"  INTO v_IdEntidadBase  FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
				WHERE "IdCliente"=v_IdCliente;
				
			END IF;
										
		END;		
		
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
			v_tipoEntidad					:= 'LIMITE';
			v_IdConcepto					:= 'MODLIM';
			p_out_RefId						:=	0;
			
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);		
			-- 1) OBTENCION DE VALORES SEGUN CAMPOS ENVIADOS DESDE UI
			-- (Se obtiene: Tipo de Dato y Valor Anterior del campo modificado
			
			
			SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
		
				
			
			V_RegistroSolicitudCambio =SELECT	
					v_secuencia_IdSolicitudCambio 	AS "IdSolicitudCambio",	
 				 	0 								AS "IdSecuenciaCambio", --- Se crea interno
					v_tipoEntidad					AS "IdTipoEntidad",	
					v_IdEntidadOrigen				AS "IdEntidadOrigen",
					MOD."CampoModificado" 			as "CampoModificado",
					MOD."DATA_TYPE_NAME" 			AS "TipoDato",
					(CASE MOD."COLUMN_NAME" WHEN 'Nombre'				THEN IFNULL(CAST (DOC."Nombre" AS NVARCHAR(255)),'')
											WHEN 'IdMesa'				THEN IFNULL(CAST (DOC."IdMesa" AS NVARCHAR(255)),'')
											WHEN 'IdCanalIngreso'		THEN IFNULL(CAST (DOC."IdCanalIngreso" AS NVARCHAR(255)),'')
											WHEN 'IdTipoInstrumento'	THEN IFNULL(CAST (DOC."IdTipoInstrumento" AS NVARCHAR(255)),'')
											WHEN 'IdTipoOperacion'		THEN IFNULL(CAST (DOC."IdTipoOperacion" AS NVARCHAR(255)),'')
											WHEN 'IdTipoProducto'		THEN IFNULL(CAST (DOC."IdTipoProducto" AS NVARCHAR(255)),'')
											WHEN 'IdInstrumento'		THEN IFNULL(CAST (DOC."IdInstrumento" AS NVARCHAR(255)),'')
											WHEN 'UnidadPlazo'			THEN IFNULL(CAST (DOC."UnidadPlazo" AS NVARCHAR(255)),'')
											WHEN 'Plazo'				THEN IFNULL(CAST (DOC."Plazo" AS NVARCHAR(255)),'')
											WHEN 'IdTipoReajuste'		THEN IFNULL(CAST (DOC."IdTipoReajuste" AS NVARCHAR(255)),'')
											WHEN 'CodigoOperador'		THEN IFNULL(CAST (DOC."CodigoOperador" AS NVARCHAR(255)),'')
											WHEN 'MontoTotal'			THEN IFNULL(CAST (DOC."MontoTotal" AS NVARCHAR(255)),'')
											WHEN 'MontoIntraday'		THEN IFNULL(CAST (DOC."MontoIntraday" AS NVARCHAR(255)),'')
											WHEN 'MontoOvernight'		THEN IFNULL(CAST (DOC."MontoOvernight" AS NVARCHAR(255)),'')
											WHEN 'IdMoneda'				THEN IFNULL(CAST (DOC."IdMoneda" AS NVARCHAR(255)),'')
											WHEN 'IdMonedaCalculo'		THEN IFNULL(CAST (DOC."IdMonedaCalculo" AS NVARCHAR(255)),'')
											WHEN 'ClasificacionRiesgoMinima' THEN IFNULL(CAST (DOC."ClasificacionRiesgoMinima" AS NVARCHAR(255)),'')
											WHEN 'IdEstado'				THEN IFNULL(CAST (DOC."IdEstado" AS NVARCHAR(255)),'')
											WHEN 'EsPersonalizada'		THEN IFNULL(CAST (DOC."EsPersonalizada" AS NVARCHAR(255)),'')
											WHEN 'FechaVigencia'		THEN IFNULL(CAST (DOC."FechaVigencia" AS NVARCHAR(255)),'')
											WHEN 'EsTotalizador'		THEN IFNULL(CAST (DOC."EsTotalizador" AS NVARCHAR(255)),'')
											WHEN 'IdAgente'				THEN IFNULL(CAST (DOC."IdAgente" AS NVARCHAR(255)),'')
											WHEN 'IdClasificacionTipoInstrumento' THEN IFNULL(CAST (DOC."IdClasificacionTipoInstrumento" AS NVARCHAR(255)),'')
											WHEN 'Porcentaje'			THEN IFNULL(CAST (DOC."Porcentaje" AS NVARCHAR(255)),'')
					END) AS "ValorAntiguo",
					MOD."ValorNuevo" AS "ValorNuevo",
					CURRENT_UTCTIMESTAMP AS "FechaSolicitud",--SE CREAN INTERNOS
					CURRENT_UTCTIMESTAMP AS "FechaRevision",--SE CREAN INTERNOS
					v_estadoAutorizacion AS "IdEstadoAutorizacion"
			FROM "BSCL.Plataforma.BD::LIM.Limite" DOC,
							(SELECT COL."DATA_TYPE_NAME",
									COL."COLUMN_NAME",
									CAM."CampoModificado",
									CAM."ValorNuevo"
							FROM SYS.TABLE_COLUMNS COL
							INNER JOIN :p_RegistroCambio CAM ON (COL."COLUMN_NAME"= CAM."CampoModificado")
							WHERE COL."TABLE_NAME"='BSCL.Plataforma.BD::LIM.Limite' AND COL.SCHEMA_NAME='BSCL') MOD
			WHERE  DOC."IdLimite" =p_IdLimite;

				
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
			
			SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto" || ' / Limite: ' || v_NombrePlantilla  into  v_Glosa 
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona" = v_IdEntidadBase;
						
			v_tablaEventos = SELECT
								v_IdConcepto AS "IdTipoEvento",              		 
							    p_IdModulo 	 AS "IdModuloSolicita",
							    p_IdUsuario	 AS "IdUsuarioSolicita",
							    TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "Fecha",
							    '' AS "IdTipoOperacion",
							    v_IdConcepto AS	"IdConcepto",
							    v_IdEntidadOrigen AS "NumeroOperacionConcepto",
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
				v_IdEvento = SELECT p_IdLimite as "IdLimite" FROM DUMMY;	
				call  "BSCL.Plataforma.BD.Servicios::LIM.ConfirmarModificarLimite" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:p_UsaReglasNegocio,
				:v_IdEvento,
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
		
		
		p_out_RefId:=v_secuencia_IdSolicitudCambio;

	END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;