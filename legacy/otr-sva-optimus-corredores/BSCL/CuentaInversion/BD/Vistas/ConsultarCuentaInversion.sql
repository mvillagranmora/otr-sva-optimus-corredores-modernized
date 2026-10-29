SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" AS 	
SELECT
		 "CuentaInversion_$0"."IdCuentaInversion" ,
		 "CuentaInversion_$0"."IdCliente" ,
		 "CuentaInversion_$0"."IdPortafolio" ,
		 "CuentaInversion_$0"."IdMoneda" ,
		 "Moneda_$4"."Codigo" AS "CodigoMoneda" ,
		 "Moneda_$4"."Nombre" AS "NombreMoneda" ,
		 "Moneda_$4"."Decimales" AS "DecimalesMoneda" ,
		 "CuentaInversion_$0"."Codigo" ,
		 "CuentaInversion_$0"."Nombre" ,
		 "CuentaInversion_$0"."FechaIngresoRegistro" ,
		 "CuentaInversion_$0"."IdEmpresa" ,
		 "CuentaInversion_$0"."IdUsuario" ,
		 "CuentaInversion_$0"."BloqueadaEdicion" ,
		 "CuentaInversion_$0"."IdEstado" ,
		 "Estado_$3"."Nombre" AS "NombreEstado",
		 "CuentaInversion_$0"."CuentaInversionPrincipal"
  FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS "CuentaInversion_$0"
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$3" ON "Estado_$3"."IdEstado" = "CuentaInversion_$0"."IdEstado" 
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda_$4" ON "Moneda_$4"."IdMoneda" = "CuentaInversion_$0"."IdMoneda" 
WITH READ ONLY;	 	


GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  
