SELECT
CAR."IdCartera"
,CAR."IdCliente"
,CC."IdPersona" AS "IDCLIENTEPERSONA"
,CAR."IdPortafolio" AS "IdPortafolioCliente"
,CC."IdEmpleadoEjecutivo" AS "IdEmpleadoDigitador"
,CE."IdPersona" AS "IDEMPLEADOPERSONA"
,CE."IdDireccionElectronicaPrincipal" AS "IDCORREOEMPLEADO"
,CAR."FolioCartera" AS "FOLIOOPERACION"
,CE."NombreCompleto" AS "AGENTE"
,TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:SegundosZonaHoraria), 'DD-MM-YYYY') AS "FECHA"
,TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:SegundosZonaHoraria), 'HH24:MI:SS') AS "HORA"
,CC."NombreCompleto" AS "CLIENTE"
,CEMP."RazonSocial" AS "CORREDORA"
,CEMP."Telefono" AS "ATENCIONCLIENTE"
,CC."DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD"
,CPDE."IdDireccionElectronica" AS "IDCORREOCLIENTE"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" = CAR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = CC."IdEmpleadoEjecutivo"
INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CEMP ON CEMP."IdEmpresa" = :IdEmpresa
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CC."IdDireccionElectronicaPrincipal"
WHERE CAR."IdEmpresa" = :IdEmpresa AND CAR."IdCartera" IN (:IdCartera)

