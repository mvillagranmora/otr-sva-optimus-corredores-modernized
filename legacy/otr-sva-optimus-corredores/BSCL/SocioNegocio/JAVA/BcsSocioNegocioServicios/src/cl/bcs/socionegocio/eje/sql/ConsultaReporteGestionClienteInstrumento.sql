-- Reporte de Gestion Cliente Instrumento 
-- Ejecutado desde ReporteGestionClienteInstrumento.java  (Servicio 2900)

SELECT "NombreCliente"
	  ,"Nemotecnico"
	  ,"Pesos"
	  ,"Nominales"
	  ,"Porcentaje"
	  ,"TotalPesos"
	  ,"TotalNominales"
	  ,"TotalPorcentaje"
FROM "BSCL.Normativa.BD::CAR.CartolaHistoricaInstrumentos"
WHERE "IdInstrumento" 	= :IdInstrumento
  AND "Fecha" 			= :FechaEntrada