
-- 
--  Proposito: Valida una sesion de usuario 
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  

CREATE OR REPLACE PROCEDURE public."BSCL.Plataforma.BD.Servicios::ACC.ValidaSesionUsuario" (
    P_TOKEN                           VARCHAR(520),

    -- Salida Correcta
    OUT P_OUT_IDEMPRESA               BIGINT,
    OUT P_OUT_IDUSUARIO               BIGINT,
    OUT P_OUT_NOMBREEMPRESA           VARCHAR(520),
    OUT P_OUT_NOMBREUSUARIO           VARCHAR(520),
    OUT P_OUT_CODIGOMENUSUARIO        VARCHAR(100),
    OUT P_OUT_DEBECAMBIARCLAVE        VARCHAR(1),
    OUT P_OUT_IDSOCIODENEGOCIO        BIGINT,
    OUT P_OUT_IDPERSONACORREDORA      BIGINT,
    OUT P_OUT_IDMONEDASISTEMA         VARCHAR(3),
    OUT P_OUT_IDPAISSISTEMA           BIGINT,
    OUT P_OUT_FECHASISTEMA            VARCHAR(10),
    OUT P_OUT_BOLSADEFECTOSISTEMA     VARCHAR(3),
    OUT P_OUT_VALORESDEFECTOSISTEMA   JSONB, -- Estructura original: TABLE ("IdValorDefectoSistema" NVARCHAR(40),"Nombre" NVARCHAR(255),"Valor" NVARCHAR(255),"IdEmpresa" NVARCHAR(40),"IdEstado" NVARCHAR(3))
    OUT P_OUT_IDEMPLEADO              BIGINT,
    OUT P_OUT_IDCLIENTE               BIGINT,
    OUT P_OUT_IDPERSONA               BIGINT,

    -- Salidas para mensajes de advertencias y errores
    OUT P_OUT_ADVERTENCIASCONTADOR    INTEGER,
    OUT P_OUT_ADVERTENCIASDETALLE     JSONB, -- Estructura original: table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520))
    OUT P_OUT_ERRORESCONTADOR         INTEGER,
    OUT P_OUT_ERRORESDETALLE          JSONB  -- Estructura original: table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
)
LANGUAGE plpgsql
AS $$
DECLARE
    ----------------------------
    -- Declaraciones Variables
    ----------------------------
    V_EXISTE                INTEGER;
    V_IDEMPRESA             BIGINT;
    V_IDUSUARIO             BIGINT;
    V_IDCANAL               VARCHAR(3);
    V_FECHAVIGENCIA         DATE;
    V_IDESTADO              VARCHAR(3);
    V_ZONAHORARIA           VARCHAR(520);
    V_NOMBREUSUARIO         VARCHAR(520);
    V_NOMBREEMPRESA         VARCHAR(520);
    V_CODIGOMENUSUARIO      VARCHAR(100);
    V_DEBECAMBIARCLAVE      VARCHAR(1);
    V_IDSOCIODENEGOCIO      BIGINT;
    V_IDPERSONACORREDORA    BIGINT;
    P_SYSDATE               VARCHAR(10);
    V_IDENTIDAD             BIGINT;

    -- Hash para mensajes (tabla → JSONB)
    V_HASH                  JSONB;  -- Estructura: [{"Codigo":..., "Valor":...}]
    V_ERROROUTMSG           VARCHAR(520);
BEGIN
    ----------------------------
    -- Inicialización de salidas
    ----------------------------
    P_OUT_ADVERTENCIASCONTADOR  := 0;
    P_OUT_ERRORESCONTADOR       := 0;
    P_OUT_ADVERTENCIASDETALLE   := '[]'::jsonb;
    P_OUT_ERRORESDETALLE        := '[]'::jsonb;
    P_OUT_VALORESDEFECTOSISTEMA := '[]'::jsonb;

    -----------------------------------------------------
    -- Valida que el token exista y sea válido
    -----------------------------------------------------
    SELECT COUNT(1) INTO V_EXISTE
      FROM "BSCL.Plataforma.BD::ACC.Sesion"
     WHERE "IdTokenSesion" = P_TOKEN
       AND "TokenValido"   = 'S';

    IF V_EXISTE = 0 THEN
        V_HASH := jsonb_build_array(jsonb_build_object('Codigo','TOKEN','Valor',P_TOKEN));
        CALL public."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario"('USUARIO_TOKEN_INVALIDO', V_HASH, V_ERROROUTMSG);

        P_OUT_ERRORESDETALLE := P_OUT_ERRORESDETALLE || jsonb_build_array(
            jsonb_build_object('Codigo','USUARIO_TOKEN_INVALIDO','Descripcion',V_ERROROUTMSG)
        );
        P_OUT_ERRORESCONTADOR := jsonb_array_length(P_OUT_ERRORESDETALLE);
        RETURN; -- (SALIDA_CONTROLADA)
    END IF;

    -----------------------------------------------------
    -- Obtiene IdUsuario y canal
    -----------------------------------------------------
    SELECT "IdUsuario","IdCanal"
      INTO V_IDUSUARIO, V_IDCANAL
      FROM "BSCL.Plataforma.BD::ACC.Sesion"
     WHERE "IdTokenSesion" = P_TOKEN
       AND "TokenValido"   = 'S';

    -----------------------------------------------------
    -- Obtiene datos de usuario
    -- (AQUI)
    -----------------------------------------------------
    SELECT "IdEmpresa","FechaVigencia","IdEstado","CodigoUsuario","IdEntidad","CodigoMenu","DebeCambiarClave"
      INTO V_IDEMPRESA, V_FECHAVIGENCIA, V_IDESTADO, V_NOMBREUSUARIO, V_IDENTIDAD, V_CODIGOMENUSUARIO, V_DEBECAMBIARCLAVE
      FROM "BSCL.Plataforma.BD::ACC.Usuario"
     WHERE "IdUsuario" = V_IDUSUARIO;

    -----------------------------------------------------
    -- Obtiene datos empresa
    -----------------------------------------------------
    SELECT emp."Nombre", emp."IdSocioDeNegocio", per."IdPersona"
      INTO V_NOMBREEMPRESA, V_IDSOCIODENEGOCIO, V_IDPERSONACORREDORA
      FROM "BSCL.Plataforma.BD::PLA.Empresa" emp
 LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc ON emp."IdSocioDeNegocio" = soc."IdSocioDeNegocio"
 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"        per ON per."IdPersona"        = soc."IdPersona"
     WHERE emp."IdEmpresa" = V_IDEMPRESA;

    -----------------------------------------------------
    -- Parámetros de sistema
    -----------------------------------------------------
    CALL public."BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(V_IDEMPRESA, 'PLA.ZonaHoraria', V_ZONAHORARIA);

    -- Moneda sistema
    CALL public."BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(V_IDEMPRESA, 'PLA.MonedaSistema', P_OUT_IDMONEDASISTEMA);

    -- País sistema
    SELECT "IdPais" INTO P_OUT_IDPAISSISTEMA
      FROM "BSCL.Plataforma.BD::PLA.Empresa"
     WHERE "IdEmpresa" = V_IDEMPRESA;

    -- Fecha sistema
    CALL public."BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(V_IDEMPRESA, 'PLA.FechaSistema', P_OUT_FECHASISTEMA);

    -- Bolsa por defecto del sistema
    CALL public."BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(V_IDEMPRESA, 'PLA.BolsaDefectoSistema', P_OUT_BOLSADEFECTOSISTEMA);

    -- Valores por defecto del sistema (tabla → JSONB)
    SELECT COALESCE(jsonb_agg(to_jsonb(T)),'[]'::jsonb)
      INTO P_OUT_VALORESDEFECTOSISTEMA
      FROM (
            SELECT
                "IdValorDefectoSistema",
                "Nombre",
                "Valor",
                "IdEmpresa",
                "IdEstado"
              FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarValorDefectoSistema"
             WHERE "IdEmpresa" = V_IDEMPRESA
               AND "IdEstado"  = 'ACT'
           ) AS T;

    -----------------------------------------------------
    -- Validaciones de estado de usuario
    -----------------------------------------------------
    IF V_IDESTADO = 'BLO' THEN
        V_HASH := jsonb_build_array(jsonb_build_object('Codigo','NombreUsuario','Valor',V_NOMBREUSUARIO));
        CALL public."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario"('USUARIO_BLOQUEADO', V_HASH, V_ERROROUTMSG);

        P_OUT_ERRORESDETALLE := P_OUT_ERRORESDETALLE || jsonb_build_array(
            jsonb_build_object('Codigo','USUARIO_BLOQUEADO','Descripcion',V_ERROROUTMSG)
        );
        P_OUT_ERRORESCONTADOR := jsonb_array_length(P_OUT_ERRORESDETALLE);
        RETURN; -- (SALIDA_CONTROLADA)
    END IF;

    -- Fecha sistema (para comparar vigencia)
    CALL public."BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(V_IDEMPRESA, P_SYSDATE);

    IF V_FECHAVIGENCIA <> DATE '0001-01-01' THEN
        IF V_FECHAVIGENCIA < TO_DATE(P_SYSDATE,'YYYY-MM-DD') THEN
            V_HASH := jsonb_build_array(jsonb_build_object('Codigo','NombreUsuario','Valor',V_NOMBREUSUARIO));
            CALL public."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario"('USUARIO_VIGENCIA_VENCIDA', V_HASH, V_ERROROUTMSG);

            P_OUT_ERRORESDETALLE := P_OUT_ERRORESDETALLE || jsonb_build_array(
                jsonb_build_object('Codigo','USUARIO_VIGENCIA_VENCIDA','Descripcion',V_ERROROUTMSG)
            );
            P_OUT_ERRORESCONTADOR := jsonb_array_length(P_OUT_ERRORESDETALLE);
            RETURN; -- (SALIDA_CONTROLADA)
        END IF;
    END IF;

    -----------------------------------------------------
    -- Validar que el usuario sea Empleado o Cliente
    -----------------------------------------------------
    IF V_IDCANAL = 'CLI' THEN
        SELECT
            COALESCE(CE."IdEmpleado",0) AS "IdEmpleado",
            COALESCE(CC."IdCliente", 0) AS "IdCliente",
            COALESCE(PP."IdPersona", 0) AS "IdPersona"
          INTO P_OUT_IDEMPLEADO, P_OUT_IDCLIENTE, P_OUT_IDPERSONA
          FROM "BSCL.Plataforma.BD::ACC.Usuario" U
     LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
            ON CE."IdEmpleado" = U."IdEntidad" AND CE."IdEstado" = 'ACT' AND U."TipoEntidad" = 'EMP'
     LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"  CC
            ON CC."IdCliente"  = U."IdEntidad" AND CC."IdEstado" IN ('ACT','BLO') AND U."TipoEntidad" = 'CLI'
     LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"  PP
            ON PP."IdPersona"  = U."IdEntidad" AND PP."IdEstado" = 'ACT' AND U."TipoEntidad" = 'PER'
         WHERE U."IdEntidad" = V_IDENTIDAD
           AND U."IdUsuario" = V_IDUSUARIO;
    ELSE
        SELECT
            COALESCE(CE."IdEmpleado",0) AS "IdEmpleado",
            0                           AS "IdCliente",
            COALESCE(PP."IdPersona", 0) AS "IdPersona"
          INTO P_OUT_IDEMPLEADO, P_OUT_IDCLIENTE, P_OUT_IDPERSONA
          FROM "BSCL.Plataforma.BD::ACC.Usuario" U
     LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
            ON CE."IdEmpleado" = U."IdEntidad" AND CE."IdEstado" = 'ACT' AND U."TipoEntidad" = 'EMP'
     LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"  PP
            ON PP."IdPersona"  = U."IdEntidad" AND PP."IdEstado" = 'ACT' AND U."TipoEntidad" = 'PER'
         WHERE U."IdEntidad" = V_IDENTIDAD
           AND U."IdUsuario" = V_IDUSUARIO;
    END IF;

    IF P_OUT_IDEMPLEADO = 0 AND P_OUT_IDCLIENTE = 0 AND P_OUT_IDPERSONA = 0 THEN
        V_HASH := jsonb_build_array(jsonb_build_object('Codigo','NombreUsuario','Valor',V_NOMBREUSUARIO));
        CALL public."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario"('USUARIO_NO_VALIDO', V_HASH, V_ERROROUTMSG);

        P_OUT_ERRORESDETALLE := P_OUT_ERRORESDETALLE || jsonb_build_array(
            jsonb_build_object('Codigo','USUARIO_NO_VALIDO','Descripcion',V_ERROROUTMSG)
        );
        P_OUT_ERRORESCONTADOR := jsonb_array_length(P_OUT_ERRORESDETALLE);
        RETURN; -- (SALIDA_CONTROLADA)
    END IF;

    -----------------------------------------------------
    -- “Transacción autónoma” en HANA → operaciones normales en PG
    -- Actualiza datos de conexión y sesión
    -----------------------------------------------------
    UPDATE "BSCL.Plataforma.BD::ACC.Usuario"
       SET "FechaUltimaConexion" = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
           "Conectado"           = 'S',
           "Hora"                = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
     WHERE "IdEmpresa" = V_IDEMPRESA
       AND "IdUsuario" = V_IDUSUARIO;

    UPDATE "BSCL.Plataforma.BD::ACC.Sesion"
       SET "FechaUltimoUso" = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
           "UsoToken"       = "UsoToken" + 1
     WHERE "IdTokenSesion" = P_TOKEN;

    -----------------------------------------------------
    -- Asignación de salidas OK
    -----------------------------------------------------
    P_OUT_IDEMPRESA            := V_IDEMPRESA;
    P_OUT_NOMBREEMPRESA        := V_NOMBREEMPRESA;
    P_OUT_IDUSUARIO            := V_IDUSUARIO;
    P_OUT_NOMBREUSUARIO        := V_NOMBREUSUARIO;
    P_OUT_CODIGOMENUSUARIO     := V_CODIGOMENUSUARIO;
    P_OUT_DEBECAMBIARCLAVE     := V_DEBECAMBIARCLAVE;
    P_OUT_IDSOCIODENEGOCIO     := V_IDSOCIODENEGOCIO;
    P_OUT_IDPERSONACORREDORA   := V_IDPERSONACORREDORA;

EXCEPTION
    WHEN OTHERS THEN
        -- Emula EXIT HANDLER FOR SQLEXCEPTION genérico
        P_OUT_ERRORESDETALLE := COALESCE(P_OUT_ERRORESDETALLE,'[]'::jsonb)
                                || jsonb_build_array(
                                     jsonb_build_object('Codigo','ERROR_INTERNO','Descripcion', SQLERRM)
                                   );
        P_OUT_ERRORESCONTADOR := jsonb_array_length(P_OUT_ERRORESDETALLE);
        RETURN;
END;
$$;

-- Ejemplo de llamada en una sola línea (parámetros OUT con NULL)
-- CALL public."BSCL.Plataforma.BD.Servicios::ACC.ValidaSesionUsuario"('TOKEN-ABC', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);