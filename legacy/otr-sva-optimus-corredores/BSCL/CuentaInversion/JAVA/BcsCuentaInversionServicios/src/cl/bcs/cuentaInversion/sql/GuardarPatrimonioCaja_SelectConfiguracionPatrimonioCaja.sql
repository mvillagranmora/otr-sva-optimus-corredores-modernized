SELECT 
"CodigoItem"
,"FuncionOperador"
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCaja" 
WHERE "IdEstado" = 'ACT' AND "FuncionOperador" IN ('+','-')