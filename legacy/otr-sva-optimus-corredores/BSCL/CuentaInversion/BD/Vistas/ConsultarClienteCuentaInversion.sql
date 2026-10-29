SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarClienteCuentaInversion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarClienteCuentaInversion" AS
SELECT  CLI."IdCliente"
	   ,CLI."IdPersona"
	   ,CLI."NombreCompleto" as "NombreCliente"
	   ,CLI."Tipo"
	   ,CLI."DocumentoIdentidad"
	   ,CLI."IdEmpresa"
	   ,CLI."IdEstado" as "IdEstadoCliente"
	   ,CLI."NombreEstado" as "NombreEstadoCliente"
	   ,POR."IdPortafolio"
	   ,POR."CodigoPropositoPortafolio" as "NombrePortafolio"
	   ,POR."IdEstado" as "IdEstadoPortafolio"
	   ,POR."Estado" as "NombreEstadoPortafolio"
	   
	   ,CASE WHEN CLI."IdEstado" = 'ACT' AND POR."IdEstado" = 'ACT' THEN 'ACT' 
	   		 WHEN CLI."IdEstado" = 'ACT' AND POR."IdEstado" <> 'ACT' THEN POR."IdEstado"
	   		 ELSE CLI."IdEstado"
	   	 END as "IdEstado"
	   ,CASE WHEN CLI."IdEstado" = 'ACT' AND POR."IdEstado" = 'ACT' THEN CLI."NombreEstado"
	   		 WHEN CLI."IdEstado" = 'ACT' AND POR."IdEstado" <> 'ACT' THEN POR."Estado"
	   		 ELSE CLI."NombreEstado"
	   	 END as "NombreEstado"
  FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR ON POR."IdCliente" = CLI."IdCliente"
  WITH READ ONLY;
  
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarClienteCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  
