
INSERT INTO "BSCL.Plataforma.BD::PAU.ControlProceso" ("IdEmpresa", "IdControlProceso", "CodigoModulo", "CodigoProceso", "FechaSistema", "FechaRegistro", "CodigoUsuario", "ResultadoProceso")
SELECT
	:IdEmpresa, 
	"BSCL.Plataforma.BD.Secuencias::IdControlProceso".NEXTVAL, 
	'ORD',
	'VENCIMIENTO_ORDEN', 
	:FechaSistema,
	CURRENT_UTCTIMESTAMP, 
	USU."CodigoUsuario",
	:ResultadoProceso
FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USU
WHERE USU."IdUsuario" = :IdUsuario 
AND   USU."IdEmpresa" = :IdEmpresa;