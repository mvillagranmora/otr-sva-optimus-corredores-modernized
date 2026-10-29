SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraDisponible";
GO


CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraDisponible" AS
WITH 
	"Cartera" AS (  SELECT  car."IdTipoInstrumento",
							car."IdInstrumento",
							car."IdEmisor",
							car."NemoSVS",
							car."Nemotecnico",
							car."IdMonedaInstrumento",
							car."IdMonedaEmision",
							car."IdTipoReajuste",
							car."FechaEmisionInstrumento" AS "FechaEmision",
							car."FechaVencimientoInstrumento" AS "FechaVencimiento",
							car."CodigoIFRS",
							car."EsFungible",
							car."IdBoveda",
							car."INGFechaLiquidacion" AS "FechaLiquidacion",							
							car."INGCantidad" AS "Cantidad",							
							car."INGValorRescate" AS "ValorRescate",
							car."INGCantidad"/(CASE WHEN car."CorteMinimo" <= 0 THEN 1 ELSE car."CorteMinimo" END) AS "Repeticiones", --Solo para RF en Perú
							car."INGCantidad" AS "ValorNominal",  --Solo para RF en Perú 
							car."INGPrecioCompra" AS "Precio",
							car."INGTasaCompra" AS "Tasa",
							car."INGMontoATasaCompraActual" as "Monto",
							car."IdCliente",
							car."IdPortafolio",		
							car."IdCartera" as "IdRegistro",
							car."FolioCartera" as "FolioRegistro",
							car."FechaOperacion",
							car."FechaVencimientoOperacion",
							car."IdTipoComprobante",
							car."FolioComprobante",
							car."FechaComprobante",							
							car."IdConcepto",
							car."IdEmpresa",
							'CARTERA' as "Origen",							
							car."IdCuentaCustodioPortafolio",
							car."CorteMinimo"
							
						FROM "BSCL.Custodia.BD::CUS.Cartera" AS car
					   INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" as CMC
					   		   ON CMC."IdTipoOperacion" 			= car."IdTipoOperacion"
					   		  AND CMC."PoseeInstrumentoSubyacente" 	= 'N' 
					   WHERE car."IdEstado"	= 'VIG'
					   --GROUP BY GROUPING SETS (car."INGFechaLiquidacion",car."IdInstrumento",car."FolioCartera")
				 ), 
	"Subyacente" AS (SELECT sub."IdTipoInstrumento",
							sub."IdInstrumento",
							sub."IdEmisor",
							sub."NemoSVS",
							sub."Nemotecnico",
							sub."IdMonedaInstrumento",
							sub."IdMonedaEmision",
							sub."IdTipoReajuste",
							sub."FechaEmisionInstrumento" AS "FechaEmision",
							sub."FechaVencimientoInstrumento" AS "FechaVencimiento",
							car."CodigoIFRS",
							sub."EsFungible",
							car."IdBoveda",
							car."FechaLiquidacion",
							sub."INGCantidad" as "Cantidad",
							sub."INGValorRescate" as "ValorRescate",
							sub."INGCantidad"/(CASE WHEN sub."CorteMinimo" <= 0 THEN 1 ELSE sub."CorteMinimo" END) AS "Repeticiones", --Solo para RF en Perú
							sub."INGCantidad" AS "ValorNominal",  --Solo para RF en Perú 
							sub."INGPrecioSubyacente" as "Precio",
							sub."INGTasaSubyacente" as "Tasa",
							sub."INGMontoATasaCompraActual" as "Monto",
							car."IdCliente",
							car."IdPortafolio",				
							sub."IdSubyacente" as "IdRegistro",
							sub."FolioSubyacente" as "FolioRegistro",
							car."FechaOperacion",
							car."FechaVencimientoOperacion",
							car."IdTipoComprobante",
							car."FolioComprobante",
							car."FechaComprobante",
							car."IdConcepto",
							car."IdEmpresa",
							'SUBYACENTE' as "Origen",							
							0 AS "IdCuentaCustodioPortafolio",
							sub."CorteMinimo"
							
						FROM "BSCL.Custodia.BD::CUS.Subyacente" AS sub
					   INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS car ON car."FolioCartera" = sub."FolioCarteraRelacionada"
					   WHERE sub."IdEstado" = 'VIG'
					     AND car."IdEstado" = 'VIG'
						--GROUP BY GROUPING SETS (sub."FechaLiquidacionOperacionRelacionada",sub."IdInstrumento",sub."FolioSubyacente")	
				 	)

SELECT  distinct CD."FechaLiquidacion"
		,CD."IdCliente"
		,CD."IdPortafolio"
		,CTI."IdClasificacionTipoInstrumento"
		,CTI."Nombre" AS "NombreClasificacionTipoInstrumento"
		,CI."IdTipoInstrumento"
		,TI."Nombre" AS "NombreTipoInstrumento"
		,CI."IdEstado"
		,CI."DecimalesPrecio"
		,CD."IdInstrumento"
--		,CI."Nemotecnico" AS "Nemotecnico"
		,CD."Nemotecnico" AS "Nemotecnico"
		,CI."ISIN"
		,COALESCE(CD."CorteMinimo",ITR."CorteMinimo",CI."CorteMinimo") AS "CorteMinimo"
		,CI."EstaInscrito"
		,CI."EsDivisible"
		,CI."RazonSocial"
		--,CI."Lista"
		,CD."FechaEmision"
		,CD."FechaVencimiento"
		,CI."IdInstrumentoRelacionado"
		,CD."IdEmisor"
		,E."NombreCorto" AS "RazonSocialEmisor"
		,CD."IdTipoReajuste"
		,TR."Nombre" AS "NombreTipoReajuste"
		,COALESCE(ITR."Reajustable",CI."Reajustable") AS "Reajustable"
		,CD."IdMonedaInstrumento" AS "IdMoneda"
		,CD."IdMonedaEmision"
		,CI."Tamano" AS "Tamano"
		,CD."EsFungible"
		,CD."CodigoIFRS"
		,CD."NemoSVS"		
		,CD."IdBoveda"
		,BVD."Nombre" AS "NombreBoveda"
		,CASE WHEN CTI."IdClasificacionTipoInstrumento" = 'IF' THEN CD."ValorRescate" ELSE CD."Cantidad" END AS "Cantidad"
		,CD."ValorRescate"
		,CD."Repeticiones"
		,CD."ValorNominal"
		,CD."Precio"
		,CD."Tasa"
		,CD."Monto"
		,CD."IdRegistro"
		,CD."FolioRegistro"
		,CD."FechaOperacion"
		,CD."FechaVencimientoOperacion"
		,CD."IdMonedaInstrumento" AS "IdMonedaOperacion"
		,CD."IdTipoComprobante"
		,CD."FolioComprobante"
		,CD."FechaComprobante"
		,CD."IdEmpresa"
		,CD."Origen"
		,CI."IndicadorBolson"
		,CI."ValorizaPor"
		,CCP."IdTipoCustodia" || ' ' || CCP."Codigo" AS "CodigoCuentaCustodio"
		,CCP."IdCuentaCustodioPortafolio"
		,CI."EsInstrumentoCalificado"
  FROM (
  		SELECT "FechaLiquidacion","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico","NemoSVS",
  			   "EsFungible","CodigoIFRS","FechaEmision","FechaVencimiento","IdTipoReajuste","IdBoveda","IdConcepto",
  			   "IdMonedaInstrumento","IdMonedaEmision","IdRegistro","FolioRegistro","Cantidad","ValorRescate","Repeticiones","ValorNominal","Precio",
  			   "Tasa","Monto","FechaOperacion","FechaVencimientoOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante",
  			   "IdEmpresa","Origen","IdCuentaCustodioPortafolio","CorteMinimo"
		  FROM "Cartera" 
		 UNION ALL 
		SELECT "FechaLiquidacion","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico","NemoSVS",
  			   "EsFungible","CodigoIFRS","FechaEmision","FechaVencimiento","IdTipoReajuste","IdBoveda","IdConcepto",
  			   "IdMonedaInstrumento","IdMonedaEmision","IdRegistro","FolioRegistro","Cantidad","ValorRescate","Repeticiones","ValorNominal","Precio",
  			   "Tasa","Monto","FechaOperacion","FechaVencimientoOperacion","IdTipoComprobante","FolioComprobante","FechaComprobante",
  			   "IdEmpresa","Origen","IdCuentaCustodioPortafolio","CorteMinimo"
		  FROM "Subyacente"
  	   ) AS CD
  LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS CI 
			   ON (CI."IdInstrumento" 		= CD."IdInstrumento")
  LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS E 
   			   ON (E."IdEmisor" 			= CD."IdEmisor")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI 
   			   ON (TI."IdTipoInstrumento" 	= CI."IdTipoInstrumento")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTI 
   			   ON (CTI."IdClasificacionTipoInstrumento" = TI."IdClasificacionTipoInstrumento")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS BVD 
   			   ON (BVD."IdBoveda" 			= CD."IdBoveda")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS C 
   			   ON (C."IdConcepto" 			= CD."IdConcepto")
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" AS TR 
   			   ON (TR."IdTipoReajuste" 		= CD."IdTipoReajuste")
  LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" AS ITR 
   			   ON (ITR."IdInstrumento"		= CD."IdInstrumento" AND
				   ITR."IdTipoInstrumento"	= CI."IdTipoInstrumento" AND
				   ITR."IdTipoReajuste"		= CD."IdTipoReajuste")
  LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCP
  	ON (CCP."IdCuentaCustodioPortafolio" = CD."IdCuentaCustodioPortafolio")

				   
WITH READ ONLY;
GO
	

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraDisponible"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
