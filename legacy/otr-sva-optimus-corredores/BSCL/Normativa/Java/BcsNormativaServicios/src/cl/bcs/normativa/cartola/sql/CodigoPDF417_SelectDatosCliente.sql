SELECT 
CLI."IdCliente"
,POR."IdPortafolio"
,COALESCE(DIR."NombrePais",'') AS "NombrePais"
,COALESCE(DIR."Departamento",'') AS "Departamento"
,COALESCE(DIR."NombreCiudad",'') AS "NombreCiudad"
,COALESCE(DIR."Calle",'') AS "Direccion"
,COALESCE(CLI."NombreCompleto",'') AS "NombreCompleto"
,COALESCE(POR."CodigoPortafolio",'') AS "CodigoPortafolio"
,COALESCE(DIR."NombreDivisionPais",'') AS "NombreDivisionPais"
,COALESCE(DIR."Numero",'') AS "Numero"
,COALESCE(CLI."DocumentoIdentidad",'') AS "DocumentoIdentidad"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON POR."IdPortafolio" = P."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON POR."IdCliente" = CLI."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR ON CLI."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica"

