SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMonedaUsuario";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMonedaUsuario" AS
SELECT 	C."IdCaja",
		C."IdEmpresa",
		C."Codigo" AS "CodigoCaja",
		C."Nombre" AS "NombreCaja",
		(C."Codigo" || ' - ' || C."Nombre") AS "NombreLargoCaja",
		CM."IdMoneda",
		CM."IdTipoMovimiento",
		CU."IdUsuario",
		CU."FechaInicio",
		CU."FechaTermino",
		CU."Sobregiro",
		"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CU."Sobregiro") as "SobregiroUI",
		C."IdEstado" AS "IdEstadoCaja", 		--Caja activa en el sistema
		CM."IdEstado" AS "IdEstadoCajaMoneda", 	--Moneda activa para trabajar en caja específica
		CU."IdEstado" AS "IdEstadoCajaUsuario", 	--Usuario habilitado a trabajar en caja específica
		E."Nombre" AS "NombreEstadoCajaUsuario"
  FROM "BSCL.Tesoreria.BD::TES.Caja" AS C
 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS E ON E."IdEstado" = C."IdEstado"
  LEFT JOIN "BSCL.Tesoreria.BD::TES.CajaMoneda" AS CM ON CM."IdCaja" = C."IdCaja"
  LEFT JOIN "BSCL.Tesoreria.BD::TES.CajaUsuario" AS CU ON CU."IdCaja" = C."IdCaja" 
WITH READ ONLY;


GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMonedaUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION;