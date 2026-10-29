SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ConfirmarCrearPreferenciaPago";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion de la preferencia de pago.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 31-03-2016
--  
"BSCL.Portafolio.BD.Servicios::ConfirmarCrearPreferenciaPago" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN 	p_IdPreferenciaPagoPortafolio				BIGINT,	
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
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
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
			DECLARE v_IdPortafolio		BIGINT:=0;
			DECLARE v_IdTipoOperacion	NVARCHAR(6)=	'';
			DECLARE v_IdMoneda			NVARCHAR(3);
			DECLARE v_EsDefecto			NVARCHAR(1);
			DECLARE v_TipoEntidad		NVARCHAR(40):= 'PREFERENCIAPAGO';		
			DECLARE v_IdEntidadOrigen	NVARCHAR(40);
		
			--Variables Logs
			DECLARE v_IdCliente							BIGINT;
			DECLARE v_EstadoCliente						NVARCHAR(3);
			DECLARE	v_NombreCliente						NVARCHAR(100);
			DECLARE	v_IdTipoDocumentoIdentidad			NVARCHAR(40);
			DECLARE	v_IdDocumentoIdentidad				NVARCHAR(40);
			DECLARE v_fechaHoraSistema 					NVARCHAR(40);
			DECLARE v_NombreProceso						NVARCHAR(100):='Ingreso de Preferencia de Pago';
			DECLARE V_subregistro 						NVARCHAR(40):= '';
			DECLARE v_tipoOperacion						NVARCHAR(100):='';
			DECLARE v_medioPago							NVARCHAR(100):='';
			DECLARE v_moneda							NVARCHAR(100):='';
			DECLARE v_porDefecto						NVARCHAR(2):='';
			DECLARE v_estado							NVARCHAR(100):='';
			DECLARE v_numeroCuentaBancaria				NVARCHAR(100):='';
			DECLARE v_numerotarjetaBancaria				NVARCHAR(100):='';
			--------------------
		--Validaciones
		--------------------
	--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Logica Negocio
		--------------------
		BEGIN

			
			v_IdEntidadOrigen:=CAST (p_IdPreferenciaPagoPortafolio AS NVARCHAR(40));
			
			UPDATE "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
			SET "IdEstadoAutorizacion"=p_IdEstadoAutorizacion
			WHERE "IdPreferenciaPagoPortafolio"=p_IdPreferenciaPagoPortafolio;
			
			--EVALUAR SI ES NECESARIO QUITAR LA MARCA POR DEFECTO DE LOS DEMAS 
			SELECT TOP 1 DOC."IdPortafolio" , PP."IdTipoOperacion", PP."IdMoneda", DOC."PorDefecto" INTO v_IdPortafolio, v_IdTipoOperacion, v_IdMoneda, v_EsDefecto
			FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
			INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP ON DOC."IdPreferenciaPago" = PP."IdPreferenciaPago" AND PP."IdEmpresa" = DOC."IdEmpresa" 
			WHERE DOC."IdPreferenciaPagoPortafolio"	= p_IdPreferenciaPagoPortafolio AND DOC."IdEmpresa" = p_IdEmpresa; 
			

			IF v_EsDefecto = 'S' THEN
				--QUITAR MARCA POR DEFECTO A TODOS LOS INDICADORES DEL PORTAFOLIO POR TIPO DE OPERACION Y MONEDA.			
				UPDATE  DOC
				SET
					DOC."PorDefecto" = 'N',
					DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC, "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP
				WHERE DOC."IdPreferenciaPago" 	= PP."IdPreferenciaPago" 
				AND DOC."IdPortafolio" 			= v_IdPortafolio
				AND PP."IdEmpresa" 				= DOC."IdEmpresa" 
				AND PP."IdEmpresa" 				= p_IdEmpresa
				AND PP."IdTipoOperacion" 		= v_IdTipoOperacion
				AND PP."IdMoneda" 				= v_IdMoneda
				AND DOC."IdPreferenciaPagoPortafolio" != p_IdPreferenciaPagoPortafolio;
			END IF;
		
			--LOG DE AUDITORIA (inicio)
			BEGIN 
				
				--DATOS CABECERA LOG
				SELECT  CLI."IdCliente"
					   ,CLI."NombreCompleto"
					   ,CLI."TipoDocumentoIdentidad"
					   ,CLI."DocumentoIdentidad"
					   ,CLI."IdEstado"
				INTO v_IdCliente,
				     v_NombreCliente,
					 v_IdTipoDocumentoIdentidad,
					 v_IdDocumentoIdentidad,
					 v_EstadoCliente					   
				  FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
				  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = POR."IdCliente"
				  WHERE POR."IdPortafolio" = :v_IdPortafolio;
				 
				 --DATOS DETALLE LOG
				SELECT 
					COALESCE("PREPAG"."NombreTipoOperacion",'')							AS "TipoOperacion",
					COALESCE("PREPAG"."NombreMedioDePago",'')							AS "MedioDePago",
					COALESCE("PREPAG"."NombreMoneda",'')								AS "Moneda", 
					COALESCE("PREPAGPORT"."PorDefecto", '')								AS "PorDefecto", 
					COALESCE("EST"."Nombre", '')										AS "Estado",
					COALESCE("CUENBANC"."NumeroCuentaBancaria", '')						AS "NumeroCuentaBancaria",
					COALESCE("TARJBANC"."NumeroTarjetaBancaria", '')					AS "NumeroTarjetaBancaria"
				INTO 
					v_tipoOperacion,
					v_medioPago,
					v_moneda,
					v_porDefecto,
					v_estado,
					v_numeroCuentaBancaria,
					v_numerotarjetaBancaria
					FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" 					AS "PREPAGPORT"
					LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" 		AS "PREPAG"
						ON("PREPAGPORT"."IdPreferenciaPago" = "PREPAG"."IdPreferenciaPago")
					LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" 									AS "EST"							
						ON(COALESCE("PREPAGPORT"."IdEstado", '') = "EST"."IdEstado")
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria"						AS "CUENBANC"
						ON(COALESCE("PREPAGPORT"."IdCuentaBancaria", 0) = "CUENBANC"."IdCuentaBancaria")
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"						AS "TARJBANC"
						ON(COALESCE("PREPAGPORT"."IdTarjetaBancaria", 0) = "TARJBANC"."IdTarjetaBancaria")
					WHERE "PREPAGPORT"."IdPreferenciaPagoPortafolio" = :p_IdPreferenciaPagoPortafolio;

				V_subregistro:= v_TipoEntidad;
			
				p_DetalleLog = (SELECT v_subregistro AS "SubRegistro", 'Operacion' AS "CampoRegistro", '' AS "ValorAntiguo", v_tipoOperacion AS "ValorNuevo"
										FROM dummy
									UNION ALL 
									SELECT v_subregistro AS "SubRegistro", 'MedioDePago' AS "CampoRegistro", '' AS "ValorAntiguo", v_medioPago AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'Moneda' AS "CampoRegistro", '' AS "ValorAntiguo", v_moneda AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'PorDefecto' AS "CampoRegistro", '' AS "ValorAntiguo", v_porDefecto AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'Estado' AS "CampoRegistro", '' AS "ValorAntiguo", v_estado AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'CuentaBancaria' AS "CampoRegistro", '' AS "ValorAntiguo", v_numeroCuentaBancaria AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'TarjetaBancaria' AS "CampoRegistro", '' AS "ValorAntiguo", v_numerotarjetaBancaria AS "ValorNuevo"
										FROM dummy);

				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);

				--GRABAR LOG
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'INGRESO' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdPortafolio => :v_IdPortafolio
					,p_IdTipoRegistro => :v_TipoEntidad--'PREFERENCIAPAGO'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadOrigen
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					,p_EstadoRegistro=> :p_IdEstadoAutorizacion
					,p_EstadoCliente=> :v_EstadoCliente --Cliente
					,p_IdCliente => :v_IdCliente
					,p_NombreCliente=> :v_NombreCliente
					,p_IdTipoDocumentoIdentidad=> :v_IdTipoDocumentoIdentidad
					,p_IdDocumentoIdentidad=> :v_IdDocumentoIdentidad	
					,p_IdTipoOperacion => :v_IdTipoOperacion
				);
	
			END;
			--LOG DE AUDITORIA (Fin)			
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
