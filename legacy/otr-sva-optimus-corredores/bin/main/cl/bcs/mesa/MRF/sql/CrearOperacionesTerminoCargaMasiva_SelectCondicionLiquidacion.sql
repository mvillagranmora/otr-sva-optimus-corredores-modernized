SELECT 
"IdCondicionDeLiquidacion" AS "IdCondicionLiquidacion"
,"CodigoCondicionDeLiquidacion" AS "CodigoCondicionLiquidacion"
,"IdTipoOperacion"
,"SoloDiasHabiles"
,"PlazoEnDias"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoOperacionYCondicionDeLiquidacion" WHERE "IdTipoOperacion" IN ('COMTER','VENTER')