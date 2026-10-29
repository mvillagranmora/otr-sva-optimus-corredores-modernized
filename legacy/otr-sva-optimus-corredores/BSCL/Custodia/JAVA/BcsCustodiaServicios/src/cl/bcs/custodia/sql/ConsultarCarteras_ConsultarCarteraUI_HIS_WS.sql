WITH
car_his_base AS (
    SELECT *
    FROM "BSCL.Custodia.BD::HIS.Cartera"
    WHERE "IdEmpresa" = :p_IdEmpresa
      AND "FechaRespaldo" = :p_FechaConsulta
),
car_neg_base AS (
    SELECT *
    FROM "BSCL.Custodia.BD::HIS.CarteraNegativa"
    WHERE "IdEmpresa" = :p_IdEmpresa
      AND "FechaRespaldo" = :p_FechaConsulta
),
cfg_fecha_sis AS (
    SELECT
        "IdEmpresa",
        TO_DATE("Valor") AS "FechaSistema"
    FROM "BSCL.Plataforma.BD::PLA.Configuracion"
    WHERE "CodigoParametro" = 'PLA.FechaSistema'
      AND "IdEmpresa" = :p_IdEmpresa
),
instrumentos_premer AS (
    SELECT DISTINCT "IdInstrumento"
    FROM car_his_base
    WHERE "IdInstrumento" IS NOT NULL
),
premer AS (
    SELECT
        PRE."IdInstrumento",
        PRE."PrecioCierre",
        PRE."Fecha",
        PRE."IdEmpresa"
    FROM (
        SELECT
            ROW_NUMBER() OVER (
                PARTITION BY prec."IdInstrumento", SIS."IdEmpresa"
                ORDER BY prec."Fecha" DESC
            ) AS row_num,
            prec."IdInstrumento",
            prec."Fecha",
            prec."IdBolsa",
            prec."PrecioCierre",
            SIS."IdEmpresa"
        FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
        INNER JOIN cfg_fecha_sis SIS
            ON prec."Fecha" <= SIS."FechaSistema"
        INNER JOIN instrumentos_premer IP
            ON IP."IdInstrumento" = prec."IdInstrumento"
        WHERE prec."IdBolsa" = 'BCS'
    ) PRE
    WHERE PRE.row_num = 1
      AND PRE."PrecioCierre" > 0
),
folios_pac AS (
    SELECT DISTINCT "FolioCartera"
    FROM car_his_base
    WHERE "IdTipoInstrumento" = 'PAC'
      AND "IdTipoOperacion" IN ('VENPAC', 'COMPAC')
      AND "IdEstado" = 'VIG'
),
sub_primer_nemo AS (
    SELECT
        S."FolioCarteraRelacionada",
        S."Nemotecnico"
    FROM (
        SELECT
            miSub."FolioCarteraRelacionada",
            miSub."Nemotecnico",
            ROW_NUMBER() OVER (
                PARTITION BY miSub."FolioCarteraRelacionada"
                ORDER BY (SELECT 1 FROM DUMMY)
            ) AS "NroFila"
        FROM "BSCL.Custodia.BD::CUS.Subyacente" AS miSub
        INNER JOIN folios_pac FP
            ON FP."FolioCartera" = miSub."FolioCarteraRelacionada"
    ) S
    WHERE S."NroFila" = 1
),
CARHIS AS (
SELECT 
	RANK() over (partition by car."IdEmpresa", car."FolioCartera", CASE car."EGRFechaLiquidacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaLiquidacion" END order by CAR."IdCartera" desc) as "RANKG"
	, car."IdCartera"
	, car."FechaOperacion"
	, car."IdEstado"
	, car."IdClasificacionTipoInstrumento"
	, car."IdTipoInstrumento"
	, CASE 
        WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
        ELSE car."Nemotecnico"
      END AS "Nemotecnico"
	, CASE 
		WHEN IFNULL(car."IdMonedaEmision",'') <> '' THEN car."IdMonedaEmision" 
	   	WHEN IFNULL(car."IdTipoReajuste",'') <> '' THEN car."IdTipoReajuste" 
	   	ELSE car."IdMonedaInstrumento"
	  END AS "IdMonedaEmision"
	, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."INGValorRescate" ELSE car."INGCantidad" END AS "INGCantidad"
	, car."INGPrecioCompra"
	, (CASE car."IdClasificacionTipoInstrumento" WHEN 'RV' THEN COALESCE(car."INGPrecioMercadoActual", PREMER."PrecioCierre",0)  ELSE car."INGPrecioMercadoActual" END )	AS "INGPrecioMercadoActual"
	, (((CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE 
								WHEN COALESCE(PREMER."PrecioCierre",0 )=0 THEN car."INGMontoMercadoActualMonedaInstrumento" 
								ELSE COALESCE(PREMER."PrecioCierre",0 ) * "INGCantidad" 
							END) 
			ELSE car."INGMontoMercadoActualMonedaInstrumento" 
		 END)- car."INGMontoCompraActualMonedaInstrumento")	
		 /  (CASE 
		 		WHEN car."INGMontoCompraActualMonedaInstrumento" >0 THEN car."INGMontoCompraActualMonedaInstrumento"
		 		ELSE 1 
		 	 END))*100 AS "Rentabilidad"
	, (	CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE 
								WHEN COALESCE(car."INGMontoMercadoActualMonedaInstrumento",0.0 )>0 THEN car."INGMontoMercadoActualMonedaInstrumento" 
								ELSE COALESCE(PREMER."PrecioCierre",0 )* car."INGCantidad" 
							END) 
			ELSE car."INGMontoMercadoActualMonedaInstrumento" 
		END) AS "INGMontoMercadoActualMonedaInstrumento"
FROM car_his_base AS car 
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" 
	LEFT JOIN premer PREMER
	    ON car."IdInstrumento" = PREMER."IdInstrumento"
	   AND car."IdEmpresa" = PREMER."IdEmpresa"			
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = car."IdRegimenFondoMutuo"	
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Port."IdMatrizAgrupadorAPV"
    LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
WHERE car."IdEmpresa" = :p_IdEmpresa 
AND car."IdEstado" NOT IN ('ANU','RPA')
AND car."IdEstado" = CASE WHEN :p_IdEstado <> '' THEN :p_IdEstado ELSE car."IdEstado" END
AND car."IdTipoInstrumento" NOT IN ('SIM','PAC','RRV','RRF','PRT','PRF','EMP','ERF')
AND car."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN :p_IdCliente ELSE car."IdCliente" END
AND car."IdPortafolio" = CASE WHEN :p_IdPortafolio > 0 THEN :p_IdPortafolio ELSE car."IdPortafolio" END
AND car."IdClasificacionTipoInstrumento" = CASE WHEN :p_IdClasificacionTipoInstrumento <> '' THEN :p_IdClasificacionTipoInstrumento ELSE car."IdClasificacionTipoInstrumento" END
AND car."IdTipoInstrumento" = CASE WHEN :p_IdTipoInstrumento <> '' THEN :p_IdTipoInstrumento ELSE car."IdTipoInstrumento" END
AND car."IdInstrumento" = CASE WHEN :p_IdInstrumento > 0 THEN :p_IdInstrumento ELSE car."IdInstrumento" END
AND car."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE car."Nemotecnico" END
AND car."FolioCartera" = CASE WHEN :p_FolioCartera > 0 THEN :p_FolioCartera ELSE car."FolioCartera" END
AND ((:p_tipoFecha = 'LIQ' AND car."INGFechaLiquidacion" <= :p_FechaConsulta AND  (car."EGRFechaLiquidacion" >= :p_FechaConsulta OR car."EGRFechaLiquidacion" ='0001-01-01')) OR
	 (:p_tipoFecha <> 'LIQ' AND car."FechaOperacion" <= :p_FechaConsulta AND car."EGRCantidad" = 0))
AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
UNION ALL
SELECT 
	RANK() over (partition by car."IdEmpresa", car."FolioCartera", CASE car."EGRFechaLiquidacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaLiquidacion" END order by CAR."IdCartera" desc) as "RANKG"
	, car."IdCartera"
	, car."FechaOperacion"
	, car."IdEstado"
	, car."IdClasificacionTipoInstrumento"
	, car."IdTipoInstrumento"
	, SPN."Nemotecnico" AS "Nemotecnico"
	, CASE 
		WHEN IFNULL(car."IdMonedaEmision",'') <> '' THEN car."IdMonedaEmision" 
	   	WHEN IFNULL(car."IdTipoReajuste",'') <> '' THEN car."IdTipoReajuste" 
	   	ELSE car."IdMonedaInstrumento"
	  END AS "IdMonedaEmision"
	, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."INGValorRescate" ELSE car."INGCantidad" END AS "INGCantidad"
	, car."INGPrecioCompra"
	, (CASE car."IdClasificacionTipoInstrumento" WHEN 'RV' THEN COALESCE(car."INGPrecioMercadoActual", PREMER."PrecioCierre",0)  ELSE car."INGPrecioMercadoActual" END )	AS "INGPrecioMercadoActual"
	, (((CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE 
								WHEN COALESCE(PREMER."PrecioCierre",0 )=0 THEN car."INGMontoMercadoActualMonedaInstrumento" 
								ELSE COALESCE(PREMER."PrecioCierre",0 ) * CAR."INGCantidad" 
							END) 
			ELSE car."INGMontoMercadoActualMonedaInstrumento" 
		 END)- car."INGMontoCompraActualMonedaInstrumento")	
		 /  (CASE 
		 		WHEN car."INGMontoCompraActualMonedaInstrumento" >0 THEN car."INGMontoCompraActualMonedaInstrumento"
		 		ELSE 1 
		 	 END))*100 AS "Rentabilidad"
	, (	CASE car."IdClasificacionTipoInstrumento" 
			WHEN 'RV' THEN (CASE 
								WHEN COALESCE(car."INGMontoMercadoActualMonedaInstrumento",0.0 )>0 THEN car."INGMontoMercadoActualMonedaInstrumento" 
								ELSE COALESCE(PREMER."PrecioCierre",0 )* car."INGCantidad" 
							END) 
			ELSE car."INGMontoMercadoActualMonedaInstrumento" 
		END) AS "INGMontoMercadoActualMonedaInstrumento"
FROM car_his_base AS CAR 
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN sub_primer_nemo SPN ON SPN."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = :p_IdEmpresa AND CLI."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END)
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON PORT."IdPortafolio" = CAR."IdPortafolioRelacionado" AND PORT."IdCliente" = CAR."IdClienteRelacionado"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = PORT."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" as CCPI ON CCPI."IdCuentaCustodioPortafolio" = car."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as BOV ON BOV."IdBoveda" = CCPI."IdBoveda"
LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCustodia" TIPOCUSTODIA on TIPOCUSTODIA."IdTipoCustodia" = CCPI."IdTipoCustodia"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN premer PREMER
    ON car."IdInstrumento" = PREMER."IdInstrumento"
   AND car."IdEmpresa" = PREMER."IdEmpresa"
WHERE CAR."IdEmpresa" = :p_IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND car."FolioCartera" = CASE WHEN :p_FolioCartera > 0 THEN :p_FolioCartera ELSE car."FolioCartera" END
AND car."IdEstado" = CASE WHEN :p_IdEstado <> '' THEN :p_IdEstado ELSE car."IdEstado" END
AND car."FechaRespaldo" = :p_FechaConsulta
AND	CASE WHEN :p_IdCliente > 0 THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END = case when :p_IdCliente > 0 then :p_IdCliente else CAR."IdCliente" END
AND	CASE WHEN :p_IdCliente > 0 THEN CAR."IdPortafolioRelacionado" ELSE CAR."IdPortafolio" END = case when :p_IdPortafolio > 0 then :p_IdPortafolio else CAR."IdPortafolio" END
AND
(
       (:p_tipoFecha = 'LIQ' AND CAR."INGFechaLiquidacion" <= :p_FechaConsulta)
    OR (:p_tipoFecha = 'ADQ' AND CAR."INGFechaOperacion" <= :p_FechaConsulta)
)
AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE SUB."IdInstrumento" END
AND SUB."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE SUB."Nemotecnico" END
AND CAR."IdTipoInstrumento" = 'PAC'
AND SUB."IdEstado"='VIG'
AND CAR."IdTipoOperacion"   IN ('VENPAC', 'COMPAC')
AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
UNION ALL
SELECT  
      1 as "RANKG"
	, car."IdCarteraNegativa" AS "IdCartera"
	, car."FechaOperacion"
    , car."IdEstado"
    , TipoInstrumento."IdClasificacionTipoInstrumento" 
    , TipoInstrumento."IdTipoInstrumento"
    , case when TipoInstrumento."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
			else car."Nemotecnico" 
	  end as "Nemotecnico"
    , CASE WHEN IFNULL(car."IdMonedaEmision",'') <> '' THEN car."IdMonedaEmision" 
           WHEN IFNULL(car."IdTipoReajuste",'') <> '' THEN car."IdTipoReajuste" 
           ELSE car."IdMonedaInstrumento"
      END AS "IdMonedaEmision"
    , -1 * car."Cantidad" AS "INGCantidad"
    , car."PrecioOperacion" AS "INGPrecioCompra"
    , (CASE CLASINST."IdClasificacionTipoInstrumento" WHEN 'RV' THEN COALESCE(PRECIO_BCS."Precio",car."PrecioMercado" , 0.0)  ELSE car."PrecioMercado"  END ) AS "INGPrecioMercadoActual"
    ,(((CASE ClasInst."IdClasificacionTipoInstrumento" WHEN 'RV' THEN (CASE WHEN COALESCE(car."MontoMercadoMonedaInstrumento" ,0.0 )>0 
	                                                                    THEN car."MontoMercadoMonedaInstrumento" 
	                                                                    ELSE COALESCE(PRECIO_BCS."Precio",0 ) * car."Cantidad"  END)  
	                                             ELSE car."MontoMercadoMonedaInstrumento" END)
	     - car."MontoOperacionMonedaInstrumento") /  (CASE WHEN car."MontoOperacionMonedaInstrumento" >0 THEN car."MontoOperacionMonedaInstrumento" ELSE 1 END))*100  
   	AS "Rentabilidad" 
    , -1 * car."MontoMercadoMonedaInstrumento" AS "INGMontoMercadoActualMonedaInstrumento"
FROM car_neg_base AS car	
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento ON car."IdInstrumento" = Instrumento."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" as ConfSisMon on ConfSisMon."CodigoParametro" = 'PLA.MonedaSistema' and ConfSisMon."IdEmpresa" = :p_IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento"  AS PRECIO_BCS
    ON  PRECIO_BCS."IdEmpresa" = :p_IdEmpresa AND PRECIO_BCS."IdInstrumento" = car."IdInstrumento"  AND PRECIO_BCS."FechaSistema" = :p_FechaConsulta

WHERE 
CAR."IdEmpresa" = :p_IdEmpresa
AND car."IdEstado"='VIG'
AND car."IdCliente" = CASE WHEN :p_IdCliente > 0 THEN :p_IdCliente ELSE car."IdCliente" END
AND car."IdPortafolio" = CASE WHEN :p_IdPortafolio > 0 THEN :p_IdPortafolio ELSE car."IdPortafolio" END
AND car."FolioCustodia" = CASE WHEN :p_FolioCartera > 0 THEN :p_FolioCartera ELSE car."FolioCustodia" END
AND car."IdInstrumento" = CASE WHEN :p_IdInstrumento > 0 THEN :p_IdInstrumento ELSE car."IdInstrumento" END
AND car."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE car."Nemotecnico" END
AND TipoInstrumento."IdTipoInstrumento" = CASE WHEN :p_IdTipoInstrumento <> '' THEN :p_IdTipoInstrumento ELSE TipoInstrumento."IdTipoInstrumento" END
AND ClasInst."IdClasificacionTipoInstrumento" = CASE WHEN :p_IdClasificacionTipoInstrumento <> '' THEN :p_IdClasificacionTipoInstrumento ELSE ClasInst."IdClasificacionTipoInstrumento" END
AND
(
       (:p_tipoFecha = 'LIQ' AND CAR."FechaLiquidacion" <= :p_FechaConsulta)
    OR (:p_tipoFecha = 'ADQ' AND CAR."FechaOperacion" <= :p_FechaConsulta)
)
)
SELECT * FROM CARHIS
WHERE  ("IdEstado" = 'VIG' AND "RANKG" > 1) OR "RANKG" = 1;