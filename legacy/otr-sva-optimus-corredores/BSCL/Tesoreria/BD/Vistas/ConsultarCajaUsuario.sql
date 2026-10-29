SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajaUsuario";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajaUsuario" AS

SELECT 
	"CajaUsuario"."IdCajaUsuario"
	,"CajaUsuario"."IdEmpresa" 
	,"CajaUsuario"."IdCaja" 
	,"Caja"."Codigo" AS "CodigoCaja" 
	,"Caja"."Nombre" AS "NombreCaja" 
	,"CajaUsuario"."IdUsuario"
	,"Usuario"."CodigoUsuario" AS "CodigoUsuario"
	,PER."NombreCompleto"
	,"CajaUsuario"."Sobregiro"
	,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("CajaUsuario"."Sobregiro") as "SobregiroUI"
	,"CajaUsuario"."FechaInicio" 
	,"CajaUsuario"."FechaTermino" 
	,"CajaUsuario"."IdEstado"
	,E."Nombre" AS "NombreEstadoCajaUsuario"
	FROM "BSCL.Tesoreria.BD::TES.CajaUsuario" AS "CajaUsuario"
	INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS E ON E."IdEstado" = "CajaUsuario"."IdEstado"
	LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Caja" AS "Caja" ON "CajaUsuario"."IdCaja" = "Caja"."IdCaja"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS "Usuario" ON "CajaUsuario"."IdUsuario" = "Usuario"."IdUsuario" 
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC 
		ON (SOC."IdEmpleado" = "Usuario"."IdEntidad" and "Usuario"."TipoEntidad" = 'EMP') 
		OR (SOC."IdCliente" = "Usuario"."IdEntidad" and "Usuario"."TipoEntidad" = 'CLI')
		OR (SOC."IdOperador" = "Usuario"."IdEntidad" and "Usuario"."TipoEntidad" = 'OPE')
		OR (SOC."IdProveedor" = "Usuario"."IdEntidad" and "Usuario"."TipoEntidad" = 'PRO')
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on PER."IdPersona" =  SOC."IdPersona"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarCajaUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION;