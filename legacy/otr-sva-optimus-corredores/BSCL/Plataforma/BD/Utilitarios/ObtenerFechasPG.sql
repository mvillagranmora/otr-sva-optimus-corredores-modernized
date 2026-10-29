-- 
--  Proposito: Obtener las diferentes fechas desde la base datos
--  Autor:  CST - BCS
--  Fecha Creacion: 16/09/2015
--  

CREATE OR REPLACE PROCEDURE public."BSCL.Plataforma.BD.Utilitarios::ObtenerFechas" (
    IN  P_IDEMPRESA                   BIGINT,
    --
    OUT P_OUT_FECHASISTEMAUTC        VARCHAR(40), 
    OUT P_OUT_FECHASISTEMALOCAL      VARCHAR(40), 
    OUT P_OUT_FECHAHORASISTEMAUTC    VARCHAR(40), 
    OUT P_OUT_FECHAHORASISTEMALOCAL  VARCHAR(40), 
    OUT P_OUT_FECHAHORAUTC           VARCHAR(40), 
    OUT P_OUT_FECHAHORALOCAL         VARCHAR(40)
)
LANGUAGE plpgsql
AS $$
DECLARE 
    V_FECHASISTEMA            VARCHAR(10);
    V_FECHASISTEMAUTC         TIMESTAMP;   -- antes: SECONDDATE
    V_FECHAHORASISTEMAUTC     TIMESTAMP;   -- antes: SECONDDATE
    V_FECHAHORASISTEMALOC     TIMESTAMP;   -- antes: SECONDDATE
    V_FECHAHORAUTC            TIMESTAMP;   -- antes: SECONDDATE
    V_FECHATEMP               TIMESTAMP;   -- no se usa luego, se conserva por trazabilidad
    V_CANTIDAD                INTEGER;
    V_SEGUNDOSZONA            INTEGER;
BEGIN 
    V_CANTIDAD := 0;

    SELECT COUNT("CodigoParametro") INTO V_CANTIDAD
      FROM "BSCL.Plataforma.BD::PLA.Configuracion"
     WHERE "IdEmpresa" = P_IDEMPRESA
       AND "CodigoParametro" = 'PLA.FechaSistema';

    IF (V_CANTIDAD < 1) THEN
        -- FECHA_DE_SISTEMA_NO_CONFIGURADA (HANA: SQL_ERROR_CODE 10003)
        RAISE EXCEPTION 'FECHA_DE_SISTEMA_NO_CONFIGURADA';
    END IF;

    SELECT "Valor" INTO V_FECHASISTEMA
      FROM "BSCL.Plataforma.BD::PLA.Configuracion"
     WHERE "IdEmpresa" = P_IDEMPRESA
       AND "CodigoParametro" = 'PLA.FechaSistema';

    SELECT CAST("Valor" AS INTEGER) INTO V_SEGUNDOSZONA
      FROM "BSCL.Plataforma.BD::PLA.Configuracion"
     WHERE "IdEmpresa" = P_IDEMPRESA
       AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';
         
    V_FECHAHORAUTC := (CURRENT_TIMESTAMP AT TIME ZONE 'UTC');

    -- SISTEMA FECHA
    V_FECHASISTEMAUTC := TO_DATE(V_FECHASISTEMA, 'YYYY-MM-DD') + make_interval(secs => -1 * V_SEGUNDOSZONA);
    P_OUT_FECHASISTEMAUTC   := to_char(V_FECHASISTEMAUTC, 'YYYY-MM-DD HH24:MI:SS');
    P_OUT_FECHASISTEMALOCAL := to_char(V_FECHASISTEMAUTC + make_interval(secs => V_SEGUNDOSZONA), 'YYYY-MM-DD  HH24:MI:SS');  -- (doble espacio preservado)

    -- SISTEMA FECHA + HORA  ( Si Sistema excede dia de BD, se repite el dia)
    V_FECHAHORASISTEMAUTC :=
        TO_DATE(V_FECHASISTEMA, 'YYYY-MM-DD')
        + make_interval(
            hours => EXTRACT(HOUR   FROM V_FECHAHORAUTC)::int,
            mins  => EXTRACT(MINUTE FROM V_FECHAHORAUTC)::int,
            secs  => EXTRACT(SECOND FROM V_FECHAHORAUTC)::int
          );

    P_OUT_FECHAHORASISTEMAUTC := V_FECHASISTEMA || ' ' || to_char(V_FECHAHORASISTEMAUTC, 'HH24:MI:SS');

    V_FECHAHORASISTEMALOC := V_FECHAHORASISTEMAUTC + make_interval(secs => V_SEGUNDOSZONA);
    P_OUT_FECHAHORASISTEMALOCAL := V_FECHASISTEMA || ' ' || to_char(V_FECHAHORASISTEMALOC, 'HH24:MI:SS');

    -- BASE DATOS FECHA + HORA
    P_OUT_FECHAHORAUTC  := to_char(V_FECHAHORAUTC, 'YYYY-MM-DD HH24:MI:SS');
    P_OUT_FECHAHORALOCAL := to_char(V_FECHAHORAUTC + make_interval(secs => V_SEGUNDOSZONA), 'YYYY-MM-DD HH24:MI:SS');        
END;
$$;

-- Ejemplo de llamada en una sola línea (parámetros OUT con NULL)
-- CALL public."BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(1, NULL, NULL, NULL, NULL, NULL, NULL);