--DESCONTINUADA
SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.CarteraRVHisAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.CarteraRVHisAnalitica" 
AS
SELECT 
	
					TO_VARCHAR(car."FechaRespaldo",'YYYY-MM-DD')								AS "FechaRespaldo"
                    , car."IdEmpresa"                               AS "IdEmpresa"
                    , car."IdCartera"                               AS "IdCartera"
                    , car."FolioCartera"                            AS "FolioCartera"
                    , TO_VARCHAR(IFNULL(car."FechaIngresoRegistro",'0001-01-01 00:00:00.0'))        AS "FechaIngresoRegistro"
                    , TO_VARCHAR(IFNULL(car."FechaModificacionRegistro",'0001-01-01 00:00:00.0'))    AS "FechaModificacionRegistro"
                    ,CASE WHEN car."IdCliente" = 0 THEN NULL ELSE car."IdCliente" END AS "IdCliente" 
					,CASE WHEN car."IdPortafolio" = 0 THEN NULL ELSE car."IdPortafolio" END AS "IdPortafolio"  
					,CASE WHEN car."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE car."IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumento" 
					,CASE WHEN car."IdTipoInstrumento" = '' THEN NULL ELSE car."IdTipoInstrumento" END AS "IdTipoInstrumento" 
					,CASE WHEN car."IdInstrumento" = 0 THEN NULL ELSE car."IdInstrumento" END AS "IdInstrumento" 
					    
                    ,CASE when car."IdClasificacionTipoInstrumento" = 'IF' THEN CAST(car."NemoSVS" AS NVARCHAR(40))
                        else CAST(car."Nemotecnico"AS NVARCHAR(40))
                    end                                     AS "Nemotecnico"
                    ,CASE WHEN car."IdMonedaInstrumento" = '' THEN NULL ELSE car."IdMonedaInstrumento" END AS "IdMonedaInstrumento"
                    , CASE
                        WHEN ifnull(CAR."IdTipoReajuste", '') <> '' THEN CAR."IdTipoReajuste"
                        WHEN ifnull(CAR."IdMonedaPrincipal",'') <> '' THEN CAR."IdMonedaPrincipal"
                        ELSE ifnull(CAR."IdMonedaInstrumento",'CLP')
                      end AS "IdTipoReajuste"
					,CASE WHEN RTRIM(car."IdEmisor") = '' THEN 'OPTIMUS' ELSE CAST(car."IdEmisor" AS NVARCHAR(40)) END AS "IdEmisor"
                    , LEFT(TO_VARCHAR(IFNULL(car."FechaEmisionInstrumento",'0001-01-01')),10) AS "FechaEmisionInstrumento"
                    , case 
                        when car."IdTipoInstrumento" IN ('SIM','PRT','EMP') then LEFT(TO_VARCHAR(IFNULL(car."FechaVencimientoOperacion",'0001-01-01')),10)
                        else LEFT(TO_VARCHAR(IFNULL(car."FechaVencimientoInstrumento",'0001-01-01')),10)    
                     end                                            AS "FechaVencimientoInstrumento"
                    , car."IdBoveda"                                AS "IdBoveda"
                    , LEFT(TO_VARCHAR(IFNULL(car."FechaOperacion",'0001-01-01')),10) AS "FechaOperacionInicial"
                    , car."Cantidad"                                AS "CantidadInicial" 
                    , car."PrecioCompra"                            AS "PrecioCompraInicial"
                    , car."MontoCompra"                             AS "MontoCompraInicial"
                    , car."MontoCompraMonedaInstrumento"            AS "MontoCompraMonedaInstrumentoInicial"
                    , car."IdCondicionLiquidacion"                  AS "IdCondicionLiquidacionInicial"
                    , LEFT(TO_VARCHAR(IFNULL(car."FechaLiquidacion",'0001-01-01')),10) AS "FechaLiquidacionInicial"
                    , LEFT(TO_VARCHAR(IFNULL(car."INGFechaOperacion",'0001-01-01')),10) AS "FechaOperacion"
                    , LEFT(TO_VARCHAR(IFNULL(car."EGRFechaOperacion",'0001-01-01')),10) AS "EGRFechaOperacion"
                    , LEFT(TO_VARCHAR(IFNULL(car."EGRFechaLiquidacion",'0001-01-01')),10) AS "EGRFechaLiquidacion"
                    , car."INGCantidad"                             AS "Cantidad"
                    , car."INGPrecioCompra"                         AS "PrecioCompra"
                    , car."INGMontoCompraActualMonedaInstrumento"   AS "MontoCompraActualMonedaInstrumento"
                    , car."INGPrecioPromedioCompra"                 AS "PrecioPromedioCompra"
                    , car."INGMontoPrecioPromedioCompra"            AS "MontoPrecioPromedioCompra"
                    , car."INGMontoPrecioPromedioMonedaInstrumento" AS "MontoPrecioPromedioMonedaInstrumento"
                    , car."INGIdCondicionLiquidacion"               AS "IdCondicionLiquidacion"
                    , LEFT(TO_VARCHAR(IFNULL(car."INGFechaLiquidacion",'0001-01-01')),10) AS "FechaLiquidacion"
                    , car."INGPrecioMercadoActual"                  AS "PrecioMercadoActual"
                    , car."INGMontoMercadoActualMonedaInstrumento"  AS "MontoMercadoActualMonedaInstrumento"
                    
					,CASE WHEN car."IdConcepto" = '' THEN NULL ELSE car."IdConcepto" END AS "IdConcepto" 
                    , car."IdTipoOperacion"                         AS "IdTipoOperacion"
					,CASE WHEN TipoOperacion."IdTipoProducto" = '' THEN NULL ELSE TipoOperacion."IdTipoProducto" END AS "IdTipoProductoTIPOPE" 
                        
					,CASE WHEN car."IdTipoComprobante"  = '' THEN NULL ELSE car."IdTipoComprobante"  END AS "IdTipoComprobante" 
					, car."FolioComprobante"                        AS "FolioComprobante"
                    , LEFT(TO_VARCHAR(IFNULL(car."FechaComprobante",'0001-01-01')),10)  AS "FechaComprobante"
 		    		, CASE WHEN car."IdSucursal"  = 0 THEN NULL ELSE car."IdSucursal"  END AS "IdSucursal" 
                    , CASE WHEN car."IdAgente" = 0 THEN NULL ELSE car."IdAgente" END AS "IdAgente" 
                    , car."FolioCarteraRelacionada"                 AS "FolioCarteraRelacionada"
                    ,CASE WHEN car."IdEstado" = '' THEN NULL ELSE car."IdEstado" END AS "IdEstado" 
                    ,CASE WHEN car."IdTipoProducto" = '' THEN NULL ELSE car."IdTipoProducto" END AS "IdTipoProducto" 
                    ,CAST(COALESCE(CLA."ValorClasificacionRiesgo",'') AS NVARCHAR(20)) AS "ClasificacionRiesgoInstrumento"
					
					, car."IdRegimenFondoMutuo"                     AS "IdRegimenTributario"
                    , IFNULL(car."MontoCompraFactor",0.0)           AS "RentabilidadReal"
                    , IFNULL(car."CostoInversionEnFactorAjuste",0.0)AS "CostoUF"
                    , COALESCE(SAR."IdTipoMovimientoFondoMutuo",'') AS "IdTipoMovimientoFondoMutuo"
                    , port."IdMatrizAgrupador"                      AS "IdMatrizAgrupador"
                    , port."EsADC"                                  AS "EsADC"
                FROM "BSCL.Custodia.BD::HIS.Cartera" AS car 
                    LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" 
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON car."IdTipoOperacion" = TipoOperacion."IdTipoOperacion" 
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste" 
                    LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON car."IdBolsa" = Bolsa."IdBolsa" 
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON car."IdConcepto" = Concepto."IdConcepto" 
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as ConceptoEGR ON car."EGRIdConcepto" = ConceptoEGR."IdConcepto" 
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON car."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion" 
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON car."IdEmpresa" = Empresa."IdEmpresa" 
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" as Modulo ON car."IdModulo" = Modulo."IdModulo" 
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON car."IdEstado" = Estado."IdEstado" 
                    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento ON car."IdInstrumento" = Instrumento."IdInstrumento"
                    LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = car."IdTipoInstrumento" 
                    LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = car."IdClasificacionTipoInstrumento" 
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON Mon1."IdMoneda" = car."IdMonedaSistema"
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON Mon2."IdMoneda" = car."IdMonedaInstrumento"
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon3 ON Mon3."IdMoneda" = car."IdMonedaPrincipal"  
                    LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon4 ON Mon4."IdMoneda" = car."IdMonedaInstrumento"
                    LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" CLA 
                            ON (CLA."IdInstrumento" = car."IdInstrumento" AND CLA."IdEntidadClasificadoraRiesgo" ='BCS')                    
                    LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SAR on (SAR."IdSolicitudAporteRescate" = CAR."IdOperacion" ) 
                    WHERE car."IdClasificacionTipoInstrumento" = 'FM' AND car."IdEstado" = 'VIG' ;
                    
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::HIS.CarteraRVHisAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
