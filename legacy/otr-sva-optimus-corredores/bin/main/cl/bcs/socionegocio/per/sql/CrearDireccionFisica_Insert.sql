INSERT INTO "BSCL.SocioNegocio.BD::PER.DireccionFisica"  (
	"IdDireccionFisica" ,
	"Calle" ,
	"Numero" ,
	"Departamento" ,
	"Sector" ,
	"CodigoPostal" ,
	"IdCiudad" ,
	"IdDivisionPais",
	"CodigoProposito" ,
	"Comentario" ,
	"IdEstado" ,
	"FechaIngreso" ,
	"FechaCreacion" ,
	"FechaModificacion" ,
	"IdPersona" ,
	"IdEstadoAutorizacion",
	"IdUsuarioCreador",
	"Predeterminado"
 ) VALUES(
	:p_IdDireccionFisicaPrincipal, --IdDireccionFisica
	:p_Calle, --Calle
	:p_Numero, --Numero
	:p_Departamento, --Departamento
	COALESCE(:p_Sector, ''), --Sector
	COALESCE(:p_CodigoPostal, ''), --CodigoPostal
	:p_IdCiudad, --IdCiudad
	:p_IdDivisionPais, -- IdDivisionPais correspondiente a comuna en Chile.
	COALESCE(:p_CodigoProposito, CASE :p_Tipo WHEN 'J' THEN 'COME' ELSE 'PART' END), --CodigoProposito
	COALESCE(:p_Comentario, ''), --Comentario
	COALESCE(:p_IdEstado, 'ACT'), --IdEstado
	CURRENT_UTCTIMESTAMP, --FechaIngreso
	CURRENT_UTCTIMESTAMP, --FechaCreacion
	CURRENT_UTCTIMESTAMP, --FechaModificacion
	:p_IdPersona, --IdPersona
	:p_IdEstadoAutorizacion, --IdEstadoAutorizacion
	:p_IdUsuario, --IdUsuarioCreador
	:p_Predeterminado
);