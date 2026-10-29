/********************************************************** 
Proposito	: Modifica la configuración de grilla usuario
autor		: GATBLAC
fecha		: 23/05/2018
***********************************************************/
UPDATE CU
SET
CU."Valor" = :p_Valor
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionUsuario" CU
WHERE CU."Llave" = :p_Llave AND CU."IdUsuario" = :p_IdUsuario AND CU."IdEmpresa" = :p_IdEmpresa;
