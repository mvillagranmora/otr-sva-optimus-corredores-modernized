/********************************************************** 
Proposito	: consulta canal ingreso
autor		: CG
fecha		: 18/01/2022
***********************************************************/

SELECT CI."IdTipoCanalIngreso" AS "IdTipoCanalIngreso",CI."CreaUsuarioCliente" as "CreaUsuarioCliente"
FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" CI	 
WHERE CI."IdTipoCanalIngreso" = :IdTipoCanalIngreso;