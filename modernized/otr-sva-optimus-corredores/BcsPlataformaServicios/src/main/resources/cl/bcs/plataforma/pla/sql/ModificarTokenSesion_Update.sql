/********************************************************** 
Proposito	: Cierra la sesion activa para refrescar hora del sistema
autor		: SHECHENLEITNER - BCS
fecha		: 24/05/2017
***********************************************************/
UPDATE Sesion
SET Sesion."TokenValido" = 'N'
FROM "BSCL.Plataforma.BD::ACC.Sesion" AS Sesion
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usuario
ON Usuario."IdUsuario" = Sesion."IdUsuario"
WHERE "TokenValido" = 'S'
AND "IdEmpresa" = :IdEmpresa