WITH FUTURO AS (
SELECT 
	 FUT."NumeroCargoAbono"								AS "IdCartera"
	,FUT."DocumentoIdentidad"							AS "DocumentoIdentidad"
	,FUT."IdPortafolio"									AS "PortafolioDerivados"
	,FUT."CodigoPortafolio"								AS "CodigoPortafolio"
	,FUT."RazonSocial"									AS "Nombre"
	,FUT."Nemotecnico"									AS "Nemotecnico"
	,FUT."IdTipoInstrumento"							AS "IdTipoInstrumento"
	,FUT."IdInstrumento"								AS "IdInstrumento"
	,FUT."FechaVencimientoOperacion"					AS "FechaVencimiento"
	,FUT."FechaOperacion"								AS "Fecha"
	,FUT."InteresAbierto"								AS "InteresAbierto"
	,FUT."Cantidad"										AS "CantidadContratos"
	,FUT."Tamano"										AS "TamanoContratos"
	,FUT."PrecioMercadoAnterior"						AS "PrecioAnterior"
	,:TipoVencimiento									AS "TipoVencimiento"

FROM
	"BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraFuturos"		FUT
WHERE 
	(FUT."IdPortafolio" = :IdPortafolio	or 0 = :IdPortafolio) AND
	((FUT."FechaVencimientoOperacion" > :FechaSistema AND 'D' = :TipoVencimiento ) OR 
	 (FUT."FechaVencimientoOperacion" = :FechaSistema AND 'V'= :TipoVencimiento ))
	 
)

SELECT
	"IdCartera"							AS "IdCartera", 
	"DocumentoIdentidad"				AS "DocumentoIdentidad",
	"PortafolioDerivados"				AS "PortafolioDerivados",
	"CodigoPortafolio"					AS "CodigoPortafolio",
	"Nombre"							AS "Nombre",
	"Nemotecnico"						AS "Nemotecnico",
	FUTURO."IdTipoInstrumento"			AS "IdTipoInstrumento", 
	FUTURO."IdInstrumento"				AS "IdInstrumento",
	"FechaVencimiento"					AS "FechaVencimiento",
	"Fecha"								AS "Fecha",
	"InteresAbierto"					AS "InteresAbierto",
	"CantidadContratos"					AS "CantidadContratos",
	"TamanoContratos"					AS "TamanoContratos",
	"PrecioAnterior"					AS "PrecioAnterior",
/*	CASE 
		WHEN MOV."TipoMovimiento" = 'AB'	
			THEN 'ABVA'			
			ELSE 'CAVA'
		END 							AS "TipoMovimiento",	*/
		
--
	CASE
		WHEN "TipoVencimiento" = 'D' THEN 
		CASE
			WHEN MOV."TipoMovimiento" = 'AB' 
				THEN 	'ABVA' 
				ELSE 	'CAVA'
			END	
		WHEN "TipoVencimiento" = 'V' THEN
		CASE 
			WHEN MOV."TipoMovimiento" = 'AB' 
				THEN 	'ABVE' 
				ELSE 	'CAVE'
			END	
		END							AS "TipoMovimiento",		
--		
	CASE 
		WHEN MOV."TipoMovimiento" = 'AB'	
			THEN MOV."Monto"			
			ELSE 0
		END 								AS "Abono",
	CASE 
		WHEN MOV."TipoMovimiento" = 'CA'	
			THEN MOV."Monto"			
			ELSE 0
		END 								AS "Cargo",
	MOV."SaldoPorFechaEfectiva"				AS "Saldo",
	MOV."NombreConcepto"					AS "DescMovimiento",
	MOV."IdCargoAbonoMovimiento"			AS "IdCargoAbonoMovimiento"
		

FROM FUTURO
INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS MOV
ON (MOV."IdOperacion" = "IdCartera")
WHERE "IdEstado" = 'CRT'
	
	