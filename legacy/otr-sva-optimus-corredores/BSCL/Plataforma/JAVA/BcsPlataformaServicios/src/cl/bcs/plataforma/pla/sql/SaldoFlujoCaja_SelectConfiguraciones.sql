SELECT  
"IdUsoFlujoCaja"
,"NombreUso"
,"ItemCalculo"
,"ItemOrigen"
,"FuncionOperador"
,"DesplegarEnUI"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionFlujoCaja"
WHERE "IdEstadoConfiguracion" = 'ACT'
AND "IdEmpresa" = :IdEmpresa
AND (:Uso IS NULL OR "NombreUso" = :Uso) AND "FuncionOperador" IN ('-','+');	