SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteTesoreriaAnalitica";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteTesoreriaAnalitica" AS
SELECT
      Comprobante."IdEmpresa" ,
      CASE WHEN IFNULL(Comprobante."FechaIngresoRegistro",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  Comprobante."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (Comprobante."FechaIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
		END  AS "FechaIngresoRegistro",

      Comprobante."IdComprobante" ,
      LEFT(TO_CHAR(IFNULL(Comprobante."FechaMovimiento",'0001-01-01')),10)  AS "FechaComprobante",
            
      CASE WHEN Comprobante."IdTipoMovimiento" = 'MOVING' THEN CAST('INGRESO' AS NVARCHAR(7)) ELSE CAST('EGRESO' AS NVARCHAR(7)) END AS "TipoComprobante",


      CASE WHEN Comprobante."IdConcepto" = '' THEN NULL ELSE Comprobante."IdConcepto" END AS "IdConcepto",
      coalesce(CAST(Concepto."Nombre" AS NVARCHAR(100)),'') AS "NombreConcepto" ,

      CASE WHEN Comprobante."IdCaja" = 0 THEN NULL ELSE Comprobante."IdCaja" END AS "IdCaja",
      coalesce(CAST(Caja."Codigo" AS NVARCHAR(40)),'')  AS "CodigoCaja" ,
      coalesce(CAST(Caja."Nombre" AS NVARCHAR(100)),'') AS "NombreCaja" ,

      CASE WHEN Comprobante."IdMoneda" = '' THEN NULL ELSE Comprobante."IdMoneda" END AS "IdMoneda",
      Formato."CantidadDecimales" AS "DecimalesMoneda", 
      CAST(Moneda."Nombre" AS NVARCHAR(100)) AS "NombreMoneda" ,
      
      Comprobante."Monto" ,
      CASE WHEN Comprobante."IdTipoMovimiento" = 'MOVEGR' 
      THEN Comprobante."Monto" 
            ELSE 0 
            END AS "MontoEgreso" ,
      CASE WHEN Comprobante."IdTipoMovimiento" = 'MOVING' 
            THEN Comprobante."Monto" 
            ELSE 0 
      END AS "MontoIngreso" ,
      CASE WHEN Comprobante."IdTipoMovimiento" = 'MOVING' 
            THEN Comprobante."Monto" 
            ELSE -1*Comprobante."Monto" 
      END AS "MontoConSigno" ,
      
      LEFT(TO_CHAR(IFNULL(Comprobante."FechaLiquidacion",'0001-01-01')),10) AS "FechaLiquidacion",

      CASE WHEN Comprobante."IdTipoPersonaTitularPago" = '' THEN NULL ELSE CAST(Comprobante."IdTipoPersonaTitularPago" AS NVARCHAR(3)) END AS "IdTipoPersonaTitular" ,
      CASE WHEN Comprobante."IdPersonaTitularPago" = 0 THEN NULL ELSE Comprobante."IdPersonaTitularPago" END AS "IdPersonaTitular",
      ident."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadTitular",
      ident."DocumentoIdentidad" AS "DocumentoIdentidadTitular",
      CAST(Comprobante."NombrePersonaTitularPago" AS NVARCHAR(302)) AS "NombrePersonaTitular",
      CASE WHEN Comprobante."IdPortafolioTitularPago" = 0 THEN NULL ELSE Comprobante."IdPortafolioTitularPago" END AS "IdPortafolioTitular",
      coalesce(CAST(PortafolioTit."CodigoPortafolio" AS NVARCHAR(100)),'') AS "CodigoPortafolioPersona" ,
      coalesce(CAST(PortafolioTit."CodigoPortafolio" ||' '|| PortafolioTit."Proposito" AS NVARCHAR(100)),'') as "NombrePortafolioPersona" ,


      CASE WHEN Comprobante."IdPersona" = 0 THEN NULL ELSE Comprobante."IdPersona" END AS "IdPersona" ,
      CAST(Comprobante."IdTipoPersona" AS NVARCHAR(3)) AS "IdTipoPersona" ,
      CAST(COALESCE(TSN."NombreTipoSocioDeNegocio",'') AS NVARCHAR(40)) AS "TipoPersona" ,
      CAST(Comprobante."NombrePersona" AS NVARCHAR(302)) AS "NombrePersona" ,
      Comprobante."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadPersona",
      CAST(Comprobante."DocumentoIdentidad" AS NVARCHAR(40))AS "DocumentoIdentidadPersona",
      
      CAST(Comprobante."SecuenciaAgrupacion" AS NVARCHAR(40)) AS "SecuenciaAgrupacion",
      Comprobante."EsBilateral" ,
      
      CASE WHEN TRIM("ComprobanteNeteado") IN ('N','') THEN 'N' ELSE 'S' END as "ComprobanteNeteado" ,
      CASE WHEN TRIM("SeparadoPorConcepto") IN ('N','') THEN 'N' ELSE 'S' END as "SeparadoPorConcepto" ,

      CASE WHEN Comprobante."IdEstado" = '' THEN NULL ELSE Comprobante."IdEstado" END AS "IdEstado",
      CAST(Estado."Nombre" AS NVARCHAR(100)) AS "NombreEstado" ,

      CASE WHEN FPMC."IdMedioDePago" = '' THEN NULL ELSE FPMC."IdMedioDePago" END AS "IdMedioDePago",
      CAST(FPMC."NombreMedioDePago" AS NVARCHAR(100)) AS "NombreMedioDePago",
      CASE WHEN FPMC."IdModalidadDePago" = '' THEN NULL ELSE CAST(FPMC."IdModalidadDePago" AS NVARCHAR(6)) END AS "IdModalidadDePago",
      CAST(FPMC."NombreModalidadDePago" AS NVARCHAR(100)) AS "NombreModalidadDePago",

      coalesce(CAST(UsuarioLiquidacion."NombreCompleto" AS NVARCHAR(302)),'') AS "NombreUsuarioLiquidacion" ,

      CASE WHEN Comprobante."IdUsuarioAnulacion" = 0 THEN NULL ELSE Comprobante."IdUsuarioAnulacion" END AS "IdUsuarioAnulacion",
      CAST(UsuarioAnul."CodigoUsuario" AS NVARCHAR(40)) AS "CodigoUsuarioAnulacion",
      CASE WHEN IFNULL(Comprobante."FechaHoraAnulacion",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  Comprobante."FechaHoraAnulacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (Comprobante."FechaHoraAnulacion", CONFIGURACION."Valor")),19)
					 	                                  END 
	  END AS "FechaHoraAnulacion",
	  
      CAST(Empresa."Nombre" AS NVARCHAR(100)) AS "NombreEmpresa" ,
      CASE WHEN Comprobante."IdUsuario" = 0 THEN NULL ELSE Comprobante."IdUsuario" END AS "IdUsuario",
      coalesce(CAST(Usuario."CodigoUsuario" AS NVARCHAR(40)),'') AS "CodigoUsuario"

	FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as per ON per."IdPersona" = Comprobante."IdPersonaTitularPago"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as ident ON per."IdDocumentoIdentidad" = ident."IdDocumentoIdentidad"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado ON Comprobante."IdEstado" = Estado."IdEstado" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" configuracion on "CodigoParametro" = 'PLA.SegundosZonaHoraria'
	LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" AS TipoMovimiento ON (TipoMovimiento."IdModulo" = 'TES' 
	      and TipoMovimiento."IdTipoMovimiento" = Comprobante."IdTipoMovimiento") 
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON (Concepto."IdConcepto" = Comprobante."IdConcepto") 
	LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Caja" AS Caja ON Caja."IdCaja" = Comprobante."IdCaja" 
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda ON (Moneda."IdMoneda" = Comprobante."IdMoneda") 
	LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS PortafolioTit ON PortafolioTit."IdPortafolio" = Comprobante."IdPortafolioTitularPago" 
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" AS TSN ON (TSN."IdTipoSocioDeNegocio" = Comprobante."IdTipoPersona") 
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS Empresa ON (Empresa."IdEmpresa" = Comprobante."IdEmpresa") 
	LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarFormasDePagoMovimientoComprobante" AS FPMC ON FPMC."IdInterno" = Comprobante."IdComprobante" AND FPMC."Tipo" = 'Comprobante' 
	LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" Usuario ON Usuario."IdUsuario" = Comprobante."IdUsuario"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" UsuarioAnul ON UsuarioAnul."IdUsuario" = Comprobante."IdUsuarioAnulacion"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS Formato 
        ON Formato."IdMoneda" = Comprobante."IdMoneda" AND Formato."IdUsoMonedaFormato" = 'GEN' AND Formato."IdTipoOperacion" = '' AND Formato."IdTipoInstrumento" = ''
	LEFT OUTER JOIN ( SELECT
		  "IdComprobante" ,
	      MAX("NombreCompleto") AS "NombreCompleto" 
	      FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" FP 
	      INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" UC ON UC."IdUsuario" = FP."IdUsuarioLiquidacion" 
	      LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC 
			ON (SOC."IdEmpleado" = UC."IdEntidad" and UC."TipoEntidad" = 'EMP') 
				OR  (SOC."IdCliente" = UC."IdEntidad" and UC."TipoEntidad" = 'CLI')
				OR	(SOC."IdOperador" = UC."IdEntidad" and UC."TipoEntidad" = 'OPE')
				OR	(SOC."IdProveedor" = UC."IdEntidad" and UC."TipoEntidad" = 'PRO')
				OR	(SOC."IdPersona" = UC."IdEntidad" and UC."TipoEntidad" = 'PER')
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on PER."IdPersona" =  SOC."IdPersona"
	      WHERE FP."IdEstado" = 'LIQ' 
	      GROUP BY FP."IdComprobante",
	      FP."FechaHoraLiquidacion" 
	      ORDER BY FP."FechaHoraLiquidacion" desc ) as UsuarioLiquidacion ON UsuarioLiquidacion."IdComprobante" = Comprobante."IdComprobante" 
	      AND Comprobante."IdEstado" IN ('LIQ') 
	      WHERE Comprobante."IdEstado" <> 'MOD'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteTesoreriaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;