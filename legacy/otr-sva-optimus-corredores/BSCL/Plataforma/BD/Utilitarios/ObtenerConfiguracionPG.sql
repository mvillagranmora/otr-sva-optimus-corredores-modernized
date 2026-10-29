CREATE OR REPLACE PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (
  IN  "p_IdEmpresa"        BIGINT,
  IN  "p_CodigoParametro"  VARCHAR(40),    -- NVARCHAR(40)
  OUT "p_out_Valor"        VARCHAR(520)    -- NVARCHAR(520)
)
LANGUAGE plpgsql
SECURITY INVOKER
AS $$
DECLARE
  "v_cantidad" INT := 0;
BEGIN
  SELECT COUNT("CodigoParametro")
  INTO   "v_cantidad"
  FROM   "BSCL.Plataforma.BD::PLA.Configuracion"
  WHERE  "IdEmpresa" = "p_IdEmpresa"
     AND "CodigoParametro" = "p_CodigoParametro";

  IF ("v_cantidad" = 0) THEN
    -- Emula: SIGNAL PARAMETRO_DE_CONFIGURACION_NO_EXISTE (10007)
    RAISE EXCEPTION 'PARAMETRO_DE_CONFIGURACION_NO_EXISTE (SQL_ERROR_CODE 10007)';
  END IF;

  SELECT "Valor"
  INTO   "p_out_Valor"
  FROM   "BSCL.Plataforma.BD::PLA.Configuracion"
  WHERE  "IdEmpresa" = "p_IdEmpresa"
     AND "CodigoParametro" = "p_CodigoParametro";
END;
$$;