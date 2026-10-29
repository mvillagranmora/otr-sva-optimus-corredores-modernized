SELECT 
CAR."IdCliente"									AS "IdCliente",
CAR."IdPortafolio"								AS "IdPortafolio",
CAR."Nemotecnico"								AS "Nemotecnico",
'C'												AS "Posicion",
CAR."PrecioCompra" 								AS "PrecioEjercicio",
TO_VARCHAR(INS."FechaVencimiento",'YYYY-MM-DD') AS "FechaVencimiento",
'ITM'											AS "Moneyness"	 

FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" 					AS CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS INS 
	ON (INS."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" AS SUB 
	ON (SUB."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" 		AS OPE 
	ON (OPE."IdTipoOperacion" = CAR."IdTipoOperacion")
LEFT JOIN "BSCL.Custodia.BD::DER.OpcionesEjercicioBloqueo" 			AS EJE 
	ON (EJE."IdCartera" = CAR."IdCartera")
WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
AND CAR."IdTipoProducto" != 'MEFW'
AND (SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'COMO' OR SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'VENO')
AND CAR."FechaOperacion" = :FechaSistema AND CAR."IdPortafolio" IN (:IdPortafolio)
ORDER BY CAR."IdCartera";