SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarEstadosCuentaInversion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarEstadosCuentaInversion" AS 	 
SELECT
		 "Estado_$1"."IdEstado" ,
		 "Estado_$1"."Nombre" AS "NombreEstado" ,
		 "EstadoCuentaInversion_$0"."NombreEntidad" 
  FROM   "BSCL.CuentaInversion.BD::CTA.EstadoCuentaInversion" AS "EstadoCuentaInversion_$0" 
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$1" ON "Estado_$1"."IdEstado" = "EstadoCuentaInversion_$0"."IdEstado"
  WITH READ ONLY;
  
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarEstadosCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  
