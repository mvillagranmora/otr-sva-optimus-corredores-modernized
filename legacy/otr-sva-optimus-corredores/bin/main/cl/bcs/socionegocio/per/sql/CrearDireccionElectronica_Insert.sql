INSERT INTO "BSCL.SocioNegocio.BD::PER.DireccionElectronica"(
	"IdDireccionElectronica",
	"Direccion",
	"CodigoProposito",
	"Comentario",
	"IdEstado",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion",
	"IdPersona",
	"IdEstadoAutorizacion",
	"IdUsuarioCreador",
	"Predeterminado"
)VALUES(
	:p_IdDireccionElectronica,
	:p_Direccion,
	COALESCE(:p_CodigoProposito, CASE :p_Tipo WHEN 'J' THEN 'COME' ELSE 'PART' END),
	COALESCE(:p_Comentario, ''),
	COALESCE(:p_IdEstado, 'ACT'),
	:p_FechaIngreso,--TO_TIMESTAMP(p_FechaIngreso,'YYYY-MM-DD HH24:MI:SS.FF7') 					
	CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
	CURRENT_UTCTIMESTAMP,							
	:p_IdPersona,				
	:p_IdEstadoAutorizacion,
	:p_IdUsuario,
	:p_Predeterminado
);