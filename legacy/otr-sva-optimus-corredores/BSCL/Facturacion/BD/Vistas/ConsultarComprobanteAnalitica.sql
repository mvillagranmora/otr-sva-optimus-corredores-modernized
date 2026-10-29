SET SCHEMA "BSCL";

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteAnalitica";

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteAnalitica" As 
SELECT 
      comprobante."IdEmpresa" ,
      CASE WHEN IFNULL(comprobante."FechaIngresoRegistro",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  comprobante."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (comprobante."FechaIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
	  END AS "FechaIngresoRegistro",

      comprobante."IdComprobante",
      LEFT(TO_CHAR(IFNULL(comprobante."FechaComprobante",'0001-01-01')),10) AS "FechaComprobante",
      LEFT(TO_CHAR(IFNULL(comprobante."FechaSistema",'0001-01-01')),10) AS "FechaSistema",
      CASE WHEN comprobante."IdTipoComprobante" = '' THEN NULL ELSE comprobante."IdTipoComprobante" END AS "IdTipoComprobante",
      coalesce(tipocomprobante."Codigo",'') AS "CodigoTipoComprobante",
      COALESCE(CAST(tipocomprobante."Nombre" AS NVARCHAR(100)),'') AS "NombreTipoComprobante",
      CASE WHEN comprobante."IdEstadoComprobante" = '' THEN NULL ELSE comprobante."IdEstadoComprobante" END AS "IdEstadoComprobante",
      CAST(estado."Nombre" AS NVARCHAR(100)) AS "NombreEstado" ,

      CASE WHEN comprobante."IdPortafolio" = 0 THEN NULL ELSE comprobante."IdPortafolio" END AS "IdPortafolio",
      CAST(COALESCE(viewportalio."CodigoPortafolio",'') AS NVARCHAR(100)) AS "CodigoPortafolio",
      CAST(COALESCE(viewportalio."Proposito",'') AS NVARCHAR(520)) AS "Proposito",
      CAST(COALESCE(viewportalio."DescripcionPortafolio",'') AS NVARCHAR(100)) AS "DescripcionPortafolio",

      CASE WHEN comprobante."IdCliente" = 0 THEN NULL ELSE comprobante."IdCliente" END AS "IdCliente",
      CAST(comprobante."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidad",
      comprobante."TipoDocumentoIdentidad",
      CAST(comprobante."NombreLargoCliente" AS NVARCHAR(302)) AS "NombreLargoCliente",

      CASE WHEN comprobante."IdConcepto" = '' THEN NULL ELSE comprobante."IdConcepto" END AS "IdConcepto",
      CAST(COALESCE(concepto."Nombre",'') AS NVARCHAR(100)) AS "NombreConcepto",

      comprobante."EsExento",

      COALESCE(comprobante."IdMoneda",'') AS "MonedaOperacion",
      COALESCE(comprobante."Monto",0) AS "MontoMonedaOperacion",
      COALESCE(comprobante."MontoCobro",0) AS "TotalCobrosMonedaOperacion",
      IFNULL(tblCobros."TotalComis",     0) as "TotalComisionMonedaOperacion",
      IFNULL(tblCobros."TotalImpto",     0) as "TotalImpuestoMonedaOperacion",
      IFNULL(tblCobros."TotalDere", 0) as "TotalDerechoMonedaOperacion",
      IFNULL(tblCobros."TotalGasto",     0) as "TotalGastoMonedaOperacion",
      IFNULL(tblCobros."TotalReten",     0) as "TotalRetencionMonedaOperacion",

      CASE WHEN comprobante."IdMonedaComprobante" = '' THEN NULL ELSE comprobante."IdMonedaComprobante" END  AS "MonedaSistema",
      CAST(moneda."Codigo" AS NVARCHAR(40)) AS "CodigoMonedaSistema" ,
      CAST(moneda."Nombre" AS NVARCHAR(100)) AS "NombreMonedaSistema",
      moneda."Simbolo" AS "SimboloMonedaSistema" ,
      Formato."CantidadDecimales" AS "DecimalesMonedaSistema" ,
      COALESCE(comprobante."MontoComprobante",0) AS "MontoMonedaSistema", 
      COALESCE(comprobante."MontoComprobanteCobro",0) AS "TotalCobrosMonedaSistema",
      COALESCE(comprobante."MontoFinal",0) AS "MontoFinalMonedaSistema",

      CASE WHEN comprobante."IdCondicionLiquidacion" = '' THEN NULL ELSE comprobante."IdCondicionLiquidacion" END AS "IdCondicionLiquidacion",
      LEFT(TO_CHAR(IFNULL(comprobante."FechaLiquidacion",'0001-01-01')),10) AS "FechaLiquidacion",
      CASE WHEN comprobante."IdMonedaLiquidacion" = '' THEN NULL ELSE  comprobante."IdMonedaLiquidacion" END AS "IdMonedaLiquidacion", 
      CAST(COALESCE(monedaLiq."Nombre",'') AS NVARCHAR(100)) AS "NombreMonedaLiquidacion" ,
      COALESCE(comprobante."MontoLiquidacion",0) AS "MontoMonedaLiquidacion",
      COALESCE(comprobante."MontoLiquidacionCobro",0) AS "TotalCobrosMonedaLiquidacion",

      CASE WHEN comprobante."IdTipoOperacion" = '' THEN NULL ELSE comprobante."IdTipoOperacion" END AS "IdTipoOperacion" ,
      CAST(COALESCE(tipooperacion."Accion",'') AS NVARCHAR(40)) As "AccionTipoOperacion",
      CAST(COALESCE(tipooperacion."Codigo",'') AS NVARCHAR(40)) AS "CodigoTipoOperacion",
      CAST(COALESCE(tipooperacion."Nombre",'') AS NVARCHAR(100)) AS "NombreTipoOperacion",

      case when comprobante."IdTipoProducto" = '' then null else comprobante."IdTipoProducto" end  AS "IdTipoProducto",
      CAST(COALESCE(tipoproducto."Codigo",'') AS NVARCHAR(40)) AS "CodigoTipoProducto",
      CAST(COALESCE(tipoproducto."Nombre",'') AS NVARCHAR(100)) AS "NombreTipoProducto",
      case when comprobante."IdClasificacionTipoInstrumento" = '' then null else comprobante."IdClasificacionTipoInstrumento" end AS "IdClasificacionTipoInstrumento"  ,

      case when comprobante."IdTipoInstrumento" in ('','0') then null else comprobante."IdTipoInstrumento" end  AS "IdTipoInstrumento" ,
      CAST(COALESCE(TipIns."Nombre",'') AS NVARCHAR(100)) As "NombreTipoInstrumento",
      CASE WHEN comprobante."IdBolsa" = '' THEN NULL ELSE comprobante."IdBolsa" END  AS "IdBolsa",
      COALESCE(CAST(Bolsa."Nombre" AS NVARCHAR(100)),'') As "NombreBolsa",
      
      CASE WHEN comprobante."IdEmpleadoEjecutivoPortafolio" = 0 THEN NULL ELSE comprobante."IdEmpleadoEjecutivoPortafolio" END AS "IdEmpleadoEjecutivoPortafolio",
      COALESCE(CAST(comprobante."NombreEmpleadoEjecutivoPortafolio" AS NVARCHAR(302)),'') AS  "NombreEmpleadoEjecutivoPortafolio",
      CASE WHEN comprobante."IdSucursalEmpleadoEjecutivoPortafolio" = 0 THEN NULL ELSE comprobante."IdSucursalEmpleadoEjecutivoPortafolio" END AS "IdSucursalEmpleadoEjecutivoPortafolio",
      CAST(comprobante."NombreSucursalEmpleadoEjecutivoPortafolio" AS NVARCHAR(100)) AS "NombreSucursalEmpleadoEjecutivoPortafolio",
  
  
      LEFT(TO_CHAR(IFNULL(comprobante."FechaEnvio",'0001-01-01')),10) AS "FechaEnvioDTE",

      CASE WHEN comprobante."IdEstadoDTE" = '' THEN NULL ELSE comprobante."IdEstadoDTE" END AS "IdEstadoDTE",
      CAST(estadodte."Nombre" AS NVARCHAR(100)) AS "NombreEstadoDTE",

      CASE WHEN comprobante."IdEstadoSII" = '' THEN NULL ELSE comprobante."IdEstadoSII" END AS "IdEstadoSII",
      CAST(estadosii."Nombre" AS NVARCHAR(100)) AS "NombreEstadoSII",
      comprobante."FolioComprobante",
      
      LEFT(TO_VARCHAR(IFNULL(comprobante."FechaEmision",'0001-01-01')),10) AS "FechaEmision",

      CASE WHEN comprobante."IdComprobanteReferencia" = 0 THEN NULL ELSE comprobante."IdComprobanteReferencia" END AS "IdComprobanteReferencia",
      comprobante."IdTipoComprobanteReferencia",
      comprobante."FolioComprobanteReferencia",
      LEFT(TO_CHAR(IFNULL(comprobante."FechaComprobanteReferencia",'0001-01-01')),10) AS "FechaComprobanteReferencia",
      
      CAST(empresa."Codigo" AS NVARCHAR(100)) AS "CodigoEmpresa" ,
      CAST(empresa."Nombre" AS NVARCHAR(100)) AS "NombreEmpresa" ,
      COALESCE(CAST(comprobante."Glosa" AS NVARCHAR(260)),'') AS "Glosa",
      CAST(comprobante."DireccionFacturacion" AS NVARCHAR(100)) AS  "DireccionFacturacion",
	  COALESCE(CAST(comprobante."IdMonedaNegociacion" AS NVARCHAR(10)),'') As "IdMonedaNegociacion"


FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS comprobante 
CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
			            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION 
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = comprobante."IdTipoComprobante" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS empresa ON empresa."IdEmpresa" = comprobante."IdEmpresa" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS moneda ON moneda."IdMoneda" = comprobante."IdMonedaComprobante" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS monedaLiq ON monedaLiq."IdMoneda" = comprobante."IdMonedaLiquidacion" 
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS estadofac ON estadofac."IdEstado" = comprobante."IdEstadoComprobante" 
and Upper(estadofac."NombreEntidad") = Upper('FAC.Comprobante') 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estado ON estado."IdEstado" = estadofac."IdEstado" 
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS estadofacdte ON estadofacdte."IdEstado" = comprobante."IdEstadoDTE" 
and Upper(estadofacdte."NombreEntidad") = Upper('FAC.Comprobante') 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estadodte ON estadodte."IdEstado" = estadofacdte."IdEstado" 
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS estadofacsii ON estadofacsii."IdEstado" = comprobante."IdEstadoSII" 
and Upper(estadofacsii."NombreEntidad") = Upper('FAC.Comprobante') 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estadosii ON estadosii."IdEstado" = estadofacsii."IdEstado" 
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as viewportalio ON viewportalio."IdPortafolio" = comprobante."IdPortafolio" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooperacion ON tipooperacion."IdTipoOperacion" = comprobante."IdTipoOperacion" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS tipoproducto ON tipoproducto."IdTipoProducto" = comprobante."IdTipoProducto" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Bolsa" As Bolsa ON Bolsa."IdBolsa" = comprobante."IdBolsa" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" As TipIns On TipIns."IdTipoInstrumento" = comprobante."IdTipoInstrumento" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON Concepto."IdConcepto" = comprobante."IdConcepto" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS Formato 
       ON Formato."IdMoneda" = comprobante."IdMonedaLiquidacion" AND Formato."IdUsoMonedaFormato" = 'GEN' AND Formato."IdTipoOperacion" = '' AND Formato."IdTipoInstrumento" = ''
LEFT OUTER JOIN ( SELECT
      "IdComprobante",
      MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' 
            THEN "MontoCobro" 
            ELSE 0 
            END) AS "TotalComis",
      MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' 
            THEN "MontoCobro" 
            ELSE 0 
            END) AS "TotalDere",
      MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' 
            THEN "MontoCobro" 
            ELSE 0 
            END) AS "TotalGasto",
      MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' 
            THEN "MontoCobro" 
            ELSE 0 
            END) AS "TotalImpto",
      MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' 
            THEN "MontoCobro" 
            ELSE 0 
            END) AS "TotalReten" 
      FROM ( SELECT
      "IdComprobante",
      "IdCobroClasificacion",
      "CodigoCobroClasificacion",
      IFNULL(SUM("MontoCobro"),
      0) as "MontoCobro" 
            FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" 
            GROUP BY "IdComprobante",
      "IdCobroClasificacion",
      "CodigoCobroClasificacion" 
            ORDER BY "IdCobroClasificacion" ) 
      group by "IdComprobante" ) AS tblCobros on tblCobros."IdComprobante" = comprobante."IdComprobante";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;