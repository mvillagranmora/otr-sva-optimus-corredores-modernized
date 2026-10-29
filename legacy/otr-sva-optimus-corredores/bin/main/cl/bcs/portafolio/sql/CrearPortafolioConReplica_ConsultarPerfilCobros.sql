SELECT 
perfilCobro."IdPerfilCobros"
FROM "BSCL.Plataforma.BD::COM.PerfilCobros" AS perfilCobro
WHERE perfilCobro."IdEmpresa" = :IdEmpresa 
AND perfilCobro."IdEstado" = 'ACT'
AND perfilCobro."IdPerfilCobros" = :IdPerfilCobro;