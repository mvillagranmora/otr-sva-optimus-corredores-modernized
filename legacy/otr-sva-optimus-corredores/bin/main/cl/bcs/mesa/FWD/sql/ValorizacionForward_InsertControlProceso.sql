INSERT INTO "BSCL.Plataforma.BD::PAU.ControlProceso" (
	 "IdEmpresa", "IdControlProceso", "CodigoModulo", "CodigoProceso"
	,"FechaSistema", "FechaRegistro", "CodigoUsuario"
	,"ResultadoProceso")
SELECT
	:IdEmpresa, 
	"BSCL.Plataforma.BD.Secuencias::IdControlProceso".NEXTVAL, 
	'MFW',
	'VALORIZACION_FWD', 
	:FechaSistema,
	CURRENT_UTCTIMESTAMP, 
	USU."CodigoUsuario",
	:ResultadoProceso
FROM "BSCL.Plataforma.BD::ACC.Usuario" AS USU
WHERE USU."IdUsuario" = :IdUsuario 
  AND USU."IdEmpresa" = :IdEmpresa;