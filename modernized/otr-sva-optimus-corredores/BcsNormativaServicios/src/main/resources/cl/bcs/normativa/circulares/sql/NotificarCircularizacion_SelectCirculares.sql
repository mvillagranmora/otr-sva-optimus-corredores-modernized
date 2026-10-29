SELECT 
	NOM."IdNomina",
	NOM."IdPersona",
	NOM."IdCliente",
	NOM."IdPortafolio",
	NOM."CodigoPortafolio",
	DIRE."IdDireccionElectronica",
	CCDG."RazonSocial" AS "Corredora",
	CCDG."Telefono" AS "AtencionCliente",
	CCDG.RUT AS "RutCorredora",
	PER."IdDireccionElectronicaPrincipal", 
	PER."NombreCompleto",
    PER."DocumentoIdentidad",
	PER."TipoDocumentoIdentidad",
    PER."IdDireccionElectronicaPrincipal",
	EMP."NombreCompleto" AS "Agente",
	USU."UsuarioClientes" AS "AccesoCliente"
FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina" NOM
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS PER ON (PER."IdPersona"= NOM."IdPersona")
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultaIndicadoresCustodiaCliente" AS CIC ON CIC."IdCliente" = NOM."IdCliente" AND CIC."ContratoCustodia" = 'S'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMP ON (PER."IdEmpleadoEjecutivo"=EMP."IdEmpleado")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS USU ON
	(NOM."IdPersona" = USU."IdPersona" AND USU."TipoEntidad" = 'CLI')
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS DIRE ON
	(PER."IdDireccionElectronicaPrincipal" = DIRE."IdDireccionElectronica" AND NOM."IdPersona" = DIRE."IdPersona" AND DIRE."CodigoProposito" = 'CIRC')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa"= :IdEmpresa
WHERE 
  ((:ClientesAux = true AND NOM."IdCliente" IN (:IdClientes)) OR :ClientesAux = false)
  AND 
  ((:PortafoliosAux = true AND NOM."IdPortafolio" IN (:IdPortafolios)) OR :PortafoliosAux = false)
  AND 
  ((:NominasAux = true AND NOM."IdNomina" IN (:IdNominas)) OR :NominasAux = false)
  AND
  ((:EstadoNotificacion = 'PEN' AND :EstadoNotificacion = NOM."IdEstadoNotificacion") or (:EstadoNotificacion = 'ENV'))
  AND
  NOM."Periodo" = :Periodo AND "Notificar" = 'SI' ORDER BY USU."UsuarioClientes" ;