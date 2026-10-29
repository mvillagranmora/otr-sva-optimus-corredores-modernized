INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona"  (
	"IdPersona" ,
	"IdEstado" ,
	"IdDocumentoIdentidad" ,
	"IdDocumentoIdentidadAdicional" ,
	"Tipo" ,
	"Nombres" ,
	"ApellidoPaterno" ,
	"ApellidoMaterno" ,
	"RazonSocial" ,
	"IdPaisNacimiento" ,
	"IdPaisNacionalidadPrincipal",
	"IdPaisResidencia" ,
	"ResidenciaTributariaExtranjera",
	"FechaNacimiento" ,
	"Sexo" ,
	"EstadoCivil" ,
	"IdDireccionFisicaPrincipal" ,
	"IdDireccionElectronicaPrincipal" ,
	"IdTelefonoPrincipal" ,
	"FechaIngreso" ,
	"FechaCreacion" ,
	"FechaModificacion" ,
	"IdPersonaConyuge",
	"RegimenConyugal",
	"EsEnrolado",
	"IdUsuarioCreador",
	"IdCarga",
	"IdProfesion",
	"IdTipoCanalIngresoCorredor",
	"IdSituacionLaboral",
	"IdGiroPrincipal",
	"CodigoRangoRenta",
	"CodigoRangoNivelVenta",
	"CodigoPropositoTransaccion"
)SELECT
    :IdPersona, --IdPersona
	:IdEstado, --IdEstado --DEFAULT 'ACT'
	null, -- IdDocumentoIdentidad
	null, -- IdDocIdentidadAdicional
	:Tipo, --Tipo
	:Nombres, --Nombres
	:ApellidoPaterno, --ApellidoPaterno
	:ApellidoMaterno, --ApellidoMaterno
	:RazonSocial, --RazonSocial
	CASE :IdPaisNacimiento WHEN 0 THEN NULL ELSE :IdPaisNacimiento END, --IdPaisNacimiento
	CASE :IdPaisNacimiento WHEN 0 THEN NULL ELSE :IdPaisNacimiento END, --IdPaisNacionalidadPrincipal
	CASE :IdCiudad WHEN 0 THEN :IdPaisResidencia ELSE (CASE CIU."IdPais" WHEN 0 THEN NULL ELSE CIU."IdPais" END) END, --IdPaisResidencia 
	:ResidenciaTributariaExtranjera,
	CASE Length(:FechaNacimiento) WHEN 0 THEN NULL ELSE :FechaNacimiento END, --FechaNacimiento
	:Sexo, --Sexo
	CASE Length(:EstadoCivil) WHEN 0 THEN NULL ELSE  :EstadoCivil END,
	null, --IdDireccionFisicaPrincipal
	null, --IdDireccionElectronicaPrincipal
	null, --IdTelefonoPrincipal
	:FechaIngreso, --FechaIngreso
	CURRENT_UTCTIMESTAMP, --FechaCreacion
	CURRENT_UTCTIMESTAMP, --FechaModificacion
	:IdPersonaConyuge,
	:RegimenConyugal,
	:EsEnrolado, --EsEnrolado
	:IdUsuario, --IdUsuarioCreador
	:IdCarga,
	:IdProfesion,
	:IdTipoCanalIngresoCorredor,
	:IdSituacionLaboral,
	:IdGiro,
	COALESCE(:CodigoRangoRenta,''),
	COALESCE(:CodigoRangoNivelVenta,''),
	COALESCE(:CodigoPropositoTransaccion,'')
FROM DUMMY as CENR
left join "BSCL.Plataforma.BD::REG.Ciudad" CIU on (CIU."IdCiudad" = :IdCiudad);
 
 