SELECT 
	'S80080' AS "CodigoAI" --Constante
	,Cliente."DocumentoIdentidad" AS "IdentificadorUnicoPersona"
	,'1' AS "TipoCodigoCliente" --Constante
	,COALESCE(Documento."DocumentoIdentidad",'') AS "CodigoCliente" 
	,Cliente."ApellidoPaterno" AS "ApellidoPaterno"
	,Cliente."ApellidoMaterno" AS "ApellidoMaterno"
	,Cliente."NombreCompleto" AS "Nombres"
	,Cliente."RazonSocial" AS "RazonSocial"
	,TO_CHAR(Cliente."FechaIngreso",'DDMMYYYY') AS "FechaInscripcionCliente"
	,'01' AS "NumeroCorrelativoBenificiario" --Cliente Mancomunado Pendiente
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"  Documento ON (Cliente."IdPersona" = Documento."IdPersona" AND Documento."IdTipoDocumentoIdentidad" = 'RUT')
WHERE Cliente."FechaModificacion" >= ADD_SECONDS(:Fecha,-(60*10)) AND Cliente."FechaModificacion" < :Fecha AND Cliente."IdEmpresa" = :IdEmpresa;