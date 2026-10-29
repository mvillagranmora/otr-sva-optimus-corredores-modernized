SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarCargaMasivaDetalle";
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" AS

SELECT
	 CMD."IdCargaMasivaDetalle"
	,CMD."DocumentoIdentidad"
	,CMD."Entidad"
	,CMD."IdPortafolio"
	,CMD."CodigoPortafolio"
	,CMD."CodigoIndicador"
	,CMD."ValorIndicador"
	,CMD."ObservacionValidacion"
	,PER."NombreCompleto" AS "NombreSocioDeNegocio"
	,CM."IdCargaMasiva"
	,TCM."IdTipoCargaMasiva"
	,CM."IdEmpresa"
	,CM."IdUsuario"
  	,CM."FechaCarga"
 	,CM."FechaProceso"
 	,CM."FechaIngresoRegistro"
 	,CM."NombreArchivo"
	,CM."IdEstado" AS "IdEstadoCargaMasiva"
	,EST."Nombre" AS "NombreEstadoCargaMasivaDetalle"
	,CMD."IdEstado" AS "IdEstadoCargaMasivaDetalle"
FROM "BSCL.SocioNegocio.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.SocioNegocio.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."IdTipoCargaMasiva" = CM."IdTipoCargaMasiva")
INNER JOIN "BSCL.SocioNegocio.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" POR ON (POR."IdPersona" = PER."IdPersona" AND POR."IdPortafolio" = CMD."IdPortafolio")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = CMD."IdEstado")
WITH READ ONLY;

GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
