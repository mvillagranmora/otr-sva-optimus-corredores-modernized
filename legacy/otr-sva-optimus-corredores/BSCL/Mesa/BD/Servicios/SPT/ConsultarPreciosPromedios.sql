SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPreciosPromedios";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPreciosPromedios"
    ( 
	-- Tabla Entrada
	IN p_FechaOperacion DATE,			   
	--Salida   	--------------------------------------------------

	OUT p_out_preciosPromedios	 		table ( 
												"IdInstrumento"			BIGINT,
												"IdTipoOperacion"		NVARCHAR(6),
												"IdMonedaPrincipal"		NVARCHAR(3),
												"IdMonedaSecundaria"	NVARCHAR(3),
												"PrecioPromedio"		DECIMAL(26,8)
										)	
)
	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS   
  BEGIN	
		--*****************************
		--*CONSULTAR PRECIOS PROMEDIOS*
		--*****************************

		p_out_preciosPromedios = 	
								--CONSULTA COMPRAS INTERBANCARIO
								SELECT 
							 			"IdInstrumento",
										"IdTipoOperacion",
										"IdMonedaPrincipal",
										"IdMonedaSecundaria",
										COALESCE(NULLIF(SUM(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" ELSE "MontoMonedaSecundaria" END), 0) / NULLIF(SUM(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END), 0), 0) AS "PrecioPromedio"
								FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
						 		WHERE ("Estado" = 'VIG'
						 		OR "Estado" = 'PEL'
								OR "Estado" = 'PDA')
							 	AND "Nemotecnico" = 'INTERB'
							 	AND "IdTipoOperacion" = 'COMSPT'
								AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
							 	GROUP BY "IdInstrumento", "IdTipoOperacion","IdMonedaPrincipal", "IdMonedaSecundaria"		
								UNION ALL
								--CONSULTA VENTAS INTERBANCARIO
								SELECT 
							 		"IdInstrumento",
									"IdTipoOperacion",
									"IdMonedaPrincipal",
									"IdMonedaSecundaria",
									COALESCE(NULLIF(SUM(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" ELSE "MontoMonedaSecundaria" END) / SUM(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END), 0), 0) AS "PrecioPromedio"
								FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 	WHERE ("Estado" = 'VIG'
							 	OR "Estado" = 'PEL'
							 	OR "Estado" = 'PDA')
							 	AND "Nemotecnico" = 'INTERB'
							 	AND "IdTipoOperacion" = 'VENSPT'
								AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
								GROUP BY "IdInstrumento", "IdTipoOperacion","IdMonedaPrincipal", "IdMonedaSecundaria"		
								UNION ALL
								--CONSULTA COMPRAS DISTRIBUCION
								SELECT 
							 		"IdInstrumento",
									"IdTipoOperacion",
									"IdMonedaPrincipal",
									"IdMonedaSecundaria",
									COALESCE(NULLIF(SUM("MontoMonedaSecundaria"), 0) / NULLIF(SUM(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END), 0), 0) AS "PromedioCompraDistribucion"
								FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" 
							 	WHERE ("Estado" = 'VIG'
							 	OR "Estado" = 'PEL'
							 	OR "Estado" = 'PDA')
							 	AND "Nemotecnico" = 'DISTRI'
							 	AND "IdTipoOperacion" = 'COMSPT'
								AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
							 	GROUP BY "IdInstrumento", "IdTipoOperacion","IdMonedaPrincipal", "IdMonedaSecundaria"		
								UNION ALL
								--CONSULTA VENTAS DISTRIBUCION
							 	SELECT 
							 		"IdInstrumento",
									"IdTipoOperacion",
									"IdMonedaPrincipal",
									"IdMonedaSecundaria",
									COALESCE(NULLIF(SUM("MontoMonedaSecundaria"), 0) / NULLIF(SUM(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END), 0), 0) AS "PrecioPromedio"
								FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 	WHERE ("Estado" = 'VIG'
							 	OR "Estado" = 'PEL'
							 	OR "Estado" = 'PDA')
							 	AND "Nemotecnico" = 'DISTRI'
							 	AND "IdTipoOperacion" = 'VENSPT'
								AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
								GROUP BY "IdInstrumento", "IdTipoOperacion","IdMonedaPrincipal", "IdMonedaSecundaria"		
								UNION ALL
								--CONSULTA COMPRAS ARBITRAJES INTERBANCARIO
								SELECT
									"IdInstrumento",
									"IdTipoOperacion",
									"IdMonedaPrincipal",
									"IdMonedaSecundaria",
									COALESCE(NULLIF(SUM("MontoMonedaSecundaria"), 0) / NULLIF(SUM("MontoMonedaPrincipal" ), 0), 0) AS "PrecioPromedio"
							 	FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 	WHERE ("Estado" = 'VIG'
							 	OR "Estado" = 'PEL'
							 	OR "Estado" = 'PDA')
							 	AND "IdMonedaPrincipal" != 'USD' 
							 	AND "IdMonedaSecundaria" = 'USD'
							 	AND "Nemotecnico" = 'ARBINT'
							 	AND "IdTipoOperacion" = 'COMSPT'
								AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
								GROUP BY "IdInstrumento", "IdTipoOperacion","IdMonedaPrincipal", "IdMonedaSecundaria"		
								UNION ALL
								--CONSULTA VENTAS ARBITRAJES INTERBANCARIO
								SELECT
									"IdInstrumento",
									"IdTipoOperacion",
									"IdMonedaPrincipal",
									"IdMonedaSecundaria",
									COALESCE(NULLIF(SUM("MontoMonedaSecundaria"), 0) / NULLIF(SUM("MontoMonedaPrincipal"), 0), 0) AS "PrecioPromedio"
							 	FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 	WHERE ("Estado" = 'VIG'
							 	OR "Estado" = 'PEL'
							 	OR "Estado" = 'PDA')
							 	AND "IdMonedaPrincipal" != 'USD' 
							 	AND "IdMonedaSecundaria" = 'USD'
							 	AND "Nemotecnico" = 'ARBINT'
							 	AND "IdTipoOperacion" = 'VENSPT'
								AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
								GROUP BY "IdInstrumento", "IdTipoOperacion","IdMonedaPrincipal", "IdMonedaSecundaria"		
								UNION ALL
								--CONSULTA COMPRAS ARBITRAJES DISTRIBUCION
								SELECT
									"IdInstrumento",
									"IdTipoOperacion",
									"IdMonedaPrincipal",
									"IdMonedaSecundaria",
									COALESCE(NULLIF(SUM("MontoMonedaSecundaria"), 0) / NULLIF(SUM("MontoMonedaPrincipal"), 0), 0) AS "PrecioPromedio"
							 	FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 	WHERE ("Estado" = 'VIG'
							 	OR "Estado" = 'PEL'
							 	OR "Estado" = 'PDA')
							 	AND "IdMonedaPrincipal" != 'USD' 
							 	AND "IdMonedaSecundaria" = 'USD'
							 	AND "Nemotecnico" = 'ARBDIS'
							 	AND "IdTipoOperacion" = 'COMSPT'
								AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
								GROUP BY "IdInstrumento", "IdTipoOperacion","IdMonedaPrincipal", "IdMonedaSecundaria"		
								UNION ALL
								--CONSULTA VENTAS ARBITRAJES DISTRIBUCION
								SELECT
									"IdInstrumento",
									"IdTipoOperacion",
									"IdMonedaPrincipal",
									"IdMonedaSecundaria",
									COALESCE(NULLIF(SUM("MontoMonedaSecundaria"), 0) / NULLIF(SUM("MontoMonedaPrincipal"), 0), 0) AS "PrecioPromedio"
							 	FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 	WHERE ("Estado" = 'VIG'
							 	OR "Estado" = 'PEL'
							 	OR "Estado" = 'PDA')
							 	AND "IdMonedaPrincipal" != 'USD' 
							 	AND "IdMonedaSecundaria" = 'USD'
							 	AND "Nemotecnico" = 'ARBDIS'
							 	AND "IdTipoOperacion" = 'VENSPT'
								AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
								GROUP BY "IdInstrumento", "IdTipoOperacion","IdMonedaPrincipal", "IdMonedaSecundaria";		
END;