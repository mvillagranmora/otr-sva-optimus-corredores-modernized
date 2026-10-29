INSERT INTO #CumpleRequisitosClienteIngresoOperacionRF_FlujoCaja_Temp (
    "NombreUso", "FechaSaldo", "IdPortafolio",
    "IdMoneda", "Saldo"
) VALUES (
    :NombreUso, :FechaSaldo, :IdPortafolio,
    :IdMoneda, :Saldo
);