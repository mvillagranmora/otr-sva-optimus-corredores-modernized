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
	"IdUsuarioModificador"
)SELECT
	:IdPersona,--"IdPersona"
	:IdEstado,--"IdEstado"
	'NOUSENTITY',--"IdClasificacionFatca"
	CASE :Tipo WHEN 'J' THEN 'PENDIENTE' ELSE '' END AS "IdClasificacionEmpresaFatca",--"IdClasificacionEmpresaFatca"
	'',--"NumeroGIIN"
	'N',--"AutorizacionInformarOrganismo"
	:FechaHoraSistema,--"FechaIngreso"
	CURRENT_UTCTIMESTAMP,--"FechaCreacion"
	CURRENT_UTCTIMESTAMP,--"FechaModificacion"
	:IdUsuarioCreador,--"IdUsuarioCreador"
	:IdUsuarioCreador--"IdUsuarioModificador"
 from DUMMY as CENR;