INSERT INTO "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" 
(
	"IdArbolClasificacionActivo",
	"Descripcion",
	"Padre",
	"Tipo"
)
VALUES 
(
	"BSCL.Plataforma.BD.Secuencias::ArbolClasificacionActivos".NEXTVAL,
	:p_Descripcion,
	:p_Padre,
	'U'
);