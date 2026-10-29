SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCustodiaFondosMutuos";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCustodiaFondosMutuos"
AS
SELECT
  "ConsultarCarteraSaldos"."IdCartera"
 ,"ConsultarCarteraSaldos"."IdCliente"
 ,"ConsultarCarteraSaldos"."CodigoPortafolio"
 ,"ConsultarCarteraSaldos"."FolioCartera"
 ,"ConsultarCarteraSaldos"."IdEmpresa"
 ,"ConsultarCliente"."RazonSocial"
 ,"ConsultarCarteraSaldos"."Nemotecnico" as "IdSerieFondo"
 ,"ConsultarCarteraSaldos"."Cantidad"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" AS "ConsultarCarteraSaldos"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON "ConsultarCarteraSaldos"."IdCliente" = "ConsultarCliente"."IdCliente";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCustodiaFondosMutuos" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
