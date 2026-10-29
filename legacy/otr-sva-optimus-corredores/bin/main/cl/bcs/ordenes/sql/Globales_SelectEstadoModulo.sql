/********************************************************** 
Proposito	: consulta estado del modulo
autor		: RU
fecha		: 01/02/2017
***********************************************************/

SELECT CRE."TipoEstado" AS "IdEstadoModulo",CRE."Nombre" as "NombreModulo"
FROM "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" CRE 
WHERE CRE."IdModulo" = :IdModulo AND CRE."IdEmpresa" = :IdEmpresa;

