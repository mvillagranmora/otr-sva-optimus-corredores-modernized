SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Orden.BD.Servicios::ConfirmarAutorizacionOrdenes";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la orden.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 13-06-2016
--  
"BSCL.Orden.BD.Servicios::ConfirmarAutorizacionOrdenes" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	--ID del Modulo que lo invoca
		  
	IN 	p_Ordenes						TABLE ("IdOrden"	BIGINT)	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_FechaOperacionSecondate SECONDDATE;
	DECLARE v_count BIGINT;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaOperacionSecondate);
	
	--El campo IdEstadoInterno queda AUT
	UPDATE ORD
	SET "IdEstadoInterno" = 'AUT'
	FROM "BSCL.Orden.BD::ORD.Orden" ORD
	INNER JOIN :p_Ordenes pOr
		ON (ORD."IdOrden" = pOr."IdOrden" AND "IdEstadoInterno" = 'PDA');
	
	--v_Ordenes = SELECT row_number() over (order by "IdOrden") AS "IdInterno","IdOrden" FROM :p_Ordenes;
	
	--Solo las ordenes donde el IdEstadoLimite es AUT pueden cambiar de Estado
	v_Ordenes =
		SELECT row_number() over (order by pOr."IdOrden") AS "IdInterno", pOr."IdOrden"
		FROM :p_Ordenes pOr
		INNER JOIN "BSCL.Orden.BD::ORD.Orden" ORD
			ON (pOr."IdOrden" = ORD."IdOrden")
		WHERE ORD."IdEstadoLimite" = 'AUT';
	
	SELECT COUNT(1) INTO v_count FROM :v_Ordenes;
	
	IF :v_count > 0 THEN
		CALL "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
	 		p_Ordenes  			=> :v_Ordenes,
	    	p_NuevoEstado  		=> 'ING',
	    	p_out_SalidaDetalle	=> v_out_Resultado,
	    	p_out_SalidaResumen	=> v_out_SalidaResumen
		);
		
		v_res_ok = SELECT RS."Resultado",RS."IdInterno",ORD."IdOrden",TBO."Nemotecnico"
				   FROM :v_out_Resultado AS RS
				   LEFT JOIN :v_Ordenes AS ORD ON ORD."IdInterno" = RS."IdInterno"
				   LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS TBO ON TBO."IdOrden"=ORD."IdOrden"
				   WHERE RS."Resultado" ='OK';
				   
		
	 
	    -- LANZA NOTIFICACION EN PANTALLA
		INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"
		("IdEmpresa","FechaRegistro","Evento","DATA","EsProcesado")
		SELECT
		:p_IdEmpresa,
		CURRENT_UTCTIMESTAMP,
		'AVISO',
		CONCAT('INGRESO ORDEN'||BINTOSTR( HEXTOBIN('0D0A')),CONCAT(CONCAT('Nemotecnico :',"Nemotecnico"||BINTOSTR( HEXTOBIN('0D0A'))),CONCAT('Id Orden :',"IdOrden"||BINTOSTR( HEXTOBIN('0D0A'))))),
		'N'  
		from :v_res_ok;
		
		
		
		
	
		
		--CONCAT(CONCAT('Nemotecnico :',"Nemotecnico"||BINTOSTR( HEXTOBIN('0D0A'))),CONCAT('IdOrden :',"IdOrden")),
		
		
		-- INSERTA TAG PARA REFRESCAR GRILLA DE NEGOCIACION DE ORDENES
		INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"
		("IdEmpresa","FechaRegistro","Evento","DATA","EsProcesado")
		SELECT
		:p_IdEmpresa,
		CURRENT_UTCTIMESTAMP,
		'CREAR_ORDEN',
		'',
		'N'  
		from :v_res_ok;
		
		
		UPDATE ORD
		SET "FechaAceptacion" = :v_FechaOperacionSecondate,"FechaAutorizacion" = :v_FechaOperacionSecondate
		FROM "BSCL.Orden.BD::ORD.Orden" ORD
		INNER JOIN :v_out_SalidaResumen RES on RES."IdOperacion" = ORD."IdOrden" AND RES."Resultado" = 'OK';
		
		v_OrdenesRuteo = SELECT "IdOrden","IdBolsa" as "IdBolsaDistribucion" 
							FROM "BSCL.Orden.BD::ORD.Orden" ORD 
							INNER JOIN :v_out_SalidaResumen RES on ORD."IdOrden" = RES."IdInterno"
							WHERE ORD."IdTipoCanalDistribucion" != 'MAN' AND RES."Resultado" = 'OK';
				
		CALL "BSCL.Orden.BD.Servicios::RutearOrdenes" (:p_IdEmpresa,:v_OrdenesRuteo,v_out_SalidaDetalleRutearOrdenes,v_out_SalidaResumenRutearOrdenes);
		
		p_DetalleLog = SELECT p_ord."IdOrden" AS "IdCabeceraLog",'' as "SubRegistro", 'IdEstadoOrden' as "CampoRegistro", ord."IdEstadoOrden" as "ValorAntiguo", 'ING' as "ValorNuevo" 
						FROM :p_Ordenes p_ord
						INNER JOIN :v_out_SalidaResumen res on p_ord."IdOrden" = res."IdOperacion" and "Resultado" = 'OK'
						INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord on p_ord."IdOrden" = ord."IdOrden";
		
		v_log = SELECT	p_ord."IdOrden" as "IdCabeceraLog",
							'INGRESO' AS "IdTipoLog",
							'ORD' as "IdModulo",
					 		:p_IdUsuario as "IdUsuario",
					 		:p_IdEmpresa as 	"IdEmpresa",
					 		TO_NVARCHAR("FechaOperacion", 'YYYY-MM-DD') as	"FechaSistemaLog",
					 		:p_IdUsuario as "IdUsuarioAtendido",
					 		0 AS "IdPersona",
					 		cli."IdCliente",
							cli."IdEstado" as "EstadoCliente",
							cli."NombreCompleto" as "NombreCliente",
							cli."TipoDocumentoIdentidad" as "IdTipoDocumentoIdentidad",
							cli."DocumentoIdentidad" as "IdDocumentoIdentidad",
							"IdPortafolioCliente" as "IdPortafolio",
							port."IdEstado" as "EstadoPortafolio",
							'ORDEN' as "IdTipoRegistro", 
							p_ord."IdOrden" as "IdRegistro",
							"IdEstadoOrden" as "EstadoRegistro",
							"IdTipoOperacion",
							p_ord."IdOrden" as "Folio",
							"Cantidad",
							"PrecioOrden" as "PrecioTasa",
							"MontoOrden" as "Monto",
							"IdMonedaOrden" as "IdMonedaOrigen",
							"IdMonedaLiquidacionOrden" as "IdMonedaLiquidacion",
							"TipoCambio",
							"Nemotecnico" as "Nemo",
							0 as "IdProceso",
							'' as "EstadoProceso",
							'' as "NombreProceso"
						FROM :p_Ordenes p_ord
						INNER JOIN :v_out_SalidaResumen res on p_ord."IdOrden" = res."IdOperacion" and "Resultado" = 'OK'
						INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord on p_ord."IdOrden" = ord."IdOrden"
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on cli."IdCliente" = ord."IdCliente"
						INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port on port."IdPortafolio" = ord."IdPortafolioCliente";
	
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :p_DetalleLog);
	END IF;
	
END;
GO