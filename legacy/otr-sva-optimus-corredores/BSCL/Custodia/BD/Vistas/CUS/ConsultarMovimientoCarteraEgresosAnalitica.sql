SET SCHEMA BSCL;

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica" AS

select 
     car."IdEmpresa"                        as "IdEmpresa"
    , car."IdCartera"                       as "IdCartera"
    , LEFT(TO_VARCHAR(IFNULL(car."EGRFechaOperacion",'0001-01-01')),10) as "FechaOperacion"
    , CASE WHEN IFNULL(car."EGRFechaOperacion",'0001-01-01')= '0001-01-01' 
                    THEN '0001-01-01 00:00:00' 
                        ELSE CASE WHEN  car."EGRFechaOperacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
                ELSE LEFT(TO_CHAR(ADD_SECONDS (car."EGRFechaOperacion", CONFIGURACION."Valor")),27)
               END 
    END                                     as "FechaRegistro"
    ,CAST('RETIRO'  as NVARCHAR(10))        as "TipoOperacion"                  
    , CASE WHEN car."IdCliente" = 0 THEN NULL ELSE car."IdCliente" END as "IdCliente"                                    
    , CASE WHEN car."IdPortafolio" = 0 THEN NULL ELSE car."IdPortafolio" END as "IdPortafolio"
    , CASE WHEN car."EGRIdConcepto" = '' THEN NULL ELSE car."EGRIdConcepto" END as "IdConcepto" 
    , coalesce(car."IdClasificacionTipoInstrumento",'')  as "IdClasificacionTipoInstrumento"
    , coalesce(car."IdTipoInstrumento",'') as "IdTipoInstrumento"
    , COALESCE(a1."IdInstrumento",car."IdInstrumento")  as "IdInstrumento"
    , CASE WHEN COALESCE(car."IdClasificacionTipoInstrumento",'') = 'IF' THEN CAST(car."NemoSVS" AS NVARCHAR(40)) 
          ELSE CAST(COALESCE(a1."Nemotecnico",car."Nemotecnico") AS NVARCHAR(40))  
      END as "Nemotecnico"
    , coalesce(CAST(Instrumento."ISIN" AS NVARCHAR(40)),'') as "ISIN"
    , case when trim(coalesce(car."IdEmisor",'')) = '' then coalesce(Instrumento2."IdEmisor",'OPTIMUS')  else car."IdEmisor" end as "IdEmisor"  
    , LEFT(TO_CHAR(IFNULL(car."FechaVencimientoInstrumento",'0001-01-01')),10) as "FechaVencimientoInstrumento"             
    , LEFT(TO_CHAR(IFNULL(car."EGRFechaLiquidacion",'0001-01-01')),10) as "FechaLiquidacion"                
    , CASE WHEN RTRIM(car."IdTipoReajuste") = '' THEN 'CLP' ELSE car."IdTipoReajuste" END as "IdTipoReajuste"                                               
    , CASE WHEN car."IdMonedaInstrumento" = '' THEN NULL ELSE car."IdMonedaInstrumento" END as "IdMonedaInstrumento"                    
    , CASE WHEN RTRIM(car."IdBoveda") = '' THEN 'COR' ELSE car."IdBoveda" END as "IdBoveda"                         
    , CAST(car."EGRCantidad" AS DECIMAL(26,8)) as "Cantidad"                       
    , CAST(car."EGRPrecio" AS DECIMAL(26,8)) as "Precio"                         
    , CAST(car."EGRTasa" AS DECIMAL(26,8))	as "Tasa"                               
    , CAST(case car."IdMonedaInstrumento" WHEN 'USD' THEN car."EGRMontoMonedaInstrumento" ELSE car."EGRMonto" END AS DECIMAL(26,8))	as "Monto"
    , car."EGRValorRescate"                 as "ValorRescate"                       
    , cast((car."INGCantidad" - "EGRCantidad") as DECIMAL(26,8)) as "SaldoCartera"                          
    , car."FolioCartera"                    as "FolioCartera"                       
    , CASE WHEN car."EGRIdTipoComprobante" = '' THEN NULL ELSE car."EGRIdTipoComprobante" END as "IdTipoComprobante"
    , car."EGRFolioComprobante"             as "FolioComprobante"
    , LEFT(TO_CHAR(IFNULL(car."EGRFechaComprobante",'0001-01-01')),10) as "FechaComprobante"
    , car."IdEstado"
    , car."EGRSpread"  
    , car."IdRegimenFondoMutuo" AS "IdRegimenTributario"
    , car."EGRMontoCompraFactor" AS "RentabilidadReal"
    , car."EGRCostoInversionEnFactorAjuste" AS "CostoUF"
    , COALESCE(SAR."IdTipoMovimientoFondoMutuo",'') AS "IdTipoMovimientoFondoMutuo"
    , car."EGRIdTipoOperacion" AS "IdTipoOperacion"                       
    , CASE WHEN COALESCE(port."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(port."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
	, CASE WHEN COALESCE(port."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MAT."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"               
	, TO_NVARCHAR(car."EGRFechaPrecioTasaMercado", 'YYYY-MM-DD') as "FechaFuente"
	, CASE WHEN COALESCE(MOV."IdTipoOperacion" , '') IN ('INGGTA', 'RETGTA') THEN COALESCE(MOV."ExcluyeMovimientoEnCartola", 'N') ELSE 'N' END AS "ExcluyeMovimientoEnCartola"
    FROM "BSCL.Custodia.BD::CUS.Cartera" as car
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SAR on (SAR."IdSolicitudAporteRescate" = car."EGRIdOperacion" )
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIGURACION on "CodigoParametro" = 'PLA.SegundosZonaHoraria'
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento ON Instrumento."IdInstrumento" = car."IdInstrumento"
    LEFT JOIN (
    select RANK() over (partition by  "FolioCarteraRelacionada" order by "IdSubyacente" ASC) as RANK
                ,"IdSubyacente"
                ,"FolioCarteraRelacionada"
                ,"IdInstrumento"
                ,"Nemotecnico"
                 from "BSCL.Custodia.BD::CUS.Subyacente"
        ) as a1 ON a1.RANK=1 and a1."FolioCarteraRelacionada" = car."FolioCartera"
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento2 ON Instrumento2."IdInstrumento" = a1."IdInstrumento"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS port ON CAR."IdPortafolio" = port."IdPortafolio"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = port."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = PORT."CodigoPortafolio" AND port."IdMatrizAgrupadorAPV" = 0
	LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MOV ON MOV."IdMovimientoCarteraEnTransito" = car."EGRIdMovimientoCarteraEnTransito"
	WHERE car."EGRIdTipoOperacion" NOT IN ('INGGTA','RETGTA') AND car."EGRCantidad" <> 0;
	
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
