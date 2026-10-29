/********************************************************** 
Proposito	: Verifica si existe la configuración de grilla usuario
autor		: GATBLAC
fecha		: 23/05/2018
***********************************************************/
SELECT CU."IdConfiguracionUsuario" 
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionUsuario" CU
WHERE CU."IdEmpresa" = :p_IdEmpresa 
AND CU."IdUsuario" = :p_IdUsuario 
AND CU."Llave" = :p_Llave;
