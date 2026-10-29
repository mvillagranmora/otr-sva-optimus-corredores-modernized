SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversionPortafolioCLiente";


CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversionPortafolioCLiente" AS 
SELECT
		CTA."IdCuentaInversion",
		CTA."IdEmpresa",
		CTA."IdMoneda",	   
		CTA."CodigoMoneda",
		CONCAT(CONCAT(CTA."CodigoMoneda",' - '), CTA."NombreMoneda") AS "Moneda",
		CTA."DecimalesMoneda" ,     
		CTA."Codigo" as "CodigoCuentaInversion",
		CTA."Nombre" as "NombreCuentaInversion",
		CTA."FechaIngresoRegistro",
		CTA."IdUsuario",
		CTA."IdEstado" as "IdEstadoCuentaInversion",
		CLI."IdCliente",
		--cli."EstaHabilitado",
		CLI."DocumentoIdentidad",
		CLI."RazonSocial",
		POR."IdPortafolio",
		POR."CodigoPortafolio",
		POR."Proposito",
		
		CASE WHEN CLI."IdEstado" = 'ACT' AND POR."IdEstado" = 'ACT' THEN 'ACT' 
	   		 WHEN CLI."IdEstado" = 'ACT' AND POR."IdEstado" <> 'ACT' THEN POR."IdEstado"
	   		 ELSE CLI."IdEstado"
	   	END as "IdEstadoPortafolio",
		CTA."CuentaInversionPrincipal",
		POR."IdEmpleadoEjecutivo"
  FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR ON CLI."IdCliente" = POR."IdCliente"
 INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" AS CTA ON CTA."IdCliente" = POR."IdCliente" AND CTA."IdPortafolio" = POR."IdPortafolio"
  WITH READ ONLY;

  
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversionPortafolioCLiente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  
