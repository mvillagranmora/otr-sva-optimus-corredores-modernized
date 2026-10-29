SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAT.UIConsultarAgenda"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAT.UIConsultarAgenda"  
AS
SELECT
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
	 "agenda"."SiguienteFechaDeEjecucion",
	 "agenda"."UltimaFechaDeEjecucion",
	 "tipoprograma"."Descripcion" as "DescripcionTipoPrograma",
	 "tipoprograma"."EsPauta",
	 "pauta"."RequiereTipoPrograma",
	 "pauta"."SiguienteTipoPrograma"
FROM "BSCL.Plataforma.BD::BAT.Agenda" "agenda" 
LEFT JOIN "BSCL.Plataforma.BD::BAT.TipoPrograma" "tipoprograma" ON  "tipoprograma"."IdTipoPrograma" = "agenda"."IdTipoPrograma"
LEFT JOIN "BSCL.Plataforma.BD::PAU.Dependencia" "pauta" ON "pauta"."IdTipoPrograma" = "agenda"."IdTipoPrograma";
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAT.UIConsultarAgenda" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


