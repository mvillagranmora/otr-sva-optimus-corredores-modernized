SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::CAR.ConsultarCargaMasivaDetalle";
GO
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" AS

SELECT
	 CMD."IdCargaMasivaDetalle"
	,CMD."IdTipoMovimiento"
	,CMD."DocumentoIdentidad"
	,CMD."IdPortafolio"
	,CMD."IdConcepto"
	,POR."Proposito" AS "PropositoPortafolio"
	,CMD."Monto"
	,EST."Nombre" AS "NombreEstadoCargaMasivaDetalle"
	,CMD."ObservacionValidacion"
	,PER."NombreCompleto" AS "NombreSocioDeNegocio"	
	,CMD."IdMoneda"
	,TCM."CodigoTipoCargaMasiva"	
	,CM."IdCargaMasiva"
	,TCM."IdTipoCargaMasiva"	
	,CM."IdEmpresa"
	,CM."IdUsuario"
  	,CM."FechaCarga"   			-- Fecha Sistema
 	,CM."FechaProceso" 			-- Fecha en que se procesa CM
 	,CM."FechaIngresoRegistro"	-- Fecha de BD al ingreso
 	,CM."NombreArchivo"
	,CM."IdEstado" AS "IdEstadoCargaMasiva"	
	,CMD."IdEstado" AS "IdEstadoCargaMasivaDetalle"		
FROM "BSCL.CuentaInversion.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.CuentaInversion.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva")
INNER JOIN "BSCL.CuentaInversion.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" POR ON (POR."IdPersona" = PER."IdPersona" AND POR."CodigoPortafolio" = CMD."IdPortafolio" AND POR."Codigo" = CMD."ProductoAgrupador")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = CMD."IdEstado")
WITH READ ONLY;

GO
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::CAR.ConsultarCargaMasivaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
