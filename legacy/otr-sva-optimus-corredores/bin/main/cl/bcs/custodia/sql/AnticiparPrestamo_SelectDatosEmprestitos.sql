SELECT cli."DocumentoIdentidad"
FROM "BSCL.Custodia.BD::CUS.Cartera" car
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = car."IdCliente"
WHERE car."FolioCarteraRelacionada" = :p_FolioPrestamo 
	AND car."IdEmpresa" = :p_IdEmpresa
	AND car."IdTipoInstrumento" IN ('EMP','ERF') 
	AND car."IdEstado" = 'VIG';