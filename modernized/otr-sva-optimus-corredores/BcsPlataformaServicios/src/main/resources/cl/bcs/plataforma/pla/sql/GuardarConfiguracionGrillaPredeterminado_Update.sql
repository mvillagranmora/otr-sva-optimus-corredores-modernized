/********************************************************** 
Proposito	: Modifica la configuración de grilla predeterminada
autor		: GATBLAC
fecha		: 23/05/2018
***********************************************************/
UPDATE CG
SET
CG."Valor" = :p_Valor
FROM "BSCL.Plataforma.BD::PLA.ConfiguracionGrilla" CG
WHERE CG."Llave" = :p_Llave 
AND CG."IdEmpresa" = :p_IdEmpresa;
