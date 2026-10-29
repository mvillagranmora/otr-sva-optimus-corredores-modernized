SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoBrokerExtranjero"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoBrokerExtranjero"  
AS
SELECT ESTADO."IdEstado",
		ESTADO."Nombre" AS "NombreEstado"
From "BSCL.Plataforma.BD::PLA.EstadoPla" ESTADOPLA
  inner Join "BSCL.Plataforma.BD::PLA.Estado" ESTADO
         On ESTADOPLA."IdEstado" = ESTADO."IdEstado" and ESTADOPLA."NombreEntidad" = 'BROKEREXTRANJERO';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoBrokerExtranjero" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
