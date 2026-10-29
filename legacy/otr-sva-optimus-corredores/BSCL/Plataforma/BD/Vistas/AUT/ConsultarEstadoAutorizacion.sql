SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEstadoAutorizacion";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEstadoAutorizacion" 
 AS SELECT 
 "EstadoAutorizacion_$0"."IdEstadoAutorizacion" AS "IdEstadoAutorizacion" ,
 UPPER("EstadoAutorizacion_$0"."IdEstado") AS "IdEstado" ,
 UPPER(CASE WHEN "EstadoAutorizacion_$0"."Descripcion" = '' THEN "Estado_$1"."Nombre" ELSE "EstadoAutorizacion_$0"."Descripcion" END) AS "NombreEstado" ,
 UPPER("EstadoAutorizacion_$0"."NombreEntidad") AS "NombreEntidad" 
 FROM  ( "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" AS "EstadoAutorizacion_$0" 
 LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$1" ON  
 ( "Estado_$1"."IdEstado" = "EstadoAutorizacion_$0"."IdEstado" )  );
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEstadoAutorizacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;