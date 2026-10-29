INSERT INTO  "BSCL.Plataforma.BD::IND.Indicador" 
					  (
					   "IdIndicador",
					   "IdEntidad",
					   "IdTipoEntidad",
					   "IdTipoIndicador",
					   "ModeloCliente", 
					   "IdEmpresa",
					   "ValorIndicador",
					   "FechaIngreso",
					   "FechaCreacion",
					   "FechaModificacion",
					   "IdEstadoAutorizacion"
					   )
VALUES (
	"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL,
	:IdEntidad,
	:IdTipoEntidad,
	:IdTipoIndicador,
	:ModeloCliente,
	:IdEmpresa,
	:ValorIndicador,
	:FechaHoraSistema,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	:IdEstadoAutorizacion
)