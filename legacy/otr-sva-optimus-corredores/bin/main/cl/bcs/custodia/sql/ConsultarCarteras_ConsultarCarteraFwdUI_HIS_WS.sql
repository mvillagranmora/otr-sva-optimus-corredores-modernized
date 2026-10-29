WITH
car_his_base AS (
    SELECT *
    FROM "BSCL.Custodia.BD::HIS.Cartera"
    WHERE "IdEmpresa" = :p_IdEmpresa
      AND "FechaRespaldo" = :p_FechaConsulta
),
PREMER AS (
    SELECT
        PRE."IdInstrumento",
        PRE."PrecioCierre",
        PRE."Fecha",
        PRE."IdEmpresa"
    FROM (
        SELECT
            ROW_NUMBER() OVER (
                PARTITION BY prec."IdInstrumento", sis."IdEmpresa"
                ORDER BY prec."Fecha" DESC
            ) AS "RN",
            prec."IdInstrumento",
            prec."Fecha",
            prec."PrecioCierre",
            sis."IdEmpresa"
        FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
        INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" sis
            ON sis."CodigoParametro" = 'PLA.FechaSistema'
           AND sis."IdEmpresa" = :p_IdEmpresa
           AND TO_DATE(prec."Fecha") <= TO_DATE(sis."Valor")
        WHERE prec."IdBolsa" = 'BCS'
    ) PRE
    WHERE PRE."RN" = 1
      AND PRE."PrecioCierre" > 0
),
CARFWDHIS AS(
SELECT 
	RANK() over (partition by car."IdEmpresa", car."FolioCartera", CASE car."EGRFechaLiquidacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaLiquidacion" END order by CAR."IdCartera" desc) as "RANKG"
	, car."IdCartera"
	, car."FechaOperacion"
	, car."IdEstado" 
	, car."IdClasificacionTipoInstrumento"
	, car."IdTipoInstrumento"
	, car."Nemotecnico"
	, CASE 
		WHEN IFNULL(car."IdMonedaEmision",'') <> '' THEN car."IdMonedaEmision" 
	   	WHEN IFNULL(car."IdTipoReajuste",'') <> '' THEN car."IdTipoReajuste" 
	   	ELSE car."IdMonedaInstrumento"
	  END AS "IdMonedaEmision"
	, car."INGCantidad"
	, car."INGPrecioCompra"
	, car."INGPrecioMercadoActual"
	, (car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento") /
	  (CASE WHEN car."INGMontoCompraActualMonedaInstrumento" > 0 THEN car."INGMontoCompraActualMonedaInstrumento" ELSE 1 END) * 100 AS "Rentabilidad"
	, car."INGMontoMercadoActualMonedaInstrumento"
FROM car_his_base AS car 
LEFT JOIN  PREMER ON (car."IdInstrumento"=PREMER."IdInstrumento"  AND CAR."IdEmpresa"= PREMER."IdEmpresa")			
WHERE car."IdEmpresa" = :p_IdEmpresa
AND car."IdEstado" = CASE WHEN :p_IdEstado <> '' THEN :p_IdEstado ELSE 'VIG' END
AND car."IdCliente" <> :p_IdCliente
AND car."IdTipoInstrumento" = 'FWD'
AND car."IdInstrumento" = CASE WHEN :p_IdInstrumento > 0 THEN :p_IdInstrumento ELSE car."IdInstrumento" END
AND car."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE car."Nemotecnico" END
AND car."FolioCartera" = CASE WHEN :p_FolioCartera > 0 THEN :p_FolioCartera ELSE car."FolioCartera" END
AND ((:p_tipoFecha = 'LIQ' AND car."INGFechaLiquidacion" <= :p_FechaConsulta AND car."EGRFechaLiquidacion" >= :p_FechaConsulta) OR
	 (:p_tipoFecha <> 'LIQ' AND car."FechaVencimientoOperacion" > :p_FechaConsulta))
AND car."IdTipoProducto" = 'MEFW')
SELECT * FROM CARFWDHIS
WHERE ("IdEstado" = 'VIG' AND "RANKG" > 1) OR "RANKG" = 1;