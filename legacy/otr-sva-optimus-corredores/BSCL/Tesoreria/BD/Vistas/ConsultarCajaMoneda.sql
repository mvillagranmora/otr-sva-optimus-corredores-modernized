SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMoneda";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMoneda" AS

SELECT 
	"CajaMoneda"."IdCajaMoneda",
	"Caja"."IdCaja",
	"Caja"."IdEmpresa" AS "IdEmpresaCaja",
	"Caja"."Codigo",
	"Caja"."Nombre" AS "NombreCaja",
	("Caja"."Codigo" || ' - ' || "Caja"."Nombre") AS "NombreLargoCaja",
	"Caja"."IdEstado" AS "IdEstadoCaja",	
	"CajaMoneda"."IdMoneda",
	"Moneda"."Nombre" AS "NombreMoneda",
	("CajaMoneda"."IdMoneda" || ' - ' || "Moneda"."Nombre") AS "NombreLargoCajaMoneda",
	"CajaMoneda"."Saldo" as "SaldoRegistro",
	AC."Saldo",
	
	
	"CajaMoneda"."IdTipoMovimiento",  -- MOVING / MOVEGR / TODOS
	"CajaMoneda"."IdEstado" AS "IdEstadoCajaMoneda",
	E."Nombre" AS "NombreEstadoCajaMoneda"
FROM "BSCL.Tesoreria.BD::TES.CajaMoneda" AS "CajaMoneda"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS E ON E."IdEstado" = "CajaMoneda"."IdEstado"
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.Caja" AS "Caja" ON ("CajaMoneda"."IdCaja" = "Caja"."IdCaja" AND "CajaMoneda"."IdEmpresa" = "Caja"."IdEmpresa")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda" ON ("CajaMoneda"."IdMoneda" =  "Moneda"."IdMoneda")
LEFT OUTER JOIN (	SELECT "IdCaja","IdMoneda",SUM("Monto") as "Saldo"
					FROM "BSCL.Tesoreria.BD.Vistas::ConsultarArqueoDeCajas"
					WHERE "IdModalidadDePago" = 'MODCAJ'
					GROUP BY "IdCaja","IdMoneda"   
				) AS AC
			 ON AC."IdCaja" = "CajaMoneda"."IdCaja" AND AC."IdMoneda" = "CajaMoneda"."IdMoneda"
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMoneda" TO  BCS_DESARROLLADOR WITH GRANT OPTION;