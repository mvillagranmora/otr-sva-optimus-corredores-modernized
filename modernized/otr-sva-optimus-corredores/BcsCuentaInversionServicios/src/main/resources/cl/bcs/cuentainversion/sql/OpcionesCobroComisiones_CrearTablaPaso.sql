CREATE LOCAL TEMPORARY TABLE #OpcionesCobro_TablaPaso (
     "IdOpcionCobro" BIGINT
    ,"OpcionCobro" BIGINT
    ,"PortafolioAPV" NVARCHAR(1)
    ,"Mercado" NVARCHAR(3)
    ,"PorcentajeCobro" DECIMAL
    ,"Estado" NVARCHAR(3)
    ,"FechaIngreso" NVARCHAR(10)
    ,"FechaVigencia" DATE
);