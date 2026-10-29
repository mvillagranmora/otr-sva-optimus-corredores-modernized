SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCarteras";
GO

CREATE PROCEDURE 
-- 
--  Proposito: Consulta custodia general por instrumento(tambien llamado custodia oficina)
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 30/11/2016  
-- 
"BSCL.Custodia.BD.Servicios::ConsultarCarteras" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
	IN p_IdEmpresa							BIGINT,
	IN p_IdUsuario							BIGINT,
	IN p_IdModulo							NVARCHAR(3), 
	IN p_FechaConsulta						DATE		DEFAULT '0001-01-01',
	IN p_IdCliente							BIGINT		DEFAULT 0,
	IN p_IdPortafolio						BIGINT		DEFAULT 0,
	IN p_IdClasificacionTipoInstrumento		NVARCHAR(3)	DEFAULT '',
	IN p_IdTipoInstrumento					NVARCHAR(3)	DEFAULT '',
	IN p_IdInstrumento						BIGINT		DEFAULT 0,
	IN p_IdMoneda							NVARCHAR(3)	DEFAULT '',
	IN p_tipoFecha							NVARCHAR(3)	DEFAULT 'LIQ',
	IN p_FolioCartera						BIGINT		DEFAULT 0,
	IN p_IdEstado							NVARCHAR(3)	DEFAULT '',
	IN p_IdProductoAgrupador				BIGINT		DEFAULT NULL,
	
	OUT P_OUT_TABLA                         TABLE( "IdCartera"      BIGINT,
	                                               "FolioCartera"   BIGINT,
	                                               "IdEmpresa"      BIGINT,
	                                               "NombreEmpresa"  NVARCHAR(100),
	                                               "FechaOperacion" DATE,
	                                               "IdCliente"      BIGINT,
	                                               "IdPortafolio"   BIGINT,
	                                               "DocumentoIdentidad"     NVARCHAR(40),
	                                               "RazonSocial"            NVARCHAR(250),
	                                               "CodigoPortafolio"       NVARCHAR(100),
	                                               "DescripcionPortafolio"  NVARCHAR(520),
	                                               "IdEstado"        NVARCHAR(3),
	                                               "NombreEstado"    NVARCHAR(100),
	                                               "IdMonedaSistema" NVARCHAR(3),
	                                               "MonedaSistema"   NVARCHAR(100),
	                                               "CodigoIFRS"      NVARCHAR(3),
	                                               "IdConcepto"      NVARCHAR(6),
	                                               "NombreConcepto"  NVARCHAR(100),
	                                               "IdTipoOperacion" NVARCHAR(6),
	                                               "IdTipoComprobante"  NVARCHAR(3),
	                                               "FolioComprobante"   BIGINT,
	                                               "FechaComprobante"   DATE,
	                                               "IdMesa"             BIGINT,
	                                               "IdBolsa"            NVARCHAR(3),
	                                               "NombreBolsa"        NVARCHAR(255),
	                                               "IdSucursal"         BIGINT,
	                                               "IdAgente"           BIGINT,
	                                               "IdCondicionLiquidacion"      NVARCHAR(2),
	                                               "NombreCondicionLiquidacion"  NVARCHAR(100),
	                                               "FechaLiquidacion"            DATE,
	                                               "IdClasificacionTipoInstrumento" NVARCHAR(3),
	                                               "ClasificacionTipoInstrumento"   NVARCHAR(100),
	                                               "IdTipoInstrumento"      NVARCHAR(3),
	                                               "TipoInstrumento"        NVARCHAR(100),
	                                               "IdInstrumento"          BIGINT,
	                                               "RazonSocialInstrumento" NVARCHAR(255),
	                                               "ISIN"                   NVARCHAR(40),
	                                               "Nemotecnico"            NVARCHAR(100),
	                                               "IdEmisor"               NVARCHAR(40),
	                                               "NemoSVS"                NVARCHAR(40),
	                                               "IdMonedaEmision"        NVARCHAR(3),
	                                               "IdTipoReajuste"         NVARCHAR(3),
	                                               "NombreTipoReajuste"     NVARCHAR(100),
	                                               "IdMonedaInstrReajuste"  NVARCHAR(3),
	                                               "NombreMonedaInstrReajuste"    NVARCHAR(100),
	                                               "FechaEmisionInstrumento"      DATE,
	                                               "FechaVencimientoInstrumento"  DATE,
	                                               "EsFungible"                   NVARCHAR(1),
	                                               "IdBoveda"                     NVARCHAR(3),
	                                               "NombreBoveda"                 NVARCHAR(100),
	                                               "NumeroCuentaBoveda"           NVARCHAR(100),
	                                               "IdTipoCustodia"               NVARCHAR(3),
	                                               "NombreTipoCustodia"           NVARCHAR(40),
	                                               "IdMonedaInstrumento"          NVARCHAR(3),
	                                               "MonedaInstrumento"            NVARCHAR(100),
	                                               "Cantidad"                     DECIMAL(26,8),
	                                               "CorteMinimo"                  DECIMAL(26,8),
	                                               "ValorRescate"                 DECIMAL(26,8),
	                                               "PrecioCompra"                 DECIMAL(26,8),
	                                               "MontoCompra"                  DECIMAL(26,8),
	                                               "INGFechaOperacion"            DATE,
	                                               "INGFechaLiquidacion"          DATE,
	                                               "INGCantidad"                  DECIMAL(26,8),
	                                               "TasaCompra"                   DECIMAL(26,8),
	                                               "INGTasaCompra"                DECIMAL(26,8),
	                                               "INGValorRescate"              DECIMAL(26,8),
	                                               "INGPrecioCompra"              DECIMAL(26,8),
	                                               "INGPrecioPromedioCompra"      DECIMAL(26,8),
	                                               "INGMontoPrecioPromedioCompra" DECIMAL(26,8),
	                                               "INGMontoATasaCompraActual"    DECIMAL(26,8),
	                                               "DiferenciaInversion"          DECIMAL(26,8),
	                                               "INGMontoCompraActualMonedaInstrumento" DECIMAL(26,8),
	                                               "INGFuenteTasaPrecioMercadoActual"      NVARCHAR(40),
	                                               "INGPrecioMercadoActual"                DECIMAL(26,8),
	                                               "Rentabilidad"                          DECIMAL(26,8),
	                                               "RentabilidadReal"					   DECIMAL(26,8),
	                                               "CostoUF"							   DECIMAL(26,8),
	                                               "INGTasaMercadoActual"                  DECIMAL(26,8),
	                                               "INGMontoATasaMercadoActual"            DECIMAL(26,8),
	                                               "INGMontoMercadoActualMonedaInstrumento" DECIMAL(26,8),
	                                               "INGFechaPagoCorteCupon"                DATE,
	                                               "INGInteresesPagoCorteCupon"            DECIMAL(26,8),
	                                               "INGMontoCorteCupon"                    DECIMAL(26,8),
	                                               "INGMontoCapitalCorteCupon"             DECIMAL(26,8),
	                                               "INGMontoInteresCorteCupon"             DECIMAL(26,8),
	                                               "INGMontoInteresCompraActual"           DECIMAL(26,8),
	                                               "INGMontoReajusteCompraActual"          DECIMAL(26,8),
	                                               "InteresCorridoMonedaInstrumento"       DECIMAL(26,8),
	                                               "ReajusteCorridoMonedaInstrumento"      DECIMAL(26,8),
	                                               "EGRFechaOperacion"                     DATE,
	                                               "EGRFechaLiquidacion"                   DATE,
	                                               "EGRIdTipoComprobante"                  NVARCHAR(3),
	                                               "EGRFolioComprobante"                   BIGINT,
	                                               "EGRFechaComprobante"                   DATE,
	                                               "EGRIdConcepto"                         NVARCHAR(6),
	                                               "EGRNombreConcepto"                     NVARCHAR(100),
	                                               "EGRCantidad"                           DECIMAL(26,8),
	                                               "EGRValorRescate"                       DECIMAL(26,8),
	                                               "EGRPrecio"                             DECIMAL(26,8),
	                                               "EGRTasa"                               DECIMAL(26,8),
	                                               "EGRMonto"                              DECIMAL(26,8),
	                                               "EGRMontoMonedaInstrumento"             DECIMAL(26,8),
	                                               "TipoOpcion"	               			   NVARCHAR(5),
	                                               "NombreProductoAgrupador"               NVARCHAR(200),
	                                               "INGFechaValorizacionCartera" 		   DATE,
	                                               "RegimenTributario"			 		   NVARCHAR(40),
	                                               "FechaOrigenCompra"					   DATE
	) DEFAULT EMPTY
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"

AS
BEGIN
	
	DECLARE v_FechaSistema DATE;
	
	--Obtiene fecha de sistema
    Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);  
    
    --Asigna tabla dependiendo de filtro fecha consulta
    IF TO_DATE(:p_FechaConsulta) >= TO_DATE(:v_FechaSistema) THEN
       v_consulta_carteraUI = SELECT CC."IdCartera"
                                    ,CC."FolioCartera"
                                    ,CC."IdEmpresa"
                                    ,CC."NombreEmpresa"
                                    ,CC."FechaOperacion"
                                    ,CC."IdCliente"
                                    ,CC."IdPortafolio"
                                    ,CC."DocumentoIdentidad"
                                    ,CC."RazonSocial"
                                    ,CC."CodigoPortafolio"
                                    ,CC."DescripcionPortafolio"
                                    ,CASE 
                                       WHEN v_FechaSistema >= p_FechaConsulta THEN CC."IdEstado" 
                                     ELSE 'VIG' 
                                     END "IdEstado"
                                    ,CASE 
                                       WHEN v_FechaSistema >= p_FechaConsulta THEN "NombreEstado" 
                                     ELSE 'VIGENTE' 
                                     END "NombreEstado"
                                    ,CC."IdMonedaSistema"
                                    ,CC."MonedaSistema"
                                    ,CC."CodigoIFRS"
                                    ,CC."IdConcepto"
                                    ,CC."NombreConcepto"
                                    ,CC."IdTipoOperacion"
                                    ,CC."IdTipoComprobante"
                                    ,CC."FolioComprobante"
                                    ,CC."FechaComprobante"
                                    ,CC."IdMesa"
                                    ,CC."IdBolsa"
                                    ,CC."NombreBolsa"
                                    ,CC."IdSucursal"
                                    ,CC."IdAgente"
                                    ,CC."IdCondicionLiquidacion"
                                    ,CC."NombreCondicionLiquidacion"
                                    ,CC."FechaLiquidacion"
                                    ,CC."IdClasificacionTipoInstrumento"
                                    ,CC."ClasificacionTipoInstrumento"
                                    ,CC."IdTipoInstrumento"
                                    ,CC."TipoInstrumento"
                                    ,CC."IdInstrumento"
                                    ,CC."RazonSocialInstrumento"
                                    ,CC."ISIN", CC."Nemotecnico"
                                    ,CC."IdEmisor"
                                    ,CC."NemoSVS"
                                    ,CC."IdMonedaEmision"
                                    ,CC."IdTipoReajuste"
                                    ,ifnull(CC."NombreTipoReajuste",'') AS "NombreTipoReajuste"
                                    ,CC."IdMonedaInstrReajuste"
                                    ,CC."NombreMonedaInstrReajuste"
                                    ,CC."FechaEmisionInstrumento"
                                    ,CC."FechaVencimientoInstrumento"
                                    ,CC."EsFungible"
                                    ,CC."IdBoveda"
                                    ,CC."NombreBoveda"
                                    ,CC."NumeroCuentaBoveda"
                                    ,ifnull(CC."IdTipoCustodia",'') AS "IdTipoCustodia"
                                    ,ifnull(CC."NombreTipoCustodia",'') AS "NombreTipoCustodia"
                                    ,CC."IdMonedaInstrumento"
                                    ,CC."MonedaInstrumento"
                                    ,CASE 
                                       WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."ValorRescate" 
                                     ELSE CC."Cantidad" 
                                     END AS "Cantidad"
                                    ,CC."CorteMinimo"
                                    ,CC."ValorRescate"
                                    ,CC."PrecioCompra"
                                    ,CC."MontoCompra"
                                    ,CC."INGFechaOperacion"
                                    ,ifnull(LAG(CC."EGRFechaLiquidacion") over (partition by "IdEmpresa", CC."FolioCartera" order by CC."EGRFechaLiquidacion" )
                                    ,CC."INGFechaLiquidacion") as "INGFechaLiquidacion"
                                    ,CASE 
                                       WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."INGValorRescate" 
                                     ELSE CC."INGCantidad" 
                                     END AS "INGCantidad"
                                    ,CC."TasaCompra"
                                    ,CC."INGTasaCompra"
                                    ,CC."INGValorRescate"
                                    ,CC."INGPrecioCompra"
                                    ,CC."INGPrecioPromedioCompra"
                                    ,CC."INGMontoPrecioPromedioCompra"
                                    ,CC."INGMontoATasaCompraActual"
                                    ,CC."DiferenciaInversion"
                                    ,CC."INGMontoCompraActualMonedaInstrumento"
                                    ,CC."INGFuenteTasaPrecioMercadoActual"
                                    ,CC."INGPrecioMercadoActual"
                                    ,CC."Rentabilidad"
                                    ,CC."RentabilidadReal"
                                    ,CC."CostoUF"
                                    ,CC."INGTasaMercadoActual"
                                    ,CC."INGMontoATasaMercadoActual"
                                    ,CC."INGMontoMercadoActualMonedaInstrumento"
                                    ,CC."INGFechaPagoCorteCupon"
                                    ,CC."INGInteresesPagoCorteCupon"
                                    ,CC."INGMontoCorteCupon"
                                    ,CC."INGMontoCapitalCorteCupon"
                                    ,CC."INGMontoInteresCorteCupon"
                                    ,CC."INGMontoInteresCompraActual"
                                    ,CC."INGMontoReajusteCompraActual"
                                    ,CC."InteresCorridoMonedaInstrumento"
                                    ,CC."ReajusteCorridoMonedaInstrumento"
                                    -----------------------------------------------
                                    ,CC."EGRFechaOperacion"
                                    ,CC."EGRFechaLiquidacion"
                                    ,CC."EGRIdTipoComprobante"
                                    ,CC."EGRFolioComprobante"
                                    ,CC."EGRFechaComprobante"
                                    ,CC."EGRIdConcepto"
                                    ,ifnull(CC."EGRNombreConcepto",'') AS "EGRNombreConcepto"
                                    ,CASE 
                                       WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."EGRValorRescate" 
                                     ELSE CC."EGRCantidad" 
                                     END AS "EGRCantidad"
                                    ,CC."EGRValorRescate"
                                    ,CC."EGRPrecio"
                                    ,CC."EGRTasa"
                                    ,CC."EGRMonto"
                                    ,CC."EGRMontoMonedaInstrumento"
                                    ,CC."TipoOpcion"
                                    ,CC."INGFechaValorizacionCartera"
                                    ,CC."RegimenTributario"
                                    ,CC."FechaOrigenCompra"
                              FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraUI" CC
                               WHERE CC."IdEstado"  NOT IN ('ANU','RPA')
                                 AND CC."IdEstado" = CASE 
                                                       WHEN p_IdEstado <> '' THEN p_IdEstado 
                                                     ELSE CC."IdEstado" 
                                                     END
                                 AND CC."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC', 'FWD','RRV','RRF')
                                 AND CC."IdCliente" = CASE 
                                                        WHEN p_IdCliente > 0 THEN p_IdCliente 
                                                      ELSE CC."IdCliente" 
                                                      END
                                 AND CC."IdPortafolio" = CASE 
                                                           WHEN p_IdPortafolio > 0 THEN p_IdPortafolio 
                                                         ELSE CC."IdPortafolio" 
                                                         END
                                 AND CC."IdClasificacionTipoInstrumento" = CASE 
                                                                             WHEN p_IdClasificacionTipoInstrumento <> '' THEN p_IdClasificacionTipoInstrumento 
                                                                           ELSE CC."IdClasificacionTipoInstrumento" 
                                                                           END
                                 AND CC."IdTipoInstrumento" = CASE 
                                                                WHEN p_IdTipoInstrumento <> '' THEN p_IdTipoInstrumento 
                                                              ELSE CC."IdTipoInstrumento" 
                                                              END
                                 AND CC."IdInstrumento" = CASE 
                                                            WHEN p_IdInstrumento > 0 THEN p_IdInstrumento 
                                                          ELSE CC."IdInstrumento" 
                                                          END
                                 AND CC."FolioCartera" = CASE 
                                                           WHEN p_FolioCartera > 0 THEN p_FolioCartera 
                                                         ELSE CC."FolioCartera" 
                                                         END
                                 AND CC."IdMonedaInstrumento" = CASE 
                                                                  WHEN p_IdMoneda <> '' THEN p_IdMoneda 
                                                                ELSE CC."IdMonedaInstrumento" 
                                                                END
                                 AND (	
			                          (p_tipoFecha = 'LIQ' AND CC."INGFechaLiquidacion" <= p_FechaConsulta AND CC."EGRFechaLiquidacion" >= p_FechaConsulta) 
		                          OR  (p_tipoFecha <> 'LIQ' AND (
				                                                 (CC."FechaOperacion" <= p_FechaConsulta AND CC."EGRCantidad" = 0 AND CC."IdEstado" = 'VIG')
				                                                )
				                      ));
				                      
    ELSE
      v_consulta_carteraUI = SELECT CC."IdCartera"
                                   ,CC."FolioCartera"
                                   ,CC."IdEmpresa"
                                   ,CC."NombreEmpresa"
                                   ,CC."FechaOperacion"
                                   ,CC."IdCliente"
                                   ,CC."IdPortafolio"
                                   ,CC."DocumentoIdentidad"
                                   ,CC."RazonSocial"
                                   ,CC."CodigoPortafolio"
                                   ,CC."DescripcionPortafolio"
                                   ,CASE 
                                      WHEN v_FechaSistema >= p_FechaConsulta THEN CC."IdEstado" 
                                    ELSE 'VIG' 
                                    END "IdEstado"
                                   ,CASE 
                                      WHEN v_FechaSistema >= p_FechaConsulta THEN "NombreEstado" 
                                    ELSE 'VIGENTE' 
                                    END "NombreEstado"
                                   ,CC."IdMonedaSistema"
                                   ,CC."MonedaSistema"
                                   ,CC."CodigoIFRS"
                                   ,CC."IdConcepto"
                                   ,CC."NombreConcepto"
                                   ,CC."IdTipoOperacion"
                                   ,CC."IdTipoComprobante"
                                   ,CC."FolioComprobante"
                                   ,CC."FechaComprobante"
                                   ,CC."IdMesa"
                                   ,CC."IdBolsa"
                                   ,CC."NombreBolsa"
                                   ,CC."IdSucursal"
                                   ,CC."IdAgente"
                                   ,CC."IdCondicionLiquidacion"
                                   ,CC."NombreCondicionLiquidacion"
                                   ,CC."FechaLiquidacion"
                                   ,CC."IdClasificacionTipoInstrumento"
                                   ,CC."ClasificacionTipoInstrumento"
                                   ,CC."IdTipoInstrumento"
                                   ,CC."TipoInstrumento"
                                   ,CC."IdInstrumento"
                                   ,CC."RazonSocialInstrumento"
                                   ,CC."ISIN", CC."Nemotecnico"
                                   ,CC."IdEmisor"
                                   ,CC."NemoSVS"
                                   ,CC."IdMonedaEmision"
                                   ,CC."IdTipoReajuste"
                                   ,ifnull(CC."NombreTipoReajuste",'') AS "NombreTipoReajuste"
                                   ,CC."IdMonedaInstrReajuste"
                                   ,CC."NombreMonedaInstrReajuste"
                                   ,CC."FechaEmisionInstrumento"
                                   ,CC."FechaVencimientoInstrumento"
                                   ,CC."EsFungible"
                                   ,CC."IdBoveda"
                                   ,CC."NombreBoveda"
                                   ,CC."NumeroCuentaBoveda"
                                   ,ifnull(CC."IdTipoCustodia",'') AS "IdTipoCustodia"
                                   ,ifnull(CC."NombreTipoCustodia",'') AS "NombreTipoCustodia"
                                   ,CC."IdMonedaInstrumento"
                                   ,CC."MonedaInstrumento"
                                   ,CASE 
                                      WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."ValorRescate" 
                                    ELSE CC."Cantidad" 
                                    END AS "Cantidad"
                                   ,CC."CorteMinimo"
                                   ,CC."ValorRescate"
                                   ,CC."PrecioCompra"
                                   ,CC."MontoCompra"
                                   ,CC."INGFechaOperacion"
                                   ,ifnull(LAG(CC."EGRFechaLiquidacion") over (partition by "IdEmpresa", CC."FolioCartera" order by CC."EGRFechaLiquidacion" )
                                   ,CC."INGFechaLiquidacion") as "INGFechaLiquidacion"
                                   ,CASE 
                                      WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."INGValorRescate" 
                                    ELSE CC."INGCantidad" 
                                    END AS "INGCantidad"
                                   ,CC."TasaCompra"
                                   ,CC."INGTasaCompra"
                                   ,CC."INGValorRescate"
                                   ,CC."INGPrecioCompra"
                                   ,CC."INGPrecioPromedioCompra"
                                   ,CC."INGMontoPrecioPromedioCompra"
                                   ,CC."INGMontoATasaCompraActual"
                                   ,CC."DiferenciaInversion"
                                   ,CC."INGMontoCompraActualMonedaInstrumento"
                                   ,CC."INGFuenteTasaPrecioMercadoActual"
                                   ,CC."INGPrecioMercadoActual"
                                   ,CC."Rentabilidad"
                                   ,CC."RentabilidadReal"
                                   ,CC."CostoUF"
                                   ,CC."INGTasaMercadoActual"
                                   ,CC."INGMontoATasaMercadoActual"
                                   ,CC."INGMontoMercadoActualMonedaInstrumento"
                                   ,CC."INGFechaPagoCorteCupon"
                                   ,CC."INGInteresesPagoCorteCupon"
                                   ,CC."INGMontoCorteCupon"
                                   ,CC."INGMontoCapitalCorteCupon"
                                   ,CC."INGMontoInteresCorteCupon"
                                   ,CC."INGMontoInteresCompraActual"
                                   ,CC."INGMontoReajusteCompraActual"
                                   ,CC."InteresCorridoMonedaInstrumento"
                                   ,CC."ReajusteCorridoMonedaInstrumento"
                                   -----------------------------------------------
                                   ,CC."EGRFechaOperacion"
                                   ,CC."EGRFechaLiquidacion"
                                   ,CC."EGRIdTipoComprobante"
                                   ,CC."EGRFolioComprobante"
                                   ,CC."EGRFechaComprobante"
                                   ,CC."EGRIdConcepto"
                                   ,ifnull(CC."EGRNombreConcepto",'') AS "EGRNombreConcepto"
                                   ,CASE 
                                      WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."EGRValorRescate" 
                                    ELSE CC."EGRCantidad" 
                                    END AS "EGRCantidad"
                                   ,CC."EGRValorRescate"
                                   ,CC."EGRPrecio"
                                   ,CC."EGRTasa"
                                   ,CC."EGRMonto"
                                   ,CC."EGRMontoMonedaInstrumento"
                                   ,CC."TipoOpcion"
                                   ,CC."INGFechaValorizacionCartera"
                                   ,CC."RegimenTributario"
                                   ,CC."FechaOrigenCompra"
                             FROM "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraUI" CC
                             WHERE CC."IdEstado"  NOT IN ('ANU','RPA')
                               AND CC."FechaRespaldo" = :p_FechaConsulta
                               AND CC."IdEstado" = CASE 
                                                      WHEN p_IdEstado <> '' THEN p_IdEstado 
                                                    ELSE CC."IdEstado" 
                                                    END
                               AND CC."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC', 'FWD','RRV','RRF')
                               AND CC."IdCliente" = CASE 
                                                      WHEN p_IdCliente > 0 THEN p_IdCliente 
                                                    ELSE CC."IdCliente" 
                                                    END
                               AND CC."IdPortafolio" = CASE 
                                                         WHEN p_IdPortafolio > 0 THEN p_IdPortafolio 
                                                       ELSE CC."IdPortafolio" 
                                                       END
                               AND CC."IdClasificacionTipoInstrumento" = CASE 
                                                                           WHEN p_IdClasificacionTipoInstrumento <> '' THEN p_IdClasificacionTipoInstrumento 
                                                                         ELSE CC."IdClasificacionTipoInstrumento" 
                                                                         END
                               AND CC."IdTipoInstrumento" = CASE 
                                                              WHEN p_IdTipoInstrumento <> '' THEN p_IdTipoInstrumento 
                                                            ELSE CC."IdTipoInstrumento" 
                                                            END
                               AND CC."IdInstrumento" = CASE 
                                                          WHEN p_IdInstrumento > 0 THEN p_IdInstrumento 
                                                        ELSE CC."IdInstrumento" 
                                                        END
                               AND CC."FolioCartera" = CASE 
                                                         WHEN p_FolioCartera > 0 THEN p_FolioCartera 
                                                       ELSE CC."FolioCartera" 
                                                       END
                               AND CC."IdMonedaInstrumento" = CASE 
                                                                WHEN p_IdMoneda <> '' THEN p_IdMoneda 
                                                              ELSE CC."IdMonedaInstrumento" 
                                                              END
                               AND (	
			                         (p_tipoFecha = 'LIQ' AND CC."INGFechaLiquidacion" <= p_FechaConsulta AND CC."EGRFechaLiquidacion" >= p_FechaConsulta) 
		                        OR  (p_tipoFecha <> 'LIQ' AND (
				                                                (CC."FechaOperacion" <= p_FechaConsulta AND CC."EGRCantidad" = 0 AND CC."IdEstado" = 'VIG')
				                                               )
				                    ));
    END IF;  
		 	                    
	
    p_out_tabla = SELECT CC."IdCartera"
                        ,CC."FolioCartera"
                        ,CC."IdEmpresa"
                        ,CC."NombreEmpresa"
                        ,CC."FechaOperacion"
                        ,CC."IdCliente"
                        ,CC."IdPortafolio"
                        ,CC."DocumentoIdentidad"
                        ,CC."RazonSocial"
                        ,CC."CodigoPortafolio"
                        ,CC."DescripcionPortafolio"
                        ,CASE 
                           WHEN v_FechaSistema >= p_FechaConsulta THEN CC."IdEstado" 
                         ELSE 'VIG' 
                         END "IdEstado"
                        ,CASE 
                           WHEN v_FechaSistema >= p_FechaConsulta THEN "NombreEstado" 
                         ELSE 'VIGENTE' 
                         END "NombreEstado"
                        ,CC."IdMonedaSistema"
                        ,CC."MonedaSistema"
                        ,CC."CodigoIFRS"
                        ,CC."IdConcepto"
                        ,CC."NombreConcepto"
                        ,CC."IdTipoOperacion"
                        ,CC."IdTipoComprobante"
                        ,CC."FolioComprobante"
                        ,CC."FechaComprobante"
                        ,CC."IdMesa"
                        ,CC."IdBolsa"
                        ,CC."NombreBolsa"
                        ,CC."IdSucursal"
                        ,CC."IdAgente"
                        ,CC."IdCondicionLiquidacion"
                        ,CC."NombreCondicionLiquidacion"
                        ,CC."FechaLiquidacion"
                        ,CC."IdClasificacionTipoInstrumento"
                        ,CC."ClasificacionTipoInstrumento"
                        ,CC."IdTipoInstrumento"
                        ,CC."TipoInstrumento"
                        ,CC."IdInstrumento"
                        ,CC."RazonSocialInstrumento"
                        ,CC."ISIN"
                        ,CC."Nemotecnico"
                        ,CC."IdEmisor"
                        ,CC."NemoSVS"
                        ,CC."IdMonedaEmision"
                        ,CC."IdTipoReajuste"
                        ,ifnull(CC."NombreTipoReajuste",'') AS "NombreTipoReajuste"
                        ,CC."IdMonedaInstrReajuste"
                        ,CC."NombreMonedaInstrReajuste"
                        ,CC."FechaEmisionInstrumento"
                        ,CC."FechaVencimientoInstrumento"
                        ,CC."EsFungible"
                        ,CC."IdBoveda"
                        ,CC."NombreBoveda"
                        ,CC."NumeroCuentaBoveda"
                        ,ifnull(CC."IdTipoCustodia",'') AS "IdTipoCustodia"
                        ,ifnull(CC."NombreTipoCustodia",'') AS "NombreTipoCustodia"
                        ,CC."IdMonedaInstrumento"
                        ,CC."MonedaInstrumento"
                        ,CASE 
                           WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."ValorRescate" 
                         ELSE CC."Cantidad" 
                         END AS "Cantidad"
                        ,CC."CorteMinimo"
                        ,CC."ValorRescate"
                        ,CC."PrecioCompra"
                        ,CC."MontoCompra"
                        ,CC."INGFechaOperacion"
                        ,ifnull(LAG(CC."EGRFechaLiquidacion") over (partition by "IdEmpresa", CC."FolioCartera" order by CC."EGRFechaLiquidacion" )
                        ,CC."INGFechaLiquidacion") as "INGFechaLiquidacion"
                        ,CASE 
                           WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."INGValorRescate" 
                         ELSE CC."INGCantidad" 
                         END AS "INGCantidad"
                        ,CC."TasaCompra"
                        ,CC."INGTasaCompra"
                        ,CC."INGValorRescate"
                        ,CC."INGPrecioCompra"
                        ,CC."INGPrecioPromedioCompra"
                        ,CC."INGMontoPrecioPromedioCompra"
                        ,CC."INGMontoATasaCompraActual"
                        ,CC."DiferenciaInversion"
                        ,CC."INGMontoCompraActualMonedaInstrumento"
                        ,CC."INGFuenteTasaPrecioMercadoActual"
                        ,CC."INGPrecioMercadoActual"
                        ,CC."Rentabilidad"
                        ,CC."RentabilidadReal"
                        ,CC."CostoUF"
                        ,CC."INGTasaMercadoActual"
                        ,CC."INGMontoATasaMercadoActual"
                        ,CC."INGMontoMercadoActualMonedaInstrumento"
                        ,CC."INGFechaPagoCorteCupon"
                        ,CC."INGInteresesPagoCorteCupon"
                        ,CC."INGMontoCorteCupon"
                        ,CC."INGMontoCapitalCorteCupon"
                        ,CC."INGMontoInteresCorteCupon"
                        ,CC."INGMontoInteresCompraActual"
                        ,CC."INGMontoReajusteCompraActual"
                        ,CC."InteresCorridoMonedaInstrumento"
                        ,CC."ReajusteCorridoMonedaInstrumento"
                        -----------------------------------------------
                        ,CC."EGRFechaOperacion"
                        ,CC."EGRFechaLiquidacion"
                        ,CC."EGRIdTipoComprobante"
                        ,CC."EGRFolioComprobante"
                        ,CC."EGRFechaComprobante"
                        ,CC."EGRIdConcepto"
                        ,ifnull(CC."EGRNombreConcepto",'') AS "EGRNombreConcepto"
                        ,CASE 
                           WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."EGRValorRescate" 
                         ELSE CC."EGRCantidad" 
                         END AS "EGRCantidad"
                        ,CC."EGRValorRescate"
                        ,CC."EGRPrecio"
                        ,CC."EGRTasa"
                        ,CC."EGRMonto"
                        ,CC."EGRMontoMonedaInstrumento"
                        ,CC."TipoOpcion"
                        ,ProductoAgrupador."NombreProductoAgrupador"
                        ,CC."INGFechaValorizacionCartera"
                        ,CC."RegimenTributario"
                        ,CC."FechaOrigenCompra"
      	          FROM :v_consulta_carteraUI AS CC
 	                LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
 	                  ON Portafolio."IdPortafolio" = CC."IdPortafolio" 
 	                 AND Portafolio."IdCliente" = CC."IdCliente"
 	                LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador 
 	                  ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
 	                LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" ProductoAgrupador 
 	                  ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
 	              WHERE "IdEmpresa" = p_IdEmpresa
 	                AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador);
		
	
END;
GO
