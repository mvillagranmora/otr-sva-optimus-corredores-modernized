-- 
--  Proposito: Procesa la llamada que hace el servlet call.  Devolviendo informacion necesaria para procesar por el call
--  Autor: CSTANGE - BCS
--  Fecha Creacion: 13/12/2016
--  Modificaciones: 
--  22-02-2017 Se agrega informacion estadistica mensual
--

CREATE OR REPLACE PROCEDURE public."BSCL.Plataforma.BD.Servicios::ACC.ProcesaLlamadaServletCall" (
    P_TOKEN                 VARCHAR(520),
    P_IDSERVICIO            INTEGER,
    P_IDTRANSACCION         VARCHAR(40),
    P_IDCANAL               VARCHAR(10),

    -- Salida Info sesion
    OUT P_OUT_IDEMPRESA     BIGINT,
    OUT P_OUT_IDUSUARIO     BIGINT,
    OUT P_OUT_NOMBREUSUARIO VARCHAR(80),
    OUT P_OUT_TIPOUSUARIO   VARCHAR(80),

    -- Salida Info Servicio
    OUT P_OUT_NOMBRESERVICIO   VARCHAR(100),
    OUT P_OUT_TIPOSERVICIO     VARCHAR(1),
    OUT P_OUT_SERVICIOTEMPIN   VARCHAR(5000),
    OUT P_OUT_SERVICIOTEMPOUT  VARCHAR(5000),

    -- Retorno
    OUT P_OUT_PERMITEUSO    VARCHAR(1),
    OUT P_OUT_MENSAJE       VARCHAR(80),
    OUT P_OUT_EXTRAS        VARCHAR(5000)
)
LANGUAGE plpgsql
AS $$
DECLARE
    -- Declaraciones Variables
    V_EXISTE           INTEGER;
    V_IDESTADO         VARCHAR(3);
    V_FECHAVIGENCIA    DATE;
    V_REGISTRAUSO      VARCHAR(1);
    P_SYSDATE          VARCHAR(10);

BEGIN
    ----------------------------------------------------------------
    -- Valida que el servicio exista
    ----------------------------------------------------------------
    SELECT COUNT(1) INTO V_EXISTE
      FROM "BSCL.Plataforma.BD::PLA.Servicio"
     WHERE "IdServicio" = P_IDSERVICIO;

    IF V_EXISTE = 0 THEN
        P_OUT_PERMITEUSO := 'N';
        P_OUT_MENSAJE    := 'SERVICIO INVALIDO';
        RETURN;  -- (SALIDA_CONTROLADA)
    END IF;

    ----------------------------------------------------------------
    -- Información del servicio (incluye extras por canal)
    ----------------------------------------------------------------
    SELECT srv."RutaServicio",
           srv."TipoServicio",
           srv."TablasTempIN",
           srv."TablasTempOUT",
           srv."RegistraUso",
           COALESCE(string_agg(srvextdet."RutaServicio", ',' ORDER BY srvextdet."RutaServicio"), '') AS "Extras"
      INTO P_OUT_NOMBRESERVICIO,
           P_OUT_TIPOSERVICIO,
           P_OUT_SERVICIOTEMPIN,
           P_OUT_SERVICIOTEMPOUT,
           V_REGISTRAUSO,
           P_OUT_EXTRAS
      FROM "BSCL.Plataforma.BD::PLA.Servicio" srv
 LEFT JOIN "BSCL.Plataforma.BD::PLA.ServicioExtra" srvext
        ON srv."IdServicio" = srvext."IdServicio"
       AND srvext."IdCanal" = P_IDCANAL
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Servicio" srvextdet
        ON srvext."IdServicioExtra" = srvextdet."IdServicio"
     WHERE srv."IdServicio" = P_IDSERVICIO
  GROUP BY srv."RutaServicio", srv."TipoServicio", srv."TablasTempIN", srv."TablasTempOUT", srv."RegistraUso";

    ----------------------------------------------------------------
    -- Valida token
    ----------------------------------------------------------------
    SELECT COUNT(1) INTO V_EXISTE
      FROM "BSCL.Plataforma.BD::ACC.Sesion"
     WHERE "IdTokenSesion" = P_TOKEN
       AND "TokenValido"   = 'S';

    IF V_EXISTE = 0 THEN
        P_OUT_PERMITEUSO := 'N';
        P_OUT_MENSAJE    := 'TOKEN INVALIDO';
        RETURN;  -- (SALIDA_CONTROLADA)
    END IF;

    ----------------------------------------------------------------
    -- Obtiene IdUsuario
    ----------------------------------------------------------------
    SELECT "IdUsuario"
      INTO P_OUT_IDUSUARIO
      FROM "BSCL.Plataforma.BD::ACC.Sesion"
     WHERE "IdTokenSesion" = P_TOKEN
       AND "TokenValido"   = 'S';

    ----------------------------------------------------------------
    -- Obtiene datos de usuario
    ----------------------------------------------------------------
    SELECT "IdEmpresa",
           "FechaVigencia",
           "IdEstado",
           "CodigoUsuario",
           "TipoEntidad"
      INTO P_OUT_IDEMPRESA,
           V_FECHAVIGENCIA,
           V_IDESTADO,
           P_OUT_NOMBREUSUARIO,
           P_OUT_TIPOUSUARIO
      FROM "BSCL.Plataforma.BD::ACC.Usuario"
     WHERE "IdUsuario" = P_OUT_IDUSUARIO;

    ----------------------------------------------------------------
    -- Fecha del sistema (mantiene llamada)
    ----------------------------------------------------------------
    CALL public."BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(P_OUT_IDEMPRESA, P_SYSDATE);

    -- Valida vigencia/estado
    IF V_FECHAVIGENCIA <> DATE '0001-01-01' THEN
        IF V_FECHAVIGENCIA < TO_DATE(P_SYSDATE, 'YYYY-MM-DD') THEN
            P_OUT_PERMITEUSO := 'N';
            P_OUT_MENSAJE    := 'CUENTA BLOQUEADA O INACTIVA';
            RETURN;  -- (SALIDA_CONTROLADA)
        END IF;
    END IF;

    IF V_IDESTADO = 'BLO' THEN
        P_OUT_PERMITEUSO := 'N';
        P_OUT_MENSAJE    := 'CUENTA BLOQUEADA O INACTIVA';
        RETURN;  -- (SALIDA_CONTROLADA)
    END IF;

    ----------------------------------------------------------------
    -- Auditoría (no existe AUTONOMOUS TRANSACTION en PG; inserción normal)
    ----------------------------------------------------------------
    INSERT INTO "BSCL.Plataforma.BD::PLA.AuditoriaServicios"
        ("FechaRegistro","IdServicio","IdUsuario","RutaServicio","CodigoEvento","Adicional1","Adicional2","Adicional3","Adicional4","IdTransaccion")
    SELECT CURRENT_TIMESTAMP, P_IDSERVICIO, P_OUT_IDUSUARIO, P_OUT_NOMBRESERVICIO, 'INICIO',
           NULL, NULL, NULL, NULL, P_IDTRANSACCION
    WHERE P_IDSERVICIO NOT IN (600017, 14, 1381);

    ----------------------------------------------------------------
    -- Salida OK
    ----------------------------------------------------------------
    P_OUT_PERMITEUSO := 'S';
    P_OUT_MENSAJE    := 'OK++';
END;
$$;

-- Ejemplo de llamada en una sola línea (parámetros OUT con NULL)
-- CALL public."BSCL.Plataforma.BD.Servicios::ACC.ProcesaLlamadaServletCall"('token-xyz', 123, 'trx-001', 'COR', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);