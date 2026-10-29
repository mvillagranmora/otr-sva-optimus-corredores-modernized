SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1889.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1889.Datos" (
    "ID",
    "IdDocumento",
    --"VboUsuario",
    --"Observaciones",
    --"FuenteDatos",
    ----
    "Numero",
    "RutCliente",
    "IdCliente",
    "FechaOperacion",
    "TipoAhorro",
    ---
    "MontoNominal",
    --
    "RentabilidadPositiva",
    "RentabilidadNegativa",
    --Actualizada ($)
    --"Factor",
    "RentabilidadActualizadaPositiva",
    "RentabilidadActualizadaNegativa",
    --
    "NumeroCertificado",
    "ComplementoIPC"
) AS
SELECT
    TO_BIGINT(d."ID") AS "ID",
    TO_BIGINT(d."IdDocumento") AS "IdDocumento",
    --d."VboUsuario",
    --d."Observaciones",
    --d."FuenteDatos",
    ----
    TO_BIGINT(d."Numero") AS "Numero",
    CAST(d."RutCliente" AS NVARCHAR(40)) AS "RutCliente",
    TO_BIGINT(d."IdCliente") AS "IdCliente",
    TO_DATE(d."FechaOperacion") AS "FechaOperacion",
    TO_BIGINT(d."TipoAhorro") AS "TipoAhorro",
    ---
    TO_DECIMAL(d."MontoNominal") AS "MontoNominal",
    --
    TO_DECIMAL(d."RentabilidadPositiva") AS "RentabilidadPositiva",
    TO_DECIMAL(d."RentabilidadNegativa") AS "RentabilidadNegativa",
    --Actualizada ($)
    TO_DECIMAL(d."RentabilidadActualizadaPositiva") AS "RentabilidadActualizadaPositiva",
    TO_DECIMAL(d."RentabilidadActualizadaNegativa") AS "RentabilidadActualizadaNegativa",
    --
    TO_BIGINT(d."NumeroCertificado") AS "NumeroCertificado",
    '' AS "ComplementoIPC"
FROM (
    SELECT 
        MAX("ID") AS "ID",
        "IdDocumento",
       	MAX("Numero") AS "Numero",
        "RutCliente",
        "IdCliente",
    	MAX(YEAR("FechaOperacion")||'-12-31') AS "FechaOperacion",
        "TipoAhorro",
        SUM("MontoNominal") AS "MontoNominal",
        ROUND(SUM("RentabilidadPositiva"),0)  AS "RentabilidadPositiva",
        SUM("RentabilidadNegativa")  AS "RentabilidadNegativa",
        ROUND(SUM("RentabilidadActualizadaPositiva"),0)  AS "RentabilidadActualizadaPositiva",
        SUM("RentabilidadActualizadaNegativa")  AS "RentabilidadActualizadaNegativa",
        MAX("NumeroCertificado") AS "NumeroCertificado"
    FROM "BSCL.Normativa.BD::TRI.DatosDJ1889"
    GROUP BY
        "IdDocumento",
        "RutCliente",
        "IdCliente",
        "TipoAhorro",
        YEAR("FechaOperacion")
        ORDER BY "ID"
        ) AS d

with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1889.Datos" to bcs_desarrollador with grant option;
