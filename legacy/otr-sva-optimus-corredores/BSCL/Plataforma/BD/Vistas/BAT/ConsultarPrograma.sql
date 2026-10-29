SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma"; 
GO 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma"  
AS
SELECT  DISTINCT
	 "IdPrograma",		 
	 "IdGrupoParametro",		  
	 "IdAgenda",   
	 "programa"."IdTipoPrograma",  
	 "Intento", 
	 "programa"."IdUsuario",
	 "idEstado",  
	 "Resultado", 
	 "fechaCreacion", 
	 "fechaEjecucion",
	 "fechaSistema",
	 "fechaFin",	
	 "estado"."Nombre",
	 "estado"."IdEstado",
	 "Descripcion",
	 "Bloqueo",
	 "CodigoUsuario",
	 "programa"."IdProgramaReprocesado",
	 "tipoprograma"."TipoSalida",
	 case when "tipoprograma"."TipoSalida" = 'R' then 'UI_REPORTE'
	 	  when "tipoprograma"."TipoSalida" = 'P' then 'UI_PROCESO'
	 	  else ''
	 	  end as "TipoSalidaUI",
	 "tipoprograma"."FormatoSalida",
	 "tipoprograma"."TieneLog",
	 "programa"."RutaSalida",
	 "programa"."RutaLog",
	 COALESCE("programa"."Comentario",'') AS "Comentario"
FROM "BSCL.Plataforma.BD::BAT.Programa" "programa" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" "estado" ON "estado"."IdEstado" = "programa"."idEstado"
LEFT JOIN "BSCL.Plataforma.BD::BAT.TipoPrograma" "tipoprograma" ON  "tipoprograma"."IdTipoPrograma" = "programa"."IdTipoPrograma"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario"  "consultarusuario" ON "consultarusuario"."IdUsuario" = "programa"."IdUsuario";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


