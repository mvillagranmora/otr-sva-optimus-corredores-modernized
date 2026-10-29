SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::ATR.ConsultarEstadoTipoAtributo";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::ATR.ConsultarEstadoTipoAtributo" 
 AS SELECT "EstadoAutorizacion_$0"."IdEstadoTipoAtributo" AS "IdEstadoTipoAtributo" ,
  "EstadoAutorizacion_$0"."IdEstado" AS "IdEstado" ,
 "Estado_$1"."Nombre" AS "NombreEstado" , "EstadoAutorizacion_$0"."NombreEntidad" AS "NombreEntidad" 
 FROM  ( "BSCL.Plataforma.BD::PLA.EstadoTipoAtributo" AS "EstadoAutorizacion_$0" 
 LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$1" ON  
 ( "Estado_$1"."IdEstado" = "EstadoAutorizacion_$0"."IdEstado" )  );

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ATR.ConsultarEstadoTipoAtributo" TO BCS_DESARROLLADOR WITH GRANT OPTION;


