SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::IngresoEgresoAnalitica";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::IngresoEgresoAnalitica" AS

SELECT
      IE."IdEmpresa" ,
      IE."IdIngresoEgreso" ,
      CASE WHEN IFNULL(IE."FechaIngresoRegistro",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  IE."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (IE."FechaIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
	  END AS "FechaIngresoRegistro",
	  LEFT(TO_CHAR(IFNULL(IE."FechaMovimiento",'0001-01-01')),10) AS  "FechaMovimiento",
      CASE WHEN IE."IdTipoMovimiento" = 'MOVING' THEN 'INGRESO' ELSE 'EGRESO' END AS "TipoMovimiento",
      CASE WHEN IE."IdConcepto" = '' THEN NULL ELSE IE."IdConcepto" END AS "IdConcepto"  ,
      CAST(COALESCE(CON."Nombre",'') AS NVARCHAR(100)) AS "NombreConcepto" ,
      CASE WHEN IE."IdMoneda" = '' THEN NULL ELSE IE."IdMoneda" END AS "IdMoneda",
      Formato."CantidadDecimales" AS "DecimalesMoneda", 
      CAST(MON."Nombre" AS NVARCHAR(100)) AS "NombreMoneda" ,
      IE."Monto" ,
      CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' 
            THEN IE."Monto" 
            ELSE 0 
      END AS "MontoEgreso" ,
      CASE WHEN IE."IdTipoMovimiento" = 'MOVING' 
            THEN IE."Monto" 
            ELSE 0 
      END AS "MontoIngreso" ,
      CASE WHEN IE."IdTipoMovimiento" = 'MOVING' 
            THEN IE."Monto" 
            ELSE -1*IE."Monto" 
      END AS "MontoConSigno" ,
      IE."IdCondicionLiquidacion" ,
      LEFT(TO_CHAR(IFNULL(TO_CHAR(IE."FechaLiquidacion"),'0001-01-01')),10) AS "FechaLiquidacion",
      CASE WHEN IE."IdCaja"  = 0 THEN NULL ELSE IE."IdCaja" END AS "IdCaja",     
      CAST(COALESCE(CAJ."Nombre",'') AS NVARCHAR(100)) AS "NombreCaja" ,
      --Persona sujeto del Pago o Cobro
      CASE WHEN IE."IdPersonaTitularPago" = 0 THEN NULL ELSE IE."IdPersonaTitularPago" END AS "IdPersonaTitular",
      CAST(IE."IdTipoPersonaTitularPago" AS NVARCHAR(3)) AS "IdTipoPersonaTitular",
      IE."IdTipoDocumentoIdentidadTitularPago" AS "IdTipoDocumentoIdentidadTitular",
      IE."DocumentoIdentidadTitularPago" AS "DocumentoIdentidadTitular",
      CAST(IE."NombrePersonaTitularPago" AS NVARCHAR(302)) AS "NombrePersonaTitular",
      CASE WHEN IE."IdPortafolioTitularPago" = 0 THEN NULL ELSE IE."IdPortafolioTitularPago" END AS "IdPortafolioTitular",
      coalesce(CAST(PORL."CodigoPortafolio" AS NVARCHAR(100)),'') AS "CodigoPortafolioTitular", 
      coalesce(CAST(PORL."CodigoPortafolio" ||' '|| PORL."Proposito" AS NVARCHAR(100)),'') AS "NombrePortafolioTitular" ,   
      --Persona que instruye el Pago o Cobro (normalmente igual alsujeto de Pago o Cobro)
      CASE WHEN IE."IdPersona" = 0 THEN NULL ELSE IE."IdPersona" END AS "IdPersona"  ,
      CAST(IE."IdTipoPersona" AS NVARCHAR(3)) AS "IdTipoPersona",
      IE."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadPersona",
      CAST(IE."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidadPersona",
      CAST(COALESCE(TSN."NombreTipoSocioDeNegocio",'') AS NVARCHAR(40)) AS "TipoPersona" ,
      CASE WHEN CPOR."IdCliente" = 0 THEN NULL ELSE CPOR."IdCliente" END AS "IdClientePersona",
      CASE WHEN SN."IdSocioDeNegocio" = 0 THEN NULL ELSE SN."IdSocioDeNegocio" END AS "IdSocioDeNegocioPersona" ,
      CAST(IE."NombrePersona" AS NVARCHAR(302)) AS "NombrePersona",
      CASE WHEN IE."IdPortafolio" = 0 THEN NULL ELSE IE."IdPortafolio" END AS "IdPortafolioPersona",
      coalesce(CAST(POR."CodigoPortafolio" ||' '|| POR."Proposito" AS NVARCHAR(100)),'') AS "NombrePortafolioPersona" ,
      coalesce(CAST(POR."CodigoPortafolio" AS NVARCHAR(100)),'') AS "CodigoPortafolioPersona", 
      CAST(REPLACE(REPLACE(REPLACE(IE."Glosa",CHAR(10),''),CHAR(13),''),'|','!') AS NVARCHAR(260)) AS "Glosa" ,
      CASE WHEN IE."IdModulo" = '' THEN NULL ELSE IE."IdModulo" END AS "IdModuloEnviaMovimiento",
      CASE WHEN IE."IdUsuario" = 0 THEN NULL ELSE IE."IdUsuario" END AS "IdUsuarioEnviaMovimiento" ,
      CASE WHEN IE."SecuenciaRelacionada" = 0 THEN NULL ELSE IE."SecuenciaRelacionada" END AS "IdMovimientoRelacionado",
      CAST(IE."SecuenciaAgrupacion" AS NVARCHAR(40)) AS "SecuenciaAgrupacion",
      CASE WHEN IE."IdEmpleadoDigitador" = 0 THEN (select "IdEntidad" from "BSCL.Plataforma.BD::ACC.Usuario" where "IdUsuario" = 0) 
      ELSE IE."IdEmpleadoDigitador" END AS "IdEmpleadoDigitador",
      CASE WHEN IE."IdSucursalEmpleadoDigitador" = 0 THEN NULL ELSE IE."IdSucursalEmpleadoDigitador" END AS "IdSucursalEmpleadoDigitador",
      IE."IdTipoComprobanteOrigen" ,
      CASE WHEN IE."IdComprobanteOrigen" = 0 THEN NULL ELSE IE."IdComprobanteOrigen" END AS "IdComprobanteOrigen",
      LEFT(TO_CHAR(IFNULL(IE."FechaComprobanteOrigen",'0001-01-01')),10) AS "FechaComprobanteOrigen",
       LEFT(TO_CHAR(IFNULL(IE."FechaLiquidacionComprobanteOrigen",'0001-01-01')),10) AS "FechaLiquidacionComprobanteOrigen",
      case when IE."IdTipoProducto" = '' then null else IE."IdTipoProducto" end AS "IdTipoProducto",
      coalesce(CAST(PROD."Nombre" AS NVARCHAR(100)),'') AS "NombreTipoProducto" ,
      CASE WHEN IE."IdOperacion" = 0 THEN NULL ELSE IE."IdOperacion" END AS "IdOperacion", 
      case when IE."IdTipoOperacion" = '' then null else IE."IdTipoOperacion" end  AS "IdTipoOperacion",
      coalesce(CAST(OPER."Nombre" AS NVARCHAR(100)),'') AS "NombreTipoOperacion" ,
      LEFT(TO_CHAR(IFNULL(IE."FechaOperacion",'0001-01-01')),10) AS "FechaOperacion" ,
      CASE WHEN IE."IdMonedaOperacion" = '' THEN NULL ELSE IE."IdMonedaOperacion" END AS "IdMonedaOperacion",
      IE."MontoOperacion" ,
      CASE WHEN IE."IdBolsa" = '' THEN NULL ELSE IE."IdBolsa" END AS "IdBolsa",
      LEFT(TO_CHAR(IFNULL(IE."FechaParidad",'0001-01-01')),10) AS "FechaParidad",
      IE."ValorParidad" ,
      IE."FolioCartera" ,
      CASE WHEN IE."IdInstrumento" = 0 THEN NULL ELSE IE."IdInstrumento" END AS "IdInstrumento",
      coalesce(CAST(INS."Nemotecnico" AS NVARCHAR(40)),'') AS "Nemotecnico",
      case when IE."IdTipoInstrumento" = '' then null else IE."IdTipoInstrumento" end AS "IdTipoInstrumento",
      CASE WHEN IE."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE IE."IdClasificacionTipoInstrumento" END   AS "IdClasificacionTipoInstrumento",
      CASE WHEN IE."IdTipoReajuste" = '' THEN NULL ELSE IE."IdTipoReajuste" END AS "IdTipoReajuste",
      IE."EsBilateral" ,
      CASE WHEN IE."IdUsuarioEmision" = 0 THEN NULL ELSE IE."IdUsuarioEmision" END AS "IdUsuarioEmisionComprobante",
      coalesce(CAST(UC."NombreCompleto" AS NVARCHAR(302)),'') AS "NombreUsuarioEmisionComprobante" ,
      CASE WHEN IFNULL(IE."FechaHoraEmision",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  IE."FechaHoraEmision" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (IE."FechaHoraEmision", CONFIGURACION."Valor")),19)
					 	                                  END 
	  END AS  "FechaHoraEmisionComprobante",
      IE."IdCarga" ,
      CASE WHEN IE."IdEstado" = '' THEN NULL ELSE IE."IdEstado" END AS "IdEstado",
      CAST(EST."Nombre" AS NVARCHAR(100)) AS "NombreEstado" ,
      CASE WHEN CD."IdComprobante" = 0 THEN NULL ELSE CD."IdComprobante" END  AS "IdComprobante" ,
      CASE WHEN FPMC."IdMedioDePago" = '' THEN NULL ELSE FPMC."IdMedioDePago" END AS "IdMedioDePago",
      CAST(FPMC."NombreMedioDePago" AS NVARCHAR(100)) AS "NombreMedioDePago",
      CASE WHEN FPMC."IdModalidadDePago" = '' THEN NULL ELSE CAST(FPMC."IdModalidadDePago" AS NVARCHAR(6)) END AS "IdModalidadDePago",
      CAST(FPMC."NombreModalidadDePago" AS NVARCHAR(100)) AS "NombreModalidadDePago",
      CASE WHEN IE."IdUsuarioAnulacion" = 0 THEN NULL ELSE IE."IdUsuarioAnulacion" END AS "IdUsuarioAnulacion",
      	CASE WHEN IFNULL(IE."FechaHoraAnulacion",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  IE."FechaHoraAnulacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (IE."FechaHoraAnulacion", CONFIGURACION."Valor")),19)
					 	                                  END 
		END AS "FechaHoraAnulacion", 
      CAST(EMP."Nombre" AS NVARCHAR(100)) AS "NombreEmpresa",
      CPOR."IdMatrizAgrupador",
      CPOR."EsADC" 
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TM ON (TM."IdTipoMovimiento" = IE."IdTipoMovimiento") 
CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
			            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CON ON (CON."IdConcepto" = IE."IdConcepto") 
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Caja" AS CAJ ON (CAJ."IdCaja" = IE."IdCaja") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = IE."IdMoneda") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (IE."IdEstado" = EST."IdEstado") 
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR ON (POR."IdPortafolio" = IE."IdPortafolio") 
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS PORL ON (PORL."IdPortafolio" = IE."IdPortafolioTitularPago") 
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" AS TSN ON (TSN."IdTipoSocioDeNegocio" = IE."IdTipoPersona") 
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SN ON (SN."IdPersona" = IE."IdPersona" 
      AND IE."IdEmpresa" = SN."IdEmpresa") 
LEFT OUTER JOIN (SELECT
      CDO."IdIngresoEgreso",
      CD."IdComprobante" 
      FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD 
      LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON (CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle") 
      WHERE (COALESCE(CD."IdEstado",
      '') NOT IN ('MOD',
      'ANU') 
            OR COALESCE(CD."IdEstado",
      '') = '') ) as CD ON (CD."IdIngresoEgreso" = IE."IdIngresoEgreso") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP ON (EMP."IdEmpresa" = IE."IdEmpresa") 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = IE."IdInstrumento" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPER ON OPER."IdTipoOperacion" = IE."IdTipoOperacion" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS PROD ON PROD."IdTipoProducto" = IE."IdTipoProducto" 
LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarFormasDePagoMovimientoComprobante" AS FPMC ON FPMC."IdInterno" = IE."IdIngresoEgreso" AND FPMC."Tipo" = 'IngresoEgreso' 
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS CPOR ON CPOR."IdPortafolio" = IE."IdPortafolio" 
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS UC ON UC."IdUsuario" = IE."IdUsuarioEmision"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS Formato 
ON Formato."IdMoneda" = IE."IdMoneda" AND Formato."IdUsoMonedaFormato" = 'GEN' AND Formato."IdTipoOperacion" = '' AND Formato."IdTipoInstrumento" = '';

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::IngresoEgresoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
