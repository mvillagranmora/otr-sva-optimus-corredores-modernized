SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ConfirmarCrearPerfilProductoDetalle";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion del detalle de un Perfil de Productos.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 29-03-2016
--  
"BSCL.Plataforma.BD.Servicios::COM.ConfirmarCrearPerfilProductoDetalle" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN 	p_IdDetallePerfilProductos					BIGINT,	
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
		DECLARE v_IdEntidadOrigen	NVARCHAR(40);
		DECLARE v_TipoEntidad		NVARCHAR(100):='PERFILPRODUCTODETALLE';
	
		--Variables Logs
		DECLARE v_fechaHoraSistema 					NVARCHAR(40);
		DECLARE v_NombreProceso						NVARCHAR(100):='Ingreso DETALLE PERFIL PRODUCTOS';
		DECLARE v_IdCliente							BIGINT:=0;
		DECLARE v_EstadoCliente						NVARCHAR(5):='';
		DECLARE	v_NombreCliente						NVARCHAR(100):='';
		DECLARE	v_IdDocumentoIdentidad				NVARCHAR(40):='';
		DECLARE	v_IdTipoDocumentoIdentidad			NVARCHAR(40):='';
		DECLARE v_IdEntidadBase						NVARCHAR(40):='0';  ---DEBERIA SER PORTAFOLIO
		DECLARE v_countPortafolio					BIGINT:=0;
		DECLARE v_IdPortafilio						BIGINT:=0;
		DECLARE v_EstadoPortafolio					NVARCHAR(5):='';
		DECLARE v_IdDetalleProducto					BIGINT:=0;
		DECLARE v_subregistro						NVARCHAR(100):='DetallePerfilProducto';
		DECLARE v_TipoProducto						NVARCHAR(100):='';
		DECLARE v_TipoOperacion						NVARCHAR(100):='';
		DECLARE v_TipoCanal							NVARCHAR(100):='';
		DECLARE v_EstadoDetalleProducto				NVARCHAR(100):='';
		DECLARE v_GrupoActivos						NVARCHAR(100):='';
		DECLARE v_PermiteRuteo						NVARCHAR(100):='';
	
			--------------------
		--Validaciones
		--------------------
	--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			
			v_IdEntidadOrigen:=CAST (p_IdDetallePerfilProductos AS NVARCHAR(40));
			
			
			UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
			SET "IdEstadoAutorizacion"=p_IdEstadoAutorizacion
			WHERE "IdDetallePerfilProductos"=p_IdDetallePerfilProductos;
			
			

		END;
		
		--INICIO LOG AUDITORIA
		BEGIN

			--PREPARAR DATOS LOG
			SELECT 
				CASE 
					WHEN "DETAPERPROD"."IdPerfilProductos" LIKE_REGEXPR 'EXT' THEN REPLACE("DETAPERPROD"."IdPerfilProductos", 'EXT_', '')
					ELSE '0'
				END AS "IdEntidadBase",
				"DETAPERPROD"."IdDetallePerfilProductos"					AS "IdDetalle", 
				COALESCE("TIPRODUC"."Nombre", '')							AS "TipoProducto",
				COALESCE("TIPOPE"."Nombre", '')								AS "TipoOperacion",
				COALESCE("TIPCANAL"."Nombre", '')							AS "TipoCanal",
				COALESCE("DETAPERPROD"."IdEstado", '')						AS "Estado",
				COALESCE("DETAPERPROD"."IdGrupoActivos", '')				AS "GrupoActivos",
				COALESCE("DETAPERPROD"."PermiteRuteo", '')					AS "PermiteRuteo" 
			INTO 
				v_IdEntidadBase,
				v_IdDetalleProducto,
				v_TipoProducto,
				v_PermiteRuteo,
				v_TipoOperacion,
				v_TipoCanal,
				v_EstadoDetalleProducto,
				v_GrupoActivos
					FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos" "DETAPERPROD"
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" "TIPRODUC"
						ON(COALESCE("DETAPERPROD"."IdTipoProducto",'') = "TIPRODUC"."Codigo")
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" "TIPOPE"
						ON(COALESCE("DETAPERPROD"."IdTipoOperacion" ,'') = "TIPOPE"."IdTipoOperacion")
					LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" "TIPCANAL"
						ON(COALESCE("DETAPERPROD"."IdCanalIngreso", '') = "TIPCANAL"."IdTipoCanalIngreso")
					WHERE "IdDetallePerfilProductos" = :v_IdEntidadOrigen;

			--CABECERA LOG
			--VAMOS BUSCAR INFORMACION A PORTAFOLIO
				
			IF(v_IdEntidadBase <> '0') THEN 
				
				SELECT count(*) INTO v_countPortafolio FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" WHERE "IdPortafolio" = :v_IdEntidadBase;
			
				IF(v_countPortafolio = 1) THEN
					
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
						v_IdPortafilio,
						v_EstadoPortafolio
						FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "PORT"
						LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" "CLI"
							ON ("PORT"."IdCliente" = "CLI"."IdCliente")
						WHERE "IdPortafolio" = :v_IdEntidadBase;
				
				END IF;
			
			END IF;

			p_DetalleLog = (SELECT v_subregistro AS "SubRegistro", 'TipoPoroducto' AS "CampoRegistro", '' AS "ValorAntiguo", v_TipoProducto AS "ValorNuevo"
										FROM dummy
									UNION ALL 
									SELECT v_subregistro AS "SubRegistro", 'TipoOperacion' AS "CampoRegistro", '' AS "ValorAntiguo", v_TipoOperacion AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'TipoCanal' AS "CampoRegistro", '' AS "ValorAntiguo", v_TipoCanal AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'EstadoProducto' AS "CampoRegistro", '' AS "ValorAntiguo", v_EstadoDetalleProducto AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'PermiteRuteo' AS "CampoRegistro", '' AS "ValorAntiguo", v_PermiteRuteo AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'GrupoActivos' AS "CampoRegistro", '' AS "ValorAntiguo", v_GrupoActivos AS "ValorNuevo"
										FROM dummy);
	
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
		
			--GRABAR LOG
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'MODIFICACION' 
				,p_IdModulo => p_IdModulo
				,p_IdUsuario => p_IdUsuario 
				,p_IdEmpresa => p_IdEmpresa 
				,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
				,p_IdUsuarioAtendido => p_IdUsuario
				,p_IdTipoRegistro => v_TipoEntidad
				,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
				,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
				,p_IdRegistro => :v_IdEntidadOrigen
				,p_IdPortafolio => :v_IdPortafilio
				,p_EstadoPortafolio => :v_EstadoPortafolio
				,p_EstadoProceso => :p_IdEstadoAutorizacion
				,p_IdCliente => :v_IdCliente
				,p_NombreCliente => :v_NombreCliente
				,p_IdTipoDocumentoIdentidad => :v_IdTipoDocumentoIdentidad
				,p_IdDocumentoIdentidad => :v_IdDocumentoIdentidad
				,p_EstadoCliente => :v_EstadoCliente
			);
		
	 	END;
	 	--FIN LOG AUDITORIA
		-------------------
		--FIN SP 
		--------------------
	END;	
END;