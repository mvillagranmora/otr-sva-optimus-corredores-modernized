SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::InformeMayorYMenorValorRFIF";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::InformeMayorYMenorValorRFIF"(
	--+ Variables de entrada obligatorias
	 IN p_IdEmpresa						BIGINT
	,IN p_IdUsuario						BIGINT
	,IN p_IdModulo						NVARCHAR(3)
	--+ Enumerar Variabes de entrada
	,IN p_FechaConsulta   				NVARCHAR(10)
	,IN p_IdMercado 					NVARCHAR(3) DEFAULT ''
	,OUT P_OUT_TABLA				TABLE(
		 "IdCartera"						BIGINT
		,"IdTipoMovimiento" 				NVARCHAR(10)
		,"IdTipoInstrumento" 				NVARCHAR(3)
		,"IdMercado" 						NVARCHAR(3)
		,"IdEmpresa"						BIGINT
		,"FolioCartera"						BIGINT
		,"Fecha"							NVARCHAR(10)
		,"DocumentoIdentidad"				NVARCHAR(40)
		,"IdPortafolio"						BIGINT
		,"CodigoPortafolio" 				NVARCHAR(100)
		,"Monto"							DECIMAL(26,8)
		,"IdMoneda"							NVARCHAR(3)
		,"IdTipoReajuste"					NVARCHAR(3)
		,"Estado"							NVARCHAR(3)
		,"Nemotecnico" 						NVARCHAR(40)
		,"IdEmisor" 						NVARCHAR(40)
		,"IdMonedaInstrumento"				NVARCHAR(3)
		,"FechaVencimientoInstrumento" 		DATE
		,"IdInstrumento" 					BIGINT
		,"INGCantidad" 						DECIMAL(26,8)		
		,"INGMontoATasaMercadoAnterior"		DECIMAL(26,8) 
		,"INGMontoATasaMercadoActual"		DECIMAL(26,8)
		,"INGMontoATasaCompraActual"		DECIMAL(26,8)
		,"INGMontoATasaCompraAnterior"		DECIMAL(26,8)
		,"INGDurationMercadoActual" 		DECIMAL(26,8)
		,"INGDurationCompraActual" 			DECIMAL(26,8)
		,"INGTasaCompra" 					DECIMAL(26,8)
		,"INGTasaMercadoActual" 			DECIMAL(26,8)
		,"MayorValorMercado"				DECIMAL(26,8)
		,"MenorValorMercado"				DECIMAL(26,8)
		,"MayorValorCompra"					DECIMAL(26,8)
		,"MenorValorCompra"					DECIMAL(26,8)
	)

)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
						 
	--------------------
	-- LOGICA NEGOCIO
	--------------------
	BEGIN
		DECLARE v_DocumentoIdentidadCorredora NVARCHAR(40) := '';
	  	DECLARE v_Contador 		INT;
	  	DECLARE v_FechaSistema 		NVARCHAR(10);
	  	DECLARE v_IdCliente 		BIGINT;
	  	
				
		/*
		 *  BUSQUEDA DEL DATOS DE LA CORREDERA 
		 * */
		SELECT  CC."IdCliente" INTO v_IdCliente
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS  CE
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CC."IdPersona" = CE."IdPersona"
		WHERE CE."IdEmpresa" = :p_IdEmpresa;
		
		--CONSULTA FECHA SISTEMA
		SELECT "Valor" INTO v_FechaSistema FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema' AND "IdEmpresa" = p_IdEmpresa;
		
		
		IF :p_FechaConsulta = :v_FechaSistema THEN
		--*Lee e inserta los movimientos 
			
		/********************************************************************************************************************
		 *                           					DISPONIBLE
		 ********************************************************************************************************************/
		
			P_OUT_TABLA = 
				SELECT 
					 CAR."IdCartera"
					,CASE
						 WHEN 	CAR."IdTipoInstrumento" = 'SIM' THEN 'SIMULTANEA' 
						 WHEN 	CAR."IdTipoInstrumento" = 'EMP' THEN 'PRESTAMISTA' 	 
						 ELSE   CONCAT('CARTERA ', CAR."IdClasificacionTipoInstrumento")
					 END AS "IdTipoMovimiento"
					,CAR."IdTipoInstrumento"
					,CAR."IdClasificacionTipoInstrumento" 	AS "IdMercado"
					,CAR."IdEmpresa"         				AS "IdEmpresa"									    
					,CAR."FolioCartera"					    AS "FolioCartera"
					,CAR."INGFechaOperacion" 				AS "Fecha"
					,CP."DocumentoIdentidad"				AS "DocumentoIdentidad"  
					,CAR."IdPortafolio"					    AS "IdPortafolio"
					,CP."CodigoPortafolio"
					,ABS(COALESCE(CAR."INGMontoATasaMercadoActual",CAR."INGMontoATasaMercadoActual"))	AS "Monto"
					,CAR."IdMonedaEmision"					AS "IdMoneda"
					,CAR."IdTipoReajuste"
					,CAR."IdEstado"							AS "Estado"
					,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."NemoSVS" ELSE CAR."Nemotecnico" END AS "Nemotecnico"
					,CAR."IdEmisor"
					,CAR."IdMonedaInstrumento"					
					,CAR."FechaVencimientoInstrumento"      AS "FechaVencimientoInstrumento"
					,CAR."IdInstrumento"				    AS "IdInstrumento"
					,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."INGValorRescate" ELSE CAR."INGCantidad" END AS "INGCantidad"
					
					--Montos anterior y actual
					,CAR."INGMontoATasaMercadoAnterior"     AS "INGMontoATasaMercadoAnterior"	
					,CAR."INGMontoATasaMercadoActual"       AS "INGMontoATasaMercadoActual"
					,CAR."INGMontoMercadoActualMonedaInstrumento" 		AS "INGMontoATasaCompraActual" --Debe representar el valor actual moneda Instrumento (Valor Mercado)
					,CAR."INGMontoATasaCompraAnterior" 		AS "INGMontoATasaCompraAnterior"
					--Duration
					,CAR."INGDurationMercadoActual" 	 	AS "INGDurationMercadoActual"
					,CAR."INGDurationCompraActual" 			AS "INGDurationCompraActual"
					--Tasas
					,CAR."INGTasaCompra" 					AS "INGTasaCompra"
					,CAR."INGTasaMercadoActual"             AS "INGTasaMercadoActual"
					--Calculo Mayor Menor Valor
					,CASE WHEN CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior" > 0 THEN CAR."INGMontoATasaMercadoActual" -CAR."INGMontoATasaMercadoAnterior" ELSE 0 END AS "MayorValorMercado"
					,CASE WHEN CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior" < 0 THEN ABS (CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior") ELSE 0 END	AS "MenorValorMercado"	
					,CASE WHEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" > 0 THEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" ELSE 0 END AS "MayorValorCompra"
					,CASE WHEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" < 0 THEN ABS(CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior") ELSE 0 END AS "MenorValorCompra"	
					
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" AS CAR --FROM 
					INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio"
					LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CARR ON (CARR."FolioCartera" = CAR."FolioCartera") 
					WHERE CAR."IdCliente"= :v_IdCliente
					  AND CAR."IdEstado"='VIG' 
					  AND CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
					  AND CARR."EGRCantidad" = 0
					  AND CAR."IdTipoInstrumento" != 'PAC'
					  AND CAR."FechaOperacion" <=:p_FechaConsulta
					  
				UNION ALL 
				
		 /********************************************************************************************************************
		 *                           					PACTEADA
		 ********************************************************************************************************************/
				
					SELECT 
					 CAR."IdCartera"
					,CASE
						 WHEN 	CAR."IdTipoInstrumento" = 'SIM' THEN 'SIMULTANEA' 
						 WHEN 	CAR."IdTipoInstrumento" = 'EMP' THEN 'PRESTAMISTA' 	 
						 ELSE   CONCAT('CARTERA ', CAR."IdClasificacionTipoInstrumento")
					 END AS "IdTipoMovimiento"
					,CAR."IdTipoInstrumento"
					,CAR."IdClasificacionTipoInstrumento" 	AS "IdMercado"
					,CAR."IdEmpresa"         				AS "IdEmpresa"									    
					,CAR."FolioCartera"					    AS "FolioCartera"
					,CAR."INGFechaOperacion" 				AS "Fecha"
					,CP."DocumentoIdentidad"				AS "DocumentoIdentidad"  
					,CAR."IdPortafolio"					    AS "IdPortafolio"
					,CP."CodigoPortafolio"
					,ABS(COALESCE("INGMontoATasaMercadoActual","INGMontoATasaMercadoActual"))	AS "Monto"
					,CAR."IdMonedaEmision"					AS "IdMoneda"
					,CAR."IdTipoReajuste"
					,CAR."IdEstado"							AS "Estado"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "NemoSVS" ELSE "Nemotecnico" END AS "Nemotecnico"
					,CAR."IdEmisor"
					,CAR."IdMonedaInstrumento"					
					,CAR."FechaVencimientoInstrumento"      AS "FechaVencimientoInstrumento"
					,CAR."IdInstrumento"				    AS "IdInstrumento"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "INGCantidad"
					
					--Montos anterior y actual
					,CAR."INGMontoATasaMercadoAnterior"     AS "INGMontoATasaMercadoAnterior"	
					,CAR."INGMontoATasaMercadoActual"       AS "INGMontoATasaMercadoActual"
					,CAR."INGMontoATasaCompraActual" 		AS "INGMontoATasaCompraActual"
					,CAR."INGMontoATasaCompraAnterior" 		AS "INGMontoATasaCompraAnterior"
					--Duration
					,CAR."INGDurationMercadoActual" 	 	AS "INGDurationMercadoActual"
					,CAR."INGDurationCompraActual" 			AS "INGDurationCompraActual"
					--Tasas
					,CAR."INGTasaCompra" 					AS "INGTasaCompra"
					,CAR."INGTasaMercadoActual"             AS "INGTasaMercadoActual"
					--Calculo Mayor Menor Valor
					,CASE WHEN CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior" > 0 THEN CAR."INGMontoATasaMercadoActual" -CAR."INGMontoATasaMercadoAnterior" ELSE 0 END AS "MayorValorMercado"
					,CASE WHEN CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior" < 0 THEN ABS (CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior") ELSE 0 END	AS "MenorValorMercado"	
					,CASE WHEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" > 0 THEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" ELSE 0 END AS "MayorValorCompra"
					,CASE WHEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" < 0 THEN ABS(CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior") ELSE 0 END AS "MenorValorCompra"	
					
					FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
					INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
					WHERE CAR."IdCliente"= :v_IdCliente
					  AND CAR."IdEstado"='VIG' 
					  AND CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
					  AND CAR."EGRCantidad" = 0
					  AND CAR."IdTipoInstrumento" = 'PAC'
					  AND CAR."FechaOperacion" <=:p_FechaConsulta
					  
				UNION ALL 
				
		 /********************************************************************************************************************
		 *                           					GARANTIA
		 ********************************************************************************************************************/
				
				  SELECT
					GAR."IdGarantia"
					,CASE
						 WHEN 	GAR."IdTipoInstrumento" = 'SIM' THEN 'SIMULTANEA' 
						 WHEN 	GAR."IdTipoInstrumento" = 'EMP' THEN 'PRESTAMISTA' 	 
						 ELSE   CONCAT('CARTERA ', GAR."IdClasificacionTipoInstrumento")
					 END AS "IdTipoMovimiento"
					,GAR."IdTipoInstrumento"
					,GAR."IdClasificacionTipoInstrumento" 	AS "IdMercado"
					,GAR."IdEmpresa"         				AS "IdEmpresa"									    
					,GAR."FolioGarantia"				    AS "FolioCartera"
					,GAR."INGFechaOperacion" 				AS "Fecha"
					,CP."DocumentoIdentidad"				AS "DocumentoIdentidad"  
					,GAR."IdPortafolio"					    AS "IdPortafolio"
					,CP."CodigoPortafolio"
					,ABS(COALESCE("INGMontoATasaMercadoActual","INGMontoATasaMercadoActual"))	AS "Monto"
					,GAR."IdMonedaEmision"					AS "IdMoneda"
					,GAR."IdTipoReajuste"
					,GAR."IdEstado"							AS "Estado"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "NemoSVS" ELSE "Nemotecnico" END AS "Nemotecnico"
					,GAR."IdEmisor"
					,GAR."IdMonedaInstrumento"					
					,GAR."FechaVencimientoInstrumento"      AS "FechaVencimientoInstrumento"
					,GAR."IdInstrumento"				    AS "IdInstrumento"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "INGCantidad"
					
					--Montos anterior y actual
					,GAR."INGMontoATasaMercadoAnterior"     AS "INGMontoATasaMercadoAnterior"	
					,GAR."INGMontoATasaMercadoActual"       AS "INGMontoATasaMercadoActual"
					,GAR."INGMontoMercadoActualMonedaInstrumento" 		AS "INGMontoATasaCompraActual"
					,GAR."INGMontoATasaCompraAnterior" 		AS "INGMontoATasaCompraAnterior"
					--Duration
					,GAR."INGDurationMercadoActual" 	 	AS "INGDurationMercadoActual"
					,GAR."INGDurationCompraActual" 			AS "INGDurationCompraActual"
					--Tasas
					,GAR."INGTasaGarantia" 					AS "INGTasaCompra"
					,GAR."INGTasaMercadoActual"             AS "INGTasaMercadoActual"
					--Calculo Mayor Menor Valor
					,CASE WHEN GAR."INGMontoATasaMercadoActual" - GAR."INGMontoATasaMercadoAnterior" > 0 THEN GAR."INGMontoATasaMercadoActual" -GAR."INGMontoATasaMercadoAnterior" ELSE 0 END AS "MayorValorMercado"
					,CASE WHEN GAR."INGMontoATasaMercadoActual" - GAR."INGMontoATasaMercadoAnterior" < 0 THEN ABS (GAR."INGMontoATasaMercadoActual" - GAR."INGMontoATasaMercadoAnterior") ELSE 0 END	AS "MenorValorMercado"	
					,CASE WHEN GAR."INGMontoATasaCompraActual" - GAR."INGMontoATasaCompraAnterior" > 0 THEN GAR."INGMontoATasaCompraActual" - GAR."INGMontoATasaCompraAnterior" ELSE 0 END AS "MayorValorCompra"
					,CASE WHEN GAR."INGMontoATasaCompraActual" - GAR."INGMontoATasaCompraAnterior" < 0 THEN ABS(GAR."INGMontoATasaCompraActual" - GAR."INGMontoATasaCompraAnterior") ELSE 0 END AS "MenorValorCompra"	 
				  FROM  "BSCL.Custodia.BD::GTA.Garantia" AS GAR
				  INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = GAR."IdPortafolio" 
				  WHERE GAR."IdCliente"= :v_IdCliente
					  AND GAR."IdEstado"='VIG' 
					  AND GAR."IdClasificacionTipoInstrumento" in ('RF','IF')
					  AND GAR."EGRCantidad" = 0
					  AND GAR."FechaOperacion" <=:p_FechaConsulta;
				  
				
			    
		--------------------------------------------------------------------------------------------------------
		ELSE  --- Si la consulta es anterior a la fecha del sistema, tiene q consultar en el respaldo histórico
		--------------------------------------------------------------------------------------------------------
		
		P_OUT_TABLA = 
				SELECT 
					 CAR."IdCartera"
					,CASE
						 WHEN 	CAR."IdTipoInstrumento" = 'SIM' THEN 'SIMULTANEA' 
						 WHEN 	CAR."IdTipoInstrumento" = 'EMP' THEN 'PRESTAMISTA' 	 
						 ELSE   CONCAT('CARTERA ', CAR."IdClasificacionTipoInstrumento")
					 END AS "IdTipoMovimiento"
					,CAR."IdTipoInstrumento"
					,CAR."IdClasificacionTipoInstrumento" 	AS "IdMercado"
					,CAR."IdEmpresa"         				AS "IdEmpresa"									    
					,CAR."FolioCartera"					    AS "FolioCartera"
					,CAR."INGFechaOperacion" 				AS "Fecha"
					,CP."DocumentoIdentidad"				AS "DocumentoIdentidad"  
					,CAR."IdPortafolio"					    AS "IdPortafolio"
					,CP."CodigoPortafolio"
					,ABS(COALESCE("INGMontoATasaMercadoActual","INGMontoATasaMercadoActual"))	AS "Monto"
					,CAR."IdMonedaEmision"					AS "IdMoneda"
					,CAR."IdTipoReajuste"
					,CAR."IdEstado"							AS "Estado"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "NemoSVS" ELSE "Nemotecnico" END AS "Nemotecnico"
					,CAR."IdEmisor"
					,CAR."IdMonedaInstrumento"					
					,CAR."FechaVencimientoInstrumento"      AS "FechaVencimientoInstrumento"
					,CAR."IdInstrumento"				    AS "IdInstrumento"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "INGCantidad"
					
					--Montos anterior y actual
					,CAR."INGMontoATasaMercadoAnterior"     AS "INGMontoATasaMercadoAnterior"	
					,CAR."INGMontoATasaMercadoActual"       AS "INGMontoATasaMercadoActual"
					,CAR."INGMontoMercadoActualMonedaInstrumento" 		AS "INGMontoATasaCompraActual"
					,CAR."INGMontoATasaCompraAnterior" 		AS "INGMontoATasaCompraAnterior"
					--Duration
					,CAR."INGDurationMercadoActual" 	 	AS "INGDurationMercadoActual"
					,CAR."INGDurationCompraActual" 			AS "INGDurationCompraActual"
					--Tasas
					,CAR."INGTasaCompra" 					AS "INGTasaCompra"
					,CAR."INGTasaMercadoActual"             AS "INGTasaMercadoActual"
					--Calculo Mayor Menor Valor
					,CASE WHEN CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior" > 0 THEN CAR."INGMontoATasaMercadoActual" -CAR."INGMontoATasaMercadoAnterior" ELSE 0 END AS "MayorValorMercado"
					,CASE WHEN CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior" < 0 THEN ABS (CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior") ELSE 0 END	AS "MenorValorMercado"	
					,CASE WHEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" > 0 THEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" ELSE 0 END AS "MayorValorCompra"
					,CASE WHEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" < 0 THEN ABS(CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior") ELSE 0 END AS "MenorValorCompra"	
					
					FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
					INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
					WHERE CAR."IdCliente"= :v_IdCliente
					  AND CAR."IdEstado"='VIG' 
					  AND CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
					  AND CAR."EGRCantidad" = 0
					  AND CAR."IdTipoInstrumento" != 'PAC'
					  AND CAR."FechaRespaldo" =:p_FechaConsulta
					  
				UNION ALL 
				
		 /********************************************************************************************************************
		 *                           					PACTEADA
		 ********************************************************************************************************************/
				
					SELECT 
					 CAR."IdCartera"
					,CASE
						 WHEN 	CAR."IdTipoInstrumento" = 'SIM' THEN 'SIMULTANEA' 
						 WHEN 	CAR."IdTipoInstrumento" = 'EMP' THEN 'PRESTAMISTA' 	 
						 ELSE   CONCAT('CARTERA ', CAR."IdClasificacionTipoInstrumento")
					 END AS "IdTipoMovimiento"
					,CAR."IdTipoInstrumento"
					,CAR."IdClasificacionTipoInstrumento" 	AS "IdMercado"
					,CAR."IdEmpresa"         				AS "IdEmpresa"									    
					,CAR."FolioCartera"					    AS "FolioCartera"
					,CAR."INGFechaOperacion" 				AS "Fecha"
					,CP."DocumentoIdentidad"				AS "DocumentoIdentidad"  
					,CAR."IdPortafolio"					    AS "IdPortafolio"
					,CP."CodigoPortafolio"
					,ABS(COALESCE("INGMontoATasaMercadoActual","INGMontoATasaMercadoActual"))	AS "Monto"
					,CAR."IdMonedaEmision"					AS "IdMoneda"
					,CAR."IdTipoReajuste"
					,CAR."IdEstado"							AS "Estado"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "NemoSVS" ELSE "Nemotecnico" END AS "Nemotecnico"
					,CAR."IdEmisor"
					,CAR."IdMonedaInstrumento"					
					,CAR."FechaVencimientoInstrumento"      AS "FechaVencimientoInstrumento"
					,CAR."IdInstrumento"				    AS "IdInstrumento"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "INGCantidad"
					
					--Montos anterior y actual
					,CAR."INGMontoATasaMercadoAnterior"     AS "INGMontoATasaMercadoAnterior"	
					,CAR."INGMontoATasaMercadoActual"       AS "INGMontoATasaMercadoActual"
					,CAR."INGMontoMercadoActualMonedaInstrumento" 		AS "INGMontoATasaCompraActual"
					,CAR."INGMontoATasaCompraAnterior" 		AS "INGMontoATasaCompraAnterior"
					--Duration
					,CAR."INGDurationMercadoActual" 	 	AS "INGDurationMercadoActual"
					,CAR."INGDurationCompraActual" 			AS "INGDurationCompraActual"
					--Tasas
					,CAR."INGTasaCompra" 					AS "INGTasaCompra"
					,CAR."INGTasaMercadoActual"             AS "INGTasaMercadoActual"
					--Calculo Mayor Menor Valor
					,CASE WHEN CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior" > 0 THEN CAR."INGMontoATasaMercadoActual" -CAR."INGMontoATasaMercadoAnterior" ELSE 0 END AS "MayorValorMercado"
					,CASE WHEN CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior" < 0 THEN ABS (CAR."INGMontoATasaMercadoActual" - CAR."INGMontoATasaMercadoAnterior") ELSE 0 END	AS "MenorValorMercado"	
					,CASE WHEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" > 0 THEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" ELSE 0 END AS "MayorValorCompra"
					,CASE WHEN CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior" < 0 THEN ABS(CAR."INGMontoATasaCompraActual" - CAR."INGMontoATasaCompraAnterior") ELSE 0 END AS "MenorValorCompra"	
					
					FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
					INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = CAR."IdPortafolio" 
					WHERE CAR."IdCliente"= :v_IdCliente
					  AND CAR."IdEstado"='VIG' 
					  AND CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
					  AND CAR."EGRCantidad" = 0
					  AND CAR."IdTipoInstrumento" = 'PAC'
					  AND CAR."FechaRespaldo" =:p_FechaConsulta
					  
				UNION ALL 
				
		 /********************************************************************************************************************
		 *                           					GARANTIA
		 ********************************************************************************************************************/
				
				  SELECT
					GAR."IdGarantia"
					,CASE
						 WHEN 	GAR."IdTipoInstrumento" = 'SIM' THEN 'SIMULTANEA' 
						 WHEN 	GAR."IdTipoInstrumento" = 'EMP' THEN 'PRESTAMISTA' 	 
						 ELSE   CONCAT('CARTERA ', GAR."IdClasificacionTipoInstrumento")
					 END AS "IdTipoMovimiento"
					,GAR."IdTipoInstrumento"
					,GAR."IdClasificacionTipoInstrumento" 	AS "IdMercado"
					,GAR."IdEmpresa"         				AS "IdEmpresa"									    
					,GAR."FolioGarantia"				    AS "FolioCartera"
					,GAR."INGFechaOperacion" 				AS "Fecha"
					,CP."DocumentoIdentidad"				AS "DocumentoIdentidad"  
					,GAR."IdPortafolio"					    AS "IdPortafolio"
					,CP."CodigoPortafolio"
					,ABS(COALESCE("INGMontoATasaMercadoActual","INGMontoATasaMercadoActual"))	AS "Monto"
					,GAR."IdMonedaEmision"					AS "IdMoneda"
					,GAR."IdTipoReajuste"
					,GAR."IdEstado"							AS "Estado"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "NemoSVS" ELSE "Nemotecnico" END AS "Nemotecnico"
					,GAR."IdEmisor"
					,GAR."IdMonedaInstrumento"					
					,GAR."FechaVencimientoInstrumento"      AS "FechaVencimientoInstrumento"
					,GAR."IdInstrumento"				    AS "IdInstrumento"
					,CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "INGCantidad"
					
					--Montos anterior y actual
					,GAR."INGMontoATasaMercadoAnterior"     AS "INGMontoATasaMercadoAnterior"	
					,GAR."INGMontoATasaMercadoActual"       AS "INGMontoATasaMercadoActual"
					,GAR."INGMontoMercadoActualMonedaInstrumento" 		AS "INGMontoATasaCompraActual"
					,GAR."INGMontoATasaCompraAnterior" 		AS "INGMontoATasaCompraAnterior"
					--Duration
					,GAR."INGDurationMercadoActual" 	 	AS "INGDurationMercadoActual"
					,GAR."INGDurationCompraActual" 			AS "INGDurationCompraActual"
					--Tasas
					,GAR."INGTasaGarantia" 					AS "INGTasaCompra"
					,GAR."INGTasaMercadoActual"             AS "INGTasaMercadoActual"
					--Calculo Mayor Menor Valor
					,CASE WHEN GAR."INGMontoATasaMercadoActual" - GAR."INGMontoATasaMercadoAnterior" > 0 THEN GAR."INGMontoATasaMercadoActual" -GAR."INGMontoATasaMercadoAnterior" ELSE 0 END AS "MayorValorMercado"
					,CASE WHEN GAR."INGMontoATasaMercadoActual" - GAR."INGMontoATasaMercadoAnterior" < 0 THEN ABS (GAR."INGMontoATasaMercadoActual" - GAR."INGMontoATasaMercadoAnterior") ELSE 0 END	AS "MenorValorMercado"	
					,CASE WHEN GAR."INGMontoATasaCompraActual" - GAR."INGMontoATasaCompraAnterior" > 0 THEN GAR."INGMontoATasaCompraActual" - GAR."INGMontoATasaCompraAnterior" ELSE 0 END AS "MayorValorCompra"
					,CASE WHEN GAR."INGMontoATasaCompraActual" - GAR."INGMontoATasaCompraAnterior" < 0 THEN ABS(GAR."INGMontoATasaCompraActual" - GAR."INGMontoATasaCompraAnterior") ELSE 0 END AS "MenorValorCompra"	 
				  FROM  "BSCL.Custodia.BD::HIS.Garantia" AS GAR
				  INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CP ON CP."IdPortafolio" = GAR."IdPortafolio" 
				  WHERE GAR."IdCliente"= :v_IdCliente
					  AND GAR."IdEstado"='VIG' 
					  AND GAR."IdClasificacionTipoInstrumento" in ('RF','IF')
					  AND GAR."EGRCantidad" = 0
					  AND GAR."FechaRespaldo" =:p_FechaConsulta; 
		END IF;
	END;
END;
