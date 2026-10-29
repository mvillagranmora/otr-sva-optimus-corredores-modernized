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
	"IdCarga"
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
	CASE :IdPaisNacionalidadPrincipal WHEN 0 THEN NULL ELSE :IdPaisNacionalidadPrincipal END, --IdPaisNacionalidadPrincipal
	CASE :IdPaisResidencia WHEN 0 THEN NULL ELSE :IdPaisResidencia END, --IdPaisResidencia
	TO_DATE(:FechaNacimiento,'DD/MM/YYYY'), --FechaNacimiento formato original DD-MM-YYYY
	:Sexo, --Sexo
	CASE Length(:EstadoCivil) WHEN 0 THEN NULL ELSE :EstadoCivil END,
	null, --IdDireccionFisicaPrincipal
	null, --IdDireccionElectronicaPrincipal
	null, --IdTelefonoPrincipal
	:FechaIngreso, --FechaIngreso
	CURRENT_UTCTIMESTAMP, --FechaCreacion
	CURRENT_UTCTIMESTAMP, --FechaModificacion
	null,
	null,
	:EsEnrolado, --EsEnrolado
	:IdUsuarioCreador, --IdUsuarioCreador
	:IdCarga
FROM DUMMY as CENR
left join "BSCL.Plataforma.BD::REG.Ciudad" CIU on (CIU."IdCiudad" = :IdCiudad);
 
 