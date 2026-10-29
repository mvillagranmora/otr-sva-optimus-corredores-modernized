-- CentralizaAnticipoPactos_SelectMovimientos.sql (ported verbatim; HANA-only syntax)
SELECT
    TO_BIGINT(:IdTipoCentralizacion)                   AS "IdTipoCentralizacion",
    ConsultaSubyacente."IdSubyacente"                  AS "IdMovimiento",
    CASE ConsultarCartera."IdTipoOperacion"
        WHEN 'VENPAC'  THEN 'APACTOVTA'
        WHEN 'COMPAC'  THEN 'APACTOCOMP'
        ELSE '????????????'
    END                                                AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                       AS "IdEmpresa",
    ConsultarCartera."FolioCartera"                    AS "Numero",
    TO_BIGINT(0)                                       AS "Linea",
    YEAR(:FechaContable)||'-'||RIGHT('0'||MONTH(:FechaContable),2)||'-'||RIGHT('0'||DAYOFMONTH(:FechaContable),2) AS "Fecha",
    ConsultarCartera."DocumentoIdentidad"              AS "DocumentoIdentidad",
    ConsultarCartera."IdPortafolio"                    AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."EGRFechaOperacion"))   AS "Ejercicio",
    RIGHT('0'||MONTH(ConsultarCartera."EGRFechaOperacion"),2)||'-'||YEAR(ConsultarCartera."EGRFechaOperacion") AS "Periodo",
    ABS(ConsultaSubyacente."MontoSubyacente")          AS "Monto",
    ConsultarCartera."IdMonedaSistema"                 AS "IdMoneda",
    ConsultarCartera."IdEstado"                        AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion
    ON Operacion."FolioCartera" = ConsultarCartera."FolioCartera"
WHERE ConsultarCartera."IdEmpresa"        = :IdEmpresa
  AND ConsultarCartera."EGRFechaOperacion" = :FechaContable
  AND ConsultarCartera."IdTipoOperacion"  IN ('COMPAC','VENPAC')
  AND ConsultarCartera."IdEstado"         = 'ANT'
  AND ConsultaSubyacente."IdEstado"       = 'ANT'
  AND Operacion."IdEstado"                = 'ANT'
  AND ConsultarCartera."IdTipoInstrumento" = 'PAC'
UNION ALL
SELECT
    TO_BIGINT(:IdTipoCentralizacion)                   AS "IdTipoCentralizacion",
    ConsultarCartera."IdCartera"                       AS "IdMovimiento",
    CASE ConsultarCartera."IdTipoOperacion"
        WHEN 'VENPAC'  THEN 'AOPACTOVTA'
        WHEN 'COMPAC'  THEN 'AOPACTOCOM'
        ELSE '????????????'
    END                                                AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                       AS "IdEmpresa",
    ConsultarCartera."FolioCartera"                    AS "Numero",
    TO_BIGINT(0)                                       AS "Linea",
    YEAR(:FechaContable)||'-'||RIGHT('0'||MONTH(:FechaContable),2)||'-'||RIGHT('0'||DAYOFMONTH(:FechaContable),2) AS "Fecha",
    ConsultarCartera."DocumentoIdentidad"              AS "DocumentoIdentidad",
    ConsultarCartera."IdPortafolio"                    AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."EGRFechaOperacion"))   AS "Ejercicio",
    RIGHT('0'||MONTH(ConsultarCartera."EGRFechaOperacion"),2)||'-'||YEAR(ConsultarCartera."EGRFechaOperacion") AS "Periodo",
    ABS(Operacion."MontoOperacionAnticipo")            AS "Monto",
    ConsultarCartera."IdMonedaSistema"                 AS "IdMoneda",
    ConsultarCartera."IdEstado"                        AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion
    ON Operacion."FolioCartera" = ConsultarCartera."FolioCartera"
WHERE ConsultarCartera."IdEmpresa"        = :IdEmpresa
  AND ConsultarCartera."EGRFechaOperacion" = :FechaContable
  AND ConsultarCartera."IdTipoOperacion"  IN ('COMPAC','VENPAC')
  AND ConsultarCartera."IdEstado"         = 'ANT'
  AND Operacion."IdEstado"                = 'ANT'
