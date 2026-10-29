SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS

	SELECT "IdSocioDeNegocio","IdPersona","IdEmpresa","IdCliente","IdEmpleado","IdOperador","IdProveedor","IdContraparte","Borrado"
	FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as SOC
	WHERE SOC."Borrado"='N';

GRANT SELECT ON  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
