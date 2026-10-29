INSERT INTO "BSCL.Normativa.BD::GBL.GarantiasForwardEBox_TablaPaso"
("SecuenciaSolicitud", "RutCliente", "NombreCliente", "GarantiaExigida", "GarantiaEnterada", "TipoLinea")
WITH Forwards AS (
    SELECT DISTINCT
         "IdOperacion"
        ,"FechaOperacion"
        ,"IdCliente"
        ,"IdEmpresa"
        ,"ValorNominal"
        ,"IdTipoOperacion"
        ,"IdMonedaPrincipal"
        ,"IdMonedaSecundaria"
        ,"PlazoEnDias"
    FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperacionesForwardsVigentesEBox"
    WHERE "IdEmpresa" = :IdEmpresa
    AND "FechaOperacion" <= :FechaHasta
),
Garantias AS (
    SELECT 
         "IdCliente"
        ,SUM(
         CASE 
             WHEN "IdOrigenGarantia" = 'CUS' 
                 THEN "INGMontoATasaMercadoActual"
             ELSE "INGMontoATasaGarantiaActual"
         END
     ) AS "MontoGarantia"
    FROM "BSCL.Custodia.BD::GTA.Garantia"
    WHERE "IdConcepto" = 'GTAFWD'
      AND "IdEstado" = 'VIG'
      AND "FechaOperacion" <= :FechaHasta
      AND "IdEmpresa" = :IdEmpresa
    GROUP BY "IdCliente"
),
Riesgo_Matriz AS (
    SELECT
         FWD."IdOperacion"
        ,R."Plazo"
        ,R."Porcentaje"
        ,ROW_NUMBER() OVER (
            PARTITION BY FWD."IdOperacion"
            ORDER BY R."Plazo"
        ) AS RN
    FROM Forwards FWD
    JOIN "BSCL.Mesa.BD::FWD.MatrizRiesgo" R
        ON R."IdTipoProducto"     = 'MEFW'
        AND R."IdTipoOperacion"    = FWD."IdTipoOperacion"
        AND R."IdMonedaPrincipal"  = FWD."IdMonedaPrincipal"
        AND R."IdMonedaSecundaria" = FWD."IdMonedaSecundaria"
        AND R."Plazo" >= FWD."PlazoEnDias"
)
SELECT 
     :SecuenciaSolicitud
    ,CLI."DocumentoIdentidad" AS "RutCliente"
    ,SUBSTRING(CLI."NombreCompleto", 1, 50) AS "NombreCliente"
    ,ROUND(SUM(FWD."ValorNominal"  * (COALESCE(R."Porcentaje",0) / 100) * COALESCE(PARUSD."TipoCambioMercado",0)),0) AS "GarantiaExigida"
    ,ROUND(COALESCE(GAR."MontoGarantia", 0), 0) AS "GarantiaEnterada"
    ,'G' AS "TipoLinea"
FROM Forwards FWD
LEFT JOIN Garantias GAR
    ON GAR."IdCliente" = FWD."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
    ON CLI."IdCliente" = FWD."IdCliente"
LEFT JOIN Riesgo_Matriz R
    ON R."IdOperacion" = FWD."IdOperacion"
    AND R.RN = 1
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARUSD
    ON PARUSD."IdMoneda" = 'DO'
    AND PARUSD."Fecha" = FWD."FechaOperacion"
WHERE NOT EXISTS (
    SELECT 1
    FROM "BSCL.SocioNegocio.BD::CNT.Contraparte" CNT
    WHERE CNT."IdPersona" = CLI."IdPersona"
      AND CNT."Banco" = 'S'
)
GROUP BY
    CLI."DocumentoIdentidad", 
    CLI."NombreCompleto",
    GAR."MontoGarantia";