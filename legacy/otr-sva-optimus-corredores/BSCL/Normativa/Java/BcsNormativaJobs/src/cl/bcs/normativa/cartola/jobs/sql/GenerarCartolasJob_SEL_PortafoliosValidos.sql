/********************************************************** 
Proposito	: Script para la selección de portafolios validos según filtros indicados en la generación de cartola
autor		: NRM
fecha		: 07/01/2018
***********************************************************/

SELECT POR."IdCliente"
	 , POR."IdPortafolio"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = POR."IdCliente"
WHERE CLI."IdEmpresa" = :IdEmpresa
AND (CLI."IdTipoPersona" = :IdTipoPersona OR 'S' = :IncluirTipoPersona)
AND (CLI."IdCategoriaInversionista" = :IdCategoriaInversionista OR 'S' = :IncluirCategoriaInversionista)
AND (CLI."IdTipoCliente" = :IdTipoCliente OR 'S' = :IncluirTipoCliente);