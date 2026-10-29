SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTramoComision";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTramoComision"
AS
SELECT 
       "TramoComision"."IdTramo"
      ,"TramoComision"."IdComision"
      ,"TramoComision"."DiasDesde"
      ,"TramoComision"."DiasHasta"
      ,"TramoComision"."PorcentajeComision"
      ,"TramoComision"."Estado" AS "EstadoTramo"
      ,"Comision"."IdSerieFondo"
      ,"Comision"."Descripcion"
      ,"Comision"."TipoComision"
      ,"Comision"."FechaVigenciaDesde"
      ,"Comision"."FechaVigenciaHasta"
      ,"Comision"."PorcentajeLiberado"
      ,"Comision"."Estado" AS "EstadoComision"
      ,"Comision"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.TramoComision" AS "TramoComision"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Comision" AS "Comision" 
  ON ("TramoComision"."IdComision" = "Comision"."IdComision");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTramoComision" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
