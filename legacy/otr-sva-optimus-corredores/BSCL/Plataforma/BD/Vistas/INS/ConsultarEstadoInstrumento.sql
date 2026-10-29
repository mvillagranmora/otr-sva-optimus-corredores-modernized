SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarEstadoInstrumento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarEstadoInstrumento"  
AS SELECT 
	estPla."IdEstado",
	est."Nombre" as "NombreEstado"

FROM "BSCL.Plataforma.BD::PLA.EstadoPla" estPla
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS est ON est."IdEstado" = estPla."IdEstado"
WHERE estPla."NombreEntidad" = 'INSTRUMENTO';
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarEstadoInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
