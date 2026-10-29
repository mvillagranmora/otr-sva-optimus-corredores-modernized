SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarCargaMasivaSpot";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarCargaMasivaSpot" AS
SELECT
	TCMS."IdTipoCargaMasiva",
	TCMS."CodigoTipoCargaMasiva",
	TCMS."Nombre" 	 AS "NombreTipoCargaMasiva",
	TCMS."IdEstado"  AS "IdEstadoTipoCargaMasiva",
	CMS."IdCargaMasiva",
	CMS."FechaCarga",
	CMS."FechaProceso",
	CMS."CantidadRegistros",
	CMS."IdEmpresa",
	CMS."IdUsuario",
	CMS."IdEstado" AS "IdEstadoCargaMasiva",
	CMS."NombreArchivo",
	USU."CodigoUsuario",
	EST."Nombre" AS "EstadoCargaMasiva"
FROM "BSCL.Mesa.BD::CAR.CargaMasivaSpot" AS CMS
INNER JOIN "BSCL.Mesa.BD::CAR.TipoCargaMasivaSpot" AS TCMS ON (TCMS."IdTipoCargaMasiva" = CMS."IdTipoCargaMasiva")
LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" 	AS USU ON (USU."IdUsuario" = CMS."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" 	AS EST ON (EST."IdEstado" = CMS."IdEstado")


WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::CAR.ConsultarCargaMasivaSpot" TO  BCS_DESARROLLADOR WITH GRANT OPTION;