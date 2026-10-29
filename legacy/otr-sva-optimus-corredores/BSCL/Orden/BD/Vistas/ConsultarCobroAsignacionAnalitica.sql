SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCobroAsignacionAnalitica"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCobroAsignacionAnalitica" AS 
SELECT 
    CA."IdCobroAsignacion",
    
    CASE 
        WHEN CA."FechaIngresoRegistro" IS NULL OR CA."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
        ELSE TO_CHAR(ADD_SECONDS(CA."FechaIngresoRegistro", CONFIGURACION."Valor"), 'YYYY-MM-DD HH24:MI:SS')
    END AS "FechaIngresoRegistro",
    
    NULLIF(CA."IdAsignacion", 0) AS "IdAsignacion",
    CAST(NULLIF(CA."IdCobro", '') AS NVARCHAR(40)) AS "IdCobro",
    CAST(C."NombreCobro" AS NVARCHAR(100)) AS "NombreCobro",
    
    CASE 
        WHEN CA."FechaIngresoCobro" IS NULL OR CA."FechaIngresoCobro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
        ELSE TO_CHAR(ADD_SECONDS(CA."FechaIngresoCobro", CONFIGURACION."Valor"), 'YYYY-MM-DD HH24:MI:SS')
    END AS "FechaIngresoCobro",
    
    CAST(CC."CodigoCobroClasificacion" AS NVARCHAR(40)) AS "CodigoCobroClasificacion",
    CA."ValorCobro" AS "ValorCobroPerfil",
    CA."ValorCobroAplicado",
    CA."MontoCobroCalculado",
    CA."MontoCobroCalculadoLocal",
    CA."MontoCobroCalculadoLiquidacion",
    CAST(CA."FormaCobro" AS NVARCHAR(40)) AS "FormaCobro",
    CA."IdCarga",
    NULLIF(CA."IdMoneda", '') AS "IdMoneda"

FROM "BSCL.Orden.BD::ORD.CobroAsignacion" CA
INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" C ON CA."IdCobro" = C."IdCobro"
INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" CC ON CC."IdCobroClasificacion" = C."IdCobroClasificacion"
CROSS JOIN (
    SELECT TOP 1 "Valor" 
    FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
    WHERE "CodigoParametro" = 'PLA.SegundosZonaHoraria'
) AS CONFIGURACION;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCobroAsignacionAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;