SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionFlujoCaja";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionFlujoCaja"

AS

SELECT
     UFC."IdUsoFlujoCaja"
	,UFC."Nombre" AS "NombreUso"
 	,UFC."IdEmpresa"
	,UFC."IdEstado" AS "IdEstadoUso"
	--,CFC."IdConfiguracionFlujoCaja"
	,CFC."ItemCalculo"
	,CFC."ItemOrigen"
	,CFC."Descripcion"  AS "DescripcionConfiguracion"
	,CFC."FuncionOperador"
	,CFC."IdEstado" AS "IdEstadoConfiguracion"
	,EST."Nombre" AS "NombreEstadoConfiguracion"
	,UFC."DesplegarEnUI" 
FROM "BSCL.Plataforma.BD::PLA.UsoFlujoCaja" AS UFC
INNER JOIN "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja" AS CFC ON (UFC."IdUsoFlujoCaja" = CFC."IdUsoFlujoCaja")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = CFC."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionFlujoCaja" TO  BCS_DESARROLLADOR WITH GRANT OPTION;