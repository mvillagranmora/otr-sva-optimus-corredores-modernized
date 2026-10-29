SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaPagoAnalitica"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaPagoAnalitica" As
SELECT   
      "CmbteFPag"."IdComprobanteFormaDePago",
      CASE WHEN IFNULL("CmbteFPag"."FechaIngresoRegistro",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  "CmbteFPag"."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS ("CmbteFPag"."FechaIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
		END AS "FechaIngresoRegistro",

      CASE WHEN "CmbteFPag"."IdComprobante" = 0 THEN NULL ELSE "CmbteFPag"."IdComprobante" END AS "IdComprobante",
      LEFT(TO_CHAR(IFNULL(Comprobante."FechaComprobante",'0001-01-01')),10) AS "FechaComprobante",
      CASE WHEN "CmbteFPag"."IdMedioDePago" = '' THEN NULL ELSE "CmbteFPag"."IdMedioDePago" END AS "IdMedioDePago",
      CAST("CmbteFPag"."NombreMedioDePago" AS NVARCHAR(100)) AS "NombreMedioDePago",

      CAST("CmbteFPag"."NumeroSerie" AS NVARCHAR(40)) AS "NumeroSerie",
      "CmbteFPag"."NumeroDocumento",
      CASE WHEN "CmbteFPag"."IdCuentaBancaria" = 0 THEN NULL ELSE "CmbteFPag"."IdCuentaBancaria" END AS "IdCuentaBancaria",
      CAST("CmbteFPag"."NombreCuentaBancaria" AS NVARCHAR(100)) AS "NombreCuentaBancaria",

      "CmbteFPag"."IdTipoCuentaBancaria" AS "IdTipoCuentaBancaria",
      CAST("CmbteFPag"."NombreTipoCuentaBancaria" AS NVARCHAR(100)) AS "NombreTipoCuentaBancaria",

      CAST("CmbteFPag"."NumeroCuentaBancaria" AS NVARCHAR(100)) AS "NumeroCuentaBancaria",
      CASE WHEN "CmbteFPag"."IdTarjetaBancaria" = 0 THEN NULL ELSE "CmbteFPag"."IdTarjetaBancaria" END AS "IdTarjetaBancaria",
      CAST("CmbteFPag"."IdTipoTarjetaBancaria" AS NVARCHAR(40))AS "IdTipoTarjetaBancaria",
      CAST("CmbteFPag"."NombreTipoTarjetaBancaria" AS NVARCHAR(100)) AS "NombreTipoTarjetaBancaria",
      CAST("CmbteFPag"."NumeroTarjetaBancaria" AS NVARCHAR(100)) AS "NumeroTarjetaBancaria",
      LEFT(TO_CHAR(IFNULL("CmbteFPag"."FechaVctoTarjetaBancaria",'0001-01-01')),10) AS "FechaVctoTarjetaBancaria",
      CASE WHEN "CmbteFPag"."IdModalidadDePago" = '' THEN NULL ELSE "CmbteFPag"."IdModalidadDePago" END AS "IdModalidadDePago",
      CAST("CmbteFPag"."NombreModalidadDePago" AS NVARCHAR(100)) AS "NombreModalidadDePago",
      CASE WHEN "CmbteFPag"."IdBanco" = 0 THEN NULL ELSE  "CmbteFPag"."IdBanco" END AS "IdBanco",
      CAST("CmbteFPag"."CodigoBanco" AS NVARCHAR(40)) AS "CodigoBanco",
      CAST("CmbteFPag"."NombreBanco" AS NVARCHAR(100)) AS "NombreBanco",
      LEFT(TO_CHAR(IFNULL("CmbteFPag"."FechaDocumentoPago",'0001-01-01')),10) AS "FechaDocumentoPago",
      CASE WHEN "CmbteFPag"."IdMoneda" = '' THEN NULL ELSE "CmbteFPag"."IdMoneda" END AS "IdMonedaLiquidacion",
      "CmbteFPag"."MontoLiquidacionFormaDePago" AS "MontoMonedaLiquidacion",
      CASE WHEN "CmbteFPag"."IdEstado" = '' THEN NULL ELSE "CmbteFPag"."IdEstado" END AS "IdEstado" 
From "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago" As "CmbteFPag" 
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS Comprobante ON Comprobante."IdComprobante" = "CmbteFPag"."IdComprobante" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIGURACION  ON CONFIGURACION."CodigoParametro" = 'PLA.SegundosZonaHoraria' and comprobante."IdEmpresa" = CONFIGURACION."IdEmpresa"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioDePago" ON ("MedioDePago"."IdMedioDePago" = "CmbteFPag"."IdMedioDePago") 
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS "EstadoFacturacion_$2" ON ("EstadoFacturacion_$2"."IdEstado" = "CmbteFPag"."IdEstado") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$3" ON ("Estado_$3"."IdEstado" = "EstadoFacturacion_$2"."IdEstado") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda_$4" ON ("Moneda_$4"."IdMoneda" = "CmbteFPag"."IdMoneda") 
WHERE Upper("EstadoFacturacion_$2"."NombreEntidad") = Upper('FAC.ComprobanteFormaDePago');

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteFormaPagoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
