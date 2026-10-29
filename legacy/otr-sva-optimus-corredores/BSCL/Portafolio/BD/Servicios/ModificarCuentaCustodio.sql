SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ModificarCuentaCustodio";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio que mediante autorizaciones actualiza una cuenta custodio.
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 05-09-2016
--  
"BSCL.Portafolio.BD.Servicios::ModificarCuentaCustodio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdCuentaCustodioPortafolio						   	BIGINT,	
	IN  p_RegistroCambio		  TABLE(	"CampoModificado"	NVARCHAR(255), -- parametro estandar
											"ValorNuevo"		NVARCHAR(255)),-- parametro estandar
	
	-- Parametros salida del servicio publico
--	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
	OUT p_out_RefId 					BIGINT,	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,
	OUT p_out_idAuth int
	
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

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_countCuentaCustodioPortafolio BIGINT DEFAULT 0;
		DECLARE v_RequiereAutorizacion			NVARCHAR DEFAULT 'S';
		DECLARE v_cuentaCustodioPortafolio		TABLE (
												"IdCuentaCustodioPortafolio" BIGINT,
												"IdPortafolio" BIGINT,
												"IdBoveda" NVARCHAR(20),
												"Codigo"	NVARCHAR(20),
												"FechaModificacion" seconddate,
												"IdEstado"	NVARCHAR(3),
												"IdEstadoAutorizacion" NVARCHAR(3)
												);
		
		DECLARE v_secuencia_IdSolicitudCambio BIGINT;
		DECLARE	v_tipoEntidad NVARCHAR(20) DEFAULT 'PORTAFOLIO';
		DECLARE	v_IdConcepto NVARCHAR(20) DEFAULT 'MCTCUP';
		DECLARE v_estadoAutorizacion NVARCHAR(3) DEFAULT 'PEN';
		DECLARE	v_Glosa		NVARCHAR(255);
		DECLARE v_out_idAuth 		bigint;
		DECLARE v_IdEntidadBase BIGINT;
		
		--PRONETIT
       	DECLARE v_countIdPortafolio 	INT:=0;
		DECLARE v_IdEntidad				BIGINT;
		DECLARE v_TipoEntidadReplica	NVARCHAR(50):= 'CUENTACUSTODIO';
		DECLARE v_TipoOperacion			NVARCHAR(1):= 'U';
		
		DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
   
		
		--------------------
		--Validaciones
		--------------------
		v_cuentaCustodioPortafolio = select
				"IdCuentaCustodioPortafolio",
				"IdPortafolio",
				"IdBoveda",
				"Codigo", 
				"FechaModificacion",
				"IdEstado",
				"IdEstadoAutorizacion"
				from "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
				WHERE "IdCuentaCustodioPortafolio" = p_IdCuentaCustodioPortafolio;
						
		-- Existencia 		
		BEGIN
			SELECT COUNT(1) INTO v_countCuentaCustodioPortafolio FROM :v_cuentaCustodioPortafolio;
			
			IF(v_countCuentaCustodioPortafolio=0) THEN
				p_out_ErroresDetalle =  select 'CUENTA_CUSTODIO_PORTAFOLIO_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
										
		END;		
		
		--------------------
		--Reglas Negocio
		--------------------		
		v_IdConcepto:= 'MCTCUP';
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			
			-- 1) DETERMINA SI LA MODIFICACION  REQUIERE DE UNA AUTORIZACION PREVIA
			
			SELECT "RequiereAutorizacion" INTO v_RequiereAutorizacion 
			FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 
			WHERE "IdConcepto" = 'MCTCUP' AND "IdEmpresa" = :p_IdEmpresa;

			-- 2) CREAR SOLICITUD DE CAMBIO
			
			SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
		
			V_RegistroSolicitudCambio =SELECT	
					v_secuencia_IdSolicitudCambio 	AS "IdSolicitudCambio",	
 				 	0 								AS "IdSecuenciaCambio", --- Se crea interno
					v_tipoEntidad					AS "IdTipoEntidad",	
					p_IdCuentaCustodioPortafolio	AS "IdEntidadOrigen",
					MOD."CampoModificado" 			as "CampoModificado",
					MOD."DATA_TYPE_NAME" 			AS "TipoDato",
					(CASE MOD."COLUMN_NAME" WHEN 'Codigo'		THEN IFNULL(DOC."Codigo",'')
											WHEN 'IdEstado'		THEN IFNULL(DOC."IdEstado",'')																																																																																				
					END) AS "ValorAntiguo",
					MOD."ValorNuevo" AS "ValorNuevo",
					CURRENT_UTCTIMESTAMP AS "FechaSolicitud",--SE CREAN INTERNOS
					CURRENT_UTCTIMESTAMP AS "FechaRevision",--SE CREAN INTERNOS
					v_estadoAutorizacion AS "IdEstadoAutorizacion"
			FROM :v_cuentaCustodioPortafolio DOC,
							(SELECT COL."DATA_TYPE_NAME",
									COL."COLUMN_NAME",
									CAM."CampoModificado",
									CAM."ValorNuevo"
							FROM SYS.TABLE_COLUMNS COL
							INNER JOIN :p_RegistroCambio CAM ON (COL."COLUMN_NAME"= CAM."CampoModificado")
							WHERE COL."TABLE_NAME"='BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio' AND COL.SCHEMA_NAME='BSCL') MOD;

				
			-- 2) ENVIO A SERVICIO CREACION SOLICITUD DE CAMBIO

			CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
			(
				:V_RegistroSolicitudCambio,
				v_out_IdSecuencias
				
			);
						
			-- 3) SI NECESITA AUTORIZACION, SE CREA EL EVENTO; EN CASO CONTRARIO, SE ACTUALIZA EL REGISTRO	
					
			IF v_RequiereAutorizacion = 'S' THEN --LA REALIDAD ES 'S'
				DECLARE v_out_AuthAdvertenciasContador 	INT;
				DECLARE v_out_AuthErroresContador 		INT;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
	
				P_OUT_REFID:= v_secuencia_IdSolicitudCambio;--ELIMINAR, OBNEER ID DEL PORTAFFOLIO MODIFICADO.
				
				SELECT "IdCliente", 'MODIFICACION CUENTA CUSTODIO: ' || ccp."Codigo" || '/' || ccp."IdBoveda" || '/' || ccp."IdEstado" 
				INTO v_IdEntidadBase,  v_Glosa
				FROM "BSCL.Portafolio.BD::POR.Portafolio" p
				INNER JOIN  :v_cuentaCustodioPortafolio ccp on ccp."IdPortafolio" = p."IdPortafolio";

				CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
						p_IdEmpresa => p_IdEmpresa,
						p_IdUsuario => p_IdUsuario,
						p_IdModulo => p_IdModulo,
						p_UsaReglasNegocio => 'N',								
						p_IdTipoEvento => v_IdConcepto,--'MODDOC',
						p_IdModuloSolicita => p_IdModulo,
						p_IdUsuarioSolicita => p_IdUsuario,
						p_Fecha => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'), 
						p_IdConcepto => v_IdConcepto,
						p_NumeroOperacionConcepto =>  v_secuencia_IdSolicitudCambio,
						p_FechaOperacionConcepto => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
						p_IdCliente => v_IdEntidadBase, --v_IdCliente,
						p_Glosa => v_Glosa,
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
				 
				 p_out_idAuth := v_out_idAuth; 
						                                         
				 p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
				                        From :v_out_AuthErroresDetalle Union All 
				                        Select "Codigo", "Descripcion" 
				                        From :p_out_ErroresDetalle; 
				IF (p_out_ErroresContador > 0) THEN
					SIGNAL SALIDA_CONTROLADA;		
				ELSE
					--Mensaje de Salida
					p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
					v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
					p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
											     select * from :p_out_AdvertenciasDetalle;
					SIGNAL SALIDA_CONTROLADA;
				END IF;		
						
			ELSE
				v_solicitudes_cambio = SELECT v_secuencia_IdSolicitudCambio AS "IdSecuenciaCambio" FROM DUMMY WHERE 1=1;
				
				CALL  "BSCL.Portafolio.BD.Servicios::ConfirmarModificarCuentaCustodio" (
					:p_IdEmpresa,
					:p_IdUsuario,
					:p_IdModulo,
					:p_UsaReglasNegocio,
					:v_solicitudes_cambio,
					'ING',
					p_out_AdvertenciasContador,
					p_out_AdvertenciasDetalle,	
					p_out_ErroresContador,
					p_out_ErroresDetalle
					);
					                   
				P_OUT_REFID:= v_secuencia_IdSolicitudCambio;--ELIMINAR, OBNEER ID DEL PORTAFFOLIO MODIFICADO.
				
				--PRONETIT
						
				v_IdEntidad := :p_IdCuentaCustodioPortafolio;
						
				SELECT COUNT("IdPortafolio") INTO v_countIdPortafolio
				FROM "BSCL.Portafolio.BD::POR.Portafolio"
				WHERE "IdPortafolio" = P_OUT_REFID
				AND "Proposito" = 'PROPOSITOAPV';
				
				IF(v_countIdPortafolio > 0)THEN
					CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
						:p_IdEmpresa,
						:p_IdUsuario,
						:p_IdModulo,
						:p_UsaReglasNegocio,
						P_OUT_REFID, 
						v_IdEntidad,
						v_TipoEntidadReplica, 
						v_TipoOperacion,
						v_out_AdvertenciasContador,
						v_out_AdvertenciasDetalle,
						v_out_ErroresContador,
						v_out_ErroresDetalle
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
		                
		            IF	p_out_ErroresContador > 0 then
						SIGNAL SALIDA_CONTROLADA;
				  	END if;
	            END IF;   

			END IF;
			
			--		5405
			IF p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;

		END;
	END;	
END;
