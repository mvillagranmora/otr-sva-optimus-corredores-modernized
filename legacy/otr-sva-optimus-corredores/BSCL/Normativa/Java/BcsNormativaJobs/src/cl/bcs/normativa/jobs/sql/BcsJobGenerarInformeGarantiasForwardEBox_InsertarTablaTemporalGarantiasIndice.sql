INSERT INTO "BSCL.Normativa.BD::GBL.GarantiasForwardEBox_TablaPaso"
("SecuenciaSolicitud", "RutCliente", "NombreCliente", "GarantiaExigida", "GarantiaEnterada", "TipoLinea")
WITH Forwards AS (
    SELECT DISTINCT
         "IdOperacion"
        ,"FechaOperacion"
        ,"IdCliente"
        ,"IdEmpresa"
        ,"ValorNominal"
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
Riesgo_Indicador AS (
    SELECT
         IND."IdEntidad" AS "IdCliente"
        ,CASE 
            WHEN IND."ValorIndicador" IS NULL 
              OR IND."ValorIndicador" = '' 
              OR IND."ValorIndicador" = 'N'
            THEN 0
            ELSE CAST(REPLACE(IND."ValorIndicador", ',', '.') AS DECIMAL(26,8))
        END AS Porcentaje
    FROM "BSCL.Plataforma.BD::IND.Indicador" IND
    WHERE IND."IdTipoEntidad" = 'CLIENTE'
      AND IND."IdTipoIndicador" = (
          SELECT "IdTipoIndicador"
          FROM "BSCL.Plataforma.BD::IND.TipoIndicador"
          WHERE "CodigoTipoIndicador" = 'PORCRIESGO'
            AND "IdTipoEntidad" = 'CLIENTE'
            AND "IdEstado" != 'INA'
      )
)
SELECT
     :SecuenciaSolicitud
    ,CLI."DocumentoIdentidad" AS "RutCliente"
    ,SUBSTRING(CLI."NombreCompleto", 1, 50) AS "NombreCliente"
    ,ROUND(SUM(FWD."ValorNominal" * (COALESCE(R.Porcentaje,0) / 100) * COALESCE(PARUSD."TipoCambioMercado",0)),0) AS "GarantiaExigida"
    ,ROUND(COALESCE(GAR."MontoGarantia", 0), 0) AS "GarantiaEnterada"
    ,'G' AS "TipoLinea"
FROM Forwards FWD
LEFT JOIN Garantias GAR
    ON GAR."IdCliente" = FWD."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
    ON CLI."IdCliente" = FWD."IdCliente"
LEFT JOIN Riesgo_Indicador R
    ON R."IdCliente" = FWD."IdCliente"
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
    
    
    