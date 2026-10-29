SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivos"  
AS

SELECT 
	GA."IdGrupoActivos",
	GA."DescripcionGrupoActivos",
	GA."IdTipoGrupoActivos",
	GA."FechaCreacion",
	GA."FechaModificacion",
	GA."IdEstado",
	GA."PorDefecto", 
	GA."IdEmpresa",
	DGA."IdDetalleGrupoActivos",
	DGA."IdInstrumento",
	I."Nemotecnico",
	I."IdTipoInstrumento",
	I."CodigoTipoInstrumento",
	I."NombreTipoInstrumento",
	I."IdMoneda",
	I."EsAutorizado",
	DGA."FechaCreacion" as "FechaCreacionDetalle",
	DGA."IdEstado" as "IdEstadoDetalle",
	CEGA."Nombre" AS "EstadoDetalle"
FROM
	"BSCL.Plataforma.BD::COM.GrupoActivos" as GA
LEFT JOIN 
	"BSCL.Plataforma.BD::COM.DetalleGrupoActivos" as DGA
	on (GA."IdGrupoActivos" = DGA."IdGrupoActivos")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarEstadoGrupoActivo" AS CEGA	
    on (DGA."IdEstado" = CEGA."IdEstado")
LEFT JOIN
	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as I
	on (I."IdInstrumento" = DGA."IdInstrumento");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 