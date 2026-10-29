--
--  Proposito: SP para agregar log utilizando tablas de entrada físicas.
--  Autor: JG
--  Fecha Creacion: 08/03/2017
--

CREATE OR REPLACE PROCEDURE public."BSCL.Plataforma.BD.Servicios::AgregarLogMasivo4Java"(
    IN P_IDTRABAJO BIGINT
)
LANGUAGE plpgsql
SECURITY INVOKER
AS $$
DECLARE
    V_CONTDETALLE           BIGINT;
    V_FECHAREGISTRO         TIMESTAMP;  -- antes SECONDDATE
    V_TABLATEMPLOG          JSONB;
    V_TABLATEMPLOGDETALLE   JSONB;
    V_CODIGOAUTLOG          JSONB;
    V_CODIGOAUTDETALLE      JSONB;
BEGIN
    -- SECONDDATE = timestamp truncado a segundos
    V_FECHAREGISTRO := date_trunc('second', (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'));

    -- Construir tabla temporal de log con IDs de secuencia
    SELECT COALESCE(jsonb_agg(to_jsonb(T)), '[]'::jsonb)
      INTO V_TABLATEMPLOG
      FROM (
          SELECT nextval('"BSCL.Plataforma.BD.Secuencias::Log"') AS "IdLog",
                 "IdCabeceraLog", "IdTipoLog", "IdModulo", "IdUsuario", "IdEmpresa",
                 "FechaSistemaLog", "IdUsuarioAtendido", "IdCliente", "EstadoCliente",
                 "NombreCliente", "IdTipoDocumentoIdentidad", "IdDocumentoIdentidad",
                 "IdPortafolio", "EstadoPortafolio", "IdRegistro", "EstadoRegistro",
                 "IdTipoRegistro", "IdTipoOperacion", "Folio", "Cantidad", "PrecioTasa",
                 "Monto", "IdMonedaOrigen", "IdMonedaLiquidacion", "TipoCambio", "Nemo",
                 "IdProceso", "EstadoProceso", "NombreProceso", "IdPersona"
            FROM "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_Log"
           WHERE "IdTrabajo" = P_IDTRABAJO
      ) AS T;

    -- Construir tabla temporal de detalle de log
    SELECT COALESCE(jsonb_agg(to_jsonb(T)), '[]'::jsonb)
      INTO V_TABLATEMPLOGDETALLE
      FROM (
          SELECT nextval('"BSCL.Plataforma.BD.Secuencias::DetalleLog"') AS "IdDetalleLog",
                 L."IdLog",
                 LD."SubRegistro",
                 LD."CampoRegistro",
                 LD."ValorAntiguo",
                 LD."ValorNuevo"
            FROM "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_LogDetalle" LD
            INNER JOIN jsonb_to_recordset(V_TABLATEMPLOG)
                 AS L("IdLog" BIGINT, "IdCabeceraLog" BIGINT)
                 ON LD."IdCabeceraLog" = L."IdCabeceraLog"
                AND LD."IdTrabajo" = P_IDTRABAJO
      ) AS T;

    -- Generar clave de autenticación para log
    -- HASH_SHA256(TO_BINARY(CONCAT(...))) → encode(digest(... ::bytea, 'sha256'), 'hex')  [requiere pgcrypto]
    SELECT COALESCE(jsonb_agg(to_jsonb(T)), '[]'::jsonb)
      INTO V_CODIGOAUTLOG
      FROM (
          SELECT "IdCabeceraLog",
                 encode(digest((
                     CAST("IdLog" AS TEXT)
                     || CAST("IdTipoLog" AS TEXT)
                     || CAST("IdModulo" AS TEXT)
                     || CAST("IdUsuario" AS TEXT)
                     || CAST("IdEmpresa" AS TEXT)
                     || COALESCE(CAST("FechaSistemaLog" AS TEXT), '')
                     || COALESCE(SUBSTRING(CAST("FechaSistemaLog" AS TEXT), 1, 7), '')  -- HANA usaba índice 0 = 1 en PG
                     || COALESCE(CAST("IdUsuarioAtendido" AS TEXT), '0')
                     || COALESCE(CAST("IdCliente" AS TEXT), '0')
                     || COALESCE("EstadoCliente", '')
                     || COALESCE("NombreCliente", '')
                     || COALESCE("IdTipoDocumentoIdentidad", '')
                     || COALESCE("IdDocumentoIdentidad", '')
                     || COALESCE(CAST("IdPortafolio" AS TEXT), '0')
                     || COALESCE("EstadoPortafolio", '')
                     || COALESCE("IdRegistro", '')
                     || COALESCE("EstadoRegistro", '')
                     || COALESCE("IdTipoRegistro", '')
                     || COALESCE("IdTipoOperacion", '')
                     || COALESCE(CAST("Folio" AS TEXT), '0')
                     || COALESCE(CAST("Cantidad" AS TEXT), '0')
                     || COALESCE(CAST("PrecioTasa" AS TEXT), '0')
                     || COALESCE(CAST("Monto" AS TEXT), '0')
                     || COALESCE(CAST("IdProceso" AS TEXT), '0')
                     || COALESCE("EstadoProceso", '')
                     || COALESCE("NombreProceso", '')
                     || COALESCE("IdMonedaOrigen", '')
                     || COALESCE("IdMonedaLiquidacion", '')
                     || COALESCE(CAST("TipoCambio" AS TEXT), '')
                     || CAST(V_FECHAREGISTRO AS TEXT)
                     || COALESCE("Nemo", '')
                     || COALESCE(CAST("IdPersona" AS TEXT), '0')
                 )::bytea, 'sha256'), 'hex') AS "CodigoLog"
            FROM jsonb_to_recordset(V_TABLATEMPLOG) AS T(
                "IdCabeceraLog" BIGINT, "IdLog" BIGINT,
                "IdTipoLog" VARCHAR, "IdModulo" VARCHAR, "IdUsuario" BIGINT, "IdEmpresa" BIGINT,
                "FechaSistemaLog" VARCHAR, "IdUsuarioAtendido" BIGINT, "IdCliente" BIGINT,
                "EstadoCliente" VARCHAR, "NombreCliente" VARCHAR,
                "IdTipoDocumentoIdentidad" VARCHAR, "IdDocumentoIdentidad" VARCHAR,
                "IdPortafolio" BIGINT, "EstadoPortafolio" VARCHAR, "IdRegistro" VARCHAR,
                "EstadoRegistro" VARCHAR, "IdTipoRegistro" VARCHAR, "IdTipoOperacion" VARCHAR,
                "Folio" BIGINT, "Cantidad" DOUBLE PRECISION, "PrecioTasa" DOUBLE PRECISION,
                "Monto" DOUBLE PRECISION, "IdMonedaOrigen" VARCHAR, "IdMonedaLiquidacion" VARCHAR,
                "TipoCambio" DOUBLE PRECISION, "Nemo" VARCHAR, "IdProceso" BIGINT,
                "EstadoProceso" VARCHAR, "NombreProceso" VARCHAR, "IdPersona" BIGINT
            )
      ) AS T;

    SELECT COUNT(*) INTO V_CONTDETALLE
      FROM jsonb_to_recordset(V_TABLATEMPLOGDETALLE) AS T("IdDetalleLog" BIGINT);

    IF V_CONTDETALLE > 0 THEN
        -- Generar clave de autenticación para detalle de log
        SELECT COALESCE(jsonb_agg(to_jsonb(T)), '[]'::jsonb)
          INTO V_CODIGOAUTDETALLE
          FROM (
              SELECT "IdDetalleLog",
                     encode(digest((
                         CAST("IdDetalleLog" AS TEXT)
                         || CAST("IdLog" AS TEXT)
                         || COALESCE("SubRegistro", '')
                         || COALESCE("ValorAntiguo", '')
                         || COALESCE("ValorNuevo", '')
                         || COALESCE("CampoRegistro", '')
                     )::bytea, 'sha256'), 'hex') AS "CodigoDetalle"
                FROM jsonb_to_recordset(V_TABLATEMPLOGDETALLE) AS T(
                    "IdDetalleLog" BIGINT, "IdLog" BIGINT,
                    "SubRegistro" VARCHAR, "CampoRegistro" VARCHAR,
                    "ValorAntiguo" VARCHAR, "ValorNuevo" VARCHAR
                )
          ) AS T;
    END IF;

    INSERT INTO "BSCL.Plataforma.BD::PLA.Log" (
        "IdLog", "IdTipoLog", "IdModulo", "IdUsuario", "IdEmpresa",
        "FechaLog", "PeriodoLog",
        "IdUsuarioAtendido", "IdCliente", "EstadoCliente", "NombreCliente",
        "IdTipoDocumentoIdentidad", "IdDocumentoIdentidad",
        "IdPortafolio", "EstadoPortafolio", "IdRegistro", "EstadoRegistro",
        "IdTipoRegistro", "IdTipoOperacion", "Folio", "Cantidad", "PrecioTasa",
        "Monto", "IdMonedaOrigen", "IdMonedaLiquidacion", "TipoCambio", "Nemo",
        "IdProceso", "EstadoProceso", "NombreProceso",
        "FechaRegistro", "IdPersona", "CodigoAutenticacion"
    )
    SELECT t."IdLog", t."IdTipoLog", t."IdModulo", t."IdUsuario", t."IdEmpresa",
           t."FechaSistemaLog",
           SUBSTRING(CAST(t."FechaSistemaLog" AS TEXT), 1, 7),  -- PeriodoLog: HANA SUBSTRING(...,0,7) = PG SUBSTRING(...,1,7)
           t."IdUsuarioAtendido", t."IdCliente", t."EstadoCliente", t."NombreCliente",
           t."IdTipoDocumentoIdentidad", t."IdDocumentoIdentidad",
           t."IdPortafolio", t."EstadoPortafolio", t."IdRegistro", t."EstadoRegistro",
           t."IdTipoRegistro", t."IdTipoOperacion", t."Folio", t."Cantidad", t."PrecioTasa",
           t."Monto", t."IdMonedaOrigen", t."IdMonedaLiquidacion", t."TipoCambio", t."Nemo",
           t."IdProceso", t."EstadoProceso", t."NombreProceso",
           V_FECHAREGISTRO, t."IdPersona",
           c."CodigoLog"
      FROM jsonb_to_recordset(V_TABLATEMPLOG) AS t(
          "IdLog" BIGINT, "IdCabeceraLog" BIGINT,
          "IdTipoLog" VARCHAR, "IdModulo" VARCHAR, "IdUsuario" BIGINT, "IdEmpresa" BIGINT,
          "FechaSistemaLog" VARCHAR, "IdUsuarioAtendido" BIGINT, "IdCliente" BIGINT,
          "EstadoCliente" VARCHAR, "NombreCliente" VARCHAR,
          "IdTipoDocumentoIdentidad" VARCHAR, "IdDocumentoIdentidad" VARCHAR,
          "IdPortafolio" BIGINT, "EstadoPortafolio" VARCHAR, "IdRegistro" VARCHAR,
          "EstadoRegistro" VARCHAR, "IdTipoRegistro" VARCHAR, "IdTipoOperacion" VARCHAR,
          "Folio" BIGINT, "Cantidad" DOUBLE PRECISION, "PrecioTasa" DOUBLE PRECISION,
          "Monto" DOUBLE PRECISION, "IdMonedaOrigen" VARCHAR, "IdMonedaLiquidacion" VARCHAR,
          "TipoCambio" DOUBLE PRECISION, "Nemo" VARCHAR, "IdProceso" BIGINT,
          "EstadoProceso" VARCHAR, "NombreProceso" VARCHAR, "IdPersona" BIGINT
      )
      INNER JOIN jsonb_to_recordset(V_CODIGOAUTLOG)
           AS c("IdCabeceraLog" BIGINT, "CodigoLog" TEXT)
           ON c."IdCabeceraLog" = t."IdCabeceraLog";

    IF V_CONTDETALLE > 0 THEN
        INSERT INTO "BSCL.Plataforma.BD::PLA.DetalleLog" (
            "IdDetalleLog", "IdLog", "SubRegistro", "CampoRegistro",
            "ValorAntiguo", "ValorNuevo", "CodigoAutenticacion"
        )
        SELECT d."IdDetalleLog", d."IdLog", d."SubRegistro", d."CampoRegistro",
               d."ValorAntiguo", d."ValorNuevo",
               c."CodigoDetalle"
          FROM jsonb_to_recordset(V_TABLATEMPLOGDETALLE) AS d(
              "IdDetalleLog" BIGINT, "IdLog" BIGINT,
              "SubRegistro" VARCHAR, "CampoRegistro" VARCHAR,
              "ValorAntiguo" VARCHAR, "ValorNuevo" VARCHAR
          )
          INNER JOIN jsonb_to_recordset(V_CODIGOAUTDETALLE)
               AS c("IdDetalleLog" BIGINT, "CodigoDetalle" TEXT)
               ON c."IdDetalleLog" = d."IdDetalleLog";
    END IF;

END;
$$;

-- Ejemplo de llamada en una sola línea
-- CALL public."BSCL.Plataforma.BD.Servicios::AgregarLogMasivo4Java"(12345);
