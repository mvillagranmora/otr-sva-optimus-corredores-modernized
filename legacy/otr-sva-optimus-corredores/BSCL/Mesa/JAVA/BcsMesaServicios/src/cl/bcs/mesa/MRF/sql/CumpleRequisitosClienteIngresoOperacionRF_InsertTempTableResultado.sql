INSERT INTO #CumpleRequisitosClienteIngresoOperacionRF_Resultado_Temp (
    "IdInterno", "IdResultado", "Codigo","NombreParametro1","ValorParametro1",
    "NombreParametro2", "ValorParametro2"
) VALUES (
    :IdInterno, :IdResultado, :Codigo,
    :NombreParametro1, :ValorParametro1, :NombreParametro2,
    :ValorParametro2
);