INSERT INTO "BSCL.Plataforma.BD::FTP.Detalle" (
	"IdDetalle",
	"IdCabecera",
	"Linea"
) VALUES (
	"BSCL.Plataforma.BD.Secuencias::Detalle".NEXTVAL,
	:IdCabecera,
	:Linea
);