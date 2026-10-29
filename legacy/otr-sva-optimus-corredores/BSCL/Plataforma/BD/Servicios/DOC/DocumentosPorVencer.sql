SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::DOC.DocumentosPorVencer";
GO
CREATE PROCEDURE  "BSCL.Plataforma.BD.Servicios::DOC.DocumentosPorVencer" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  		--ID del Modulo que lo invoca	
		  
	-- Parametros entrada del servicio publico
	    	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
 
	DECLARE v_errorhashmsgtable TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg 		NVARCHAR(520);
	--DECLARE v_SecuenciaEvento	BIGINT;	  
	DECLARE v_out_Resultado 	NVARCHAR(1);
    DECLARE v_out_Mensaje 		NVARCHAR(520);
	
	DECLARE	v_HorasVigencia				DECIMAL(5,2);
	DECLARE	v_EtapasSecuenciales		NVARCHAR(1);	
	DECLARE	V_IdUsuarioSuperAutorizador BIGINT;
	DECLARE	V_IdRolSuperAutorizador    	BIGINT; 
	
	DECLARE v_CodigoError 	INT;
    DECLARE v_MensajeError 	NVARCHAR(1000);	
    DECLARE v_FechaValida	CHAR(1);

	DECLARE v_FechaHoraVigencia 	DATETIME;
	DECLARE v_FechaHoraResgistro	DATETIME;
	DECLARE v_FechaSistema			NVARCHAR(40);
	
	DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));

	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			p_out_ErroresDetalle = SELECT '' AS "Codigo", ::SQL_ERROR_MESSAGE AS "Descripcion" FROM dummy;
			--Error INTerno no manejado  (El log ser� captado por la capa de XS control)		
			--RESIGNAL; v_MensajeError
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		declare v_FechaHoraRegistroVarchar NVARCHAR(40);
		DECLARE v_obtenerHastaFecha BIGINT;
		DECLARE v_vencidasHastaFecha BIGINT;
		DECLARE v_Resultado_Asuntos_Pendientes BIGINT;
		DECLARE temp_table_asuntos_pendiente  TABLE( 
			"Codigo" NVARCHAR(50), 
			"FechaVisibleDesde" SECONDDATE,
			"FechaVisibleHasta" SECONDDATE,
			"IdEmpleado" BIGINT,
			"Observaciones" NVARCHAR(200),
			"TipoEntidad" NVARCHAR(3),
			"IdEntidad" BIGINT,
			"TipoOperacion" NVARCHAR(3),
			"IdOperacion" BIGINT,
			"Glosa" NVARCHAR(200),
			"Monto" BIGINT);
			
		temp_table_asuntos_pendiente = select '' AS "Codigo", '0001-01-01 00:00:00' AS "FechaVisibleDesde", '0001-01-01 00:00:00' AS "FechaVisibleHasta", 0 AS "IdEmpleado", '' AS "Observaciones", '' AS "TipoEntidad", 0 AS "IdEntidad", '' AS "TipoOperacion", 0 AS "IdOperacion", '' AS "Glosa", 0 AS "Monto" FROM DUMMY WHERE 1=2;
		-------------------------
		--Validaciones basicas 
		-------------------------

		--Si existen errores en las validaciones debe salir
		---------------------------------------------------
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
			/* CONTINUA SOLO CON LOS EVENTOS SIN PROBLEMA */
			--p_tablaEventos=SELECT EV.* FROM :p_tablaEventos EV WHERE NOT EXISTS (SELECT 1 FROM  :p_out_ErroresDetalle DET WHERE DET."Codigo"= EV."IdTipoEvento");
		END IF;		
					
		-------------------- 
		--Negocio Aqui
		--------------------
		BEGIN
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa, :v_FechaHoraRegistroVarchar);
            v_FechaHoraResgistro := TO_SECONDDATE(v_FechaHoraRegistroVarchar, 'YYYY-MM-DD HH24:MI:SS');
            
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(:p_IdEmpresa, 'EVT.VENCIMIENTODOCUMENTO', :v_obtenerHastaFecha);
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(:p_IdEmpresa, 'EVT.AVISODOCUMENTOVENCIDO', :v_vencidasHastaFecha);			

			--Borramos los asuntos pendientes asociados a los documentos de identidad
			DELETE FROM "BSCL.Plataforma.BD::PND.DetalleAsuntoPendiente" 
			WHERE "Codigo" = 'DOCIDENTIDADAVENCER'
			OR "Codigo" = 'DOCIDENTIDADVENCIDOS';
			
			--Documentos de Identidad Por Vencer
			temp_table_doc_identidad_por_vencer =
			SELECT
				'DOCIDENTIDADAVENCER' AS "Codigo", 
				DI."FechaVencimiento" AS "FechaVisibleDesde", 
				DI."FechaVencimiento" AS "FechaVisibleHasta", 
				C."IdEmpleadoEjecutivo" AS "IdEmpleado",
				'' AS "Observaciones", 
				'CLI' AS "TipoEntidad", 
				C."IdCliente" AS "IdEntidad", 
				'DOCIDEPV' AS "TipoOperacion", --DAV: Documento a vencer
				0 AS "IdOperacion", 
				REPLACE(REPLACE(REPLACE("FormatoGlosa",'#documento#' , DI."DocumentoIdentidad" || ' ' || TDI."NombreTipoDocumentoIdentidad"), 
				'#cliente#', C."NombreCompleto"), 
				'#fecha#', TO_NVARCHAR(DI."FechaVencimiento", 'DD-MM-YYYY'))  AS "Glosa", 
				0 AS "Monto" 
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" C
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = C."IdEmpleadoEjecutivo"
				INNER JOIN "BSCL.Plataforma.BD::PND.AsuntoPendiente" AP ON AP."Codigo" = 'DOCIDENTIDADAVENCER'
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = C."IdPersona"
				LEFT JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" TDI ON TDI."IdTipoDocumentoIdentidad" = DI."IdTipoDocumentoIdentidad"
				WHERE DI."FechaVencimiento" > TO_DATE('01-01-0001', 'DD-MM-YYYY')
				AND DI."FechaVencimiento" > v_FechaHoraResgistro
				AND DI."IdEstado" = 'ACT';
				
			--Documentos de Identidad Vencidos
			temp_table_doc_identidad_vencidos =
			SELECT
				'DOCIDENTIDADVENCIDOS' AS "Codigo", 
				DI."FechaVencimiento" AS "FechaVisibleDesde", 
				DI."FechaVencimiento" AS "FechaVisibleHasta", 
				C."IdEmpleadoEjecutivo" AS "IdEmpleado",
				'' AS "Observaciones", 
				'CLI' AS "TipoEntidad", 
				C."IdCliente" AS "IdEntidad", 
				'DOCIDEAV' AS "TipoOperacion", --DAV: Documento a vencer
				0 AS "IdOperacion", 
				REPLACE(REPLACE(REPLACE("FormatoGlosa",'#documento#' , DI."DocumentoIdentidad" || ' ' || TDI."NombreTipoDocumentoIdentidad"), 
				'#cliente#', C."NombreCompleto"), 
				'#fecha#', TO_NVARCHAR(DI."FechaVencimiento", 'DD-MM-YYYY'))  AS "Glosa", 
				0 AS "Monto" 
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" C
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = C."IdEmpleadoEjecutivo"
				INNER JOIN "BSCL.Plataforma.BD::PND.AsuntoPendiente" AP ON AP."Codigo" = 'DOCIDENTIDADVENCIDOS'
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = C."IdPersona"
				LEFT JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" TDI ON TDI."IdTipoDocumentoIdentidad" = DI."IdTipoDocumentoIdentidad"
				WHERE DI."FechaVencimiento" > TO_DATE('01-01-0001', 'DD-MM-YYYY') 
				AND DI."FechaVencimiento" < v_FechaHoraResgistro
				AND DI."IdEstado" = 'VCD';
			
			--Documentos Por Vencer
			temp_table_asuntos_pendiente = 
			SELECT
				'DOCAVENCER' AS "Codigo", 
				ADD_DAYS(v_FechaHoraResgistro, AP."DiasVisibleDesde") AS "FechaVisibleDesde", 
				ADD_DAYS(v_FechaHoraResgistro, AP."DiasVisibleHasta") AS "FechaVisibleHasta", 
				C."IdEmpleadoEjecutivo" AS "IdEmpleado",
				'' AS "Observaciones", 
				'CLI' AS "TipoEntidad", 
				C."IdCliente" AS "IdEntidad", 
				'DAV' AS "TipoOperacion", --DAV: Documento a vencer
				TO_NVARCHAR(CA."IdAnexo") AS "IdOperacion", 
				REPLACE(REPLACE(REPLACE("FormatoGlosa",'#documento#' , CA."NombreDocumento"), '#cliente#', C."NombreCompleto"), '#fecha#', TO_NVARCHAR(CA."FechaVigencia", 'DD-MM-YYYY'))  AS "Glosa", 
				0 AS "Monto" 
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" CA
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" C ON C."IdCliente" = CA."IdCliente"
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = C."IdEmpleadoEjecutivo"
				INNER JOIN "BSCL.Plataforma.BD::PND.AsuntoPendiente" AP ON AP."Codigo" = 'DOCAVENCER'
			WHERE DAYS_BETWEEN(v_FechaHoraResgistro, CA."FechaVigencia") <= v_obtenerHastaFecha 
			AND DAYS_BETWEEN(v_FechaHoraResgistro, CA."FechaVigencia") >= 0;
			
			--Documentos Vencidos
			temp_table_asuntos_pendiente = 
			SELECT
				'DOCVENCIDOS' AS "Codigo", 
				ADD_DAYS(v_FechaHoraResgistro, AP."DiasVisibleDesde") AS "FechaVisibleDesde", 
				ADD_DAYS(v_FechaHoraResgistro, AP."DiasVisibleHasta") AS "FechaVisibleHasta", 
				C."IdEmpleadoEjecutivo" AS "IdEmpleado",
				'' AS "Observaciones", 
				'CLI' AS "TipoEntidad", 
				C."IdCliente" AS "IdEntidad", 
				'DVE' AS "TipoOperacion", --DVE: Documento vencido
				TO_NVARCHAR(CA."IdAnexo") AS "IdOperacion", 
				REPLACE(REPLACE(REPLACE("FormatoGlosa",'#documento#' , CA."NombreDocumento"), '#cliente#', C."NombreCompleto"), '#fecha#', TO_NVARCHAR(CA."FechaVigencia", 'DD-MM-YYYY'))  AS "Glosa", 
				0 AS "Monto"
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" CA
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" C ON C."IdCliente" = CA."IdCliente"
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = C."IdEmpleadoEjecutivo"
				INNER JOIN "BSCL.Plataforma.BD::PND.AsuntoPendiente" AP ON AP."Codigo" = 'DOCVENCIDOS'
				LEFT JOIN :temp_table_asuntos_pendiente TTAP  ON TTAP."TipoOperacion" = 'DAV' AND TTAP."IdOperacion" = TO_NVARCHAR(CA."IdAnexo") AND TTAP."TipoEntidad" = 'CLI' AND TTAP."IdEntidad" = C."IdCliente"
			WHERE DAYS_BETWEEN(CA."FechaVigencia", v_FechaHoraResgistro) < v_vencidasHastaFecha 
			AND DAYS_BETWEEN(CA."FechaVigencia", v_FechaHoraResgistro) >= 0 AND COALESCE(TTAP."Codigo",'') = ''
			UNION ALL SELECT * FROM :temp_table_asuntos_pendiente
			UNION ALL SELECT * FROM :temp_table_doc_identidad_por_vencer
			UNION ALL SELECT * FROM :temp_table_doc_identidad_vencidos;
			
			--Eliminar asuntos pendientes ya ingresados y en estado sin completar
			temp_table_asuntos_pendiente = SELECT TTAP.* 
			FROM :temp_table_asuntos_pendiente TTAP
			LEFT JOIN "BSCL.Plataforma.BD::PND.DetalleAsuntoPendiente" DAP ON DAP."TipoOperacion" = TTAP."TipoOperacion" AND DAP."IdOperacion" = TTAP."IdOperacion" AND DAP."TipoEntidad" = TTAP."TipoEntidad" AND DAP."IdEntidad" = TTAP."IdEntidad" AND DAP."IdEstado" = 'SNC'
			WHERE COALESCE(DAP."IdDetalleAsuntoPendiente",0)=0;

			--Crear Asuntos Pendientes
			CALL "BSCL.Plataforma.BD.Servicios::PND.CrearAsuntoPendiente"(p_IdEmpresa , p_IdUsuario, p_IdModulo, '', :temp_table_asuntos_pendiente, v_Resultado_Asuntos_Pendientes, :p_out_AdvertenciasContador, :p_out_AdvertenciasDetalle, :p_out_ErroresContador, :p_out_ErroresDetalle);

			select * from :temp_table_asuntos_pendiente;
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
