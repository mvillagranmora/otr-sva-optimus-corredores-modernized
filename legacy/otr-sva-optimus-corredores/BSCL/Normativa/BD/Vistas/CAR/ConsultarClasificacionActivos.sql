SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarClasificacionActivos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarClasificacionActivos" AS
    SELECT INSTR."IdInstrumento",
		INSTR."Nemotecnico",
	 	COALESCE(CLASIF."Mercado",SUB."Mercado",MERC."DescripcionMercado", clasInst."Nombre" ) AS "Mercado",
	 	COALESCE(SUB."DescripcionSubMercado", CLASIF."SubMercado",'' ) AS "SubMercado",
	 	COALESCE(CLASIF."DescripcionClasificacionActivo", '' ) AS "ClasificacionActivo"	
	FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INSTR
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tpInstr ON (tpINSTR."IdTipoInstrumento" = INSTR."IdTipoInstrumento")
		LEFT JOIN "BSCL.Plataforma.BD::INS.RelacionClasificacionActivos" AS RELCLASIF ON  INSTR."IdInstrumento" = RELCLASIF."IdInstrumento"
		LEFT JOIN "BSCL.Normativa.BD.Vistas::CAR.ConsultarMercadosClasificacion" MERC ON (MERC."IdMercado" = RELCLASIF."IdArbolClasificacionActivo" AND RELCLASIF."IdInstrumento" = INSTR."IdInstrumento")
		LEFT JOIN "BSCL.Normativa.BD.Vistas::CAR.ConsultarSubMercadosClasificacion" SUB ON (SUB."IdSubMercado" = RELCLASIF."IdArbolClasificacionActivo" AND RELCLASIF."IdInstrumento" = INSTR."IdInstrumento")
		LEFT JOIN "BSCL.Normativa.BD.Vistas::CAR.ConsultarArbolClasificacion" CLASIF ON( CLASIF."IdCalsificacionActivo" = RELCLASIF."IdArbolClasificacionActivo" AND RELCLASIF."IdInstrumento" = INSTR."IdInstrumento")
		INNER JOIN 	"BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" clasInst ON clasInst."IdClasificacionTipoInstrumento" = tpINSTR."IdClasificacionTipoInstrumento";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarClasificacionActivos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
