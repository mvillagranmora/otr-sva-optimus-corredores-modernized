/********************************************************** 
Proposito	: Script para obtener estado modulo
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT CASE CRE."TipoEstado" WHEN 'A' THEN 'S' ELSE 'N' END AS "ModuloAbierto"
FROM  "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" CRE 
WHERE CRE."IdModulo" = 'ORD' AND CRE."IdEmpresa" = :p_IdEmpresa;
