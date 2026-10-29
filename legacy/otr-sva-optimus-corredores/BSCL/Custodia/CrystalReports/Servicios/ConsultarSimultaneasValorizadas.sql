SET SCHEMA "BSCL";

GO
    DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::ConsultarSimultaneasValorizadas";

GO
    CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::ConsultarSimultaneasValorizadas" (
        IN p_IdInforme NVARCHAR(90),
        IN p_IdEmpresa BIGINT,
        IN p_IdCliente BIGINT,
        IN p_IdSocioNegocio BIGINT
    ) LANGUAGE SQLSCRIPT SQL SECURITY INVOKER DEFAULT SCHEMA "BSCL" AS BEGIN DECLARE v_count INT;

DECLARE v_FechaSistema	NVARCHAR(10);
DECLARE v_FechaVal NVARCHAR(10);

CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);

SELECT
    "FechaValorizacionActual"
	INTO v_FechaVal
	FROM (
	    SELECT
	        "FechaValorizacionActual",
	        ROW_NUMBER() OVER (ORDER BY "FechaValorizacionActual" DESC) AS rn
	    FROM
	        "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF"
	    WHERE
	        "IdInforme" = :p_IdInforme
	) AS sub
WHERE rn = 1;

v_Datos =  WITH simultaneas_valorizadas AS (
        SELECT
            CARV."IdValorizacion",
            COR."NombreCompleto",
            COALESCE(DI."DocumentoIdentidad",'') AS "DocumentoIdentidad",
            CASE WHEN IFNULL(PER."Nombres",'') = '' THEN PER."RazonSocial" 
                ELSE CONCAT(CONCAT(CONCAT(PER."Nombres",' '),CONCAT(PER."ApellidoPaterno",' ') )  ,PER."ApellidoMaterno") 
            END  AS "NombreCliente",
            POR."CodigoPortafolio",
            TIPOCONTRARIO."Accion" || ' A PLAZO' AS "IdTipoOperacion",
            CARV."Nemotecnico",
            CARV."NemoSVS",
            CARV."IdTipoInstrumento",
            CARV."IdMonedaInstrumento",
            CARV."Emisor",
            TO_NVARCHAR(CARV."FechaOperacion", 'DD-MM-YYYY') as "FechaOperacion",
            TO_NVARCHAR(CARV."FechaLiquidacion", 'DD-MM-YYYY') as "FechaLiquidacion",
            TO_NVARCHAR(CARV."FechaVencimientoOperacion", 'DD-MM-YYYY') as "FechaVencimientoOperacion",
            CASE
                WHEN CARV."FechaLiquidacion" IS NULL
                OR CARV."FechaLiquidacion" = '0001-01-01'
                OR CARV."FechaVencimientoOperacion" IS NULL
                OR CARV."FechaVencimientoOperacion" = '0001-01-01' 
                OR CARV."FechaValorizacionProximo" = '0001-01-01' 
                OR CARV."FechaValorizacionProximo" IS NULL THEN '-'
                ELSE TO_VARCHAR(
                    GREATEST(DAYS_BETWEEN(
                            CARV."FechaLiquidacion",
                            CARV."FechaValorizacionProximo"
                        ),1)
                ) || '/' || TO_VARCHAR(
                    DAYS_BETWEEN(
                        CARV."FechaLiquidacion",
                        CARV."FechaVencimientoOperacion"
                    )
                )
            END AS "NumeroDias",
                CASE 
                WHEN TIPOCONTRARIO."Accion" = 'VENTA' THEN 
                    'V '
                WHEN TIPOCONTRARIO."Accion" = 'COMPRA' THEN 
                    'C '
                ELSE '-'
            END AS "Codigo",
            CARV."TasaCompra",
            CARV."Cantidad",
            CARV."PrecioCompra",
            CAST((CARV."MontoActual" / CARV."Cantidad") AS DECIMAL(26,8)) AS "PrecioActual",
            CARV."PrecioProximo",
            CARV."MontoCompra",
            CARV."MontoInteres",
            CARV."MontoInteresCorrido",
            CARV."MontoActual",
            CARV."MontoProximo",
            CARV."FolioComprobante",
            CARV."FolioCartera",
            CARV."IdInforme",
            CASE
                WHEN CARV."FechaLiquidacion" IS NULL
                OR CARV."FechaLiquidacion" = '0001-01-01'
                OR CARV."FechaVencimientoOperacion" IS NULL
                OR CARV."FechaVencimientoOperacion" = '0001-01-01' THEN -1
                ELSE DAYS_BETWEEN(
                        CARV."FechaValorizacionProximo",
                        CARV."FechaVencimientoOperacion"
                    )
            END AS "CantidadDias",
            COALESCE(DET."FlagDeterioro",'N') AS "FlagDeterioro",
            COALESCE(DET."PorcentajeDeterioro",0 ) AS "PorcentajeDeterioro",
            CASE DET."FlagDeterioro" WHEN 'S' 
                THEN CAST((COALESCE(DET."PorcentajeDeterioro" ,0)/100) * COALESCE(CARV."Cantidad",0) AS DECIMAL(26,8))
                ELSE 0 
            END AS "Deterioro",
            COALESCE(DET."Fase",0) AS "Fase",
            CARV."IdAgente",
            CARV."IdBolsa",
            TIPO."Nombre" AS "NombreTipoOperacion",
            REPLACE(COALESCE(CARV."FolioTransaccionNormal", '0'), ' ', '') AS "PaloNormal",
            REPLACE(COALESCE(CARV."FolioTransaccionPlazo", '0'), ' ', '') AS "PaloPlazo",
            CARV."PrecioAPlazo",
            CARV."MontoAPlazo",
            CONCAT(CONCAT(CONCAT(PEREMP."Nombres",' '),CONCAT(PEREMP."ApellidoPaterno",' ') )  ,PEREMP."ApellidoMaterno") AS "NombreAgente"
        FROM
            "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CARV
            LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIPO ON TIPO."IdTipoOperacion" = CARV."IdTipoOperacion"
            LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOCONTRARIO ON TIPOCONTRARIO."IdTipoOperacion" = TIPO."IdTipoOperacionRelacionada"
            LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CARV."IdPortafolio"
            --INFORMACION CLIENTE
            LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CARV."IdCliente"
            LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona" 
            LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = PER."IdPersona" AND DI."Predeterminado" = 'S'
            --INFORMACION AGENTE
            LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
            LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PEREMP ON PEREMP."IdPersona" = EMP."IdPersona"
            --INFORMACION CONTRAPARTE
            LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" COR ON COR."IdCorredora" = CARV."IdCorredoraContraparte"
            LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CON ON CON."IdCondicionDeLiquidacion" = CARV."IdCondicionLiquidacion"
            LEFT JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DET ON DET."Emisor" = CARV."Emisor" AND DET."IdEstado" = 'ACT'
        WHERE
            CARV."IdInforme" = :p_IdInforme
    )
    SELECT
        "IdValorizacion",
        COALESCE("NombreCompleto", '') AS "NombreCompleto",
        "NombreCliente",
        "NombreAgente",
        "CodigoPortafolio",
        "IdTipoOperacion",
        "NombreTipoOperacion",
        "Nemotecnico",
        "NemoSVS",
        "IdTipoInstrumento",
        "IdMonedaInstrumento",
        "FechaOperacion",
        "FechaLiquidacion",
        "FechaVencimientoOperacion",
        "Codigo",
        "TasaCompra",
        "Cantidad",
        "PrecioCompra",
        "PrecioActual",
        "PrecioProximo",
        "MontoCompra",
        "MontoInteres",
        "MontoInteresCorrido",
        "MontoActual",
        "MontoProximo",
        "FolioComprobante",
        "FolioCartera",
        "IdInforme",
        CASE
            WHEN "CantidadDias" > 7 THEN 'B'
            WHEN "CantidadDias" <= 7
            AND "CantidadDias" >= 0 THEN 'A'
            ELSE 'C'
        END AS "Grupo",
        "NumeroDias",
        "FlagDeterioro",
        "PorcentajeDeterioro",
        "Deterioro",
        "Fase",
        "IdAgente",
        "IdBolsa",
        "PaloNormal",
        "PaloPlazo",
        "PrecioAPlazo",
        "MontoAPlazo"
    FROM
        simultaneas_valorizadas;

    SELECT
        COUNT(*) INTO v_count
    FROM
        :v_Datos;

    IF (v_count > 0) THEN
        SELECT
            *
        FROM
            :v_Datos;

    ELSE 
        SELECT
            0 AS "IdValorizacion",
            '' AS "NombreCompleto",
            '' AS "NombreCliente",
            '' AS "NombreAgente",
            0 AS "CodigoPortafolio",
            '' AS "IdTipoOperacion",
            '' AS "NombreTipoOperacion",
            '' AS "Nemotecnico",
            '' AS "NemoSVS",
            '' AS "IdTipoInstrumento",
            '' AS "IdMonedaInstrumento",
            '0001-01-01' AS "FechaOperacion", 
            '0001-01-01' AS "FechaLiquidacion",
            '0001-01-01' AS "FechaVencimientoOperacion",
            '' AS "Codigo",
            0.0 AS "TasaCompra",
            0 AS "Cantidad",
            0.0 AS "PrecioCompra",
            0.0 AS "PrecioActual",
            0 AS "PrecioProximo",
            0 AS "MontoCompra",
            0 AS "MontoProximo",
            0.0 AS "MontoInteres",
            0.0 AS "MontoInteresCorrido",
            0.0 AS "MontoActual",
            0 AS "FolioComprobante",
            0 AS "FolioCartera",
            '' AS "IdInforme",
            '' AS "Grupo",
            '/' AS "NumeroDias",
            '' AS "FlagDeterioro",
            0 AS "PorcentajeDeterioro",
            0 AS "Deterioro",
            0 AS "Fase",
            '' AS "IdAgente",
            '' AS "IdBolsa",
            0 AS "PaloNormal",
            0 AS "PaloPlazo",
            0.0 AS "PrecioAPlazo",
            0.0 AS "MontoAPlazo"
        FROM
            DUMMY;
    END IF;

END;