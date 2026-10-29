/********************************************************** 
Proposito	: Script para obtener todos los portafolios de clientes
autor		: NRM
fecha		: 03/05/2017
***********************************************************/


SELECT 	
	 POR."IdCliente"
	,POR."IdPortafolio"
	,POR."CodigoPortafolio"
	,POR."Proposito"
	,DOC."DocumentoIdentidad"
FROM "BSCL.Normativa.BD::CAR.PortafolioPaso" as CLIENTE_PASO
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio"  as POR ON POR."IdCliente" = CLIENTE_PASO."IdClientePaso"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdCliente" = POR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
WHERE CLIENTE_PASO."IdTrabajo" = :IdTrabajo;