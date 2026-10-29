UPDATE "BSCL.Orden.BD::ORD.ShortNames" 
SET
	"ShortName"= :NewShortName,
	"Habilitado"= :Habilitado,
	"IdSocioDeNegocio"= :IdSocioDeNegocio,
	"DocumentoIdentidad"= :DocumentoIdentidad,
	"IdPortafolio"= :IdPortafolio,
	"CodigoPortafolio" = :CodigoPortafolio,
	"FechaHoraModificacion" = :FechaSistema,
	"UsuarioModificador"= :IdUsuario
WHERE "ShortName"= :OldShortName

 

