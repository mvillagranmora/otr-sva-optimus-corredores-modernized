SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarOrigenEvento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarOrigenEvento"  
AS

SELECT "CodigoTipoEvento", 
        CAST("FechaIngreso" AS DATE) AS "FechaIngreso",  
       "IdEstado", 
       "CantidadEventos", 
       "IdEmpresa",
       "IdOrigenEvento",
       "DescripcionOrigen",
       "SecuenciaOrigen",
       "TipoEventoOrigen"
      
       
FROM   "BSCL.Plataforma.BD::EVT.OrigenEvento";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarOrigenEvento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 
