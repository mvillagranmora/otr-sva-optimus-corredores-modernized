SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalleAnalitica";
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalleAnalitica" As 
SELECT  
      CmbteDet."IdComprobanteDetalle",
      
      CASE WHEN IFNULL(CmbteDet."FechaIngresoRegistro",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  CmbteDet."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (CmbteDet."FechaIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
	  END AS "FechaIngresoRegistro",

      CASE WHEN CmbteDet."IdComprobante" = 0 THEN NULL ELSE CmbteDet."IdComprobante" END AS "IdComprobante",
      LEFT(TO_CHAR(IFNULL("cmp"."FechaComprobante",'0001-01-01')),10) AS "FechaComprobante",

      CmbteDet."NroLinea" AS "NumeroLinea",

      CmbteDet."IdConcepto" AS "IdConcepto",
      CAST(COALESCE(concepto."Nombre",'') AS NVARCHAR(100)) AS "NombreConcepto",

      CASE WHEN CmbteDet."IdInstrumento" = 0 THEN NULL ELSE CmbteDet."IdInstrumento" END AS "IdInstrumento",
      (Case When CmbteDet."IdTipoInstrumento" in ('PRT',
            'EMP',
            'SIM') 
            then CAST(CmbteDet."NemotecnicoSubyacente" AS NVARCHAR(40))  
            Else (CASE WHEN TRANS."IdClasificacionTipoInstrumento" ='IF' THEN CAST(TRANS."NemotecnicoSVS" AS NVARCHAR(40))ELSE CAST(CmbteDet."NemotecnicoInstrumento" AS NVARCHAR(40)) END)	 
      END) As "Nemotecnico",

     CmbteDet."Cantidad" ,
      CmbteDet."Precio" ,

      CASE WHEN "cmp"."IdMoneda" = '' THEN NULL ELSE "cmp"."IdMoneda" END  AS "IdMonedaOperacion",
      CmbteDet."Monto" AS "MontoMonedaOperacion",
      
      CASE WHEN "cmp"."IdMonedaComprobante" = '' THEN NULL ELSE "cmp"."IdMonedaComprobante" END AS "IdMonedaSistema",
      CmbteDet."MontoComprobanteDetalle" AS "MontoMonedaSistema",
      
      LEFT(TO_CHAR(IFNULL(CmbteDet."FechaLiquidacion",'0001-01-01')),10)  AS "FechaLiquidacion",
      CASE WHEN "cmp"."IdMonedaLiquidacion" = '' THEN NULL ELSE "cmp"."IdMonedaLiquidacion" END AS "IdMonedaLiquidacion", 
      CmbteDet."MontoLiquidacion" AS "MontoMonedaLiquidacion",

      case when CmbteDet."IdTipoInstrumento" = '' then null else CmbteDet."IdTipoInstrumento" end  AS "IdTipoInstrumento",
      COALESCE(CAST(TipoIns."Codigo" AS NVARCHAR(40)),'') AS "CodigoTipoInstrumento" ,
      COALESCE(CAST(TipoIns."Nombre" AS NVARCHAR(100)),'') AS "NombreTipoInstrumento",

      CASE WHEN CmbteDet."IdTipoOperacion" = '' THEN NULL ELSE CmbteDet."IdTipoOperacion" END AS "IdTipoOperacion",
      CAST(tipooperacion."Codigo" AS NVARCHAR(40)) AS "CodigoTipoOperacion",
      CAST(tipooperacion."Nombre" AS NVARCHAR(100)) AS "NombreTipoOperacion",

      case when CmbteDet."IdTipoProducto" = '' then null else CmbteDet."IdTipoProducto" end AS "IdTipoProducto",
      COALESCE(CAST(tipoproducto."Codigo" AS NVARCHAR(40)),'') AS "CodigoTipoProducto",
      COALESCE(CAST(tipoproducto."Nombre" AS NVARCHAR(100)),'') AS "NombreTipoProducto",

      CASE WHEN TRIM(CmbteDet."IdBolsa") = '' THEN 'BCS' ELSE CmbteDet."IdBolsa" END AS "IdBolsa",
      
      CASE WHEN CmbteDet."IdEstadoComprobanteDetalle" = '' THEN NULL ELSE CmbteDet."IdEstadoComprobanteDetalle" END AS "IdEstadoComprobanteDetalle",
      CAST("Estado"."Nombre" AS NVARCHAR(100)) AS "NombreEstado",
      CmbteDet."Tasa",
      CmbteDet."FolioTransaccion"

FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS CmbteDet 
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS "cmp" ON "cmp"."IdComprobante" = CmbteDet."IdComprobante" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIGURACION ON "CodigoParametro" = 'PLA.SegundosZonaHoraria'
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRANS ON CmbteDet."IdTransaccion" = TRANS."IdTransaccion"			            
LEFT JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS "EstFac" ON "EstFac"."IdEstado" = CmbteDet."IdEstadoComprobanteDetalle" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON "Estado"."IdEstado" = "EstFac"."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS tipoproducto ON tipoproducto."IdTipoProducto" = CmbteDet."IdTipoProducto" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooperacion ON tipooperacion."IdTipoOperacion" = CmbteDet."IdTipoOperacion" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Ins ON (Ins."IdInstrumento" = CmbteDet."IdInstrumento") 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoIns ON (Ins."IdTipoInstrumento" = TipoIns."IdTipoInstrumento") 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON Concepto."IdConcepto" = CmbteDet."IdConcepto" 
WHERE Upper("EstFac"."NombreEntidad") = Upper('FAC.ComprobanteDetalle');

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalleAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
