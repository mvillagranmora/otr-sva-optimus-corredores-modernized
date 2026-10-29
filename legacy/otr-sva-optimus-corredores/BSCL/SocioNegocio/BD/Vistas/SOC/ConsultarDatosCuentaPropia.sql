SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarDatosCuentaPropia"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarDatosCuentaPropia" AS 

	select
		emp."IdEmpresa",
		emp."IdSocioDeNegocio",
		socprop."IdPersona",
		socprop."IdCliente",
		socprop."IdEmpleado",
		socprop."IdProveedor"
	 from "BSCL.Plataforma.BD::PLA.Empresa" as emp 
		left join "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS socprop 
			on (socprop."IdSocioDeNegocio" = emp."IdSocioDeNegocio");
		 
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarDatosCuentaPropia" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
