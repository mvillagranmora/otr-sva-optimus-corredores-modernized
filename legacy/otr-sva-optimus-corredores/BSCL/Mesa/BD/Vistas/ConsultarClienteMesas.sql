SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarClienteMesas";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarClienteMesas" AS
SELECT Mesa."IdEmpresa",
       ConsClie."IdCliente", 
       ConsClie."DocumentoIdentidad", 
       ConsClie."NombreCompleto", 
       Por."CodigoPortafolio", 
       Mesa."IdMesa", 
       Mesa."IdPortafolio",
       ConsClie."IdSocioDeNegocio",  
       Mesa."IdTipoProducto",
       ConsClie."DocumentoIdentidad" || ' ' || ConsClie."NombreCompleto" || '(' || Por."CodigoPortafolio" || ')' AS "NombreCompletoPortafolio"
FROM "BSCL.Mesa.BD::MES.Mesa" AS Mesa
  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ConsClie
     ON ConsClie."IdCliente" = Mesa."IdCliente"
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Por
     ON Por."IdPortafolio" = Mesa."IdPortafolio";
GO

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarClienteMesas" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO
