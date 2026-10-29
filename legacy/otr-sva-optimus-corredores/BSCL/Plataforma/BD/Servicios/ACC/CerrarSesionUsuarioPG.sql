-- 
--  Proposito: Cierra una sesion de usuario 
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 15/02/2016
--  

CREATE OR REPLACE PROCEDURE public."BSCL.Plataforma.BD.Servicios::ACC.CerrarSesionUsuario" (
    -- Parametros entrada comunes en todos los servicios publicos
    IN  P_IDEMPRESA                 BIGINT,       -- ID Empresa destino
    IN  P_IDUSUARIO                 BIGINT,       -- ID del usuario que solicita el servicio
    IN  P_IDMODULO                  VARCHAR(3),   -- ID del Modulo que lo invoca

    IN  P_TOKEN                     VARCHAR(520),

    -- Salida Correcta
    OUT P_OUT_ESTADO                VARCHAR(5),

    -- Salidas para mensajes de advertencias y errores
    OUT P_OUT_ADVERTENCIASCONTADOR  INTEGER,
    OUT P_OUT_ADVERTENCIASDETALLE   JSONB,  -- Estructura original: table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520))
    OUT P_OUT_ERRORESCONTADOR       INTEGER,
    OUT P_OUT_ERRORESDETALLE        JSONB   -- Estructura original: table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
)
LANGUAGE plpgsql
AS $$
DECLARE
    ----------------------------
    -- Declaraciones Variables
    ----------------------------
    V_EXISTE                       INTEGER;
    P_DETALLELOG                   JSONB;  -- tabla local convertida a JSONB: ["SubRegistro","CampoRegistro","ValorAntiguo","ValorNuevo"]
    P_SYSDATE                      DATE;
    P_TIMESTAMPVALIDACIONUSUARIO   TIMESTAMP := (CURRENT_TIMESTAMP AT TIME ZONE 'UTC');
    V_ERROROUTMSG                  VARCHAR(520);
    V_HASH                         JSONB;
BEGIN
    -- Inicialización de salidas (equivalente a selects sobre DUMMY)
    P_OUT_ESTADO                 := 'nok++';
    P_OUT_ADVERTENCIASCONTADOR   := 0;
    P_OUT_ERRORESCONTADOR        := 0;
    P_OUT_ADVERTENCIASDETALLE    := '[]'::jsonb;
    P_OUT_ERRORESDETALLE         := '[]'::jsonb;

    -----------------------------------------
    -- Valida que el token exista y sea válido
    -----------------------------------------
    SELECT COUNT(1) INTO V_EXISTE
      FROM "BSCL.Plataforma.BD::ACC.Sesion"
     WHERE "IdTokenSesion" = P_TOKEN
       AND "TokenValido"   = 'S';

    IF V_EXISTE = 0 THEN
        V_HASH := jsonb_build_array(jsonb_build_object('Codigo','TOKEN','Valor',P_TOKEN));
        CALL public."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario"('USUARIO_TOKEN_INVALIDO', V_HASH, V_ERROROUTMSG);

        P_OUT_ERRORESDETALLE := P_OUT_ERRORESDETALLE
                                || jsonb_build_array(jsonb_build_object('Codigo','USUARIO_TOKEN_INVALIDO','Descripcion',V_ERROROUTMSG));
        P_OUT_ERRORESCONTADOR := jsonb_array_length(P_OUT_ERRORESDETALLE);
        RETURN; -- (SALIDA_CONTROLADA)
    END IF;

    ----------------------------------------------------------
    -- Sesión más reciente del usuario (para armar detalle log)
    ----------------------------------------------------------
    WITH ses AS (
        SELECT s.*
          FROM "BSCL.Plataforma.BD::ACC.Sesion" s
         WHERE s."IdUsuario" = P_IDUSUARIO
         ORDER BY s."FechaUltimoUso" DESC
         LIMIT 1
    )
    SELECT COALESCE(
             jsonb_agg(x ORDER BY ord),
             '[]'::jsonb
           )
      INTO P_DETALLELOG
      FROM (
            SELECT 1 AS ord, jsonb_build_object(
                'SubRegistro','SESION',
                'CampoRegistro','FechaSalida',
                'ValorAntiguo', COALESCE(to_char(s."FechaSalida",'YYYY-MM-DD HH24:MI:SS'),''),
                'ValorNuevo',  to_char(P_TIMESTAMPVALIDACIONUSUARIO,'YYYY-MM-DD HH24:MI:SS')
            ) AS x
              FROM ses s
            UNION ALL
            SELECT 2, jsonb_build_object(
                'SubRegistro','SESION',
                'CampoRegistro','FechaUltimoUso',
                'ValorAntiguo', COALESCE(to_char(s."FechaUltimoUso",'YYYY-MM-DD HH24:MI:SS'),''),
                'ValorNuevo',  to_char(P_TIMESTAMPVALIDACIONUSUARIO,'YYYY-MM-DD HH24:MI:SS')
            )
              FROM ses s
            UNION ALL
            SELECT 3, jsonb_build_object(
                'SubRegistro','SESION',
                'CampoRegistro','TokenValido',
                'ValorAntiguo', COALESCE(s."TokenValido",''),
                'ValorNuevo',  'N'
            )
              FROM ses s
            UNION ALL
            SELECT 4, jsonb_build_object(
                'SubRegistro','SESION',
                'CampoRegistro','ACCION',
                'ValorAntiguo', '',
                'ValorNuevo',  'TERMINO SESION'
            )
              FROM ses s
          ) t;

    ------------------------------
    -- Actualiza datos de conexión
    ------------------------------
    UPDATE "BSCL.Plataforma.BD::ACC.Usuario"
       SET "Conectado" = 'N'
     WHERE "IdEmpresa" = P_IDEMPRESA
       AND "IdUsuario" = P_IDUSUARIO;

    UPDATE "BSCL.Plataforma.BD::ACC.Sesion"
       SET "FechaSalida" = P_TIMESTAMPVALIDACIONUSUARIO,
           "TokenValido" = 'N',
           "UsoToken"     = "UsoToken" + 1
     WHERE "IdTokenSesion" = P_TOKEN;

    -- Salida OK
    P_OUT_ESTADO := 'ok+++';

    -----------------------------
    -- Obtener fecha del sistema
    -----------------------------
    P_SYSDATE := current_date;

    -----------------------------------------
    -- Ejecutar el programa de logeo (AgregarLog)
    -- Nota: en PostgreSQL CALL es posicional; se envían todos los IN.
    -- Firma migrada previamente (31 parámetros, todos IN):
    --   1 P_IDTIPOLOG, 2 P_IDMODULO, 3 P_IDUSUARIO, 4 P_IDEMPRESA, 5 P_FECHASISTEMALOG,
    --   6 P_IDUSUARIOATENDIDO, 7 P_IDPERSONA, 8 P_IDCLIENTE, 9 P_ESTADOCLIENTE, 10 P_NOMBRECLIENTE,
    --  11 P_IDTIPODOCUMENTOIDENTIDAD, 12 P_IDDOCUMENTOIDENTIDAD, 13 P_IDPORTAFOLIO, 14 P_ESTADOPORTAFOLIO,
    --  15 P_IDTIPOREGISTRO, 16 P_IDREGISTRO, 17 P_ESTADOREGISTRO, 18 P_IDTIPOOPERACION,
    --  19 P_FOLIO, 20 P_CANTIDAD, 21 P_PRECIOTASA, 22 P_MONTO,
    --  23 P_IDMONEDAORIGEN, 24 P_IDMONEDALIQUIDACION, 25 P_TIPOCAMBIO, 26 P_NEMO,
    --  27 P_IDPROCESO, 28 P_ESTADOPROCESO, 29 P_NOMBREPROCESO, 30 P_IDTIPOPROGRAMA,
    --  31 P_DETALLELOG (JSONB)
    -----------------------------------------
    CALL public."BSCL.Plataforma.BD.Servicios::AgregarLog"(
        'INFORMATIVO',                 --  1 P_IDTIPOLOG
        'PLA',                         --  2 P_IDMODULO
        P_IDUSUARIO,                   --  3 P_IDUSUARIO
        P_IDEMPRESA,                   --  4 P_IDEMPRESA
        to_char(P_SYSDATE,'YYYY-MM-DD'), -- 5 P_FECHASISTEMALOG
        P_IDUSUARIO,                   --  6 P_IDUSUARIOATENDIDO
        0,                             --  7 P_IDPERSONA
        0,                             --  8 P_IDCLIENTE
        '',                            --  9 P_ESTADOCLIENTE
        '',                            -- 10 P_NOMBRECLIENTE
        '',                            -- 11 P_IDTIPODOCUMENTOIDENTIDAD
        '',                            -- 12 P_IDDOCUMENTOIDENTIDAD
        0,                             -- 13 P_IDPORTAFOLIO
        '',                            -- 14 P_ESTADOPORTAFOLIO
        'SESION',                      -- 15 P_IDTIPOREGISTRO
        '',                            -- 16 P_IDREGISTRO
        '',                            -- 17 P_ESTADOREGISTRO
        '',                            -- 18 P_IDTIPOOPERACION
        0,                             -- 19 P_FOLIO
        0,                             -- 20 P_CANTIDAD
        0,                             -- 21 P_PRECIOTASA
        0,                             -- 22 P_MONTO
        '',                            -- 23 P_IDMONEDAORIGEN
        '',                            -- 24 P_IDMONEDALIQUIDACION
        '',                            -- 25 P_TIPOCAMBIO
        '',                            -- 26 P_NEMO
        0,                             -- 27 P_IDPROCESO
        '',                            -- 28 P_ESTADOPROCESO
        '',                            -- 29 P_NOMBREPROCESO
        '',                            -- 30 P_IDTIPOPROGRAMA
        COALESCE(P_DETALLELOG, '[]'::jsonb) -- 31 P_DETALLELOG
    );

EXCEPTION
    WHEN OTHERS THEN
        P_OUT_ERRORESDETALLE := COALESCE(P_OUT_ERRORESDETALLE,'[]'::jsonb)
                                || jsonb_build_array(
                                     jsonb_build_object('Codigo','ERROR_INTERNO','Descripcion', SQLERRM)
                                   );
        P_OUT_ERRORESCONTADOR := jsonb_array_length(P_OUT_ERRORESDETALLE);
        RETURN;
END;
$$;

-- Ejemplo de llamada en una sola línea (DBeaver: incluir NULL para cada OUT)
-- CALL public."BSCL.Plataforma.BD.Servicios::ACC.CerrarSesionUsuario"(1, 100, 'PLA', 'TOKEN-XYZ'::varchar,
--   NULL, NULL, NULL, NULL);