SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PND.CrearAsuntoPendiente";
-- 
--  Proposito: Servicio para la creacion de asuntos pendientes
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 02/08/2016
--
GO  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::PND.CrearAsuntoPendiente" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para la creacion del usuario
	IN p_CodigoAsunto				NVARCHAR(50),
	IN p_AsuntosPendientes			TABLE(
									"Codigo"            		NVARCHAR(50),
									"FechaVisibleDesde"       	SECONDDATE,
									"FechaVisibleHasta"     	SECONDDATE,
									"IdEmpleado"          		BIGINT,
								 	"Observaciones" 			NVARCHAR(200),
									"TipoEntidad"           	NVARCHAR(3),
									"IdEntidad"             	BIGINT,
									"TipoOperacion"				NVARCHAR(3),
									"IdOperacion"           	BIGINT,
									"Glosa"                		NVARCHAR(300),
									"Monto"                 	BIGINT
									),
		
	--Parametros de salida
	OUT p_out_Resultado				BIGINT,

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN


	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
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
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE FECHA_CREACION 		SECONDDATE;
		DECLARE CODIGO_NOTIFICACION NVARCHAR(50);
		DECLARE p_DetalleAsunto TABLE("IdDetalleAsuntoPendiente"	BIGINT				,
									   "Codigo"     				NVARCHAR(50)		,
									   "FechaCreacion" 				SECONDDATE			,
									   "FechaVisibleDesde"			SECONDDATE			,
									   "FechaVisibleHasta"			SECONDDATE			,
									   "IdEmpresa"         			BIGINT              , 
									   "IdEmpleado"					BIGINT		        ,
									   "Observaciones"				NVARCHAR(200)		,
									   "TipoEntidad"				NVARCHAR(3)			,
									   "IdEntidad"					BIGINT				,
									   "TipoOperacion"				NVARCHAR(3)			,   
									   "IdOperacion"				BIGINT				,
									   "Glosa"						NVARCHAR(300)		,
									   "Monto"						BIGINT				,
									   "CompletadoAutomatico"		NVARCHAR(1)			,
									   "IdModulo"					NVARCHAR(3)			,
									   "IdEstado"					NVARCHAR(3));
		--DECLARE p_out_IdEvento TABLE("IdEvento" BIGINT);

		DECLARE p_EventoDetalle TABLE("IdSecuencia"  BIGINT,"Llave"	 NVARCHAR(40),"Valor"	NVARCHAR(300));
		DECLARE p_ArchivosAdjuntos TABLE("IdSecuencia"  BIGINT,"ArchivoAdjunto"	 BLOB,"Nombre" NVARCHAR(520),"Contrasenia" NVARCHAR(520));
		DECLARE p_IdDirecciones TABLE ("IdSecuencia" BIGINT, "IdDireccion" BIGINT,"TipoDestinatario" NVARCHAR(6));
		DECLARE p_out_IdEvento TABLE ("IdEvento" BIGINT,"IdSecuencia" BIGINT);
		-----------------------------------------------------
		--Logica de negocios
		-----------------------------------------------------
		--p_out_IdEvento = select 0 as "IdEvento" from DUMMY WHERE 1=2;
		SELECT CURRENT_UTCTIMESTAMP INTO FECHA_CREACION FROM DUMMY;
		
		--Generar IDS para cada nuevo detalle de asunto pendiente.
		p_DetalleAsunto = select 
      	 "BSCL.Plataforma.BD.Secuencias::DetalleAsuntoPendiente".NEXTVAL AS "IdDetalleAsuntoPendiente",
	     "Codigo",
	     FECHA_CREACION AS "FechaCreacion", --FechaCreacion
	     "FechaVisibleDesde",
	     "FechaVisibleHasta",
	     p_IdEmpresa AS "IdEmpresa",
	     "IdEmpleado",
	     '' AS "Observaciones", --Observaciones
	     "TipoEntidad",
	     "IdEntidad",
	     "TipoOperacion",
	     "IdOperacion",
	     "Glosa",
	     "Monto",
	     '' AS "CompletadoAutomatico", --Fue Completado Automaticamente
	     p_IdModulo AS "IdModulo",
	     'SNC' AS "IdEstado"-- Por defecto carga "SIN COMPLETAR"
      	from :p_AsuntosPendientes;
      
				
		INSERT INTO "BSCL.Plataforma.BD::PND.DetalleAsuntoPendiente"
		(
         "IdDetalleAsuntoPendiente",
         "Codigo",
         "FechaCreacion",
         "FechaVisibleDesde",
         "FechaVisibleHasta",
         "IdEmpresa",
         "IdEmpleado",
         "Observaciones",
         "TipoEntidad",
         "IdEntidad",
         "TipoOperacion",
         "IdOperacion",
         "Glosa",
         "Monto",
         "CompletadoAutomatico",
         "IdModulo",
         "IdEstado"
      )
      select 
      	 "IdDetalleAsuntoPendiente",
	     "Codigo",
	     "FechaCreacion",
	     "FechaVisibleDesde",
	     "FechaVisibleHasta",
	     "IdEmpresa",
	     "IdEmpleado",
	     "Observaciones",
	     "TipoEntidad",
	     "IdEntidad",
	     "TipoOperacion",
	     "IdOperacion",
	     "Glosa",
	     "Monto",
	     "CompletadoAutomatico",
	     "IdModulo",
	     "IdEstado"
      from :p_DetalleAsunto;
      
      --Contar cuantos asuntos pendientes se inscribieron.
      select COUNT("Codigo") INTO p_out_Resultado from "BSCL.Plataforma.BD::PND.DetalleAsuntoPendiente" WHERE "FechaCreacion" = FECHA_CREACION;
      
      IF p_out_Resultado > 0 THEN
      	--Preguntar si el Asunto pendiente tiene codigo de notificacion.
		DECLARE CURSOR c_notificaciones FOR
		SELECT "CodigoNotificacion"
		FROM "BSCL.Plataforma.BD::PND.AsuntoPendiente" AP
		INNER JOIN :p_DetalleAsunto DA ON AP."Codigo" = DA."Codigo"
		GROUP BY "CodigoNotificacion";
		
		FOR cur_row as c_notificaciones DO
		    CODIGO_NOTIFICACION := cur_row."CodigoNotificacion";
		    
		    IF CODIGO_NOTIFICACION='NOTICUCLI' OR CODIGO_NOTIFICACION='DOCVENCIDOS' OR CODIGO_NOTIFICACION='DOCAVENCER' THEN
				
		    	p_Eventos = SELECT 
				CODIGO_NOTIFICACION AS "CodigoTipoEvento", 
				DE."IdDetalleAsuntoPendiente" AS "IdSecuencia", 
				DE."FechaCreacion" AS "FechaCreacionSistema", 
				0 AS "IdPersonaRemitente", 
				CE."IdPersona" AS "IdPersonaDestinatario",
				0 AS "IdPortafolio",
				CE."IdDireccionElectronicaPrincipal"
				FROM :p_DetalleAsunto DE
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = DE."IdEmpleado"
				WHERE DE."Codigo" =     CASE  WHEN CODIGO_NOTIFICACION = 'NOTICUCLI' THEN 'CUCLI' ELSE CODIGO_NOTIFICACION  END;
				
				p_IdDirecciones = SELECT 
				E."IdSecuencia",
				E."IdDireccionElectronicaPrincipal" AS "IdDireccion",
				'TO' AS "TipoDestinatario"
				FROM :p_Eventos E;
				
				p_ArchivosAdjuntos = SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
				
				v_info = 
				SELECT
                    E."IdSecuencia",
                    CEMP."NombreCompleto" AS "AGENTE",
                    CCDG."RazonSocial" AS "CORREDORA",
                    CCDG."Telefono" AS "ATENCIONCLIENTE",
                    LC."RutaPublica" AS "LOGOCORREDORA"
                FROM :p_Eventos E
                INNER JOIN :p_DetalleAsunto DE ON E."IdSecuencia" = DE."IdDetalleAsuntoPendiente"
                INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON CEMP."IdEmpleado" = DE."IdEmpleado"
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :p_IdEmpresa
                INNER JOIN   "BSCL.Plataforma.BD::PLA.Imagenes" AS LC  ON  "NombreImagen"='LOGO_CORREDOR' AND (LC."IdEmpresa"=:p_IdEmpresa);
                
                
				p_EventoDetalle = 
				SELECT "IdSecuencia", 'GLOSA' AS "Llave", "Glosa" AS "Valor" 
				FROM :p_Eventos E 
				INNER JOIN :p_DetalleAsunto DE ON E."IdSecuencia" = DE."IdDetalleAsuntoPendiente"
				UNION ALL
				SELECT "IdSecuencia", "Llave", "Valor"
                    FROM (
                        SELECT * ,
                                
                        COALESCE(
                            "1" || "IdSecuencia", 
                            "2" || "AGENTE",
                            "3" || "CORREDORA",
                            "4" || "ATENCIONCLIENTE",
                            "5" || "LOGOCORREDORA"
                        ) AS "Valor",
                                
                        COALESCE(
                            "1" || 'IdSecuencia', 
                            "2" || 'AGENTE',
                            "3" || 'CORREDORA',
                            "4" || 'ATENCIONCLIENTE',
                            "5" || 'LOGOCORREDORA'
                        ) AS "Llave" 
                        FROM :v_info T, "BSCL.Plataforma.BD::PLA.Identidad"
                    )
                WHERE "Valor" IS NOT NULL;
                
				--Guardar eventos
				CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
	 				p_IdEmpresa => p_IdEmpresa,
	 				p_IdUsuario => p_IdUsuario,
	 				p_IdModulo => p_IdModulo,
	 				p_Eventos => :p_Eventos,
	 				p_Valores => :p_EventoDetalle,
	 				p_ArchivosAdjuntos => :p_ArchivosAdjuntos,
	 				p_IdDirecciones => :p_IdDirecciones,
	 				p_out_IdEvento => p_out_IdEvento
	 			);
	 			
			END IF; 
			 
	    END FOR;
	    
      END IF;
      
	END;
	
END;

