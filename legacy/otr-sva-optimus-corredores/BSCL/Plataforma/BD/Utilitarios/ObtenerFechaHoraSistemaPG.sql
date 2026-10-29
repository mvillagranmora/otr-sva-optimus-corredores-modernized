CREATE OR REPLACE PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (
  IN  "p_IdEmpresa"             BIGINT,
  OUT "p_out_FechaHoraSistema"  VARCHAR(40)   -- NVARCHAR(40)
)
LANGUAGE plpgsql
SECURITY INVOKER
AS $$
DECLARE
  "v_FechaSistema"          VARCHAR(10);
  "v_FechaSistemaUTC"       TIMESTAMP;  -- SECONDDATE ~ TIMESTAMP
  "v_FechaHoraSistemaUTC"   TIMESTAMP;
  "v_FechaHoraUTC"          TIMESTAMP;
  "v_Cantidad"              INTEGER;
  "v_SegundosZona"          INTEGER;
BEGIN
  "v_Cantidad" := 0;

  SELECT COUNT("CodigoParametro") INTO "v_Cantidad"
  FROM   "BSCL.Plataforma.BD::PLA.Configuracion"
  WHERE  "IdEmpresa" = "p_IdEmpresa"
     AND "CodigoParametro" = 'PLA.FechaSistema';

  IF ("v_Cantidad" < 1) THEN
    -- Emula SIGNAL FECHA_DE_SISTEMA_NO_CONFIGURADA (10003)
    RAISE EXCEPTION 'FECHA_DE_SISTEMA_NO_CONFIGURADA (SQL_ERROR_CODE 10003)';
  ELSE
    SELECT "Valor" INTO "v_FechaSistema"
    FROM   "BSCL.Plataforma.BD::PLA.Configuracion"
    WHERE  "IdEmpresa" = "p_IdEmpresa"
       AND "CodigoParametro" = 'PLA.FechaSistema';
  END IF;

  SELECT CAST("Valor" AS INTEGER) INTO "v_SegundosZona"
  FROM   "BSCL.Plataforma.BD::PLA.Configuracion"
  WHERE  "IdEmpresa" = "p_IdEmpresa"
     AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';

  -- CURRENT_UTCTIMESTAMP
  "v_FechaHoraUTC" := (CURRENT_TIMESTAMP AT TIME ZONE 'UTC');

  -- v_FechaSistemaUTC := ADD_SECONDS(TO_DATE(v_FechaSistema,'YYYY-MM-DD'), -1*v_SegundosZona);
  "v_FechaSistemaUTC" :=
      TO_DATE("v_FechaSistema",'YYYY-MM-DD')::timestamp
      + make_interval(secs => -1 * "v_SegundosZona");

  -- v_FechaHoraSistemaUTC := fecha_sistema + hora(UTC actual)
  "v_FechaHoraSistemaUTC" :=
      TO_DATE("v_FechaSistema",'YYYY-MM-DD')::timestamp
      + make_interval(
          hours => EXTRACT(HOUR   FROM "v_FechaHoraUTC")::int,
          mins  => EXTRACT(MINUTE FROM "v_FechaHoraUTC")::int,
          secs  => FLOOR(EXTRACT(SECOND FROM "v_FechaHoraUTC"))::int
        );

  -- p_out_FechaHoraSistema := v_FechaSistema || ' ' || TO_VARCHAR(v_FechaHoraSistemaUTC,'HH24:MI:SS');
  "p_out_FechaHoraSistema" :=
      "v_FechaSistema" || ' ' || to_char("v_FechaHoraSistemaUTC", 'HH24:MI:SS');
END;
$$;

--CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(1, NULL);