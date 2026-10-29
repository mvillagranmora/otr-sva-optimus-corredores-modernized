SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosAnalitica"; 

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosAnalitica" As 
SELECT
      CmbteCobro."IdComprobanteCobro",
      CASE WHEN IFNULL(CmbteCobro."FechaIngresoRegistro",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  CmbteCobro."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (CmbteCobro."FechaIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
	  END AS "FechaIngresoRegistro",
      CASE WHEN CmbteCobro."IdComprobante" = 0 THEN NULL ELSE CmbteCobro."IdComprobante" END AS "IdComprobante",

      LEFT(TO_CHAR(IFNULL(comprobante."FechaComprobante",'0001-01-01')),10) AS "FechaComprobante",
      
      LEFT(TO_CHAR(IFNULL(CmbteCobro."FechaSistema",'0001-01-01')),10) AS "FechaSistema",
 
      CASE WHEN "Cobro_$1"."IdCobroClasificacion" = 0 THEN NULL ELSE "Cobro_$1"."IdCobroClasificacion" END AS "IdCobroClasificacion" ,
      COALESCE(CAST("CobroClasificacion_$1"."CodigoCobroClasificacion" AS NVARCHAR(40)),'') AS "CodigoCobroClasificacion",

      CASE WHEN CmbteCobro."IdCobro" = '' THEN NULL ELSE CmbteCobro."IdCobro" END AS "IdCobro",
      CAST("Cobro_$1"."NombreCobro" AS NVARCHAR(100)) AS "NombreCobro",
      Case "Cobro_$1"."FormaCobro" When 'V' 
            Then CAST('Variable' AS NVARCHAR(100)) 
            Else CAST('Fijo' AS NVARCHAR(100)) 
      End As "FormaCobro" ,
      CmbteCobro."Porcentaje",

      CASE WHEN CmbteCobro."IdMonedaCobro" = '' THEN NULL ELSE CmbteCobro."IdMonedaCobro" END AS "IdMonedaCobroOperacion",
      CmbteCobro."MontoCobro" AS "MontoCobroMonedaOperacion",
      
      CASE WHEN CmbteCobro."IdMonedaComprobanteCobro" = '' THEN NULL ELSE CmbteCobro."IdMonedaComprobanteCobro" END AS "IdMonedaCobroSistema",
      CAST("Moneda_$4"."Nombre" AS NVARCHAR(100)) AS "NombreMonedaSistema",
      Formato."CantidadDecimales" AS "DecimalesMonedaSistema",
      CmbteCobro."MontoComprobanteCobro" AS "MontoCobroMonedaSistema",
      
      CASE WHEN CmbteCobro."IdMonedaLiquidacionCobro" = '' THEN NULL ELSE CmbteCobro."IdMonedaLiquidacionCobro" END AS "IdMonedaCobroLiquidacion",
      CmbteCobro."MontoLiquidacionCobro" AS "MontoCobroMonedaLiquidacion",

      CASE WHEN CmbteCobro."IdEstado" = '' THEN NULL ELSE CmbteCobro."IdEstado" END AS "IdEstado",
      CAST("Estado_$3"."Nombre" AS NVARCHAR(100)) AS "NombreEstado"
      
FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" AS CmbteCobro 
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS comprobante  ON comprobante."IdComprobante" = CmbteCobro."IdComprobante"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIGURACION  ON CONFIGURACION."CodigoParametro" = 'PLA.SegundosZonaHoraria' and comprobante."IdEmpresa" = CONFIGURACION."IdEmpresa"
LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS "Cobro_$1" ON ("Cobro_$1"."IdCobro" = CmbteCobro."IdCobro") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS "CobroClasificacion_$1" ON ("CobroClasificacion_$1"."IdCobroClasificacion" = "Cobro_$1"."IdCobroClasificacion") 
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS "EstadoFacturacion_$2" ON ("EstadoFacturacion_$2"."IdEstado" = CmbteCobro."IdEstado") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$3" ON ("Estado_$3"."IdEstado" = "EstadoFacturacion_$2"."IdEstado") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda_$4" ON ("Moneda_$4"."IdMoneda" = CmbteCobro."IdMonedaComprobanteCobro")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS Formato 
       ON Formato."IdMoneda" = CmbteCobro."IdMonedaComprobanteCobro" AND Formato."IdUsoMonedaFormato" = 'GEN' AND Formato."IdTipoOperacion" = '' AND Formato."IdTipoInstrumento" = ''
WHERE Upper("EstadoFacturacion_$2"."NombreEntidad") = Upper('FAC.ComprobanteCobro');

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
