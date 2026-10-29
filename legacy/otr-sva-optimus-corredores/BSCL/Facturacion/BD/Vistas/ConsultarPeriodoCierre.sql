SET SCHEMA "BSCL"; 
GO 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarPeriodoCierre"; 
GO

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarPeriodoCierre" As
SELECT "IdPeriodoCierre", 
       "Periodo", 
       substring("Periodo", 4, 4) As "AnoPeriodo",
       "IdEstadoPeriodo",
       "Nombre" AS "NombreEstadoPeriodoCierre",
       PerCierre."IdEstado",
       "IdUsuario", 
       "FechaIngresoRegistro"
 	   
FROM "BSCL.Facturacion.BD::FAC.PeriodoCierre" AS PerCierre
  INNER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS EstFac
     ON EstFac."IdEstado" = PerCierre."IdEstadoPeriodo"
    AND Upper(EstFac."NombreEntidad") = Upper('FAC.PeriodoCierre')
   INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstPla
     ON EstPla."IdEstado" = EstFac."IdEstado";

GO

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarPeriodoCierre" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO