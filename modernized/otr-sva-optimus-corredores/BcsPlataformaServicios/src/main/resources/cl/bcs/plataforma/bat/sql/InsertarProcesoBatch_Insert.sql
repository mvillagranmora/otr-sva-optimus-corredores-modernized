insert into "BSCL.Plataforma.BD::BAT.Programa"("IdPrograma","IdGrupoParametro","IdAgenda","IdTipoPrograma",
"Intento","IdUsuario","idEstado","Resultado","fechaCreacion","fechaEjecucion","fechaSistema","Bloqueo","Nodo",
"IdProgramaReprocesado", "Comentario")
values (:IdPrograma,:IdGrupoParametro,0,:IdTipoPrograma,0,:IdUsuario,'PEN','Esperando ejecución (Programa adelantado)',CURRENT_UTCTIMESTAMP,
		:FechaEjecucion,:FechaSistema,'N',:Nodo,0, :Comentario);
