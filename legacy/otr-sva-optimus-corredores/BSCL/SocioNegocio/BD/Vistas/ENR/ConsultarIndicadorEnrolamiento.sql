SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarIndicadorEnrolamiento";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarIndicadorEnrolamiento" AS
SELECT 
	ENROLAMIENTO."IdEnrolamiento",
	CAST(ENROLAMIENTO."CLI_IdTipoCliente" AS NVARCHAR(40)) AS "IdTipoCliente",
	CAST(TIPOINDICADOR."IdTipoEntidad" AS NVARCHAR(40)) AS "IdTipoEntidad",
	CAST(TIPOINDICADOR."NombreTipoIndicador" AS NVARCHAR(255)) AS "NombreTipoIndicador",
	CAST(TIPOINDICADOR."GrupoIndicador" AS NVARCHAR(40)) AS "GrupoIndicador",
	CAST(TIPOINDICADOR."IdUso" AS NVARCHAR(3)) AS "IdUso",
	TIPOINDICADOR."IdTipoIndicador", -- nuevo
	CAST(TIPOINDICADOR."CodigoTipoIndicador" AS NVARCHAR(40)) AS "CodigoTipoIndicador",
	CAST(TIPOINDICADOR."ClaseDato" AS NVARCHAR(40)) AS "ClaseDato", -- nuevo
	TIPOINDICADOR."CantidadDecimales",
	TIPOINDICADOR."LongitudMinima",
	TIPOINDICADOR."LongitudMaxima",
	CAST(TIPOINDICADOR."IdTipoPersona" AS NVARCHAR(10)) AS "IdTipoPersona",
	CAST(ENROLAMIENTO."CLI_IdTipoCliente" AS NVARCHAR(40)) AS "ModeloCliente",
	CAST(IFNULL(INDICADOR."ValorIndicador",'') AS NVARCHAR(255))  as "ValorIndicador",
	CAST(IFNULL(DETALLEINDICADOR."Codigo",'') AS NVARCHAR(40)) as "CodigoValorIndicador", -- nuevo
	CAST(IFNULL(DETALLEINDICADOR."DetalleIndicador",'') AS NVARCHAR(255)) as "NombreValorIndicador", -- nuevo
	IFNULL(INDICADOR."IdIndicador",0) as "IdIndicador",
	TIPOINDICADOR."IdEmpresa"
FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" as ENROLAMIENTO
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.Indicador" AS INDICADOR ON INDICADOR."IdEnrolamiento" = ENROLAMIENTO."IdEnrolamiento"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIPOINDICADOR ON TIPOINDICADOR."IdTipoIndicador" = INDICADOR."IdTipoIndicador"
LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" AS DETALLEINDICADOR ON CAST(DETALLEINDICADOR."IdDetalleIndicador" AS NVARCHAR) = INDICADOR."ValorIndicador" AND DETALLEINDICADOR."IdTipoIndicador" = TIPOINDICADOR."IdTipoIndicador";


GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarIndicadorEnrolamiento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;