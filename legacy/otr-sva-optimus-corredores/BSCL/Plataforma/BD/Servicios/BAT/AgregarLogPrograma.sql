SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.AgregarLogPrograma";

CREATE PROCEDURE
-- 
--  Proposito: Agrega log por programa
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.AgregarLogPrograma"
 (
	p_IdTipoLog						NVARCHAR(40) , -- Obligatorio Ej: ERROR,INFORMATIVO,PROCESO,ELIMINACION,EDICION
	 p_IdModulo						NVARCHAR(40) , -- Obligatorio
	 p_IdUsuario					BIGINT       , -- Obligatorio
	 p_IdEmpresa					BIGINT       , -- Obligatorio
	 p_FechaSistemaLog				NVARCHAR(10) DEFAULT NULL, -- YYYY-MM-DD a partir de la fecha del sistema.
	 p_IdUsuarioAtendido			BIGINT       , -- Obligatorio
	 p_IdCliente					BIGINT DEFAULT 0		 , --Opcional
	 p_EstadoCliente				NVARCHAR(3) DEFAULT ''	 , --Opcional
	 p_NombreCliente				NVARCHAR(100) DEFAULT '' , --Opcional. Corresponde al nombre completo, sea persona natural o juridica.
	 p_IdTipoDocumentoIdentidad		NVARCHAR(40) DEFAULT ''  , --Opcional
	 p_IdDocumentoIdentidad			NVARCHAR(40) DEFAULT ''	 , --Opcional
	 p_IdPortafolio 				BIGINT DEFAULT 0		 , --Opcional	
	 p_EstadoPortafolio				NVARCHAR(3) DEFAULT ''   , --Opcional
	 p_IdTipoRegistro				NVARCHAR(40) 			 , -- Obligatorio Ej: ORDEN, ASIGNACION, MOVIMIENTOFACTURACION
	 p_IdRegistro					NVARCHAR(40) DEFAULT ''	 , -- Ej: IdOrden, IdAsignacion, IdMovimientoFacturar, IdComprobanteFacturacion, etc
	 p_EstadoRegistro				NVARCHAR(3) DEFAULT ''   , --Opcional
	 p_IdTipoOperacion				NVARCHAR(40) DEFAULT ''  , --Opcional
	 p_Folio						BIGINT DEFAULT 0		 , --Opcional
	 p_Cantidad						DECIMAL(26,8) DEFAULT 0		 , --Opcional
	 p_PrecioTasa					DECIMAL(26,8) DEFAULT 0		 , --Opcional
	 p_Monto						DECIMAL(26,8) DEFAULT 0		 , --Opcional
	 p_IdMonedaOrigen				NVARCHAR(3) DEFAULT ''	 , --Opcional
	 p_IdMonedaLiquidacion			NVARCHAR(3) DEFAULT ''	 , --Opcional
	 p_TipoCambio					NVARCHAR(40) DEFAULT ''	 , --Opcional
	 p_Nemo							NVARCHAR(260) DEFAULT '' , --Opcional
	 p_IdProceso					BIGINT DEFAULT 0		 , --Opcional
	 p_EstadoProceso				NVARCHAR(3) DEFAULT ''   , --Opcional
	 p_NombreProceso				NVARCHAR(100) DEFAULT '' , --Opcional
	 p_IdTipoPrograma				NVARCHAR(40)  DEFAULT ''  --Opcional							
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
		DECLARE v_FechaSistema NVARCHAR(10);
		IF (p_FechaSistemaLog IS NULL) THEN
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		ELSE
			v_FechaSistema = p_FechaSistemaLog;
		END IF;
		
 		v_detalleLog = select '' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
	 		p_IdTipoLog 					=>	:p_IdTipoLog,
	 		p_IdModulo						=>	:p_IdModulo,
	 		p_IdUsuario						=>	:p_IdUsuario,
	 		p_IdEmpresa						=>	:p_IdEmpresa,
	 		p_FechaSistemaLog				=>	:v_FechaSistema,
	 		p_IdUsuarioAtendido				=>	:p_IdUsuarioAtendido,
	 		p_IdCliente						=>	:p_IdCliente,
	 		p_EstadoCliente					=>	:p_EstadoCliente,
	 		p_NombreCliente					=>	:p_NombreCliente,
	 		p_IdTipoDocumentoIdentidad		=>	:p_IdTipoDocumentoIdentidad,
	 		p_IdDocumentoIdentidad			=>	:p_IdDocumentoIdentidad,
		 	p_IdPortafolio 					=>	:p_IdPortafolio,
	 		p_EstadoPortafolio				=>	:p_EstadoPortafolio,
	 		p_IdTipoRegistro				=>	:p_IdTipoRegistro,
	 		p_IdRegistro					=>	:p_IdRegistro,
	 		p_EstadoRegistro				=>	:p_EstadoRegistro,
			p_IdTipoOperacion				=>	:p_IdTipoOperacion,
			p_Folio							=>	:p_Folio,
			p_Cantidad						=>	:p_Cantidad,
			p_PrecioTasa					=>	:p_PrecioTasa,
			p_Monto							=>	:p_Monto,
			p_IdMonedaOrigen				=>	:p_IdMonedaOrigen,
			p_IdMonedaLiquidacion			=>	:p_IdMonedaLiquidacion,
			p_TipoCambio					=>	:p_TipoCambio,
			p_Nemo							=>	:p_Nemo,
			p_IdProceso						=>	:p_IdProceso,
			p_EstadoProceso					=>	:p_EstadoProceso,
			p_NombreProceso					=>	:p_NombreProceso,
			p_IdTipoPrograma				=>	:p_IdTipoPrograma,
			p_DetalleLog					=>	:v_detalleLog
			);


 END;


 
