INSERT INTO "BSCL.SocioNegocio.BD::PER.Telefono"  (
	"IdTelefono" ,
	"IdPais" ,
	"IdTipoTelefono",
	"Numero" ,
	"Anexo" ,
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
	:p_IdTelefonoPrincipal, --IdTelefono
	:p_IdPais, --IdPais
	:p_IdTipoTelefono, -- IdTipoTelefono
	:p_Numero, --Numero
	COALESCE(:p_Anexo, ''), --''--Anexo
	COALESCE(:p_CodigoProposito, CASE :p_Tipo WHEN 'J' THEN 'COME' ELSE 'PART' END),
	COALESCE(:p_Comentario, ''), --''--Comentario
	COALESCE(:p_IdEstado, 'ACT'), --'act' --IdEstado
	CURRENT_UTCTIMESTAMP, --FechaIngreso
	CURRENT_UTCTIMESTAMP, --FechaCreacion
	CURRENT_UTCTIMESTAMP, --FechaModificacion
	:p_IdPersona, --IdPersona
	:p_IdEstadoAutorizacion, --'ING', --IdEstadoAutorizacion
	:p_IdUsuario, --IdUsuarioCreador
	:p_Predeterminado
);