SELECT 
"Comision"."IdTramo"
, "Comision"."IdComision"
, "Comision"."DiasDesde"
, "Comision"."DiasHasta"
, "Comision"."PorcentajeComision"
, "Comision"."EstadoTramo"
, "Comision"."IdSerieFondo"
, "Comision"."Descripcion"
, "Comision"."TipoComision"
, "Comision"."FechaVigenciaDesde"
, "Comision"."FechaVigenciaHasta"
, "Comision"."PorcentajeLiberado"
, "Comision"."EstadoComision"
, "Comision"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTramoComision" AS "Comision"
 INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionMaxima" AS "ComisionMAX" 
   ON ("ComisionMAX"."IdSerieFondo" = "Comision"."IdSerieFondo" AND "Comision"."IdComision"="ComisionMAX"."IdComision" )
WHERE "Comision"."IdSerieFondo" IN (:IdSerieFondo) 
  AND "Comision"."IdEmpresa" = :IdEmpresa
  AND "Comision"."EstadoTramo" = 'ACT' 
  AND "Comision"."EstadoComision" = 'ACT';