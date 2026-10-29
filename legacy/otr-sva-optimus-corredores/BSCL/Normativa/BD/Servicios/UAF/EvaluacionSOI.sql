SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.UAF::EvaluacionSOI";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio que evalua la operacion, para determinar si reqUiere el envio de Notificacion a Cumplimiento. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 27-12-2016
--  
"BSCL.Normativa.BD.Servicios.UAF::EvaluacionSOI" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	IN  p_simularOperacion							NVARCHAR(1) DEFAULT 'N', -- S/N se usa para simular operacion (algunos casos)	  
	IN  p_IngresoMasivo								NVARCHAR(1) DEFAULT 'N', -- S/N determina si es masivo o no el ingreso de la operacion
	-- Parametros entrada del servicio publico

	IN p_tablaOperacion 		TABLE("IdCliente"						BIGINT,
									  "IdPortafolio"					BIGINT,
									  "IdTipoProducto" 					NVARCHAR(4),
									  "IdTipoOperacion"        			NVARCHAR(6),
 									  "IdOperacion" 					BIGINT, 
									  "MontoUtilizado"          		DECIMAL(26,8),
									  "IdMoneda"                		NVARCHAR(3),
									  "IdTipoInstrumento"       		NVARCHAR(3),
									  "IdClasificacionTipoInstrumento" 	NVARCHAR(3),
									  "FechaOperacion"					NVARCHAR(10)--'YYYY-MM-DD'
									  ),
	
	-- Parametros salida del servicio publico

	--OUT p_out_Estado            		NVARCHAR(3),
	OUT p_out_EvaluacionSOI				NVARCHAR(100), --Marca de Evaluacion
	OUT p_out_EvaluacionDetalle	table (		"IdCliente"						BIGINT,
		  								    "IdPortafolio"					BIGINT,
											"IdOperacion" 				 	BIGINT,
											"Estado"            			NVARCHAR(3),
											"NotificacionSOI" 				NVARCHAR(2), --SI/NO
											"TipoEvaluacion" 				NVARCHAR(40),
											"Detalle" 						NVARCHAR(100)
										),
	OUT p_out_EvaluacionResumen	table (	"IdCliente"						BIGINT,
		  								"IdPortafolio"					BIGINT,
										"IdOperacion" 				 	BIGINT,
										"IdEstado"                   	NVARCHAR(3)
									  ),	
	
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
	DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
    

	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
				
			p_out_AdvertenciasContador	:= 0;
			p_out_ErroresContador		:= 0;
			v_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			v_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
		--salida/error/advertencia controlado por este servicio

		select count(1) into p_out_ErroresContador 
		from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :v_out_AdvertenciasDetalle;
		
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
								'' as "CodigoDato",
								'' as "ValorDato" 
								FROM :v_out_ErroresDetalle
								UNION ALL
								SELECT "Codigo" AS "CodigoMensaje",
								'' as "CodigoDato",
								'' as "ValorDato" 
								FROM :v_out_AdvertenciasDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		--v_out_ErroresDetalleMensajes = 	SELECT '' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
								
		p_out_ErroresDetalle = 	SELECT 	errores."Codigo",
									errores."Descripcion"
									--msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT 	adv."Codigo",
									adv."RequiereAutorizacion",
									adv."Descripcion"
									--msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
								
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 									
				 					 	
	END;
	
		

	BEGIN	

		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNormativa 						 		 	NVARCHAR(15):='UAF';
		DECLARE v_EVAL_PLAPRO_UAFSOI,v_NOTIF_PLAPRO_UAFSOI,v_GEN_IDPERSONA_CUMPLIMIENTO1	 	NVARCHAR(520):='';
			
		DECLARE v_TipoEvaluacion,v_NombreDestinatario		 	NVARCHAR(40):='';
		DECLARE v_Detalle										NVARCHAR(100):='';
		DECLARE v_estadoOK,	v_estadoNOTIF,v_estadoNOOK			NVARCHAR(3):='';
		DECLARE v_NotificacionSOI								NVARCHAR(2):='';
		--variables requeridas en la creacion 
		DECLARE	v_Count,v_IdPersona,v_IdPersonaCumplimiento,v_IdDireccionDestinatario		BIGINT:=0;
		DECLARE v_out_error			  			 NVARCHAR(500);
		DECLARE v_FechaIngreso					SECONDDATE;
		---Tabla de registro de evaluaciones en proceso
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

			v_estadoOK			 			:= 'ACP';
			v_estadoNOOK		 			:= 'ADV';
			v_estadoNOTIF					:= 'NOT';
			
			p_out_EvaluacionDetalle=SELECT 
										"IdCliente"  AS "IdCliente",
	  								    "IdPortafolio" AS "IdPortafolio",
										"IdOperacion"  AS "IdOperacion",
										v_estadoOK AS "Estado",
										'N' AS "NotificacionSOI",
										'' AS "TipoEvaluacion",
										'' AS "Detalle"
									FROM :p_tablaOperacion;

			
			
			p_out_EvaluacionResumen=SELECT 
									"IdCliente"  AS "IdCliente",
		  							"IdPortafolio"  AS "IdPortafolio",
									"IdOperacion"  AS "IdOperacion",
									v_estadoOK AS "IdEstado"
									FROM :p_tablaOperacion;
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			----Parametro de Evaluacion
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='EVAL_PLAPRO_UAFSOI' AND "IdNormativa"=v_IdNormativa;
			IF v_Count>0 THEN
				SELECT "Valor" INTO v_EVAL_PLAPRO_UAFSOI
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='EVAL_PLAPRO_UAFSOI' AND "IdNormativa"=v_IdNormativa;
				ELSE
					v_EVAL_PLAPRO_UAFSOI:='N';
			END IF;
	

				---------------------------------------------------
				--Parametro de Evaluacion Plantilla Producto' (S/N)
				---------------------------------------------------			

			IF v_EVAL_PLAPRO_UAFSOI ='S' THEN
				v_TipoEvaluacion		 			:= 'PERFIL_PRODUCTO';
				v_Detalle							:= 'Evaluacion Plantilla Producto';
				
				
				----Parametro de Notificacion
					SELECT COUNT(1) INTO v_Count
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
					WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='NOTIF_PLAPRO_UAFSOI' AND "IdNormativa"=v_IdNormativa;
					IF v_Count>0 THEN
						SELECT CAST(TRIM("Valor") AS NVARCHAR(2)) INTO v_NOTIF_PLAPRO_UAFSOI
							FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
						WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='NOTIF_PLAPRO_UAFSOI' AND "IdNormativa"=v_IdNormativa;
						ELSE
							v_NOTIF_PLAPRO_UAFSOI:='N';
					END IF;
				
				
				
				V_PLANTILLA=
					SELECT 
										OPER."IdCliente"    AS "IdCliente",
	  								    OPER."IdPortafolio" AS "IdPortafolio",
										OPER."IdOperacion"  AS "IdOperacion",
										v_estadoOK			AS "Estado",
										'N' 				AS "NotificacionSOI",
										v_TipoEvaluacion 	AS "TipoEvaluacion",
										v_Detalle 			AS "Detalle"
					FROM   :p_tablaOperacion OPER
					INNER JOIN 	"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
					ON (OPER."IdPortafolio"=POR."IdPortafolio")
					WHERE EXISTS 
					 (SELECT 1 FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" PLA
					 WHERE POR."IdPerfilProductos"=PLA."IdPerfilProductos" ---Solo Plantilla BASE
					  AND  PLA."IdTipoProducto"   =OPER."IdTipoProducto" 
					  AND  (PLA."IdTipoOperacion" =OPER."IdTipoOperacion" OR PLA."IdTipoOperacion"='0')--opcion todos
					  AND  PLA."EstadoProducto"='ACT');
				
					SELECT COUNT(1) INTO v_Count FROM :V_PLANTILLA;
					IF v_Count=0 THEN					
					
								p_out_EvaluacionDetalle=
								SELECT 
									"IdCliente"  			AS "IdCliente",
  								    "IdPortafolio" 			AS "IdPortafolio",
									"IdOperacion" 			AS "IdOperacion",
									v_estadoNOOK 			AS "Estado",
									CAST(v_NOTIF_PLAPRO_UAFSOI AS NVARCHAR(2)) 	AS "NotificacionSOI",
									v_TipoEvaluacion		AS "TipoEvaluacion",
									v_Detalle				AS "Detalle"
								FROM :p_tablaOperacion;
					ELSE
								p_out_EvaluacionDetalle=
								 SELECT 
									OPER."IdCliente"  			AS "IdCliente",
  								    OPER."IdPortafolio" 		AS "IdPortafolio",
									OPER."IdOperacion" 			AS "IdOperacion",
									(CASE WHEN COALESCE(PLA."IdPortafolio",0)=0 THEN v_estadoNOOK 		    ELSE	v_estadoOK END ) AS "Estado",
									(CASE WHEN COALESCE(PLA."IdPortafolio",0)=0 THEN CAST(v_NOTIF_PLAPRO_UAFSOI AS NVARCHAR(2))  ELSE	'N'	       END ) AS "NotificacionSOI",
									v_TipoEvaluacion		AS "TipoEvaluacion",
									v_Detalle				AS "Detalle"
								FROM :p_tablaOperacion OPER
								LEFT OUTER JOIN :V_PLANTILLA PLA
								ON ( OPER."IdPortafolio"= PLA."IdPortafolio" AND OPER."IdOperacion" = PLA."IdOperacion");
								
								
									
								p_out_EvaluacionResumen=
								SELECT 
									RES."IdCliente"  	AS "IdCliente",
		  							RES."IdPortafolio"  AS "IdPortafolio",
									RES."IdOperacion"  	AS "IdOperacion",
									v_estadoNOOK 		AS "IdEstado"
								 FROM :p_out_EvaluacionResumen RES
								WHERE EXISTS 
									(SELECT 1 FROM :p_out_EvaluacionDetalle DET 
										WHERE DET."Estado"=v_estadoNOOK
										AND  DET."IdPortafolio" =RES."IdPortafolio"
									)
								UNION ALL	
								SELECT 
									RES."IdCliente"  	AS "IdCliente",
		  							RES."IdPortafolio"  AS "IdPortafolio",
									RES."IdOperacion"  	AS "IdOperacion",
									v_estadoOK 			AS "IdEstado"
								 FROM :p_out_EvaluacionResumen RES
								WHERE NOT EXISTS 
									(SELECT 1 FROM :p_out_EvaluacionDetalle DET 
										WHERE DET."Estado"=v_estadoNOOK
										AND  DET."IdPortafolio" =RES."IdPortafolio"
									);	
								
												
								
					END IF;
					
					SELECT COUNT(1) INTO v_Count FROM :p_out_EvaluacionDetalle DET 
					WHERE DET."Estado"=v_estadoNOOK;
					---INSERCION EN TABLA FISICA DE EVALUACION
					IF v_Count>0 THEN
					
							INSERT INTO "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI"
							 ( "IdEvaluacion",
							   "IdEmpresa",
							   "IdModulo",
							   "IdCliente",
							   "IdPortafolio",
							   "IdTipoProducto",
							   "IdTipoOperacion",
							   "IdOperacion",
							   "MontoUtilizado",
							   "IdMoneda",
							   "IdTipoInstrumento",
							   "IdClasificacionTipoInstrumento",
							   "FechaOperacion",--'YYYY-MM-DD'
							    ---Evaluacion
								"TipoEvaluacion",
								"ConceptoEvaluacion",
								---Auditoria
							   "FechaIngreso",
							   "FechaCreacion",
							   "IdUsuarioCreador")
							SELECT
								"BSCL.Normativa.BD.Secuencias::UAF.IdEvaluacion" .NEXTVAL,
								p_IdEmpresa,
								p_IdModulo,
								DET."IdCliente",
								DET."IdPortafolio",
								OPE."IdTipoProducto",
								OPE."IdTipoOperacion",
								OPE."IdOperacion",
								OPE."MontoUtilizado",
								OPE."IdMoneda",
								OPE."IdTipoInstrumento",
								OPE."IdClasificacionTipoInstrumento",
								OPE."FechaOperacion",
								DET."TipoEvaluacion",
								'TipoProducto, Plantilla',
								v_FechaIngreso,
								CURRENT_UTCTIMESTAMP,
								p_IdUsuario								
							FROM :p_out_EvaluacionDetalle DET 
							INNER JOIN :p_tablaOperacion OPE
							ON (DET."IdOperacion"=OPE."IdOperacion" AND DET."IdPortafolio"=OPE."IdPortafolio")	
							WHERE DET."Estado"=v_estadoNOOK
							AND NOT EXISTS
							(SELECT 1 FROM "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI" SOI
								WHERE   SOI."IdEmpresa"=p_IdEmpresa AND  SOI."IdModulo"=p_IdModulo AND SOI."IdCliente"=DET."IdCliente"
								    AND SOI."IdPortafolio"=DET."IdPortafolio" AND SOI."IdOperacion"=OPE."IdOperacion" AND SOI."IdTipoOperacion"=OPE."IdTipoOperacion" AND SOI."TipoEvaluacion"=v_TipoEvaluacion);  
							  
					END IF;

									
				
					---NOTIFICAR OFICIAL DE CUMPLIMIENTO
					IF v_NOTIF_PLAPRO_UAFSOI ='S' AND p_IngresoMasivo='N' AND v_Count>0 THEN
						
									----Parametro de Oficial de Cumplimiento
								SELECT COUNT(1) INTO v_Count
								FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
								WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='GEN_IDPERSONA_CUMPLIMIENTO1' AND "IdNormativa"='';
								IF v_Count>0 THEN
									SELECT "Valor" INTO v_GEN_IDPERSONA_CUMPLIMIENTO1
										FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
									WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='GEN_IDPERSONA_CUMPLIMIENTO1' AND "IdNormativa"='';
									ELSE
										v_GEN_IDPERSONA_CUMPLIMIENTO1:='0';
								END IF;
								
								v_IdPersonaCumplimiento=CAST(TRIM(v_GEN_IDPERSONA_CUMPLIMIENTO1) AS BIGINT);
								IF v_Count>0 THEN
								
										----NOTIFICACION A CUMPLIMIENTO
										--Generar grupo evento  de Notificar
										v_GrupoEventoNotificacion = SELECT 
												ROW_NUMBER()over ( order by OPE."IdCliente",OPE."IdPortafolio") AS "IdInterno",
												OPE.* 
												,CLI."NombreCompleto"
												,CLI."DocumentoIdentidad"
												,CCDG."RazonSocial" AS "Corredora"
												,CCDG."Telefono" AS "AtencionCliente"
												FROM :p_out_EvaluacionResumen RES
												INNER JOIN :p_tablaOperacion OPE
												ON (RES."IdOperacion"=OPE."IdOperacion" AND RES."IdPortafolio"=OPE."IdPortafolio")	
												INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
												ON (CLI."IdCliente"=OPE."IdCliente")
												INNER JOIN   "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS CCDG ON (CCDG."IdEmpresa"=:p_IdEmpresa)
												WHERE RES."IdEstado"=v_estadoNOOK;

										---Datos Oficial de Cumplimientos
										
											SELECT COUNT(1) INTO v_Count
											FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
												WHERE PER."IdPersona"=v_IdPersonaCumplimiento;
												
										IF v_Count>0 THEN		

													SELECT PER."IdDireccionElectronicaPrincipal",PER."NombreCompleto"
													INTO v_IdDireccionDestinatario,v_NombreDestinatario
													FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
														WHERE PER."IdPersona"=v_IdPersonaCumplimiento;
													
														
														
												v_Eventos = 
													 SELECT 
													CAST('CUMPLIMIENTO_SOI' AS NVARCHAR(40))	AS "CodigoTipoEvento", 
													NOF."IdInterno" 					AS "IdSecuencia", 
													CURRENT_UTCTIMESTAMP 				AS "FechaCreacionSistema", 
													0 									AS "IdPersonaRemitente", 
													v_IdPersonaCumplimiento 			AS "IdPersonaDestinatario", 
													0 									AS "IdPortafolio" 
													FROM  :v_GrupoEventoNotificacion NOF;
																	
													v_IdDirecciones =
													SELECT NOF."IdInterno"					  AS "IdSecuencia",
															v_IdDireccionDestinatario 		  AS "IdDireccion",
														'TO' 								  AS "TipoDestinatario"
													FROM  :v_GrupoEventoNotificacion NOF;
													
													v_ArchivosAdjuntos =
													SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
													
													--Datos del Cliente en mail
																				
												v_EventoDetalle =   						--- DATOS CLIENTE
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'NombreCompleto' AS "Llave",  
													NOF."NombreCompleto" AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													UNION
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'DocumentoIdentidad' AS "Llave",  
													NOF."DocumentoIdentidad" AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													UNION
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'IdCliente' AS "Llave",  
													CAST (NOF."IdCliente" AS  NVARCHAR(100)) AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													UNION 
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'MontoUtilizado' AS "Llave",  
													CAST (NOF."MontoUtilizado" AS  NVARCHAR(100)) AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													UNION
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'Moneda' AS "Llave",  
													CAST (NOF."IdMoneda" AS  NVARCHAR(100)) AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													UNION  
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'IdOperacion' AS "Llave",  
													CAST (NOF."IdOperacion" AS  NVARCHAR(100)) AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													UNION 
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'TipoOperacion' AS "Llave",  
													COALESCE(OPE."Nombre",'') AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE
													ON (NOF."IdTipoOperacion"=OPE."IdTipoOperacion")
													UNION
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'TipoProducto' AS "Llave",  
													COALESCE(PRO."Nombre",'') AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRO
													ON (PRO."IdTipoProducto"=NOF."IdTipoProducto")
													UNION    											      
													SELECT NOF."IdInterno" AS "IdSecuencia",  -----DATOS DESTINATARIO
													'HoraRegistro' AS "Llave",  
													TO_NVARCHAR(CURRENT_UTCTIMESTAMP,'DD-MM-YYYY HH:MI') AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													UNION
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'NombreDestinatario' AS "Llave",  
													v_NombreDestinatario AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF
													UNION
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'CORREDORA' AS "Llave",  
													TRIM(NOF."Corredora") AS "Valor"  
													FROM :v_GrupoEventoNotificacion NOF
													UNION
													SELECT NOF."IdInterno" AS "IdSecuencia", 
													'ATENCIONCLIENTE' AS "Llave", 
													TRIM(NOF."AtencionCliente") AS "Valor" 
													FROM :v_GrupoEventoNotificacion NOF;
													
												
												
													v_out_Agregar_Evento = SELECT 0 AS "IdEvento",0 as "IdSecuencia" FROM DUMMY WHERE 1=2;	
														
										 				CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
										 					p_IdEmpresa => p_IdEmpresa,
										 					p_IdUsuario => p_IdUsuario,
										 					p_IdModulo => p_IdModulo,
										 					p_Eventos => :v_Eventos,
										 					p_Valores => :v_EventoDetalle,
										 					p_ArchivosAdjuntos => :v_ArchivosAdjuntos,
										 					p_IdDirecciones => :v_IdDirecciones,
										 					p_out_IdEvento => :v_out_Agregar_Evento
										 					);
										 		p_out_IdEvento=SELECT "IdEvento"  FROM :v_out_Agregar_Evento;	
								
									END IF; 
								
								END IF; --FIN NOTIFICACION A CUMPLIMIENTO
								
					END IF;
				
		
			END IF;--Fin Evaluacion Plantilla Producto'
			

			SIGNAL SALIDA_CONTROLADA;		
		END;		
					
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
