SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Orden.BD.Servicios::ConsultarInformacionEnvioFacturacionMasivo";

CREATE PROCEDURE
-- 
--  Proposito: envio a facturacion con filtros
--  Autor: TEAM ORDENES
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Orden.BD.Servicios::ConsultarInformacionEnvioFacturacionMasivo" (

	IN p_IdEmpresa          			BIGINT,
	IN p_FechaHoraHasta					NVARCHAR(19),
	IN p_IdClasificacionTipoInstrumento	NVARCHAR(2) DEFAULT NULL,
	IN p_IdTipoInstrumento				NVARCHAR(3) DEFAULT NULL,
	IN p_IdLibroOrdenes					BIGINT DEFAULT NULL,
	IN p_IdAccionOrden					NVARCHAR(40) DEFAULT NULL,
	IN p_IdInstrumento					BIGINT DEFAULT NULL,
	IN p_IdCondicionDeLiquidacion		NVARCHAR(2) DEFAULT NULL,
	IN p_IdAsignacion					BIGINT DEFAULT NULL,
	IN p_FolioTransaccion				NVARCHAR(40) DEFAULT NULL,
	IN p_IdOrden						BIGINT DEFAULT NULL,
	IN p_IdBolsa						NVARCHAR(3) DEFAULT NULL,
	
	OUT p_out_Asignaciones		TABLE(
									"IdTransaccion"							BIGINT,
									"IdOrden"								BIGINT,
									"IdAsignacion"							BIGINT,
									"FolioTransaccion" 						NVARCHAR(40),
									"CantidadAsignada"						DECIMAL(26,8),
									"ValorRescateAsignado"					DECIMAL(26,8),
									
									
									"IdClasificacionTipoInstrumento" 		NVARCHAR(3),
									"IdTipoInstrumento" 					NVARCHAR(3),
									"AccionOrden" 							NVARCHAR(40),
									"IdBolsa" 								NVARCHAR(3),
									"IdMonedaInstrumento"					NVARCHAR(3),
									"IdMonedaOperacion"						NVARCHAR(3),
									"NombreLibroOrdenes" 					NVARCHAR(100),
									"Instrumento"		 					NVARCHAR(100),
									"EsAPlazo"			 					NVARCHAR(1),
									"EsAPlazoUI"			 				NVARCHAR(100)
								
								)
	 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
		DECLARE v_FechaAsignacionDesde		SECONDDATE;
		DECLARE v_FechaAsignacionHasta		SECONDDATE;
		
		v_FechaAsignacionDesde = TO_SECONDDATE(TO_DATE(:p_FechaHoraHasta) || ' 00:00:00','YYYY-MM-DD HH:MI:SS');
		v_FechaAsignacionHasta = TO_SECONDDATE(:p_FechaHoraHasta);
				
		p_out_Asignaciones =	
		SELECT "IdTransaccion","IdOrden","IdAsignacion","FolioTransaccion","CantidadAsignada","ValorRescateAsignado",
									"IdClasificacionTipoInstrumento",
									"IdTipoInstrumento","AccionOrden","IdBolsa","IdMonedaInstrumento","IdMonedaOperacion","NombreLibroOrdenes","Instrumento","EsAPlazo",
									"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("EsAPlazo") as "EsAPlazoUI"
									
								FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion" ASIG
								WHERE "FechaAsignacion" >= :v_FechaAsignacionDesde AND "FechaAsignacion" <= :v_FechaAsignacionHasta
								AND "IdLibroOrdenes" = COALESCE(:p_IdLibroOrdenes,"IdLibroOrdenes")
								AND "IdClasificacionTipoInstrumento" = COALESCE(:p_IdClasificacionTipoInstrumento,"IdClasificacionTipoInstrumento")
								AND "IdTipoInstrumento" = COALESCE(:p_IdTipoInstrumento,"IdTipoInstrumento")
								AND "IdInstrumento" = COALESCE(:p_IdInstrumento,"IdInstrumento")
								AND "IdCondicionDeLiquidacion" = COALESCE(:p_IdCondicionDeLiquidacion,"IdCondicionDeLiquidacion")
								AND "AccionOrden" = COALESCE(:p_IdAccionOrden,"AccionOrden")
								AND "IdBolsa" = COALESCE(:p_IdBolsa,"IdBolsa")
								AND "IdAsignacion" = COALESCE(:p_IdAsignacion,"IdAsignacion")
								AND "IdOrden" = COALESCE(:p_IdOrden,"IdOrden")
								AND "FolioTransaccion" = COALESCE(:p_FolioTransaccion,"FolioTransaccion")
								AND "IdEstadoAsignacion" = 'NEF'
								;
								
								
								
		
END
