-- 
--  Proposito: Busca formatos para instrumentos y montos, cantidades, etc.
--  Autor:  - BCS
--  Fecha Creacion: 04/11/2015
--  

CREATE OR REPLACE PROCEDURE public."BSCL.Plataforma.BD.Servicios::ConsultarFormatos" (
    OUT P_OUT_MONEDAFORMATO JSONB  -- Estructura original: TABLE("IdUsoMonedaFormato" NVARCHAR(3),"IdMoneda" NVARCHAR(3),"IdTipoInstrumento" NVARCHAR(3),"IdTipoOperacion" NVARCHAR(6),"PrecioDecimales" INTEGER,"PrecioTrucaRedondea" NVARCHAR(1),"TasaDecimales" INTEGER,"TasaTrucaRedondea" NVARCHAR(1),"CantidadDecimales" INTEGER,"CantidadTrucaRedondea" NVARCHAR(1),"MontoDecimales" INTEGER,"MontoTrucaRedondea" NVARCHAR(1),"TipoCambioDecimales" INTEGER,"TipoCambioTrucaRedondea" NVARCHAR(1),"ParidadDecimales" INTEGER,"ParidadTrucaRedondea" NVARCHAR(1),"PuntosDecimales" INTEGER,"PuntosTrucaRedondea" NVARCHAR(1))
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Consulta formatos para cantidades, montos, precios, etc.
    SELECT COALESCE(
        jsonb_agg(to_jsonb(T)),
        '[]'::jsonb
    )
    INTO P_OUT_MONEDAFORMATO
    FROM (
        SELECT
            "MonedaFormato"."IdUsoMonedaFormato",
            "MonedaFormato"."IdMoneda",
            "MonedaFormato"."IdTipoInstrumento",
            "MonedaFormato"."IdTipoOperacion",
            "MonedaFormato"."PrecioDecimales",
            "MonedaFormato"."PrecioTrucaRedondea",
            "MonedaFormato"."TasaDecimales",
            "MonedaFormato"."TasaTrucaRedondea",
            "MonedaFormato"."CantidadDecimales",
            "MonedaFormato"."CantidadTrucaRedondea",
            "MonedaFormato"."MontoDecimales",
            "MonedaFormato"."MontoTrucaRedondea",
            "MonedaFormato"."TipoCambioDecimales",
            "MonedaFormato"."TipoCambioTrucaRedondea",
            "MonedaFormato"."ParidadDecimales",
            "MonedaFormato"."ParidadTrucaRedondea",
            "MonedaFormato"."PuntosDecimales",
            "MonedaFormato"."PuntosTrucaRedondea"
        FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" AS "MonedaFormato"
        INNER JOIN "BSCL.Plataforma.BD::PLA.UsoMonedaFormato" AS "UsoMonedaFormato"
                ON ("MonedaFormato"."IdUsoMonedaFormato" = "UsoMonedaFormato"."IdUsoMonedaFormato")
        WHERE "MonedaFormato"."IdEstado" = 'ACT'
    ) AS T;
END;
$$;

-- Ejemplo de llamada en una sola línea (parámetro OUT con NULL)
-- CALL public."BSCL.Plataforma.BD.Servicios::ConsultarFormatos"(NULL);