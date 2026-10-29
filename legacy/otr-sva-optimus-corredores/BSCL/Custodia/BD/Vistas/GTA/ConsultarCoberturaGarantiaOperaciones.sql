SET SCHEMA "BSCL";  
GO 
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaOperaciones";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaOperaciones" AS
SELECT 
	 CAR."IdCliente"	
	,CAR."IdPortafolio"
	,CAR."IdTipoInstrumento" AS "IdTipoInstrumentoGarantizado"   -- SIM
	,CAR."IdInstrumento" AS "IdInstrumentoGarantizado"           -- 2249 (VENSIM)  
    ,CAR."NombreTipoOperacion"
	,CAR."FolioCartera"
	,CAR."IdConcepto"
	,CAR."UsaGarantiaEspecifica" AS "EsEspecifica"
	,CAR."INGMontoATasaMercadoActual" AS "Monto"
	,CAR."DocumentoIdentidad" AS "DocumentoIdentidad" 
	,CAR."RazonSocial" AS "NombreCliente" 	
	,CAR."CodigoPortafolio" AS "CodigoPortafolio"
	,CAR."PropositoPortafolio"
	,CAR."IdBolsa"
	,EDG."IdContraparte" AS "IdContraparteGarantia"   -- Entidad Destino
	,CAR."IdTipoComprobante" AS "IdTipoComprobanteOperacionRelacionada"
	,CAR."FolioComprobante" AS "FolioComprobanteOperacionRelacionada"
	,CASE WHEN COALESCE(SUB."IdSubyacente",0) = 0 THEN CAR."Nemotecnico" 				ELSE COALESCE(SUB."Nemotecnico",'') 	END AS "Nemotecnico"
	,CASE WHEN COALESCE(SUB."IdSubyacente",0) = 0 THEN CAR."INGCantidad" 				ELSE COALESCE(SUB."INGCantidad",0)  		END AS "Cantidad"
	,CASE WHEN COALESCE(SUB."IdSubyacente",0) = 0 THEN CAR."INGMontoATasaCompraActual"	ELSE COALESCE(SUB."INGTasaSubyacente",0) 	END AS "TasaCompra"	
	,CASE WHEN COALESCE(SUB."IdSubyacente",0) = 0 THEN CAR."INGPrecioCompra"			ELSE COALESCE(SUB."INGPrecioSubyacente",0) 	END AS "PrecioCompra"		
	,CASE WHEN COALESCE(SUB."IdSubyacente",0) = 0 THEN CAR."INGMontoATasaCompraActual"	ELSE COALESCE(SUB."INGMontoATasaSubyacenteActual",0) 	END AS "MontoCompra"
	,CASE WHEN COALESCE(SUB."IdSubyacente",0) = 0 THEN CAR."FechaVencimientoOperacion" 	ELSE COALESCE(CAR."FechaVencimientoOperacion",'0001-01-01') END  AS "FechaVencimiento"
	,CASE WHEN COALESCE(SUB."IdSubyacente",0) = 0 THEN CAR."IdMonedaInstrumento" 		ELSE COALESCE(SUB."IdMonedaInstrumento",'') END  AS "IdMonedaInstrumento"

	-- Exigibilidad
--	,CAR."INGMontoATasaMercadoActual" * ((COALESCE(EXI."ExigibilidadEntidad",100)) /100) AS "ExigibilidadEntidad"
--	,CAR."INGMontoATasaMercadoActual" * ((COALESCE(EXI."ExigibilidadCorredora",100)) /100) AS "ExigibilidadCorredora"
/**************
	-- se cambia x compra, mientras no se esta valorizando.... volver a mercado cuando se valorice
*************/
	,CAR."INGMontoATasaCompraActual" * ((COALESCE(EXI."ExigibilidadEntidad",100)) /100) AS "ExigibilidadEntidad"
	,CAR."INGMontoATasaCompraActual" * ((COALESCE(EXI."ExigibilidadCorredora",100)) /100) AS "ExigibilidadCorredora"

	,COALESCE(EXI."ExigibilidadEntidad", 0) 		AS "ExigibilidadEntidadPorcentaje"
	,COALESCE(EXI."ExigibilidadCorredora", 0) 		AS "ExigibilidadCorredoraPorcentaje"
	,CAR."IdOperacion" AS "IdOperacionOrigenGarantia"	  -- Folio de la orden que origino la garantía
	,'N'											AS "EsVentaCorta"
	,0 AS "Plazo"
	,0 AS "TasaTir"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" AS CAR
INNER JOIN "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" AS BTI
	 ON BTI."IdBolsa" 				= CAR."IdBolsa"
	AND BTI."IdTipoInstrumento" 	= CAR."IdTipoInstrumento"
	AND BTI."ExigeGarantia" 		= 'S'
LEFT JOIN "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" AS EDG
	ON  EDG."IdEntidadDestinoGarantia" = BTI."IdEntidadDestinoGarantia"
    AND EDG."IdEstado" = 'ACT'
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB
     ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" 
    AND SUB."IdEstado" = 'VIG'
-- Exigibilidad
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"  AS INTR 
      ON INTR."IdInstrumento" = SUB."IdInstrumento"	
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::ConsultarExigenciaGarantia" AS EXI 
	 ON EXI."IdBolsa" = CAR."IdBolsa"
	AND EXI."IdTipoInstrumento" = CAR."IdTipoInstrumento"	
	AND EXI."IdTipoInstrumentoSubyacente"  = CASE WHEN COALESCE(SUB."IdSubyacente",0) = 0 THEN CAR."IdTipoInstrumento" ELSE SUB."IdTipoInstrumento" END                                                        	 
	AND EXI."IdTipoClasificacionRiesgo"    = INTR."IdTipoClasificacionRiesgo"	
	AND EXI."ClasificacionRiesgo"          = INTR."ValorClasificacionRiesgo"
	AND EXI."IdInstrumento" 			   = CAR."IdInstrumento"
	
UNION ALL 

-- VENTA CORTA
SELECT 
	 POR."IdCliente"	
	,POR."IdPortafolio"
	,CAR."IdTipoInstrumento"					 						AS "IdTipoInstrumentoGarantizado"	-- PRT -> VENTA CORTA
	,SUB."IdInstrumento" 												AS "IdInstrumentoGarantizado"  
    ,TOPE."Nombre"														AS "NombreTipoOperacion"
	,CAR."FolioCartera"
	,''																	AS "IdConcepto"
	,'N'							 									AS "EsEspecifica"
	,CAR."INGMontoATasaCompraActual"			 						AS "Monto"
	,CLI."DocumentoIdentidad" 											AS "DocumentoIdentidad" 
	,CLI."RazonSocial" 													AS "NombreCliente" 	
	,POR."CodigoPortafolio" 											AS "CodigoPortafolio"
	,''																	AS "PropositoPortafolio"
	,CAR."IdBolsa"
	,EDG."IdContraparte" 												AS "IdContraparteGarantia"   -- Entidad Destino
	,MOV."IdTipoComprobante" 											AS "IdTipoComprobanteOperacionRelacionada"
	,CAR."FolioComprobante" 											AS "FolioComprobanteOperacionRelacionada"
	, SUB."Nemotecnico"
	, CAR."INGCantidad"													AS "Cantidad"
	, COALESCE(CAR."INGTasaCompra",0) 									AS "TasaCompra"
	, COALESCE(CAR."INGPrecioCompra",0) 								AS "PrecioCompra"
	, COALESCE(CAR."INGMontoATasaCompraActual",0) 						AS "MontoCompra"
	, COALESCE(CAR."FechaVencimientoOperacion",'0001-01-01') 			AS "FechaVencimiento"
	, COALESCE(SUB."IdMonedaInstrumento",'') 							AS "IdMonedaInstrumento"
	,CAR."INGMontoATasaCompraActual" * ((COALESCE(EXI."ExigibilidadEntidad",100)) /100) 	AS "ExigibilidadEntidad"
	,CAR."INGMontoATasaCompraActual" * ((COALESCE(EXI."ExigibilidadCorredora",100)) /100) 	AS "ExigibilidadCorredora"
	,COALESCE(EXI."ExigibilidadEntidad", 0) 							AS "ExigibilidadEntidadPorcentaje"
	,COALESCE(EXI."ExigibilidadCorredora", 0) 							AS "ExigibilidadCorredoraPorcentaje"
	,ORD."IdOrden"														AS "IdOperacionOrigenGarantia"	  -- Folio de la orden que origino la garantía
	,'S'																AS "EsVentaCorta"
	,CAR."PlazoEnDiasOperacion"											AS "Plazo"
	,COALESCE(SUB."INGTasaSubyacente",0) 								AS "TasaTir"
	FROM "BSCL.Custodia.BD::CUS.Cartera" 								AS CAR
	INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente"						AS SUB  ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 		AS POR 	ON POR."IdPortafolio" = CAR."IdPortafolio" 	
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 		AS CLI 	ON CLI."IdCliente" = POR."IdCliente" 
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS INST ON INST."IdInstrumento" = SUB."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"					AS TOPE ON TOPE."IdTipoOperacion" = CAR."IdTipoOperacion"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 			AS MOV 	ON MOV."IdComprobante" = CAR."FolioComprobante"
	LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" 							AS ASIG ON MOV."IdMovimientoComprobante" = ASIG."IdMovimientoComprobante"
	LEFT JOIN "BSCL.Orden.BD::ORD.Orden" 								AS ORD 	ON ORD."IdOrden" = ASIG."IdOrden"
	LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 			AS BTI 	ON BTI."IdBolsa" = ORD."IdBolsa" AND BTI."IdTipoInstrumento" = ORD."IdTipoInstrumento" AND BTI."ExigeGarantia" = 'S'
	LEFT JOIN "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" 			AS EDG 	ON EDG."IdEntidadDestinoGarantia" = BTI."IdEntidadDestinoGarantia" AND EDG."IdEstado" = 'ACT'
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::ConsultarExigenciaGarantia"	AS EXI  ON  ( 
									EXI."IdBolsa" 						= CAR."IdBolsa"
								AND EXI."IdInstrumento" 			   	= CAR."IdInstrumento"
								AND EXI."IdTipoInstrumento" 			= CAR."IdTipoInstrumento"	
								AND EXI."IdTipoInstrumentoSubyacente"  	= SUB."IdTipoInstrumento"
								AND EXI."IdTipoClasificacionRiesgo"    	= INST."IdTipoClasificacionRiesgo"	
								AND EXI."ClasificacionRiesgo"        	= COALESCE(INST."ValorClasificacionRiesgo",'')
								) 
	WHERE CAR."IdTipoInstrumento" IN ('PRF', 'PRT') 
	  AND CAR."IdEstado" = 'VIG'
		

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaOperaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
