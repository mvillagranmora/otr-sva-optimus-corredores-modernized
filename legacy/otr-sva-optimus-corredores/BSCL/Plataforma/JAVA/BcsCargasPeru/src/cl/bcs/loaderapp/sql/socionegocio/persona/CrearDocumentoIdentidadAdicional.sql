insert into "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"(
"IdDocumentoIdentidad",
"IdPersona",
"IdTipoDocumentoIdentidad",
"DocumentoIdentidad",
"IdEstadoAutorizacion",
"FechaIngreso",
"FechaCreacion",
"FechaModificacion",
"IdUsuarioCreador"
) VALUES (
"BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL, 
:IdPersona,
'R',
:DocumentoIdentidadAdicional,
'ING',
CURRENT_UTCTIMESTAMP,
CURRENT_UTCTIMESTAMP,
CURRENT_UTCTIMESTAMP,
:IdUsuario);