INSERT INTO "BSCL.Plataforma.BD::FTP.Detalle" (
	"IdDetalle",
	"IdCabecera",
	"Linea",
	"IdTipoEntidad",
	"IdEntidad"
) VALUES (
	"BSCL.Plataforma.BD.Secuencias::Detalle".NEXTVAL,
	:IdCabecera,
	:Linea,
	:IdTipoEntidad,
	:IdEntidad
);