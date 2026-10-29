SELECT CLASIF."IdInstrumento",
	CLASIF."Nemotecnico",
 	CLASIF."Mercado",
 	CLASIF."SubMercado",
 	CLASIF."ClasificacionActivo"
FROM "BSCL.Normativa.BD.Vistas::CAR.ConsultarClasificacionActivos" AS CLASIF;
