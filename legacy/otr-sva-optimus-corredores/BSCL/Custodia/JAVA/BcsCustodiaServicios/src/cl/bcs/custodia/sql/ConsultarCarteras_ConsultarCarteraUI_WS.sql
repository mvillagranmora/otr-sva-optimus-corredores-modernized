WITH
RES_TOT AS (
    SELECT
        res."FolioCarteraOrigen",
        SUM(res."IngCantidad") AS "IngCantidad",
        SUM(res."IngValorRescate") AS "IngValorRescate",
        SUM(res."MontoValorizado") AS "IngMontoValorizado"
    FROM "BSCL.Custodia.BD::CUS.Reserva" AS res
    WHERE res."IdEstado" = 'VIG'
      AND res."EgrCantidad" = 0
    GROUP BY res."FolioCarteraOrigen"
),
instrumentos_premer AS (
    SELECT DISTINCT "IdInstrumento"
    FROM "BSCL.Custodia.BD::CUS.Cartera"  
    WHERE "IdInstrumento" IS NOT NULL
),cfg_fecha_sis AS (
    SELECT
        "IdEmpresa",
        TO_DATE("Valor") AS "FechaSistema"
    FROM "BSCL.Plataforma.BD::PLA.Configuracion"
    WHERE "CodigoParametro" = 'PLA.FechaSistema'
      AND "IdEmpresa" = :p_IdEmpresa
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
        INNER JOIN cfg_fecha_sis SIS
            ON prec."Fecha" <= SIS."FechaSistema"
        INNER JOIN instrumentos_premer IP
            ON IP."IdInstrumento" = prec."IdInstrumento"
        WHERE prec."IdBolsa" = 'BCS'
    ) PRE
    WHERE PRE."RN" = 1
      AND PRE."PrecioCierre" > 0
),

SUB_FIRST_NEMO AS (
    SELECT
        X."FolioCarteraRelacionada",
        X."Nemotecnico"
    FROM (
        SELECT
            sub."FolioCarteraRelacionada",
            sub."Nemotecnico",
            ROW_NUMBER() OVER (
                PARTITION BY sub."FolioCarteraRelacionada"
                ORDER BY (SELECT 1 FROM DUMMY)
            ) AS "RN"
        FROM "BSCL.Custodia.BD::CUS.Subyacente" sub
    ) X
    WHERE X."RN" = 1
),

SUB_TOP_FILTRADO AS (
    SELECT *
    FROM (
        SELECT
            sub.*,
            ROW_NUMBER() OVER (
                PARTITION BY sub."FolioCarteraRelacionada"
                ORDER BY sub."IdSubyacente" DESC
            ) AS "RN"
        FROM "BSCL.Custodia.BD::CUS.Subyacente" sub
        WHERE sub."IdEstado" = 'VIG'
          AND (
                IFNULL(:p_IdClasificacionTipoInstrumento, '') = ''
                OR sub."IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento
              )
          AND (
                IFNULL(:p_IdTipoInstrumento, '') = ''
                OR sub."IdTipoInstrumento" = :p_IdTipoInstrumento
              )
          AND (
                IFNULL(:p_IdInstrumento, 0) <= 0
                OR sub."IdInstrumento" = :p_IdInstrumento
              )
          AND (
                IFNULL(:p_Nemotecnico, '') = ''
                OR sub."Nemotecnico" = :p_Nemotecnico
              )
    ) X
    WHERE X."RN" = 1
),

CARTERA_NORMAL AS (
    SELECT
        RANK() OVER (
            PARTITION BY
                car."IdEmpresa",
                car."FolioCartera",
                CASE car."EGRFechaLiquidacion"
                    WHEN '0001-01-01' THEN '2500-12-31'
                    ELSE car."EGRFechaLiquidacion"
                END
            ORDER BY car."IdCartera" DESC
        ) AS "RANKG",

        car."IdCartera",
        car."FechaOperacion",
        car."IdEstado",
        car."IdClasificacionTipoInstrumento",
        car."IdTipoInstrumento",

        CASE
            WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
            ELSE car."Nemotecnico"
        END AS "Nemotecnico",

        CASE
            WHEN IFNULL(car."IdMonedaEmision", '') <> '' THEN car."IdMonedaEmision"
            WHEN IFNULL(car."IdTipoReajuste", '') <> '' THEN car."IdTipoReajuste"
            ELSE car."IdMonedaInstrumento"
        END AS "IdMonedaEmision",

        CASE
            WHEN COALESCE(resTot."IngCantidad", 0) > 0 THEN
                CASE
                    WHEN car."EsFungible" = 'S'
                     AND car."IdClasificacionTipoInstrumento" = 'IF' THEN
                        CASE
                            WHEN car."INGValorRescate" > resTot."IngValorRescate" THEN 1
                            ELSE 0
                        END
                    ELSE car."INGCantidad" - resTot."IngCantidad"
                END
            ELSE car."INGCantidad"
        END AS "INGCantidad",

        car."INGPrecioCompra",

        CASE
            WHEN car."IdClasificacionTipoInstrumento" IN ('RV', 'FM') THEN PRECIO_BCS."Precio"
            ELSE car."INGPrecioMercadoActual"
        END AS "INGPrecioMercadoActual",

        (
            (
                CASE
                    WHEN car."IdClasificacionTipoInstrumento" = 'RV' THEN
                        CASE
                            WHEN COALESCE(car."INGMontoATasaMercadoActual", 0.0) > 0
                                THEN car."INGMontoATasaMercadoActual"
                            ELSE COALESCE(PREMER."PrecioCierre", 0) * car."INGCantidad"
                        END
                    ELSE car."INGMontoATasaMercadoActual"
                END
                - car."INGMontoATasaCompraActual"
            )
            / CASE
                WHEN car."INGMontoATasaCompraActual" > 0 THEN car."INGMontoATasaCompraActual"
                ELSE 1
              END
        ) * 100 AS "Rentabilidad",

        CASE
            WHEN car."IdClasificacionTipoInstrumento" = 'RV' THEN
                COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"

            WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN
                CASE
                    WHEN car."INGValorRescate" > resTot."IngValorRescate" THEN
                        ((car."INGValorRescate" - resTot."IngValorRescate") * car."INGMontoMercadoActualMonedaInstrumento")
                        / car."INGValorRescate"
                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
                END

            ELSE
                CASE
                    WHEN resTot."IngCantidad" > 0
                     AND car."INGCantidad" > 0 THEN
                        ((car."INGCantidad" - resTot."IngCantidad") * car."INGMontoMercadoActualMonedaInstrumento")
                        / car."INGCantidad"
                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
                END
        END AS "INGMontoMercadoActualMonedaInstrumento"

    FROM "BSCL.Custodia.BD::CUS.Cartera" car

    LEFT JOIN RES_TOT resTot
        ON resTot."FolioCarteraOrigen" = car."FolioCartera"

    INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TipoInstrumento
        ON TipoInstrumento."IdTipoInstrumento" = car."IdTipoInstrumento"
       AND TipoInstrumento."Codigo" <> 'DER'

    LEFT JOIN PREMER
        ON PREMER."IdInstrumento" = car."IdInstrumento"
       AND PREMER."IdEmpresa" = car."IdEmpresa"

    LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" PRECIO_BCS
        ON PRECIO_BCS."IdEmpresa" = :p_IdEmpresa
       AND PRECIO_BCS."IdInstrumento" = car."IdInstrumento"
       AND PRECIO_BCS."FechaSistema" = :p_FechaConsulta

    WHERE car."IdEmpresa" = :p_IdEmpresa
      AND car."IdEstado" NOT IN ('ANU', 'RPA')
      AND (IFNULL(:p_IdEstado, '') = '' OR car."IdEstado" = :p_IdEstado)
      AND (:p_ExcluyeVigentes = 'N' OR car."IdEstado" <> 'VIG')
      AND car."IdTipoInstrumento" NOT IN ('SIM', 'PAC', 'PRT', 'PRF', 'EMP', 'ERF', 'RRV', 'RRF')
      AND (IFNULL(:p_IdCliente, 0) <= 0 OR car."IdCliente" = :p_IdCliente)
      AND (IFNULL(:p_IdPortafolio, 0) <= 0 OR car."IdPortafolio" = :p_IdPortafolio)
      AND (
            IFNULL(:p_IdClasificacionTipoInstrumento, '') = ''
            OR car."IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento
          )
      AND (
            IFNULL(:p_IdTipoInstrumento, '') = ''
            OR car."IdTipoInstrumento" = :p_IdTipoInstrumento
          )
      AND (
            IFNULL(:p_IdInstrumento, 0) <= 0
            OR car."IdInstrumento" = :p_IdInstrumento
          )
      AND (
            IFNULL(:p_Nemotecnico, '') = ''
            OR car."Nemotecnico" = :p_Nemotecnico
          )
      AND (
            IFNULL(:p_FolioCartera, 0) <= 0
            OR car."FolioCartera" = :p_FolioCartera
          )
      AND (
            (
                :p_tipoFecha = 'LIQ'
                AND car."INGFechaLiquidacion" <= :p_FechaConsulta
                AND (
                        car."EGRFechaLiquidacion" >= :p_FechaConsulta
                        OR car."EGRFechaLiquidacion" = '0001-01-01'
                    )
            )
            OR
            (
                :p_tipoFecha <> 'LIQ'
                AND car."FechaOperacion" <= :p_FechaConsulta
                AND car."EGRCantidad" = 0
            )
          )
),

CARTERA_PAC AS (
    SELECT
        RANK() OVER (
            PARTITION BY
                car."IdEmpresa",
                car."FolioCartera",
                CASE car."EGRFechaLiquidacion"
                    WHEN '0001-01-01' THEN '2500-12-31'
                    ELSE car."EGRFechaLiquidacion"
                END
            ORDER BY sub."IdSubyacente" DESC
        ) AS "RANKG",

        car."IdCartera",
        car."FechaOperacion",
        car."IdEstado",
        car."IdClasificacionTipoInstrumento",
        car."IdTipoInstrumento",

        subNemo."Nemotecnico" AS "Nemotecnico",

        CASE
            WHEN IFNULL(car."IdMonedaEmision", '') <> '' THEN car."IdMonedaEmision"
            WHEN IFNULL(car."IdTipoReajuste", '') <> '' THEN car."IdTipoReajuste"
            ELSE car."IdMonedaInstrumento"
        END AS "IdMonedaEmision",

        CASE
            WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN car."INGValorRescate"
            ELSE car."INGCantidad"
        END AS "INGCantidad",

        car."INGPrecioCompra",

        CASE
            WHEN car."IdClasificacionTipoInstrumento" IN ('RV', 'FM') THEN PRECIO_BCS."Precio"
            ELSE car."INGPrecioMercadoActual"
        END AS "INGPrecioMercadoActual",

        (
            (
                CASE
                    WHEN car."IdClasificacionTipoInstrumento" = 'RV' THEN
                        CASE
                            WHEN COALESCE(car."INGMontoATasaMercadoActual", 0.0) > 0
                                THEN car."INGMontoATasaMercadoActual"
                            ELSE COALESCE(PREMER."PrecioCierre", 0) * car."INGCantidad"
                        END
                    ELSE car."INGMontoATasaMercadoActual"
                END
                - car."INGMontoATasaCompraActual"
            )
            / CASE
                WHEN car."INGMontoATasaCompraActual" > 0 THEN car."INGMontoATasaCompraActual"
                ELSE 1
              END
        ) * 100 AS "Rentabilidad",

        CASE
            WHEN car."IdClasificacionTipoInstrumento" = 'RV' THEN
                COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"

            WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN
                CASE
                    WHEN car."INGValorRescate" > resTot."IngValorRescate" THEN
                        ((car."INGValorRescate" - resTot."IngValorRescate") * car."INGMontoMercadoActualMonedaInstrumento")
                        / car."INGValorRescate"
                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
                END

            ELSE
                CASE
                    WHEN resTot."IngCantidad" > 0
                     AND car."INGCantidad" > 0 THEN
                        ((car."INGCantidad" - resTot."IngCantidad") * car."INGMontoMercadoActualMonedaInstrumento")
                        / car."INGCantidad"
                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
                END
        END AS "INGMontoMercadoActualMonedaInstrumento"

    FROM "BSCL.Custodia.BD::CUS.Cartera" car

    INNER JOIN SUB_TOP_FILTRADO sub
        ON sub."FolioCarteraRelacionada" = car."FolioCartera"

    LEFT JOIN SUB_FIRST_NEMO subNemo
        ON subNemo."FolioCarteraRelacionada" = car."FolioCartera"

    LEFT JOIN RES_TOT resTot
        ON resTot."FolioCarteraOrigen" = car."FolioCartera"

    LEFT JOIN PREMER
        ON PREMER."IdInstrumento" = car."IdInstrumento"
       AND PREMER."IdEmpresa" = car."IdEmpresa"

    LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" PRECIO_BCS
        ON PRECIO_BCS."IdEmpresa" = :p_IdEmpresa
       AND PRECIO_BCS."IdInstrumento" = car."IdInstrumento"
       AND PRECIO_BCS."FechaSistema" = :p_FechaConsulta

    WHERE car."IdEmpresa" = :p_IdEmpresa
      AND car."IdEstado" NOT IN ('ANU', 'RPA', 'ANT')
      AND (IFNULL(:p_IdEstado, '') = '' OR car."IdEstado" = :p_IdEstado)
      AND (:p_ExcluyeVigentes = 'N' OR car."IdEstado" <> 'VIG')
      AND (IFNULL(:p_IdCliente, 0) <= 0 OR car."IdClienteRelacionado" = :p_IdCliente)
      AND (
            (
                IFNULL(:p_IdCliente, 0) > 0
                AND IFNULL(:p_IdPortafolio, 0) > 0
                AND car."IdPortafolioRelacionado" = :p_IdPortafolio
            )
            OR
            (
                IFNULL(:p_IdCliente, 0) > 0
                AND IFNULL(:p_IdPortafolio, 0) <= 0
                AND car."IdPortafolioRelacionado" = car."IdPortafolio"
            )
            OR
            (
                IFNULL(:p_IdCliente, 0) <= 0
                AND IFNULL(:p_IdPortafolio, 0) > 0
                AND car."IdPortafolio" = :p_IdPortafolio
            )
            OR
            (
                IFNULL(:p_IdCliente, 0) <= 0
                AND IFNULL(:p_IdPortafolio, 0) <= 0
            )
          )
      AND (
            (:p_tipoFecha = 'LIQ' AND car."INGFechaLiquidacion" <= :p_FechaConsulta)
            OR
            (:p_tipoFecha = 'ADQ' AND car."INGFechaOperacion" <= :p_FechaConsulta)
          )
      AND car."IdTipoInstrumento" = 'PAC'
      AND car."IdTipoOperacion" IN ('VENPAC', 'COMPAC')
),

CARTERA_NEGATIVA AS (
    SELECT
        1 AS "RANKG",

        car."IdCarteraNegativa" AS "IdCartera",
        car."FechaOperacion",
        car."IdEstado",
        TipoInstrumento."IdClasificacionTipoInstrumento",
        TipoInstrumento."IdTipoInstrumento",

        CASE
            WHEN TipoInstrumento."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
            ELSE car."Nemotecnico"
        END AS "Nemotecnico",

        CASE
            WHEN IFNULL(car."IdMonedaEmision", '') <> '' THEN car."IdMonedaEmision"
            WHEN IFNULL(car."IdTipoReajuste", '') <> '' THEN car."IdTipoReajuste"
            ELSE car."IdMonedaInstrumento"
        END AS "IdMonedaEmision",

        -1 * car."Cantidad" AS "INGCantidad",
        car."PrecioOperacion" AS "INGPrecioCompra",

        CASE
            WHEN ClasInst."IdClasificacionTipoInstrumento" = 'RV' THEN
                COALESCE(PRECIO_BCS."Precio", car."PrecioMercado", 0.0)
            ELSE car."PrecioMercado"
        END AS "INGPrecioMercadoActual",

        (
            (
                CASE
                    WHEN ClasInst."IdClasificacionTipoInstrumento" = 'RV' THEN
                        CASE
                            WHEN COALESCE(car."MontoMercadoMonedaInstrumento", 0.0) > 0
                                THEN car."MontoMercadoMonedaInstrumento"
                            ELSE COALESCE(PRECIO_BCS."Precio", 0) * car."Cantidad"
                        END
                    ELSE car."MontoMercadoMonedaInstrumento"
                END
                - car."MontoOperacionMonedaInstrumento"
            )
            / CASE
                WHEN car."MontoOperacionMonedaInstrumento" > 0 THEN car."MontoOperacionMonedaInstrumento"
                ELSE 1
              END
        ) * 100 AS "Rentabilidad",

        -1 * car."MontoMercadoMonedaInstrumento" AS "INGMontoMercadoActualMonedaInstrumento"

    FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" car

    INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento
        ON Instrumento."IdInstrumento" = car."IdInstrumento"

    INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TipoInstrumento
        ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"

    INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ClasInst
        ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento"

    LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" PRECIO_BCS
        ON PRECIO_BCS."IdEmpresa" = :p_IdEmpresa
       AND PRECIO_BCS."IdInstrumento" = car."IdInstrumento"
       AND PRECIO_BCS."FechaSistema" = :p_FechaConsulta

    WHERE car."IdEmpresa" = :p_IdEmpresa
      AND (:p_ExcluyeVigentes = 'N' OR car."IdEstado" <> 'VIG')
      AND (IFNULL(:p_IdEstado, '') = '' OR car."IdEstado" = :p_IdEstado)
      AND (IFNULL(:p_IdCliente, 0) <= 0 OR car."IdCliente" = :p_IdCliente)
      AND (IFNULL(:p_IdPortafolio, 0) <= 0 OR car."IdPortafolio" = :p_IdPortafolio)
      AND (IFNULL(:p_FolioCartera, 0) <= 0 OR car."FolioCustodia" = :p_FolioCartera)
      AND (IFNULL(:p_IdInstrumento, 0) <= 0 OR car."IdInstrumento" = :p_IdInstrumento)
      AND (IFNULL(:p_Nemotecnico, '') = '' OR car."Nemotecnico" = :p_Nemotecnico)
      AND (
            IFNULL(:p_IdTipoInstrumento, '') = ''
            OR TipoInstrumento."IdTipoInstrumento" = :p_IdTipoInstrumento
          )
      AND (
            IFNULL(:p_IdClasificacionTipoInstrumento, '') = ''
            OR ClasInst."IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento
          )
      AND (
            (:p_tipoFecha = 'LIQ' AND car."FechaLiquidacion" <= :p_FechaConsulta)
            OR
            (:p_tipoFecha = 'ADQ' AND car."FechaOperacion" <= :p_FechaConsulta)
          )
)

SELECT *
FROM CARTERA_NORMAL
WHERE "IdEstado" = 'VIG'
   OR "RANKG" = 1
UNION ALL

SELECT *
FROM CARTERA_PAC
WHERE "RANKG" = 1
UNION ALL

SELECT *
FROM CARTERA_NEGATIVA;