SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion" AS
SELECT  MonTes."IdMoneda"
	   ,MonTes."NombreLargo" as "NombreLargoMoneda"
	   ,CliCtaInv."IdCliente"
	   ,CliCtaInv."IdPortafolio"
	   ,CtaInv."IdCuentaInversion"
	   ,COALESCE(CtaInv."CuentaInversionPrincipal",'N') as "CuentaInversionPrincipal"
	   ,COALESCE(CtaInv."IdEstado",'SAS') as "IdEstado"
	   ,COALESCE(CtaInv."NombreEstado",Est."Nombre") as "NombreEstado"
  FROM "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" as MonTes
 CROSS JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarClienteCuentaInversion" as CliCtaInv
  LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CtaInv
  		 ON CtaInv."IdCliente" = CliCtaInv."IdCliente"  and CtaInv."IdPortafolio" = CliCtaInv."IdPortafolio" and CtaInv."IdMoneda" = MonTes."IdMoneda"
 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" as Est
 		 ON Est."IdEstado" = 'SAS'
  WITH READ ONLY;
  
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  
