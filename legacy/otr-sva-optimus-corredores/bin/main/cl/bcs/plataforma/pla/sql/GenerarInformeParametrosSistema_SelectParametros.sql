/********************************************************** 
Proposito	: Obtener los parametros de optimus
autor		: CSI - BCS
fecha		: 17/05/2017
***********************************************************/
SELECT * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParametrosSistema" 
WHERE "IdEmpresa" = :IdEmpresa
