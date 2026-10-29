SET SCHEMA "BSCL";

INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"(
	 "IdTipoPrograma"
	,"Descripcion"
	,"MaximoIntentos"
	,"Modulo"
	,"NombreClasePrograma"
	,"TipoClasePrograma"
	,"Nodo"
	,"EsPauta"
	,"TipoSalida"
	,"FormatoSalida"
	,"TieneLog"
)VALUES(
	 'INGRESAR_BONIFICACION_FISCAL_REINVERSION'
	,'Ingresar Orden Bonificacion fiscal por reinversion 1 a N'
	,1
	,'FMM'
	,'cl.bcs.fondomutuo.jobs.IngresarOrdenBonificacionFiscalReinversion'
	,'JAVA'
	,''
	,'N'
	,'P'
	,''
	,'N'
);