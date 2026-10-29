-- 
--  Proposito: Obtener el dia en que esta posicionado el sistema
--  Autor: NRM - BCS
--  Fecha Creacion: 01/07/2015
--  

CREATE OR REPLACE PROCEDURE public."BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" ( 
    IN  P_IDEMPRESA        BIGINT,
    OUT P_OUT_FECHASISTEMA VARCHAR(10)   -- antes NVARCHAR(10)
)
LANGUAGE plpgsql
AS $$
DECLARE
    V_CANTIDAD INTEGER;
BEGIN
    V_CANTIDAD := 0;

    SELECT COUNT("CodigoParametro")
      INTO V_CANTIDAD
      FROM "BSCL.Plataforma.BD::PLA.Configuracion"
     WHERE "IdEmpresa" = P_IDEMPRESA
       AND "CodigoParametro" = 'PLA.FechaSistema';

    IF (V_CANTIDAD < 1) THEN
        -- FECHA_SISTEMA_NO_ENCONTRADA (HANA: SQL_ERROR_CODE 10003)
        RAISE EXCEPTION 'FECHA_SISTEMA_NO_ENCONTRADA';
    ELSE
        SELECT "Valor"
          INTO P_OUT_FECHASISTEMA
          FROM "BSCL.Plataforma.BD::PLA.Configuracion"
         WHERE "IdEmpresa" = P_IDEMPRESA
           AND "CodigoParametro" = 'PLA.FechaSistema';
    END IF;
END;
$$;

-- Ejemplo de llamada en una sola línea (parámetro OUT con NULL)
-- CALL public."BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(1, NULL);