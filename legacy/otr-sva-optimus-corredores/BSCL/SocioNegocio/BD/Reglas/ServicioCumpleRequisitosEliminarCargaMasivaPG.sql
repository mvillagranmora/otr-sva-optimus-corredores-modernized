--
--  Autor: GSALINI
--  Fecha Creacion: 09/04/2023
--

CREATE OR REPLACE PROCEDURE public."BSCL.SocioNegocio.BD.Reglas::ServicioCumpleRequisitosEliminarCargaMasiva"(
    IN  P_CARGAMASIVAELIMINAR     JSONB,  -- Estructura original: TABLE ("IdCargaMasiva" BIGINT)
    OUT P_OUT_REGISTROSRECH       JSONB,  -- Estructura original: TABLE ("IdCargaMasiva" BIGINT)
    OUT P_OUT_ERRORESDETALLE      JSONB,  -- Estructura original: TABLE ("Codigo" nvarchar(100), "Descripcion" nvarchar(520))
    OUT P_OUT_ADVERTENCIASDETALLE JSONB,  -- Estructura original: TABLE ("Codigo" nvarchar(100), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(520))
    OUT P_OUT_RESULTADOREGLA      VARCHAR(40)  -- antes NVARCHAR(40) — OK | ADVE | RECH
)
LANGUAGE plpgsql
SECURITY INVOKER
AS $$
DECLARE
    V_ERROROUTMSG        VARCHAR(520);
    V_ERRORNODATA        VARCHAR(100);
    V_ERROR_GENERAL      BIGINT;
    V_ERROR_NEGOCIO_ADVE BIGINT;
    V_TABLAREQUISITOS    JSONB;
    V_TABLACODIGOSMENSAJES JSONB;
BEGIN
    P_OUT_RESULTADOREGLA      := 'OK';
    P_OUT_ERRORESDETALLE      := '[]'::jsonb;
    P_OUT_ADVERTENCIASDETALLE := '[]'::jsonb;
    P_OUT_REGISTROSRECH       := '[]'::jsonb;
    V_TABLAREQUISITOS         := '[]'::jsonb;
    V_TABLACODIGOSMENSAJES    := '[]'::jsonb;

    -- Valida que la carga masiva exista y que no esté procesada
    SELECT COALESCE(jsonb_agg(to_jsonb(T)), '[]'::jsonb)
      INTO V_TABLAREQUISITOS
      FROM (
          SELECT 2 AS "Prioridad",
                 CASE WHEN COALESCE(CME."IdCargaMasiva", 0) = 0 THEN 0
                      WHEN CM."IdEstado" = 'PRO'               THEN 0
                      ELSE 1
                 END AS "Cumple",
                 CASE WHEN COALESCE(CME."IdCargaMasiva", 0) = 0 THEN 'CARGA_MASIVA_NO_EXISTE'
                      WHEN CM."IdEstado" = 'PRO'               THEN 'CARGA_MASIVA_PROCESADA'
                      ELSE ''
                 END AS "CodigoError",
                 'IdCargaMasiva' AS "CodigoDato",
                 CAST(CME."IdCargaMasiva" AS VARCHAR(50)) AS "ValorDato",
                 CME."IdCargaMasiva"
            FROM jsonb_to_recordset(P_CARGAMASIVAELIMINAR) AS CME("IdCargaMasiva" BIGINT)
            LEFT JOIN "BSCL.SocioNegocio.BD::CAR.CargaMasiva" AS CM
                   ON CM."IdCargaMasiva" = CME."IdCargaMasiva"
      ) AS T;

    SELECT COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END),
           COUNT(CASE WHEN "Prioridad" = 2 THEN 1 END)
      INTO V_ERROR_GENERAL, V_ERROR_NEGOCIO_ADVE
      FROM jsonb_to_recordset(V_TABLAREQUISITOS) AS T("Prioridad" INT, "Cumple" BIGINT)
     WHERE "Cumple" = 0;

    P_OUT_RESULTADOREGLA := CASE WHEN V_ERROR_GENERAL > 0       THEN 'RECH'
                                 WHEN V_ERROR_NEGOCIO_ADVE > 0  THEN 'ADVE'
                                 ELSE 'OK'
                            END;

    -- Registros rechazados por validación
    SELECT COALESCE(jsonb_agg(to_jsonb(T)), '[]'::jsonb)
      INTO P_OUT_REGISTROSRECH
      FROM (
          SELECT "IdCargaMasiva"
            FROM jsonb_to_recordset(V_TABLAREQUISITOS)
                 AS T("Prioridad" INT, "Cumple" BIGINT, "IdCargaMasiva" BIGINT)
           WHERE "Prioridad" > 0 AND "Cumple" = 0
      ) AS T;

    -- Construir tabla de códigos para ConsultarMensajesDiccionario
    SELECT COALESCE(jsonb_agg(to_jsonb(T)), '[]'::jsonb)
      INTO V_TABLACODIGOSMENSAJES
      FROM (
          SELECT "CodigoError" AS "CodigoMensaje",
                 "CodigoDato",
                 "ValorDato"
            FROM jsonb_to_recordset(V_TABLAREQUISITOS)
                 AS T("CodigoError" VARCHAR(40), "CodigoDato" VARCHAR(100),
                      "ValorDato" VARCHAR(520), "Cumple" BIGINT, "Prioridad" INT)
           WHERE "Cumple" = 0
           ORDER BY "Prioridad" DESC
      ) AS T;

    CALL public."BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario"(V_TABLACODIGOSMENSAJES, P_OUT_ERRORESDETALLE);

    -- Advertencias (prioridad > 1)
    SELECT COALESCE(jsonb_agg(to_jsonb(T)), '[]'::jsonb)
      INTO P_OUT_ADVERTENCIASDETALLE
      FROM (
          SELECT ED."Codigo",
                 '' AS "RequiereAutorizacion",
                 CASE WHEN TR."Prioridad" > 1
                      THEN ED."Descripcion" || ' (' || TR."IdCargaMasiva"::TEXT || ')'
                      ELSE ED."Descripcion"
                 END AS "Descripcion"
            FROM jsonb_to_recordset(P_OUT_ERRORESDETALLE)
                 AS ED("Codigo" VARCHAR(100), "Descripcion" VARCHAR(520))
            INNER JOIN jsonb_to_recordset(V_TABLAREQUISITOS)
                 AS TR("CodigoError" VARCHAR(40), "Cumple" BIGINT, "Prioridad" INT, "IdCargaMasiva" BIGINT)
                 ON TR."CodigoError" = ED."Codigo"
           WHERE TR."Cumple" = 0 AND TR."Prioridad" > 1
      ) AS T;

    -- Errores (prioridad = 1)
    SELECT COALESCE(jsonb_agg(to_jsonb(T)), '[]'::jsonb)
      INTO P_OUT_ERRORESDETALLE
      FROM (
          SELECT ED."Codigo",
                 CASE WHEN TR."Prioridad" > 1
                      THEN ED."Descripcion" || ' (' || TR."IdCargaMasiva"::TEXT || ')'
                      ELSE ED."Descripcion"
                 END AS "Descripcion"
            FROM jsonb_to_recordset(P_OUT_ERRORESDETALLE)
                 AS ED("Codigo" VARCHAR(100), "Descripcion" VARCHAR(520))
            INNER JOIN jsonb_to_recordset(V_TABLAREQUISITOS)
                 AS TR("CodigoError" VARCHAR(40), "Cumple" BIGINT, "Prioridad" INT, "IdCargaMasiva" BIGINT)
                 ON TR."CodigoError" = ED."Codigo"
           WHERE TR."Cumple" = 0 AND TR."Prioridad" = 1
      ) AS T;

EXCEPTION
    -- Emula EXIT HANDLER FOR SQLEXCEPTION (incluye SALIDA_CONTROLADA y NO_DATA_FOUND)
    WHEN OTHERS THEN
        P_OUT_ERRORESDETALLE := COALESCE(P_OUT_ERRORESDETALLE, '[]'::jsonb)
            || jsonb_build_array(jsonb_build_object('Codigo', 'ERROR_INTERNO', 'Descripcion', SQLERRM));
        P_OUT_RESULTADOREGLA := 'RECH';
        RETURN;
END;
$$;

-- Ejemplo de llamada en una sola línea (parámetros OUT con NULL)
-- CALL public."BSCL.SocioNegocio.BD.Reglas::ServicioCumpleRequisitosEliminarCargaMasiva"('[{"IdCargaMasiva":1}]'::jsonb, NULL, NULL, NULL, NULL);
