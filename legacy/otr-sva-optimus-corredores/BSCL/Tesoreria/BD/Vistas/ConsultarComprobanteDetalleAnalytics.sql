SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalleAnalytics";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalleAnalytics" AS
SELECT
      Comprobante."IdEmpresa" ,
      ComprobanteDetalle."IdComprobanteDetalle" ,
      CASE WHEN IFNULL(Comprobante."FechaIngresoRegistro",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  Comprobante."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (Comprobante."FechaIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
		END  AS "FechaIngresoRegistro",

      CASE WHEN Comprobante."IdComprobante" = 0 THEN NULL ELSE Comprobante."IdComprobante" END AS "IdComprobante",
      LEFT(TO_CHAR(IFNULL(Comprobante."FechaMovimiento",'0001-01-01')),10) AS "FechaComprobante",

      CASE WHEN ComprobanteDetalle."IdTipoMovimiento"  = 'MOVING' THEN CAST('INGRESO' AS VARCHAR(10)) ELSE CAST('EGRESO' AS NVARCHAR(10)) END AS "TipoMovimiento",

      ComprobanteDetalle."NroLinea" AS "NumeroLinea",
      
      CASE WHEN ComprobanteDetalle."IdConcepto" = '' THEN NULL ELSE CAST(ComprobanteDetalle."IdConcepto" AS NVARCHAR(6)) END AS "IdConceptoDetalle",
      
      coalesce(CAST(Concepto."Nombre" AS NVARCHAR(100)),'') AS "NombreConceptoDetalle",
      CASE WHEN ComprobanteDetalle."IdMoneda" = '' THEN NULL ELSE CAST(ComprobanteDetalle."IdMoneda" AS NVARCHAR(3)) END AS "IdMonedaDetalle",
      CAST(MONDET."Nombre" AS NVARCHAR(100)) AS "NombreMonedaDetalle",
      ComprobanteDetalle."Monto" AS "MontoDetalle",

      CASE WHEN Comprobante."IdConcepto" = '' THEN NULL ELSE CAST(Comprobante."IdConcepto" AS NVARCHAR(6)) END AS "IdConceptoComprobante" ,
      coalesce(CAST(ConceptoCmbte."Nombre" AS NVARCHAR(100)),'') AS "NombreConceptoComprobante",
      CASE WHEN Comprobante."IdMoneda" = '' THEN NULL ELSE CAST(Comprobante."IdMoneda" AS NVARCHAR(3)) END AS "IdMonedaComprobante" ,
      CAST(MONCMBT."Nombre" AS NVARCHAR(100)) AS "NombreMonedaComprobante",
      Comprobante."Monto" AS "MontoComprobante" ,
      LEFT(TO_CHAR(IFNULL(Comprobante."FechaLiquidacion",'0001-01-01')),10) AS "FechaLiquidacionComprobante",
      CASE WHEN Comprobante."IdCaja" = 0 THEN NULL ELSE Comprobante."IdCaja" END AS "IdCaja",

      CAST(ComprobanteDetalle."SecuenciaAgrupacion" AS NVARCHAR(40)) AS "SecuenciaAgrupacion",
      CASE WHEN ComprobanteDetalle."SecuenciaRelacionada" = 0 THEN NULL ELSE ComprobanteDetalle."SecuenciaRelacionada" END AS "IdMovimientoRelacionado",
      
      CASE WHEN ComprobanteDetalle."IdEmpleadoDigitador" = 0 THEN NULL ELSE ComprobanteDetalle."IdEmpleadoDigitador" END AS "IdEmpleadoDigitador",
      CASE WHEN ComprobanteDetalle."IdSucursalEmpleadoDigitador" = 0 THEN NULL ELSE ComprobanteDetalle."IdSucursalEmpleadoDigitador" END AS "IdSucursalEmpleadoDigitador",

      CAST(ComprobanteDetalle."IdTipoComprobanteOrigen" AS NVARCHAR(3)) AS "IdTipoComprobanteOrigen" ,
      CASE WHEN ComprobanteDetalle."IdComprobanteOrigen" = 0 THEN NULL ELSE ComprobanteDetalle."IdComprobanteOrigen" END AS "IdComprobanteOrigen",
      LEFT(TO_CHAR(IFNULL(ComprobanteDetalle."FechaComprobanteOrigen",'0001-01-01')),10) AS "FechaComprobanteOrigen",
      LEFT(TO_CHAR(IFNULL(ComprobanteDetalle."FechaLiquidacionComprobanteOrigen",'0001-01-01')),10) AS "FechaLiquidacionComprobanteOrigen",

      CASE WHEN ComprobanteDetalle."IdTipoProducto" = '' THEN NULL ELSE CAST(ComprobanteDetalle."IdTipoProducto" AS NVARCHAR(4)) END   AS "IdTipoProducto",
      coalesce(CAST(TipoProducto."Nombre" AS NVARCHAR(100)),'') AS "NombreTipoProducto" ,

      CASE WHEN ComprobanteDetalle."IdTipoOperacion" = '' THEN NULL ELSE CAST(ComprobanteDetalle."IdTipoOperacion" AS NVARCHAR(6)) END    AS "IdTipoOperacion",
      coalesce(CAST(TipoOperacion."Nombre" AS NVARCHAR(100)),'') AS "NombreTipoOperacion" ,
      CASE WHEN ComprobanteDetalle."IdMonedaOperacion" = '' THEN NULL ELSE CAST(ComprobanteDetalle."IdMonedaOperacion" AS NVARCHAR(3)) END AS "IdMonedaOperacion",
      ComprobanteDetalle."MontoOperacion" ,
      CASE WHEN ComprobanteDetalle."IdBolsa" = '' THEN NULL ELSE CAST(ComprobanteDetalle."IdBolsa" AS NVARCHAR(3)) END AS "IdBolsa",

      LEFT(TO_CHAR(IFNULL(ComprobanteDetalle."FechaParidad",'0001-01-01')),10) AS "FechaParidad",
      ComprobanteDetalle."ValorParidad" ,

      CASE WHEN ComprobanteDetalle."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE CAST(ComprobanteDetalle."IdClasificacionTipoInstrumento" AS NVARCHAR(3)) END  AS "IdClasificacionTipoInstrumento",
      CASE WHEN ComprobanteDetalle."IdTipoInstrumento" IN ('','0') THEN NULL ELSE CAST(ComprobanteDetalle."IdTipoInstrumento" AS NVARCHAR(3)) END  AS "IdTipoInstrumento",
      
      ComprobanteDetalle."FolioCartera" ,
      CASE WHEN ComprobanteDetalle."IdInstrumento" = 0 THEN NULL ELSE ComprobanteDetalle."IdInstrumento" END AS "IdInstrumento",
      coalesce(CAST(INS."Nemotecnico" AS NVARCHAR(40)),'') AS "Nemotecnico",
      CASE WHEN ComprobanteDetalle."IdTipoReajuste" = '' THEN NULL ELSE CAST(ComprobanteDetalle."IdTipoReajuste" AS NVARCHAR(3)) END  AS "IdTipoReajuste",

      ComprobanteDetalle."IdCarga" ,
      CASE WHEN ComprobanteDetalle."IdEstado" = '' THEN NULL ELSE CAST(ComprobanteDetalle."IdEstado"  AS NVARCHAR(3)) END as "EstadoComprobanteDetalle",
      CAST(Estado."Nombre" AS NVARCHAR(100)) AS "NombreEstadoComprobanteDetalle" ,
      
      CAST(Comprobante."IdTipoPersonaTitularPago" AS NVARCHAR(3)) AS "IdTipoPersonaTitularPago",
      CASE WHEN Comprobante."IdPersonaTitularPago" = 0 THEN NULL ELSE Comprobante."IdPersonaTitularPago" END AS "IdPersonaTitularPago",
      CAST(Comprobante."NombrePersonaTitularPago" AS NVARCHAR(300)) AS "NombrePersonaTitularPago",
      CASE WHEN Comprobante."IdPortafolioTitularPago" = 0 THEN NULL ELSE Comprobante."IdPortafolioTitularPago" END AS "IdPortafolioTitularPago",

      CAST(Comprobante."IdTipoPersona" AS NVARCHAR(3)) AS "IdTipoPersona",
      CASE WHEN Comprobante."IdPersona" = 0 THEN NULL ELSE Comprobante."IdPersona"  END AS "IdPersona" ,
      CAST(Comprobante."NombrePersona" AS NVARCHAR(300)) AS "NombrePersona",
      CAST(Comprobante."IdTipoDocumentoIdentidad" AS NVARCHAR(3)) AS "IdTipoDocumentoIdentidad",
      CAST(Comprobante."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidad" ,

      Comprobante."EsBilateral" ,
      
      CASE WHEN TRIM("ComprobanteNeteado") IN ('N','') THEN 'N' ELSE 'S' END as "ComprobanteNeteado" ,
      CASE WHEN TRIM("SeparadoPorConcepto") IN ('N','') THEN 'N' ELSE 'S' END as "SeparadoPorConcepto" ,

      CASE WHEN Comprobante."IdEstado" = '' THEN NULL ELSE CAST(Comprobante."IdEstado" AS NVARCHAR(3)) END AS "IdEstadoComprobante",
      CAST(EST."Nombre" AS NVARCHAR(100))AS "NombreEstadoComprobante" ,
               
      Comprobante."IdUsuario"
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ComprobanteDetalle 
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante ON Comprobante."IdComprobante" = ComprobanteDetalle."IdComprobante" 
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS ConceptoCmbte ON (ConceptoCmbte."IdConcepto" = Comprobante."IdConcepto")
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONCMBT ON (MONCMBT."IdMoneda" = Comprobante."IdMoneda") 
CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
			            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TipoMovimiento ON TipoMovimiento."IdTipoMovimiento" = ComprobanteDetalle."IdTipoMovimiento" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado ON ComprobanteDetalle."IdEstado" = Estado."IdEstado" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON (Concepto."IdConcepto" = ComprobanteDetalle."IdConcepto")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = ComprobanteDetalle."IdInstrumento" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS TipoProducto ON (TipoProducto."IdTipoProducto" = ComprobanteDetalle."IdTipoProducto") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TipoOperacion ON (TipoOperacion."IdTipoOperacion" = ComprobanteDetalle."IdTipoOperacion")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = Comprobante."IdEstado") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONDET ON (MONDET."IdMoneda" = ComprobanteDetalle."IdMoneda") 
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalleAnalytics" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO