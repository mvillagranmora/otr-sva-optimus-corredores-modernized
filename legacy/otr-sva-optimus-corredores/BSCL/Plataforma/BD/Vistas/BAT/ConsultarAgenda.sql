SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarAgenda"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarAgenda"  
AS SELECT
	 "agenda"."IdAgenda",
	 "agenda"."IdTipoPrograma",
	 "agenda"."EsDemonio",
	 "agenda"."DiaDeMes",
	 "agenda"."DiaDeSemana",
	 "agenda"."Mes",
	 "agenda"."Hora",
	 "agenda"."Minuto",
	 "agenda"."SoloDiasHabiles",
	 "agenda"."SoloPrincipioMes",
	 "agenda"."SoloFinDeMes",
	 "agenda"."habilitado",
	 "parametrosagenda"."NombreParametro",
	 "parametrosagenda"."Valor" ,
	 "agenda"."SiguienteFechaDeEjecucion",
	 "agenda"."UltimaFechaDeEjecucion",
	 "tipoprograma"."Descripcion" as "DescripcionTipoPrograma",
	 "tipoprograma"."EsPauta"
FROM "BSCL.Plataforma.BD::BAT.Agenda" "agenda" 
LEFT JOIN "BSCL.Plataforma.BD::BAT.TipoPrograma" "tipoprograma" ON  "tipoprograma"."IdTipoPrograma" = "agenda"."IdTipoPrograma"
LEFT JOIN "BSCL.Plataforma.BD::BAT.ParametrosAgenda" "parametrosagenda" ON "agenda"."IdAgenda" = "parametrosagenda"."IdAgenda";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAT.ConsultarAgenda" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


