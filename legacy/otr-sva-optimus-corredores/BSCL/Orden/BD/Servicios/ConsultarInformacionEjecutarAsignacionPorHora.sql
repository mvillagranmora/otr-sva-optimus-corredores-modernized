SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::ConsultarInformacionEjecutarAsignacionPorHora";
GO
CREATE PROCEDURE
-- 
--  Proposito: asignacion por hora, cronologica o proporcional
--  Autor: TEAM ORDENES
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Orden.BD.Servicios::ConsultarInformacionEjecutarAsignacionPorHora" (

	IN p_IdEmpresa          			BIGINT,
    IN p_HoraHasta						NVARCHAR(10),
	IN p_IdLibroOrdenes					BIGINT DEFAULT NULL,
	IN p_IdClasificacionTipoInstrumento	NVARCHAR(3) DEFAULT NULL,
	IN p_IdCondicionDeLiquidacion		NVARCHAR(2) DEFAULT NULL,
	IN p_IdTipoInstrumento				NVARCHAR(3) DEFAULT NULL,
	IN p_IdAccionOrden					NVARCHAR(40) DEFAULT NULL,
	IN p_IdBolsa						NVARCHAR(3) DEFAULT NULL,		
	IN p_IdInstrumento					BIGINT DEFAULT NULL,
	IN p_IdMonedaNegociacion			NVARCHAR(3) DEFAULT NULL,

	
	OUT p_out_Transacciones		TABLE(
									"IdTransaccion"							BIGINT,
									"CantidadDisponibleTransaccion" 		DECIMAL(29,8),
									"ValorRescateDisponibleTransaccion"		DECIMAL(29,8),
									"PrecioTransaccion"						DECIMAL(29,8),
									"FechaOperacionTransaccion"				SECONDDATE,
									"IdLibroOrdenes" 						BIGINT,
									"IdClasificacionTipoInstrumento" 		NVARCHAR(3),
									"IdCondicionDeLiquidacion"		 		NVARCHAR(2),
									"IdTipoInstrumento" 					NVARCHAR(3),
									"AccionOrden" 							NVARCHAR(40),
									"IdBolsa" 								NVARCHAR(3),
									"FolioTransaccion"						NVARCHAR(40),
									"IdMonedaInstrumento"					NVARCHAR(3),
									"NombreLibroOrdenes" 					NVARCHAR(100),
									"Instrumento"		 					NVARCHAR(100),
									"IdInstrumento"							BIGINT
									
								),
    OUT p_out_Ordenes         TABLE(
    								"IdOrden"								BIGINT,
									"FechaOperacionOrden"					SECONDDATE,
									"IdLibroOrdenes" 						BIGINT,
									"IdClasificacionTipoInstrumento" 		NVARCHAR(3),
									"IdCondicionDeLiquidacion"		 		NVARCHAR(2),
									"IdTipoInstrumento" 					NVARCHAR(3),
									"AccionOrden" 							NVARCHAR(40),
									"IdBolsa" 								NVARCHAR(3),
									"TipoPrecio"							NVARCHAR(40),
									"CantidadDisponibleOrden" 				DECIMAL(29,8),
									"PrecioOrden"				 			DECIMAL(29,8),
									"NombreLibroOrdenes" 					NVARCHAR(100),
									"IdMonedaOrden"							NVARCHAR(3),
									"Instrumento"		 					NVARCHAR(100),
									"IdInstrumento"							BIGINT
    								)
	 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
		DECLARE v_FechaSistema				NVARCHAR(10);
		DECLARE v_FechaOperacionDesde		SECONDDATE;
		DECLARE v_FechaOperacionHasta		SECONDDATE;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		v_FechaOperacionDesde = TO_SECONDDATE(CONCAT (:v_FechaSistema, CONCAT(' ','00:00:00')));
		v_FechaOperacionHasta = TO_SECONDDATE(CONCAT (:v_FechaSistema, CONCAT(' ',:p_HoraHasta)));
		
		p_out_Transacciones = SELECT "IdTransaccion",
								"CantidadDisponibleTransaccion","ValorRescateDisponibleTransaccion","PrecioTransaccion",
								"FechaOperacionTransaccion","IdLibroOrdenes","IdClasificacionTipoInstrumento","IdCondicionDeLiquidacion",
								"IdTipoInstrumento","AccionOrden","IdBolsa","FolioTransaccion","IdMonedaInstrumento",
								"NombreLibroOrdenes","Instrumento", "IdInstrumento"
								FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionPorAsignar"
								WHERE "FechaOperacionTransaccion" >= :v_FechaOperacionDesde AND "FechaOperacionTransaccion" <= :v_FechaOperacionHasta
								AND "IdLibroOrdenes" = COALESCE(:p_IdLibroOrdenes,"IdLibroOrdenes")
								AND "IdClasificacionTipoInstrumento" = COALESCE(:p_IdClasificacionTipoInstrumento,"IdClasificacionTipoInstrumento")
								AND "IdTipoInstrumento" = COALESCE(:p_IdTipoInstrumento,"IdTipoInstrumento")
								AND "AccionOrden" = COALESCE(:p_IdAccionOrden,"AccionOrden")
								AND "IdCondicionDeLiquidacion" = COALESCE(:p_IdCondicionDeLiquidacion,"IdCondicionDeLiquidacion")
								AND "IdBolsa" = COALESCE(:p_IdBolsa,"IdBolsa")
								AND "IdInstrumento" = COALESCE(:p_IdInstrumento, "IdInstrumento")
								AND "IdMonedaInstrumento" = COALESCE(:p_IdMonedaNegociacion, "IdMonedaInstrumento")
								AND "IdTipoInstrumento" != 'SIM'
								AND "IdClasificacionTipoInstrumento" IN ('RV','RF','DV');
		
		p_out_Ordenes = SELECT "IdOrden",
						"FechaOperacionOrden","IdLibroOrdenes","IdClasificacionTipoInstrumento","IdCondicionDeLiquidacion","IdTipoInstrumento","AccionOrden","IdBolsa","TipoPrecio",
						"CantidadDisponibleOrden","PrecioOrdenOrigen" AS "PrecioOrden","NombreLibroOrdenes","IdMonedaOrden","Instrumento", "IdInstrumento"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrdenPorAsignar"
						WHERE "IdLibroOrdenes" = COALESCE(:p_IdLibroOrdenes,"IdLibroOrdenes")
						AND "IdClasificacionTipoInstrumento" = COALESCE(:p_IdClasificacionTipoInstrumento,"IdClasificacionTipoInstrumento")
						AND "IdTipoInstrumento" = COALESCE(:p_IdTipoInstrumento,"IdTipoInstrumento")
						AND "AccionOrden" = COALESCE(:p_IdAccionOrden,"AccionOrden")
						AND "IdCondicionDeLiquidacion" = COALESCE(:p_IdCondicionDeLiquidacion,"IdCondicionDeLiquidacion")
						AND "IdBolsa" = COALESCE(:p_IdBolsa,"IdBolsa")
						AND "IdInstrumento" = COALESCE(:p_IdInstrumento,"IdInstrumento")
						AND "IdMonedaOrden" = COALESCE(:p_IdMonedaNegociacion,"IdMonedaOrden")
						AND "IdTipoInstrumento" != 'SIM' AND "IdClasificacionTipoInstrumento" != 'RE' AND "EsOrdenEspecial" = 'N';
END
GO