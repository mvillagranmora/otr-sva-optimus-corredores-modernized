SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas.CLI::ConsultarConfiguracionSocioNegocio"; 
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas.CLI::ConsultarConfiguracionSocioNegocio"  
AS  
SELECT  "IdEmpresa",
		"CodigoParametro",
		"Descripcion",
		"Valor"
FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio";
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas.CLI::ConsultarConfiguracionSocioNegocio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO