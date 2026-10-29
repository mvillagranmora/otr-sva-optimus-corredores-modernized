SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaPagoCobroAnalytics";
GO 
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaPagoCobroAnalytics" AS
SELECT
      Comprobante."IdEmpresa",
      CASE WHEN IFNULL(ComprobanteFormaDePago."FechaIngresoRegistro",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  ComprobanteFormaDePago."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (ComprobanteFormaDePago."FechaIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
	  END AS "FechaIngresoRegistro",

      CASE WHEN ComprobanteFormaDePago."IdComprobante" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdComprobante" END AS "IdComprobante" ,
      LEFT(TO_CHAR(IFNULL(Comprobante."FechaMovimiento",'0001-01-01')),10) AS "FechaComprobante",
      
      CASE WHEN ComprobanteFormaDePago."IdComprobanteFormaDePago" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdComprobanteFormaDePago" END AS "IdComprobanteFormaDePago",
      
      CASE WHEN ComprobanteFormaDePago."IdTipoMovimiento" = 'MOVING' THEN CAST('INGRESO' AS NVARCHAR(10)) ELSE CAST('EGRESO' AS NVARCHAR(10)) END AS "TipoComprobante",

      CASE WHEN ComprobanteFormaDePago."IdMedioDePago" = '' THEN NULL ELSE CAST(ComprobanteFormaDePago."IdMedioDePago" AS NVARCHAR(3)) END AS "IdMedioDePago" ,
      CAST(ComprobanteFormaDePago."NombreMedioDePago" AS NVARCHAR(100)) AS "NombreMedioDePago",
      CAST(coalesce(MedioDePago."TipoMedioDePago",'') AS NVARCHAR(3)) as "TipoMedioDePago"  ,
      CASE WHEN ComprobanteFormaDePago."IdModalidadDePago" = '' THEN NULL ELSE CAST(ComprobanteFormaDePago."IdModalidadDePago" AS NVARCHAR(6)) END AS "IdModalidadDePago",
      CAST(ComprobanteFormaDePago."NombreModalidadDePago" AS NVARCHAR(100)) AS "NombreModalidadDePago",

      CASE WHEN Comprobante."IdCaja" = 0 THEN NULL ELSE Comprobante."IdCaja" END  AS "IdCaja"  ,
      coalesce(CAST(Caja."Codigo" AS NVARCHAR(40)),'') AS "CodigoCaja",
      coalesce(CAST(Caja."Nombre" AS NVARCHAR(100)),'') AS "NombreCaja" ,

	  CASE WHEN ComprobanteFormaDePago."IdCuentaBancariaCorredora" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdCuentaBancariaCorredora" END AS "IdCuentaBancariaCorredora",
      CAST(ComprobanteFormaDePago."IdTipoCuentaBancariaCorredora" AS NVARCHAR(4)) AS "IdTipoCuentaBancariaCorredora",
      CAST(ComprobanteFormaDePago."NombreTipoCuentaBancariaCorredora" AS NVARCHAR(100)) AS "NombreTipoCuentaBancariaCorredora",
      CASE WHEN BancoCorredora."IdBanco" = 0 THEN NULL ELSE BancoCorredora."IdBanco" END AS "IdBancoCorredora" ,
      coalesce(CAST(BancoCorredora."NombreBanco" AS NVARCHAR(100)),'') AS "NombreBancoCorredora" ,
      coalesce(CAST(ComprobanteFormaDePago."NombreCuentaBancariaCorredora" AS NVARCHAR(100)),'') AS "NombreCuentaBancariaCorredora",
      CAST(ComprobanteFormaDePago."NumeroCuentaBancariaCorredora" AS NVARCHAR(100)) AS "NumeroCuentaBancariaCorredora",
      CASE WHEN ComprobanteFormaDePago."IdTarjetaBancariaCorredora" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdTarjetaBancariaCorredora" END AS "IdTarjetaBancariaCorredora"  ,
      CAST(ComprobanteFormaDePago."IdTipoTarjetaBancariaCorredora" AS NVARCHAR(40)) AS "IdTipoTarjetaBancariaCorredora",
      CAST(ComprobanteFormaDePago."NombreTipoTarjetaBancariaCorredora" AS NVARCHAR(100)) AS "NombreTipoTarjetaBancariaCorredora",
      CAST(ComprobanteFormaDePago."NumeroTarjetaBancariaCorredora" AS NVARCHAR(100)) AS "NumeroTarjetaBancariaCorredora",
      CASE WHEN ComprobanteFormaDePago."IdChequera" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdChequera" END AS "IdChequeraCorredora",
      coalesce(CAST(Chequera."Serie" AS NVARCHAR(40)),'') AS "NumeroSerieCorredora" ,
      ComprobanteFormaDePago."NumeroDocumentoCorredora" ,
      LEFT(TO_CHAR(IFNULL(ComprobanteFormaDePago."FechaDocumentoPagoCorredora",'0001-01-01')),10) AS "FechaDocumentoPagoCorredora",

      CASE WHEN ComprobanteFormaDePago."IdCuentaBancariaSocio" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdCuentaBancariaSocio" END AS "IdCuentaBancariaTitular",             
      CAST(ComprobanteFormaDePago."IdTipoCuentaBancariaSocio" AS NVARCHAR(4)) AS "IdTipoCuentaBancariaTitular",        
      CAST(ComprobanteFormaDePago."NombreTipoCuentaBancariaSocio" AS NVARCHAR(100)) AS "NombreTipoCuentaBancariaTitular",    
      CAST(ComprobanteFormaDePago."NombreCuentaBancariaSocio" AS NVARCHAR(100)) AS "NombreCuentaBancariaTitular",        
      CASE WHEN ComprobanteFormaDePago."IdBancoSocio" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdBancoSocio" END AS "IdBancoTitular",                     
      coalesce(CAST(BancoSocio."NombreBanco" AS NVARCHAR(100)),'')                  AS "NombreBancoTitular",
      CAST(ComprobanteFormaDePago."NumeroCuentaBancariaSocio" AS NVARCHAR(100)) AS "NumeroCuentaBancaria",        
      CASE WHEN ComprobanteFormaDePago."IdTarjetaBancariaSocio" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdTarjetaBancariaSocio" END AS "IdTarjetaBancaria",           
      CAST(ComprobanteFormaDePago."IdTipoTarjetaBancariaSocio" AS NVARCHAR(40)) AS "IdTipoTarjetaBancariaTitular",       
      CAST(ComprobanteFormaDePago."NombreTipoTarjetaBancariaSocio" AS NVARCHAR(100)) AS "NombreTipoTarjetaBancariaTitular",   
      CAST(ComprobanteFormaDePago."NumeroTarjetaBancariaSocio" AS NVARCHAR(100)) AS "NumeroTarjetaBancariaTitular",       
      CAST(ComprobanteFormaDePago."NumeroSerieSocio" AS NVARCHAR(40))  AS "NumeroSerieTitular",                 
      ComprobanteFormaDePago."NumeroDocumentoSocio"                    AS "NumeroDocumentoTitular", 
      LEFT(TO_CHAR(IFNULL(ComprobanteFormaDePago."FechaDocumentoPagoSocio",'0001-01-01')),10) AS "FechaDocumentoPagoTitular",          
      CASE WHEN ComprobanteFormaDePago."IdMoneda" = '' THEN NULL ELSE CAST(ComprobanteFormaDePago."IdMoneda" AS NVARCHAR(3)) END AS "IdMoneda",
      Formato."CantidadDecimales" AS "DecimalesMoneda", 
      CAST(Moneda."Nombre" AS NVARCHAR(100)) AS "NombreMoneda" ,

      ComprobanteFormaDePago."Monto" ,

      CASE WHEN ComprobanteFormaDePago."IdTipoMovimiento" = 'MOVEGR' 
            THEN ComprobanteFormaDePago."Monto" 
            ELSE 0 
      END AS "MontoEgreso" ,

      CASE WHEN ComprobanteFormaDePago."IdTipoMovimiento" = 'MOVING' 
            THEN ComprobanteFormaDePago."Monto" 
            ELSE 0 
      END AS "MontoIngreso" ,

      CASE WHEN ComprobanteFormaDePago."IdTipoMovimiento" = 'MOVING' 
            THEN ComprobanteFormaDePago."Monto" 
            ELSE -1*ComprobanteFormaDePago."Monto" 
      END AS "MontoConSigno" ,

      CASE WHEN ComprobanteFormaDePago."IdEstado" = '' THEN NULL ELSE CAST(ComprobanteFormaDePago."IdEstado" AS NVARCHAR(3)) END AS "IdEstadoMedioPago",
      CAST(Estado."Nombre" AS NVARCHAR(100))AS "NombreEstadoMedioPago",
      
      CASE WHEN ComprobanteFormaDePago."IdUsuarioGiro" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdUsuarioGiro" END AS "IdUsuarioGiro",
      coalesce(CAST(UsuarioGiro."NombreCompleto" AS NVARCHAR(302)),'') AS "NombreUsuarioGiro" ,
      CASE WHEN IFNULL(ComprobanteFormaDePago."FechaGiro",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  ComprobanteFormaDePago."FechaGiro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (ComprobanteFormaDePago."FechaGiro", CONFIGURACION."Valor")),19)
					 	                                  END 
	  END AS "FechaGiro",
      
      CASE WHEN ComprobanteFormaDePago."IdComprobantePartida" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdComprobantePartida" END  AS "IdComprobantePartida" ,

      CAST(Comprobante."IdTipoPersona"  AS NVARCHAR(3)) AS "IdTipoSocioNegocioPersona",
      coalesce(CAST(TipoSocio."NombreTipoSocioDeNegocio" AS NVARCHAR(100)),'') AS "NombreTipoSocioNegocioPersona",
      CASE WHEN Comprobante."IdPersona" = 0 THEN NULL ELSE Comprobante."IdPersona" END AS "IdPersona" ,
      CAST(Comprobante."NombrePersona" AS NVARCHAR(300)) AS "NombrePersona",
      CAST(Comprobante."IdTipoDocumentoIdentidad" AS NVARCHAR(3)) AS "IdTipoDocumentoIdentidadPersona",
      CAST(Comprobante."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidadPersona",
      
      TipoPersona."IdTipoPersona" as "IdTipoPersona",
      CAST(TipoPersona."Descripcion" AS NVARCHAR(100)) as "NombreTipoPersona",
      
      CAST(Comprobante."IdTipoPersonaTitularPago" AS NVARCHAR(3)) AS "IdTipoSocioNegocioPersonaTitular",
      coalesce(CAST(TipoSocioPersonaTitular."NombreTipoSocioDeNegocio" AS NVARCHAR(100)),'') AS "NombreTipoSocioNegocioPersonaTitular",
      CASE WHEN Comprobante."IdPersonaTitularPago" = 0 THEN NULL ELSE "IdPersonaTitularPago" END AS "IdPersonaTitular",
      CAST(Comprobante."NombrePersonaTitularPago" AS NVARCHAR(300)) AS "NombrePersonaTitular",
      CASE WHEN Comprobante."IdPortafolioTitularPago" = 0 THEN NULL ELSE Comprobante."IdPortafolioTitularPago" END AS "IdPortafolioPersonaTitular",
      
      TipoPersonaTitularPago."IdTipoPersona" as "IdTipoPersonaTitular",
      CAST(TipoPersonaTitularPago."Descripcion" AS NVARCHAR(100)) as "NombreTipoPersonaTitular",
      
      coalesce(CAST(Portafolio."CodigoPortafolio" AS NVARCHAR(100)),'') AS "CodigoPortafolioPersonaTitular",
      coalesce(CAST(Portafolio."CodigoPortafolio" ||' '|| Portafolio."Proposito" AS NVARCHAR(621)),'') as "NombrePortafolioPersonaTitular",

      CASE WHEN ComprobanteFormaDePago."IdUsuarioCertificacion" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdUsuarioCertificacion" END AS "IdUsuarioCertificacionMedioPago",
      coalesce(CAST(UsuarioCertifica."NombreCompleto" AS NVARCHAR(302)),'') AS "NombreUsuarioCertificacionMedioPago" ,
      CASE WHEN IFNULL(ComprobanteFormaDePago."FechaHoraCertificacion",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  ComprobanteFormaDePago."FechaHoraCertificacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (ComprobanteFormaDePago."FechaHoraCertificacion", CONFIGURACION."Valor")),19)
					 	                                  END 
	  END AS "FechaHoraCertificacionMedioPago",

      CASE WHEN ComprobanteFormaDePago."IdUsuarioLiquidacion" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdUsuarioLiquidacion" END AS "IdUsuarioLiquidacionMedioPago",
      coalesce(CAST(UsuarioLiquidacion."NombreCompleto" AS NVARCHAR(302)),'') AS "NombreUsuarioLiquidacionMedioPago",
      
      TO_CHAR(CAST((CASE WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ' 
            THEN ADD_DAYS (TO_DATE (Comprobante."FechaLiquidacion",
            'YYYY-MM-DD'),
            ComprobanteFormaDePago."DiasRetencion") 
                  ELSE ComprobanteFormaDePago."FechaHoraLiquidacion" 
                  END) AS DATE)) AS "FechaLiquidacion" ,
                  
      TO_CHAR((CASE WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ' 
      THEN ADD_DAYS (TO_DATE (Comprobante."FechaLiquidacion",
      'YYYY-MM-DD'),
      ComprobanteFormaDePago."DiasRetencion") 
      ELSE ComprobanteFormaDePago."FechaHoraLiquidacion" 
      END)) AS "FechaHoraLiquidacionMedioPago" ,

      CASE WHEN ComprobanteFormaDePago."IdDeposito" = 0 THEN NULL ELSE ComprobanteFormaDePago."IdDeposito" END AS "IdDeposito",
      COALESCE(ComprobanteFormaDePago."Depositado",'N') AS "Depositado",

      ComprobanteFormaDePago."IdNominaTransferencia",

      LEFT(TO_CHAR(IFNULL(ComprobanteFormaDePago."FechaAfectaSaldo",'0001-01-01')),10) AS "FechaAfectaSaldo" ,

      ComprobanteFormaDePago."DiasRetencion" ,
      ComprobanteFormaDePago."SaldoDisponible" ,
      ComprobanteFormaDePago."SaldoContable" ,
      
      CASE WHEN instr(TipoMovimientoMedioDePago."AfectaSaldoEnEstado",ComprobanteFormaDePago."IdEstado")> 0 
      THEN 'S' 
      ELSE 'N' 
      END AS "AfectoSaldo" ,
            CASE WHEN instr(TipoMovimientoMedioDePago."EstadosPosibleTipoMovimientoMedioDePago",
            'NDP')> 0 
      THEN 'S' 
      ELSE 'N' 
      END AS "DocumentoEnviaADeposito" ,

      CASE WHEN Comprobante."IdEstado" = '' THEN NULL ELSE CAST(Comprobante."IdEstado" AS NVARCHAR(3)) END AS "IdEstadoComprobante",
      CAST(EstadoCmbte."Nombre" AS NVARCHAR(100)) AS "NombreEstadoComprobante",
      CASE WHEN Comprobante."IdConcepto" = '' THEN NULL ELSE CAST(Comprobante."IdConcepto" AS NVARCHAR(6)) END AS "IdConceptoComprobante",
      CAST(Concepto."Nombre" AS NVARCHAR(100)) AS "NombreConceptoComprobante",
      CASE WHEN Comprobante."IdEstado" = 'LIQ' THEN LEFT(TO_CHAR(IFNULL(Comprobante."FechaLiquidacion",'0001-01-01')),10) ELSE '0001-01-01' END  AS "FechaLiquidacionComprobante" 
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago 
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante ON (Comprobante."IdComprobante" = ComprobanteFormaDePago."IdComprobante") 
      LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio ON (Portafolio."IdPortafolio" = Comprobante."IdPortafolioTitularPago") 
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoCmbte ON (EstadoCmbte."IdEstado" = Comprobante."IdEstado") 
CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
			            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado ON (ComprobanteFormaDePago."IdEstado" = Estado."IdEstado") 
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" AS TipoMovimiento ON (TipoMovimiento."IdModulo" = 'TES' 
      and TipoMovimiento."IdTipoMovimiento" = ComprobanteFormaDePago."IdTipoMovimiento") 
INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON (Concepto."IdConcepto" = Comprobante."IdConcepto") 
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda ON (Moneda."IdMoneda" = ComprobanteFormaDePago."IdMoneda") 
INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TipoMovimientoMedioDePago ON (TipoMovimientoMedioDePago."IdTipoMovimiento" = ComprobanteFormaDePago."IdTipoMovimiento" 
      AND TipoMovimientoMedioDePago."IdMedioDePago" = ComprobanteFormaDePago."IdMedioDePago" ) 

INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as Persona ON (Persona."IdPersona" = Comprobante."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PersonaTitularPago ON (PersonaTitularPago."IdPersona" = Comprobante."IdPersonaTitularPago")
INNER JOIN "BSCL.SocioNegocio.BD::PER.TipoPersona" as TipoPersona ON (TipoPersona."IdTipoPersona" = PersonaTitularPago."Tipo")
INNER JOIN "BSCL.SocioNegocio.BD::PER.TipoPersona" as TipoPersonaTitularPago ON (TipoPersonaTitularPago."IdTipoPersona" = PersonaTitularPago."Tipo")

LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Caja" AS Caja ON Caja."IdCaja" = Comprobante."IdCaja" 
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Chequera" AS Chequera ON (Chequera."IdChequera" = ComprobanteFormaDePago."IdChequera") 
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CuentaBancariaCorredora ON (CuentaBancariaCorredora."IdCuentaBanco" = ComprobanteFormaDePago."IdCuentaBancariaCorredora") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BancoCorredora ON BancoCorredora."IdBanco" = CuentaBancariaCorredora."IdBanco" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BancoSocio ON (BancoSocio."IdBanco" = ComprobanteFormaDePago."IdBancoSocio") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MedioDePago ON (MedioDePago."IdMedioDePago" = ComprobanteFormaDePago."IdMedioDePago") 
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" AS TipoSocio ON TipoSocio."IdTipoSocioDeNegocio" = Comprobante."IdTipoPersona" 

LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" AS TipoSocioPersonaTitular ON TipoSocioPersonaTitular."IdTipoSocioDeNegocio" = Comprobante."IdTipoPersonaTitularPago" 

LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS UsuarioLiquidacion ON UsuarioLiquidacion."IdUsuario" = ComprobanteFormaDePago."IdUsuarioLiquidacion" 
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS UsuarioGiro ON UsuarioGiro."IdUsuario" = ComprobanteFormaDePago."IdUsuarioGiro" 
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS UsuarioCertifica ON UsuarioCertifica."IdUsuario" = ComprobanteFormaDePago."IdUsuarioCertificacion" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS Formato 
ON Formato."IdMoneda" = ComprobanteFormaDePago."IdMoneda" AND Formato."IdUsoMonedaFormato" = 'GEN' AND Formato."IdTipoOperacion" = '' AND Formato."IdTipoInstrumento" = ''
WHERE ComprobanteFormaDePago."IdEstado" <> 'MOD'

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaPagoCobroAnalytics" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO