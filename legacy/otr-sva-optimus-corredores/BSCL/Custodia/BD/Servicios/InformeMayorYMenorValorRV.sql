SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::InformeMayorYMenorValorRV";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::InformeMayorYMenorValorRV"(
	--+ Variables de entrada obligatorias
	 IN p_IdEmpresa						BIGINT
	,IN p_IdUsuario						BIGINT
	,IN p_IdModulo						NVARCHAR(3)
	--+ Enumerar Variabes de entrada
	,IN p_FechaConsulta   				NVARCHAR(10)
	,OUT P_OUT_TABLA				TABLE(
		 "IdCartera"						BIGINT
		,"TipoCartera" 						NVARCHAR(10)
		,"IdEmpresa"						BIGINT
		,"FolioCartera"						BIGINT
		,"Fecha"							NVARCHAR(10)
		,"DocumentoIdentidad"				NVARCHAR(40)
		,"IdPortafolio"						BIGINT
		,"CodigoPortafolio" 				NVARCHAR(100)
		,"Monto"							DECIMAL(26,8)
		,"IdMoneda"							NVARCHAR(3)
		,"Estado"							NVARCHAR(3)
		,"Nemotecnico" 						NVARCHAR(40)
		,"IdEmisor" 						NVARCHAR(40)
		,"IdInstrumento" 					BIGINT
		,"INGCantidad" 						DECIMAL(26,8)
		,"INGPrecioCompra"					DECIMAL(26,8)
		,"INGPrecioMercadoActual" 			DECIMAL(26,8)
		,"INGMontoATasaMercadoAnterior"		DECIMAL(26,8) 
		,"INGMontoATasaMercadoActual"		DECIMAL(26,8)		
		,"INGMontoATasaCompraActual"		DECIMAL(26,8)
		,"INGMontoATasaCompraAnterior"		DECIMAL(26,8)		
		,"MayorValorMercado"				DECIMAL(26,8)
		,"MenorValorMercado"				DECIMAL(26,8)
		,"IdMonedaInstrumento"				NVARCHAR(3)
		,"IdTipoInstrumento" 				NVARCHAR(3)
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
	  	
				
		SELECT COUNT(1) INTO v_Contador FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = p_IdEmpresa;
		IF :v_Contador = 1 THEN
			SELECT "DocumentoIdentidad" INTO v_DocumentoIdentidadCorredora 
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = p_IdEmpresa;
		END IF;
		
		SELECT "Valor" INTO v_FechaSistema FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema' AND "IdEmpresa" = p_IdEmpresa;
		
		
		
		IF :p_FechaConsulta = :v_FechaSistema THEN
			--*Lee e inserta los movimientos 
			P_OUT_TABLA = 
				SELECT 
					Cart."IdCartera"																	AS "IdCartera"
					,CASE WHEN 	Cart."IdTipoInstrumento" = 'SIM' THEN 'SIMULTANEA'
						  WHEN 	Cart."IdTipoInstrumento" = 'EMP' THEN 'PRESTAMISTA'
						  ELSE 'CARTERA'
					 END 																				AS "TipoCartera"
					,Cart."IdEmpresa"         															AS "IdEmpresa"
					,Cart."FolioCartera"																AS "FolioCartera"
					,Cart."INGFechaOperacion" 															AS "Fecha"
					,Cart."DocumentoIdentidad"															AS "DocumentoIdentidad"
					,Cart."IdPortafolio"																AS "IdPortafolio"
					,Cart."CodigoPortafolio"
					,ABS(COALESCE(sby."INGMontoATasaMercadoActual", Cart."INGMontoATasaMercadoActual"))	AS "Monto"
					,Cart."IdMonedaEmision"																AS "IdMoneda"
					,Cart."IdEstado"																	AS "Estado"
					,COALESCE(sby."Nemotecnico", Cart."Nemotecnico") 									AS "Nemotecnico"
					,COALESCE(sby."IdEmisor", Cart."IdEmisor") 											AS "IdEmisor"
					,COALESCE(sby."IdInstrumento", Cart."IdInstrumento") 								AS "IdInstrumento"
					,COALESCE(sby."INGCantidad",Cart."INGCantidad") 									AS "INGCantidad"
					,COALESCE(Cart."PrecioCompra",0) 													AS "INGPrecioCompra"
					,COALESCE(Cart."INGPrecioMercadoActual",0) 											AS "INGPrecioMercadoActual"
					,COALESCE(sby."INGMontoATasaMercadoAnterior",Cart."INGMontoATasaMercadoAnterior") 	AS "INGMontoATasaMercadoAnterior"	
					,COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") 		AS "INGMontoATasaMercadoActual"
					,COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual") 		AS "INGMontoATasaCompraActual"
					,COALESCE(sby."INGMontoATasaCompraAnterior",Cart."INGMontoATasaCompraAnterior") 	AS "INGMontoATasaCompraAnterior"
					,CASE WHEN (COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") - COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual")) > 0 THEN (COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") - COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual")) ELSE 0 END
					   AS "MayorValorMercado"
					,CASE WHEN (COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") - COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual")) < 0 THEN ABS((COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") - COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual"))) ELSE 0 END
					   AS "MenorValorMercado"			
					,Cart."IdMonedaInstrumento"
					,COALESCE(sby."IdTipoInstrumento", Cart."IdTipoInstrumento") 						AS "IdTipoInstrumento"
					
				FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" Cart
					LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (Cart."FolioCartera" = ultSub."FolioCarteraRelacionada")
					LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
				
				WHERE Cart."IdEmpresa" = :p_IdEmpresa
					AND Cart."IdEstado" = 'VIG'
					AND Cart."IdClasificacionTipoInstrumento" = 'RV'
					AND (Cart."IdTipoInstrumento" NOT IN ('PRT','EMP','PAC', 'FWD','RRV','RRF') OR (Cart."IdTipoInstrumento" = 'SIM' AND Cart."IdTipoOperacion" = 'VENSIM')) 
					AND Cart."DocumentoIdentidad" = :v_DocumentoIdentidadCorredora
					AND Cart."FechaOperacion" <= :p_FechaConsulta 
					AND Cart."EGRCantidad" = 0
			          
			    UNION ALL
			    SELECT 
					ConsultarGarantias."IdGarantia"						AS "IdCartera"
					,'GARANTIA'											AS "TipoCartera"
					,ConsultarGarantias."IdEmpresa"         			AS "IdEmpresa"
					,ConsultarGarantias."FolioGarantia"					AS "FolioCartera"
					,ConsultarGarantias."INGFechaOperacion" 			AS "Fecha"
					,ConsultarGarantias."DocumentoIdentidad"			AS "DocumentoIdentidad"  
					,ConsultarGarantias."IdPortafolio"					AS "IdPortafolio"
					,ConsultarGarantias."CodigoPortafolio"  
					,ABS(ConsultarGarantias."MontoGarantia")			AS "Monto"
					,ConsultarGarantias."IdMonedaEmision"				AS "IdMoneda"
					,ConsultarGarantias."IdEstado"						AS "Estado"
					,ConsultarGarantias."Nemotecnico"
					,ConsultarGarantias."IdEmisor"
					,ConsultarGarantias."IdInstrumento"
					,ConsultarGarantias."INGCantidad"
					,ConsultarGarantias."INGPrecioGarantia" 			AS "INGPrecioCompra"
					,ConsultarGarantias."INGPrecioMercadoActual" 		AS "INGPrecioMercadoActual"
					,ConsultarGarantias."INGMontoATasaMercadoAnterior"
					,ConsultarGarantias."INGMontoATasaMercadoActual"
					,ConsultarGarantias."INGMontoATasaCompraActual"
					,ConsultarGarantias."INGMontoATasaCompraAnterior"
					,CASE WHEN (ConsultarGarantias."INGMontoATasaMercadoActual" - ConsultarGarantias."INGMontoATasaCompraActual") > 0 THEN (ConsultarGarantias."INGMontoATasaMercadoActual" - ConsultarGarantias."INGMontoATasaCompraActual") ELSE 0 END 
						AS "MayorValorMercado"
					,CASE WHEN (ConsultarGarantias."INGMontoATasaMercadoActual" - ConsultarGarantias."INGMontoATasaCompraActual") < 0 THEN ABS((ConsultarGarantias."INGMontoATasaMercadoActual" - ConsultarGarantias."INGMontoATasaCompraActual")) ELSE 0 END 
						AS "MenorValorMercado"
					,ConsultarGarantias."IdMonedaInstrumento"
					,ConsultarGarantias."IdTipoInstrumento"
				FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" ConsultarGarantias
				WHERE "IdEmpresa" = p_IdEmpresa
				AND "IdClasificacionTipoInstrumento" = 'RV'
				AND "INGFechaOperacion" <= :p_FechaConsulta 
				AND "EGRCantidad" = 0
		        AND "DocumentoIdentidad" = :v_DocumentoIdentidadCorredora
		        AND "NemoGarantizado" != 'SIMULTANEA VENTA'
		        AND ConsultarGarantias."IdEstado" = 'VIG';
		
		--------------------------------------------------------------------------------------------------------
		ELSE  --- Si la consulta es anterior a la fecha del sistema, tiene q consultar en el respaldo histórico
		--------------------------------------------------------------------------------------------------------
			--*Lee e inserta los movimientos 
			P_OUT_TABLA = 
				SELECT 
					Cart."IdCartera"										AS "IdCartera"
					,CASE WHEN 	Cart."IdTipoInstrumento" = 'SIM' THEN 'SIMULTANEA'
						  WHEN 	Cart."IdTipoInstrumento" = 'EMP' THEN 'PRESTAMISTA'
						  ELSE 'CARTERA'
					 END 													AS "TipoCartera"
					,Cart."IdEmpresa"         								AS "IdEmpresa"
					,Cart."FolioCartera"									AS "FolioCartera"
					,Cart."INGFechaOperacion" 								AS "Fecha"
					,cli."DocumentoIdentidad"								AS "DocumentoIdentidad"
					,Cart."IdPortafolio"									AS "IdPortafolio"
					,Port."CodigoPortafolio"
					,ABS(COALESCE(sby."INGMontoATasaMercadoActual", Cart."INGMontoATasaMercadoActual"))	AS "Monto"
					,Cart."IdMonedaSistema"									AS "IdMoneda"
					,Cart."IdEstado"										AS "Estado"
					,COALESCE(sby."Nemotecnico", Cart."Nemotecnico") 		AS "Nemotecnico" 
					,COALESCE(sby."IdEmisor", Cart."IdEmisor") 				AS "IdEmisor"
					,COALESCE(sby."IdInstrumento", Cart."IdInstrumento") 	AS "IdInstrumento"
					,COALESCE(sby."INGCantidad",Cart."INGCantidad") 		AS "INGCantidad"
					,COALESCE(Cart."PrecioCompra",0) 						AS "INGPrecioCompra"
					,COALESCE(Cart."INGPrecioMercadoActual",0) 				AS "INGPrecioMercadoActual"
					,COALESCE(sby."INGMontoATasaMercadoAnterior",Cart."INGMontoATasaMercadoAnterior") AS "INGMontoATasaMercadoAnterior"	
					,COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") AS "INGMontoATasaMercadoActual"
					,COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual") AS "INGMontoATasaCompraActual"
					,COALESCE(sby."INGMontoATasaCompraAnterior",Cart."INGMontoATasaCompraAnterior") AS "INGMontoATasaCompraAnterior"
					,CASE WHEN (COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") - COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual")) > 0 THEN (COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") - COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual")) ELSE 0 END
						AS "MayorValorMercado"
					,CASE WHEN (COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") - COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual")) < 0 THEN ABS((COALESCE(sby."INGMontoATasaMercadoActual",Cart."INGMontoATasaMercadoActual") - COALESCE(sby."INGMontoATasaCompraActual",Cart."INGMontoATasaCompraActual"))) ELSE 0 END
						AS "MenorValorMercado"			
					,Cart."IdMonedaInstrumento"
					,Cart."IdTipoInstrumento"
					
				FROM "BSCL.Custodia.BD::HIS.Cartera" Cart
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cli ON cli."IdCliente" = Cart."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Port ON Port."IdPortafolio" = Cart."IdPortafolio"
					LEFT JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente" as ultSub on (Cart."FolioCartera" = ultSub."FolioCarteraRelacionada" AND Cart."FechaRespaldo" = ultSub."FechaRespaldo")
					LEFT JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente" AND sby."FechaRespaldo" = Cart."FechaRespaldo"
				
				WHERE Cart."IdEmpresa" = :p_IdEmpresa
					AND Cart."FechaRespaldo" = :p_FechaConsulta
					AND Cart."IdEstado" = 'VIG'
					AND Cart."INGFechaOperacion" <= :p_FechaConsulta 
					AND Cart."EGRCantidad" = 0
					AND Cli."DocumentoIdentidad" = :v_DocumentoIdentidadCorredora
			        AND Cart."IdClasificacionTipoInstrumento" = 'RV'
			        AND (Cart."IdTipoInstrumento" NOT IN ('PRT','EMP','PAC', 'FWD','RRV','RRF') OR (Cart."IdTipoInstrumento" = 'SIM' AND Cart."IdTipoOperacion" = 'VENSIM'))
			        
			          
			    UNION ALL
			    SELECT 
					ConsultarGarantias."IdGarantia"							AS "IdCartera"
					,'GARANTIA'												AS "TipoCartera"
					,ConsultarGarantias."IdEmpresa"         				AS "IdEmpresa"
					,ConsultarGarantias."FolioGarantia"						AS "FolioCartera"
					,ConsultarGarantias."INGFechaOperacion" 				AS "Fecha"
					,ConsultarGarantias."DocumentoIdentidad"				AS "DocumentoIdentidad"
					,ConsultarGarantias."IdPortafolio"						AS "IdPortafolio"
					,ConsultarGarantias."CodigoPortafolio"  
					,ABS(ConsultarGarantias."MontoGarantia")				AS "Monto"
					,"IdMonedaSistema"										AS "IdMoneda"
					,ConsultarGarantias."IdEstado"							AS "Estado"
					,ConsultarGarantias."Nemotecnico"
					,ConsultarGarantias."IdEmisor"
					,ConsultarGarantias."IdInstrumento"
					,ConsultarGarantias."INGCantidad"
					,ConsultarGarantias."INGPrecioGarantia" 				AS "INGPrecioCompra"
					,ConsultarGarantias."INGPrecioMercadoActual" 			AS "INGPrecioMercadoActual"
					,ConsultarGarantias."INGMontoATasaMercadoAnterior"
					,ConsultarGarantias."INGMontoATasaMercadoActual"
					,ConsultarGarantias."INGMontoATasaCompraActual"
					,ConsultarGarantias."INGMontoATasaCompraAnterior"
					,CASE WHEN (ConsultarGarantias."INGMontoATasaMercadoActual" - ConsultarGarantias."INGMontoATasaCompraActual") > 0 THEN (ConsultarGarantias."INGMontoATasaMercadoActual" - ConsultarGarantias."INGMontoATasaCompraActual") ELSE 0 END
						AS "MayorValorMercado"
					,CASE WHEN (ConsultarGarantias."INGMontoATasaMercadoActual" - ConsultarGarantias."INGMontoATasaCompraActual") < 0 THEN ABS((ConsultarGarantias."INGMontoATasaMercadoActual" - ConsultarGarantias."INGMontoATasaCompraActual")) ELSE 0 END 
						AS "MenorValorMercado"
					,ConsultarGarantias."IdMonedaInstrumento"
					,ConsultarGarantias."IdTipoInstrumento"
				FROM "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantias" ConsultarGarantias
				WHERE ConsultarGarantias."IdEmpresa" = p_IdEmpresa
					AND ConsultarGarantias."FechaRespaldo" = :p_FechaConsulta
					AND ConsultarGarantias."IdClasificacionTipoInstrumento" = 'RV'
					AND ConsultarGarantias."IdEstado" = 'VIG'
					AND ConsultarGarantias."DocumentoIdentidad" = :v_DocumentoIdentidadCorredora
					AND "INGFechaOperacion" <= :p_FechaConsulta 
					AND "EGRCantidad" = 0
			        AND ConsultarGarantias."NemoGarantizado" != 'SIMULTANEA VENTA';
		END IF;
	END;
END;
