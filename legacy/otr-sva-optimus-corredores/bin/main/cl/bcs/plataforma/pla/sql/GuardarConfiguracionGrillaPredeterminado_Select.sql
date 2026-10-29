/********************************************************** 
Proposito	: Verifica si existe la configuración de grilla predeterminada
autor		: GATBLAC
fecha		: 23/05/2018
***********************************************************/
SELECT CG."IdConfiguracionGrilla" 
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionGrilla" CG
WHERE CG."IdEmpresa" = :p_IdEmpresa 
AND CG."Llave" = :p_Llave;
