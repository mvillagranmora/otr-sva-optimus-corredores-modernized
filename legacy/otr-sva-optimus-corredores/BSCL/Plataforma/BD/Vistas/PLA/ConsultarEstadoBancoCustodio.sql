SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoBancoCustodio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoBancoCustodio"  
AS
SELECT ESTADO."IdEstado",
       ESTADO."Nombre" as "NombreEstado"
From "BSCL.Plataforma.BD::PLA.EstadoPla" ESTADOPLA
  inner Join "BSCL.Plataforma.BD::PLA.Estado" ESTADO
         On ESTADOPLA."IdEstado" = ESTADO."IdEstado" and ESTADOPLA."NombreEntidad" = 'BANCOCUSTODIO';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoBancoCustodio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
