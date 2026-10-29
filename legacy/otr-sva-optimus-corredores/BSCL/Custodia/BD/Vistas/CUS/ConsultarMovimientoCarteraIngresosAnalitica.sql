SET SCHEMA BSCL;

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" AS

SELECT
	"IdEmpresa"
    , "IdCartera"
    , "FechaOperacion"
    , "FechaRegistro"
    , "TipoOperacion"
    , "IdCliente"
    , "IdPortafolio"
    , "IdConcepto"
    , "IdClasificacionTipoInstrumento"
    , "IdTipoInstrumento"
    , "IdInstrumento"
    , "Nemotecnico" 
    , "ISIN"
    , "IdEmisor"  
    , "FechaVencimientoInstrumento"
    , "FechaLiquidacion"
    , "IdTipoReajuste"                                               
    , "IdMonedaInstrumento" 
    , "IdBoveda"                         
    , CAST("Cantidad" AS DECIMAL(26,8)) AS "Cantidad"
    , CAST("Precio" AS DECIMAL(26,8)) AS "Precio"
    , CAST("Tasa" AS DECIMAL(26,8)) AS "Tasa"
    , "Monto"
    , "ValorRescate"
    , "SaldoCartera"
    , "FolioCartera"
    , "IdTipoComprobante"  
    , "FolioComprobante"
    , "FechaComprobante"
    , "IdEstado"
    , "EGRSpread"
    , "IdRegimenTributario"
    , "RentabilidadReal"
    , "CostoUF"
    , "IdTipoMovimientoFondoMutuo"
    , "IdTipoOperacion"                          
    , "IdMatrizAgrupador"
    , "EsADC"
    , "FechaFuente"
    , "ExcluyeMovimientoEnCartola"
FROM ( 
	SELECT
		car."IdEmpresa"
		, car."IdCartera" as "IdCartera"
	    , LEFT(TO_CHAR(IFNULL(car."INGFechaOperacion",'0001-01-01')),10)   as "FechaOperacion"-- REVISAR
	    ,CASE WHEN IFNULL(car."INGFechaOperacion" ,'0001-01-01') = '0001-01-01' 
	                    THEN '0001-01-01 00:00:00' 
	                ELSE 
	                    CASE WHEN  car."INGFechaOperacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') 
	                        THEN '0001-01-01 00:00:00' 
	                    ELSE LEFT(TO_CHAR(ADD_SECONDS (car."INGFechaOperacion" , CONFIGURACION."Valor")),27)
	                END 
	     END as "FechaRegistro"
	    , CAST('INGRESO' AS NVARCHAR(10)) as "TipoOperacion"
	    , CASE WHEN car."IdCliente" = 0 THEN NULL ELSE car."IdCliente" END as "IdCliente"
	    , CASE WHEN car."IdPortafolio" = 0 THEN NULL ELSE car."IdPortafolio" END as "IdPortafolio"
	    , CASE WHEN car."IdConcepto" = '' THEN NULL ELSE car."IdConcepto" END as "IdConcepto"
	    , car."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
	    , CASE WHEN car."IdTipoInstrumento" = '' THEN NULL ELSE car."IdTipoInstrumento" END as "IdTipoInstrumento"
	    , COALESCE(sub."IdInstrumento", comsim."IdInstrumentoSubyacente",car."IdInstrumento")  as "IdInstrumento"
	    , CASE WHEN COALESCE(car."IdClasificacionTipoInstrumento",'') = 'IF' THEN CAST(car."NemoSVS" AS NVARCHAR(40)) 
	        ELSE CAST(COALESCE(sub."Nemotecnico",Instrumento2."Nemotecnico",car."Nemotecnico") AS NVARCHAR(40))  
	      END as "Nemotecnico"
	    , coalesce(CAST(Instrumento."ISIN" AS NVARCHAR(40)),'') as "ISIN"
	    , case when trim(coalesce(car."IdEmisor",'')) = '' then coalesce(Instrumento3."IdEmisor",Instrumento2."IdEmisor",'OPTIMUS')  else car."IdEmisor" end as "IdEmisor"  
	    , LEFT(TO_CHAR(IFNULL(car."FechaVencimientoInstrumento",'0001-01-01')),10)  as "FechaVencimientoInstrumento"
	    , LEFT(TO_CHAR(IFNULL(car."FechaLiquidacion",'0001-01-01')),10)  as "FechaLiquidacion"
	    , CASE WHEN RTRIM(car."IdTipoReajuste") = '' THEN 'CLP' ELSE car."IdTipoReajuste" END as "IdTipoReajuste"                                               
	    , CASE WHEN car."IdMonedaInstrumento" = '' THEN NULL ELSE car."IdMonedaInstrumento" END as "IdMonedaInstrumento" 
	    , CASE WHEN RTRIM(car."IdBoveda") = '' THEN 'COR' ELSE car."IdBoveda" END as "IdBoveda"                         
	    , car."Cantidad" as "Cantidad"
	    , car."PrecioCompra" as "Precio"
	    , car."TasaCompra" as "Tasa"
	    , CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'FM' THEN car."MontoCompra" WHEN car."MontoCompraMonedaInstrumento" = 0 THEN car."Cantidad"*car."PrecioCompra" ELSE car."MontoCompraMonedaInstrumento" END as "Monto"
	    , car."ValorRescate" as "ValorRescate"
	    , car."Cantidad" as "SaldoCartera"
	    , car."FolioCartera" as "FolioCartera"
	    , CASE WHEN car."IdTipoComprobante" = '' THEN NULL ELSE car."IdTipoComprobante" END AS "IdTipoComprobante"  
	    , car."FolioComprobante"
	    , LEFT(TO_CHAR(IFNULL(car."FechaComprobante",'0001-01-01')),10) as "FechaComprobante"
	    ,car."IdEstado"
	    ,car."EGRSpread"
	    ,car."IdRegimenFondoMutuo" AS "IdRegimenTributario"
	    ,car."MontoCompraFactor" AS "RentabilidadReal"
	    ,car."CostoInversionEnFactorAjuste" AS "CostoUF"
	    ,COALESCE(SAR."IdTipoMovimientoFondoMutuo",'') AS "IdTipoMovimientoFondoMutuo"
	    ,car."IdTipoOperacion"                          
	    , CASE WHEN COALESCE(PORT."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(PORT."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
		, CASE WHEN COALESCE(PORT."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MAT."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
	    , TO_NVARCHAR(car."INGFechaPrecioTasaMercadoActual", 'YYYY-MM-DD') as "FechaFuente"
	    , CASE WHEN COALESCE(MOV."IdTipoOperacion" , '') IN ('INGGTA', 'RETGTA') THEN COALESCE(MOV."ExcluyeMovimientoEnCartola", 'N') ELSE 'N' END AS "ExcluyeMovimientoEnCartola"
	FROM "BSCL.Custodia.BD::CUS.Cartera" as car
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SAR on (SAR."IdSolicitudAporteRescate" = car."IdOperacion" )
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIGURACION on "CodigoParametro" = 'PLA.SegundosZonaHoraria'
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento ON Instrumento."IdInstrumento" = car."IdInstrumento"
    LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS comsim ON car."IdTipoInstrumento" = 'SIM' AND comsim."IdComprobante" = car."FolioComprobante" 
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento2 ON Instrumento2."IdInstrumento" = comsim."IdInstrumentoSubyacente"
    LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" sub ON car."IdTipoInstrumento" IN ('PRT', 'EMP', 'FWD') AND  sub."FolioCarteraRelacionada" = car."FolioCartera" and sub."EsPrimeraCartera" = 'S'
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento3 ON Instrumento3."IdInstrumento" = sub."IdInstrumento"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON CAR."IdPortafolio" = PORT."IdPortafolio"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = PORT."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = PORT."CodigoPortafolio" AND PORT."IdMatrizAgrupadorAPV" = 0
	LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MOV ON MOV."IdMovimientoCarteraEnTransito" = car."EGRIdMovimientoCarteraEnTransito"
	WHERE car."EsPrimeraCartera" = 'S'
);

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
