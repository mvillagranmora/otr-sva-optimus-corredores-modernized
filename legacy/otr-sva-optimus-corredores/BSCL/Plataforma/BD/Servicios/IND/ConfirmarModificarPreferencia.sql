SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::IND.ConfirmarModificarPreferencia";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a la preferencia de una entidad.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 13-04-2016
--  
"BSCL.Plataforma.BD.Servicios::IND.ConfirmarModificarPreferencia" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	
	
	IN 	p_IdSolicitudCambio						BIGINT,	
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
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log serÃ¡ captado por la capa de XS control)		
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
			DECLARE v_countSolicitud								BIGINT:=0;
			DECLARE v_countAutorizacionesPendientes					BIGINT;
			DECLARE v_IdEstadoAutorizacion_Calculado				NVARCHAR(3);
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'PREFERENCIA';
			DECLARE v_CountIndicadorModificado						BIGINT:=0;
			DECLARE v_CountIndicadorNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			DECLARE v_IdEntidadOrigenAPV							NVARCHAR(40):='';
			DECLARE v_CountProposito								BIGINT:=0;
			DECLARE v_IdPortafolio									NVARCHAR(40):='';
			DECLARE v_IdTipoEntidad         						NVARCHAR(40):='';
			DECLARE v_TipoEntidadAPV								NVARCHAR(40):= 'INDICADORES';
			DECLARE v_CodigoUsuario									NVARCHAR(40):= '';

		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdSolicitudCambio" =p_IdSolicitudCambio;
		
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
		--Logica Negocio
		--------------------
		BEGIN
			

			--ACTUALIZACION DE VALORES (* ENTIDAD EXISTENTE *)
									
			--DATOS MAESTROS DE LA ENTIDAD 
			v_DatosSolicitudCambio = SELECT * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"
			WHERE "IdSolicitudCambio" =p_IdSolicitudCambio;

			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountIndicadorModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountIndicadorModificado > 0) 	 THEN
			
				UPDATE  ATR
				SET
					ATR."ValorIndicador" = MOD."ValorNuevo",
					ATR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
					ATR."IdEstadoAutorizacion" =p_IdEstadoAutorizacion
				FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
						INNER JOIN :v_DatosSolicitudCambio MOD ON (ATR."IdIndicador"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."IdTipoEntidad"			= v_TipoEntidad
					AND MOD."IdSolicitudCambio" 	= p_IdSolicitudCambio
					AND ATR."IdIndicador"			= cast (MOD."IdEntidadOrigen" as int);
				
				
				/* APV REPLICA*/
				
				SELECT TOP 1 "IdEntidadOrigen" 
				  INTO v_IdEntidadOrigenAPV 
				  FROM :v_DatosSolicitudCambio;
				
				SELECT "IdEntidad", "IdTipoEntidad" 
				  INTO v_IdPortafolio, v_IdTipoEntidad 
				  FROM "BSCL.Plataforma.BD::IND.Indicador"
				 WHERE "IdIndicador" = v_IdEntidadOrigenAPV;

				IF(:v_IdTipoEntidad = 'PORTAFOLIO') THEN
				 SELECT COUNT(Portafolio."IdPortafolio") 
				   INTO v_CountProposito
				   FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				  WHERE Portafolio."Proposito" = 'PROPOSITOAPV'
					AND Portafolio."IdPortafolio" = v_IdPortafolio;	
				END IF;

				IF(:v_CountProposito > 0) THEN								
				
					CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
							:p_IdEmpresa,
							:p_IdUsuario,
							:p_IdModulo,
							:p_UsaReglasNegocio,
							v_IdPortafolio, 
							:p_IdSolicitudCambio,
							v_TipoEntidadAPV, 
							'U',
							p_out_AdvertenciasContador,
							p_out_AdvertenciasDetalle,
							p_out_ErroresContador,
							p_out_ErroresDetalle
						);
				
				END IF;
				 
				 /* FIN APV */
				
				/*OWP Permiso usuario indicador */
			
				UPDATE USU
				SET USU."UsuarioClientes" = CASE WHEN ATR."ValorIndicador" = 'S' THEN 'N' ELSE 'S' END
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 
						INNER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" USUCOM ON USUCOM."IdPersona" = CLI."IdPersona"
						INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON USU."IdUsuario" = USUCOM."IdUsuario"
						INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON ATR."IdEntidad" = CLI."IdCliente"
						INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPO ON ATR."IdTipoIndicador" = TIPO."IdTipoIndicador"  AND TIPO."CodigoTipoIndicador" = 'NPA'
						INNER JOIN :v_DatosSolicitudCambio MOD ON (ATR."IdIndicador"= cast (MOD."IdEntidadOrigen" as int)) AND MOD."CampoModificado" = TIPO."IdTipoIndicador" 
				WHERE 
						MOD."IdTipoEntidad"			= v_TipoEntidad
					AND MOD."IdSolicitudCambio" 	= p_IdSolicitudCambio
					AND ATR."IdIndicador"			= cast (MOD."IdEntidadOrigen" as int);
				
				/*FIN OWP*/
				
			
			END IF;	
			
	
			
			--Se termina el flujo con la actualizacion del registro de solicitud.

			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambio" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_IdSolicitudCambio		=>:p_IdSolicitudCambio,
				p_IdEstadoAutorizacion	=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador			=>:p_out_ErroresContador,
				p_out_ErroresDetalle			=>:p_out_ErroresDetalle
			);


			
						
		END;
	
			--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_DetalleLog 						TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE	v_TipoEntidadBase					NVARCHAR(40);
				DECLARE	v_IdEntidadBase						NVARCHAR(40);
				DECLARE v_fechaHoraSistema 					NVARCHAR(40);
				DECLARE v_NombreProceso						NVARCHAR(100):='Confirmar Cambio valor Indicador';
				DECLARE v_IdCliente							BIGINT:=0;
				DECLARE v_EstadoCliente						NVARCHAR(5):='';
				DECLARE	v_NombreCliente						NVARCHAR(100):='';
				DECLARE	v_IdDocumentoIdentidad				NVARCHAR(40):='';
				DECLARE	v_IdTipoDocumentoIdentidad			NVARCHAR(40):='';
				DECLARE v_IdPortafolio						BIGINT:=0;
				DECLARE v_EstadoPortafolio					NVARCHAR(5):='';

				--DATOS CABECERA LOG
				SELECT TOP 1 "IdEntidadOrigen" INTO v_IdEntidadOrigen FROM :v_DatosSolicitudCambio;
				
				SELECT "IdTipoEntidad",
					CAST ("IdEntidad" AS NVARCHAR(40))
				INTO v_TipoEntidadBase,
					v_IdEntidadBase
					FROM "BSCL.Plataforma.BD::IND.Indicador" WHERE "IdIndicador"=cast (v_IdEntidadOrigen AS int);
				
				IF(v_TipoEntidadBase = 'CLIENTE' AND v_IdEntidadBase > 0) THEN
					
					SELECT  
						"IdCliente",									
						COALESCE("NombreCompleto",''),					
						"DocumentoIdentidad",
						"TipoDocumentoIdentidad",
						"IdEstado"
					INTO 
						v_IdCliente,
						v_NombreCliente,
						v_IdDocumentoIdentidad,
						v_IdTipoDocumentoIdentidad,
						v_EstadoCliente
							FROM  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
							WHERE "IdCliente" = :v_IdEntidadBase;
				
				END IF;
			
				IF(v_TipoEntidadBase = 'PORTAFOLIO' AND v_IdEntidadBase > 0) THEN 
					
					v_IdEntidadBase:= COALESCE(v_IdEntidadBase, '');
				
					SELECT 
						"CLI"."IdCliente",									
						COALESCE("CLI"."NombreCompleto",''),					
						"CLI"."DocumentoIdentidad",
						"CLI"."TipoDocumentoIdentidad",
						"CLI"."IdEstado", 
						"PORT"."IdPortafolio",
						"PORT"."IdEstado"
					INTO 
						v_IdCliente,
						v_NombreCliente,
						v_IdDocumentoIdentidad,
						v_IdTipoDocumentoIdentidad,
						v_EstadoCliente,
						v_IdPortafolio,
						v_EstadoPortafolio
							FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "PORT"
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" "CLI"
								ON ("PORT"."IdCliente" = "CLI"."IdCliente")
							WHERE "IdPortafolio" = :v_IdEntidadBase;	
				END IF;
			
				--PREPARAMOS DETALLE LOG
				IF(v_TipoEntidadBase = 'CLIENTE' OR v_TipoEntidadBase = 'PORTAFOLIO') THEN
					
					v_DetalleLog = (SELECT 
										MOD."IdTipoEntidad"||'S'||REPLACE("TIPIND"."GrupoIndicador", 'GRUPO', '')		 		AS "SubRegistro",
										CAST("TIPIND"."NombreTipoIndicador" AS NVARCHAR(255))									AS "CampoRegistro",
										CASE 
											WHEN TRIM(MOD."ValorAntiguo") = '0001-01-01 00:00:00' THEN ''
											ELSE MOD."ValorAntiguo"  
										END 																					AS "ValorAntiguo",
										MOD."ValorNuevo"
											FROM :v_DatosSolicitudCambio MOD
											LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" 						AS "IND"
												ON (MOD."IdEntidadOrigen" = "IND"."IdIndicador")
											INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" 					AS 	"TIPIND"
												ON("IND"."IdTipoIndicador" = "TIPIND"."IdTipoIndicador" AND "IND"."IdTipoEntidad" = "TIPIND"."IdTipoEntidad"
												AND ("TIPIND"."ClaseDato" = 'CHECKBOX' OR "TIPIND"."ClaseDato" = 'NVARCHAR' OR "TIPIND"."ClaseDato" = 'DATE'))
											WHERE MOD."IdSolicitudCambio" = :p_IdSolicitudCambio AND MOD."ValorAntiguo" <>  MOD."ValorNuevo"	
									);
							
					v_DetalleLog = (SELECT * 
											FROM :v_DetalleLog
									UNION ALL 
									SELECT 
										MOD."IdTipoEntidad"	||'S'||REPLACE("TIPIND"."GrupoIndicador", 'GRUPO', '')	 	AS "SubRegistro",
										CAST("TIPIND"."NombreTipoIndicador"AS NVARCHAR(255)) 							AS "CampoRegistro",
										CASE 
											WHEN COALESCE(MOD."ValorAntiguo", '') = CAST(COALESCE("DETAINDIANT"."IdDetalleIndicador", 0) AS NVARCHAR(40)) THEN "DETAINDIANT"."DetalleIndicador" 
											WHEN "TIPIND"."CodigoTipoIndicador" = 'BLOCKCHAIN_PORTAFOLIO_PRESTAMOS' THEN  COALESCE("PORTANT"."Proposito", '')         
											WHEN "TIPIND"."CodigoTipoIndicador" = 'PORTAFOLIO_DERIVADOS' THEN  COALESCE("PORTANT"."Proposito", '')  
											WHEN "TIPIND"."CodigoTipoIndicador" = 'GEN_CODDIR' THEN  COALESCE("DIRFISANT"."NombreLargo", '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'GEN_CODIFRS' THEN COALESCE("CODIFRSANT"."Descripcion", '') 
											WHEN "TIPIND"."CodigoTipoIndicador" = 'FAC_DIRELE' THEN COALESCE("DIRELEANT"."Direccion", '') 
											WHEN "TIPIND"."CodigoTipoIndicador" = 'FAC_DIRFIS' THEN COALESCE("DIRFISANT"."NombreLargo", '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'ORDMAILNTF' THEN COALESCE("DIRELEANT"."Direccion" , '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'ORDLIBRO' THEN COALESCE("LIBORDANT"."NombreLibroOrdenes" , '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'ORDLIBROAGENTE' THEN COALESCE("LIBORDANT"."NombreLibroOrdenes", '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'ORDLIBRORETAIL' THEN COALESCE("LIBORDANT"."NombreLibroOrdenes", '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'NOT_EMAIL' THEN COALESCE("DIRELEANT"."Direccion", '') 
											ELSE MOD."ValorAntiguo"
										END  																			AS "ValorAntiguo", 
										CASE
											WHEN COALESCE(MOD."ValorNuevo", '') = CAST(COALESCE("DETAINDINUE"."IdDetalleIndicador", 0) AS NVARCHAR(40)) THEN "DETAINDINUE"."DetalleIndicador" 
											WHEN "TIPIND"."CodigoTipoIndicador" = 'BLOCKCHAIN_PORTAFOLIO_PRESTAMOS' THEN COALESCE("PORTNUE"."Proposito" , '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'PORTAFOLIO_DERIVADOS' THEN  COALESCE("PORTNUE"."Proposito", '')  
											WHEN "TIPIND"."CodigoTipoIndicador" = 'GEN_CODDIR' THEN COALESCE("DIRFISNUE"."NombreLargo", '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'GEN_CODIFRS' THEN COALESCE("CODIFRSNUE"."Descripcion", '') 
											WHEN "TIPIND"."CodigoTipoIndicador" = 'FAC_DIRELE' THEN COALESCE("DIRELENUE"."Direccion", '') 
											WHEN "TIPIND"."CodigoTipoIndicador" = 'FAC_DIRFIS' THEN COALESCE("DIRFISNUE"."NombreLargo", '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'ORDMAILNTF' THEN COALESCE("DIRELENUE"."Direccion" , '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'ORDLIBRO' THEN COALESCE("LIBORDNUE"."NombreLibroOrdenes" , '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'ORDLIBROAGENTE' THEN COALESCE("LIBORDNUE"."NombreLibroOrdenes", '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'ORDLIBRORETAIL' THEN COALESCE("LIBORDNUE"."NombreLibroOrdenes", '')
											WHEN "TIPIND"."CodigoTipoIndicador" = 'NOT_EMAIL' THEN COALESCE("DIRELENUE"."Direccion", '') 
											ELSE MOD."ValorNuevo"
										END 																			AS "ValorNuevo"
											FROM :v_DatosSolicitudCambio MOD
											LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" "IND"
												ON (MOD."IdEntidadOrigen" = "IND"."IdIndicador")
											INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" "TIPIND"
												ON("IND"."IdTipoIndicador" =  "TIPIND"."IdTipoIndicador" AND "IND"."IdTipoEntidad" = "TIPIND"."IdTipoEntidad" 
												AND "TIPIND"."ClaseDato" = 'COMBO')
											LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" 													AS "DETAINDIANT"
												ON(COALESCE(MOD."CampoModificado", '0') = CAST("DETAINDIANT"."IdTipoIndicador" AS NVARCHAR(40))
												AND COALESCE(MOD."ValorAntiguo", '0') = CAST("DETAINDIANT"."IdDetalleIndicador" AS NVARCHAR(40)))
											LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"													AS "DETAINDINUE"
												ON(COALESCE(MOD."CampoModificado", '0') = CAST("DETAINDINUE"."IdTipoIndicador" AS NVARCHAR(40))
												AND COALESCE(MOD."ValorNuevo", '0') = CAST("DETAINDINUE"."IdDetalleIndicador" AS NVARCHAR(40)))
											--OTRA INFORMACION NO RELACIONADA CON DETALLE INDICADOR
											LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 															AS "PORTANT"
												ON(COALESCE(MOD."ValorAntiguo", '0') =  CAST("PORTANT"."IdPortafolio" AS NVARCHAR(40)))
											LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 															AS "PORTNUE"
												ON(COALESCE(MOD."ValorNuevo", '0') =  CAST("PORTNUE"."IdPortafolio" AS NVARCHAR(40)))
											LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 							AS "DIRFISANT"
												ON(COALESCE(MOD."ValorAntiguo", '0') = CAST("DIRFISANT"."IdDireccionFisica" AS NVARCHAR(40)))
											LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 							AS "DIRFISNUE"
												ON(COALESCE(MOD."ValorNuevo", '0') = CAST("DIRFISNUE"."IdDireccionFisica" AS NVARCHAR(40)))
											LEFT JOIN "BSCL.Plataforma.BD::PLA.CodigoIFRS" 															AS "CODIFRSANT"
												ON(COALESCE(MOD."ValorAntiguo", '') = "CODIFRSANT"."Codigo")
											LEFT JOIN "BSCL.Plataforma.BD::PLA.CodigoIFRS" 															AS "CODIFRSNUE"
												ON(COALESCE(MOD."ValorNuevo", '') = "CODIFRSNUE"."Codigo")
											LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica"												AS "DIRELEANT"
												ON(COALESCE(MOD."ValorAntiguo", '0') = CAST("DIRELEANT"."IdDireccionElectronica" AS NVARCHAR(40)))
											LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica"												AS "DIRELENUE"
												ON(COALESCE(MOD."ValorNuevo", '0') = CAST("DIRELENUE"."IdDireccionElectronica" AS NVARCHAR(40)))
											LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" 															AS "LIBORDANT"
												ON(COALESCE(MOD."ValorAntiguo", '0') = CAST("LIBORDANT"."IdLibroOrdenes"AS NVARCHAR(40)))
											LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" 															AS "LIBORDNUE"
												ON(COALESCE(MOD."ValorNuevo", '0') = CAST("LIBORDNUE"."IdLibroOrdenes"AS NVARCHAR(40)))
											WHERE MOD."IdSolicitudCambio" = :p_IdSolicitudCambio AND MOD."ValorAntiguo" <>  MOD."ValorNuevo"
									);
									
						---ULTMIO CASO TIPO DATO MULTI
						v_DetalleLog = (SELECT * 
											FROM :v_DetalleLog
										UNION ALL 	
										SELECT 
											"TIPORD"."SubRegistro", 
											"TIPORD"."NombreIndicador"														AS "CampoRegistro",
											"DET1ANT"."DetalleIndicador"||'('||REPLACE("TIPORD"."OEANT", 'OE', '')||')'||' '|| 
											"DET2ANT"."DetalleIndicador"||'('||REPLACE("TIPORD"."OMANT", 'OM', '')||')'||' '||
											"DET3ANT"."DetalleIndicador"||'('||REPLACE("TIPORD"."ACANT", 'AC', '')||')'||' '||
											"DET4ANT"."DetalleIndicador"||'('||REPLACE("TIPORD"."OVANT", 'OV', '')||')'||' '||
											"DET5ANT"."DetalleIndicador"||'('||REPLACE("TIPORD"."ODANT", 'OD', '')||')'||' '||
											"DET6ANT"."DetalleIndicador"||'('||REPLACE("TIPORD"."ORANT", 'OR', '')||')'			AS "ValorAntiguo",
											"DET1NUE"."DetalleIndicador"||'('||REPLACE("TIPORD"."OENUE", 'OE', '')||')'||' '|| 
											"DET2NUE"."DetalleIndicador"||'('||REPLACE("TIPORD"."OMNUE", 'OM', '')||')'||' '||
											"DET3NUE"."DetalleIndicador"||'('||REPLACE("TIPORD"."ACNUE", 'AC', '')||')'||' '||
											"DET4NUE"."DetalleIndicador"||'('||REPLACE("TIPORD"."OVNUE", 'OV', '')||')'||' '||
											"DET5NUE"."DetalleIndicador"||'('||REPLACE("TIPORD"."ODNUE", 'OD', '')||')'||' '||
											"DET6NUE"."DetalleIndicador"||'('||REPLACE("TIPORD"."ORNUE", 'OR', '')||')'			AS "ValorNuevo"
											FROM (SELECT
													MOD."IdTipoEntidad"	||'S'||REPLACE("TIPIND"."GrupoIndicador", 'GRUPO', '')	 		AS "SubRegistro",
													"IND"."IdTipoIndicador"   														 	AS "IdTipoIndicador",
													"TIPIND"."NombreTipoIndicador" 														AS "NombreIndicador",
													--valor antiguo
													CASE 
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OE":true'	THEN 'OEVIG'	
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OE":false' THEN 'OEINA'
														ELSE ''
													END 																				AS "OEANT",
													CASE 
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OM":true' 	THEN 'OMVIG'	
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OM":false' THEN 'OMINA'
														ELSE ''
													END 																				AS "OMANT",
													CASE 
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"AC":true' 	THEN 'ACVIG'	
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"AC":false' THEN 'ACINA'
														ELSE ''
													END 																				AS "ACANT",
													CASE 
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OV":true' 	THEN 'OVVIG'	
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OV":false' THEN 'OVINA'
														ELSE ''
													END 																				AS "OVANT",
													CASE 		
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OD":true' 	THEN 'ODVIG'	
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OD":false' THEN 'ODINA'
														ELSE ''
													END 																				AS "ODANT",
													CASE 
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OR":true' 	THEN 'ORVIG'	
														WHEN MOD."ValorAntiguo" LIKE_REGEXPR '"OR":false' THEN 'ORINA'
														ELSE ''
													END 																				AS "ORANT", 
													--valor nuevo
													CASE 
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OE":true'	THEN 'OEVIG'	
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OE":false' THEN 'OEINA'
														ELSE ''
													END 																				AS "OENUE",
													CASE 
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OM":true' 	THEN 'OMVIG'	
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OM":false' THEN 'OMINA'
														ELSE ''
													END 																				AS "OMNUE",
													CASE 
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"AC":true' 	THEN 'ACVIG'	
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"AC":false' THEN 'ACINA'
														ELSE ''
													END 																				AS "ACNUE",
													CASE 
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OV":true' 	THEN 'OVVIG'	
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OV":false' THEN 'OVINA'
														ELSE ''
													END 																				AS "OVNUE",
													CASE 		
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OD":true' 	THEN 'ODVIG'	
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OD":false' THEN 'ODINA'
														ELSE ''
													END 																				AS "ODNUE",
													CASE 
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OR":true' 	THEN 'ORVIG'	
														WHEN MOD."ValorNuevo" LIKE_REGEXPR '"OR":false' THEN 'ORINA'
														ELSE ''
													END 																				AS "ORNUE" 
														FROM :v_DatosSolicitudCambio MOD
														LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" 						AS "IND"
															ON (MOD."IdEntidadOrigen" = "IND"."IdIndicador")
														INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" 					AS 	"TIPIND"
															ON("IND"."IdTipoIndicador" = "TIPIND"."IdTipoIndicador" 
															AND "IND"."IdTipoEntidad" = "TIPIND"."IdTipoEntidad" AND "TIPIND"."ClaseDato" = 'MULTI')
														WHERE MOD."IdSolicitudCambio" = :p_IdSolicitudCambio AND MOD."ValorAntiguo" <>  MOD."ValorNuevo")  AS "TIPORD"
														--valor antiguo y nuevo
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET1ANT"  
															ON ("DET1ANT"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET1ANT"."Codigo" = REPLACE("TIPORD"."OEANT", 'VIG', '') OR "DET1ANT"."Codigo" = REPLACE("TIPORD"."OEANT", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET1NUE"  
															ON ("DET1NUE"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET1NUE"."Codigo" = REPLACE("TIPORD"."OENUE", 'VIG', '') OR "DET1NUE"."Codigo" = REPLACE("TIPORD"."OENUE", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET2ANT"  
															ON ("DET2ANT"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET2ANT"."Codigo" = REPLACE("TIPORD"."OMANT", 'VIG', '') OR "DET2ANT"."Codigo" = REPLACE("TIPORD"."OMANT", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET2NUE"  
															ON ("DET2NUE"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET2NUE"."Codigo" = REPLACE("TIPORD"."OMNUE", 'VIG', '') OR "DET2NUE"."Codigo" = REPLACE("TIPORD"."OMNUE", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET3ANT"  
															ON ("DET3ANT"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET3ANT"."Codigo" = REPLACE("TIPORD"."ACANT", 'VIG', '') OR "DET3ANT"."Codigo" = REPLACE("TIPORD"."ACANT", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET3NUE"  
															ON ("DET3NUE"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET3NUE"."Codigo" = REPLACE("TIPORD"."ACNUE", 'VIG', '') OR "DET3NUE"."Codigo" = REPLACE("TIPORD"."ACNUE", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET4ANT"  
															ON ("DET4ANT"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET4ANT"."Codigo" = REPLACE("TIPORD"."OVANT", 'VIG', '') OR "DET4ANT"."Codigo" = REPLACE("TIPORD"."OVANT", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET4NUE"  
															ON ("DET4NUE"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET4NUE"."Codigo" = REPLACE("TIPORD"."OVNUE", 'VIG', '') OR "DET4NUE"."Codigo" = REPLACE("TIPORD"."OVNUE", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET5ANT"  
															ON ("DET5ANT"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET5ANT"."Codigo" = REPLACE("TIPORD"."ODANT", 'VIG', '') OR "DET5ANT"."Codigo" = REPLACE("TIPORD"."ODANT", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET5NUE"  
															ON ("DET5NUE"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET5NUE"."Codigo" = REPLACE("TIPORD"."ODNUE", 'VIG', '') OR "DET5NUE"."Codigo" = REPLACE("TIPORD"."ODNUE", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET6ANT"  
															ON ("DET6ANT"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET6ANT"."Codigo" = REPLACE("TIPORD"."ORANT", 'VIG', '') OR "DET6ANT"."Codigo" = REPLACE("TIPORD"."ORANT", 'INA', '')))
														LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador"		AS "DET6NUE"  
															ON ("DET6NUE"."IdTipoIndicador" = "TIPORD"."IdTipoIndicador" AND ("DET6NUE"."Codigo" = REPLACE("TIPORD"."ORNUE", 'VIG', '') OR "DET6NUE"."Codigo" = REPLACE("TIPORD"."ORNUE", 'INA', '')))
										);
				ELSE
					v_DetalleLog = (Select 
										MOD."IdTipoEntidad"		 as "SubRegistro",
										MOD."CampoModificado" || '|' || TIPIND."NombreTipoIndicador" as "CampoRegistro",
										MOD."ValorAntiguo"  	 as "ValorAntiguo",
										MOD."ValorNuevo" 		 as "ValorNuevo" 
											FROM :v_DatosSolicitudCambio MOD
											JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPIND ON (TIPIND."IdTipoIndicador" = MOD."CampoModificado")
									);
				END IF;

							
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);		

				--GRABAR LOG
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => v_TipoEntidadBase
					,p_DetalleLog => :v_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadBase
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					,p_EstadoRegistro=> :p_IdEstadoAutorizacion
					,p_IdPortafolio => :v_IdPortafolio
					,p_EstadoPortafolio => :v_EstadoPortafolio
					,p_IdCliente => :v_IdCliente
					,p_NombreCliente => :v_NombreCliente
					,p_IdTipoDocumentoIdentidad => :v_IdTipoDocumentoIdentidad
					,p_IdDocumentoIdentidad => :v_IdDocumentoIdentidad
					,p_EstadoCliente => :v_EstadoCliente				
				);
	
			END;
			--LOG DE AUDITORIA (Fin)			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;