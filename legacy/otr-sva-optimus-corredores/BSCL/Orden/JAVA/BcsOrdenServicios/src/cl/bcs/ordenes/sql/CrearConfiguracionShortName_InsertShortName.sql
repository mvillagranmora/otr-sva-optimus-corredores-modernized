INSERT INTO "BSCL.Orden.BD::ORD.ShortNames" (
	"ShortName",
	"Habilitado",
	"IdSocioDeNegocio",
	"DocumentoIdentidad",
	"IdPortafolio",
    "FechaHoraCreacion",
    "UsuarioCreador",
    "FechaHoraModificacion",
    "UsuarioModificador",
    "CodigoPortafolio"
    )
VALUES(
	:ShortName,
	:Habilitado,
	:IdSocioDeNegocio,
    :DocumentoIdentidad,
    :IdPortafolio,
	:FechaSistema,
    :IdUsuario,
    :FechaSistema,
    :IdUsuario,
    COALESCE (( SELECT top 1"CodigoPortafolio" FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" WHERE "IdPortafolio"  = :IdPortafolio ) , 0 )
)
