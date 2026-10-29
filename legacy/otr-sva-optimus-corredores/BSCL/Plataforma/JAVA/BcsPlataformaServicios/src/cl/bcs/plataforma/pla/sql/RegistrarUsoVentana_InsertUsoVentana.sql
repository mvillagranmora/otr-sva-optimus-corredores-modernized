INSERT INTO "BSCL.Plataforma.BD::PLA.UsoVentana" (	
	"IdEmpresa",
	"IdUsuario",
	"IdTipoCanalIngreso",
	"Adicional1",
	"IdVentana",
	"TituloVentana",
	"FechaAcceso"
)
VALUES (
    COALESCE(:p_IdEmpresa,1),
    COALESCE(:p_IdUsuario,0),
    COALESCE(:p_IdTipoCanalIngreso,''),
    COALESCE(:p_Adicional1,''),
    COALESCE(:p_IdVentana,''),
    COALESCE(:p_Ventana,''),
	CURRENT_UTCTIMESTAMP
);
