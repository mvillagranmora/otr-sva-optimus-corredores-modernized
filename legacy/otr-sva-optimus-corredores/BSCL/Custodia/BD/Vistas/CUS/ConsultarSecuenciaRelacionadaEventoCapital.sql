SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarSecuenciaRelacionadaEventoCapital";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarSecuenciaRelacionadaEventoCapital"
AS 
SELECT
	EC."IdEventoCapital"						AS "IdEventoCapital",
	EC."IdTipoEventoCapital"					AS "IdTipoEventoCapital",
	EC."Secuencia"								AS "Secuencia",
	EC."Secuencia" || ' - ' || EC."Descripcion"	AS "Descripcion",
	EC."IdInstrumento"							AS "IdInstrumento",
	EC."Nemo"									AS "Nemo",
	EC."IdEstado"								AS "Estado",
	EC."FechaHoraIngresoRegistro"				AS "FechaIngreso"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS EC
WHERE EC."IdTipoEventoCapital" = 'EP'
AND EC."FechaHoraIngresoRegistro" >= ADD_YEARS(CURRENT_DATE, -1)
AND EC."IdEstado" = 'CER'
--AND EC."IdInstrumento" IN (:IdInstrumento) //PARA CONSULTAS INTERNAS
ORDER BY EC."FechaHoraIngresoRegistro" DESC;
GO	 
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarSecuenciaRelacionadaEventoCapital" TO BCS_DESARROLLADOR WITH GRANT OPTION;