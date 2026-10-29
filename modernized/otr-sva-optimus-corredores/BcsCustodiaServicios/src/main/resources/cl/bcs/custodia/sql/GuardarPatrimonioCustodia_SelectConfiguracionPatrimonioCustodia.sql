SELECT 
"CodigoItem"
,"CodigoMontoValorizado"
,"FuncionOperador"
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCustodia" 
WHERE "IdEstado" = 'ACT' AND "FuncionOperador" IN ('+','-')