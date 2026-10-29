 --Inserta Persona Fatca
INSERT INTO "BSCL.SocioNegocio.BD::PER.Fatca"  (
	"IdPersona",
	"IdEstado",
	"IdClasificacionFatca",
	"IdClasificacionEmpresaFatca",
	"NumeroGIIN",
	"AutorizacionInformarOrganismo",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion",
	"IdUsuarioCreador",
	"IdUsuarioModificador",
	"ExcepcionNacimiento",
	"ExcepcionResidente"
)SELECT
	:IdPersona,--"IdPersona"
	:IdEstado,--"IdEstado"
	'NOUSENTITY' AS "IdClasificacionFatca",--"IdClasificacionFatca"
	CASE :Tipo WHEN 'J' THEN 'PENDIENTE' ELSE '' END AS "IdClasificacionEmpresaFatca",--"IdClasificacionEmpresaFatca"
	'',--"NumeroGIIN"
	'N',--"AutorizacionInformarOrganismo"
	:FechaHoraSistema,--"FechaIngreso"
	CURRENT_UTCTIMESTAMP,--"FechaCreacion"
	CURRENT_UTCTIMESTAMP,--"FechaModificacion"
	:IdUsuario,--"IdUsuarioCreador"
	:IdUsuario,--"IdUsuarioModificador"
	CASE WHEN PAISNAC."CodigoPais" = 'US' THEN 'S' ELSE 'N' END AS "ExcepcionNacimiento",
	CASE WHEN PAISRES."CodigoPais" = 'US' THEN 'S' ELSE 'N' END AS "ExcepcionResidente"
 from DUMMY as CENR
 cross join "BSCL.Plataforma.BD::REG.Pais" AS PAISRES 
 cross join "BSCL.Plataforma.BD::REG.Pais" AS PAISNAC
 WHERE PAISRES."IdPais" = :IdPaisResidencia
 AND PAISNAC."IdPais" = :IdPaisNacimiento;