WITH FUTURO AS (
SELECT 
	 FUT."NumeroCargoAbono"										AS "IdCartera"
	,FUT."IdCliente"											AS "IdCliente"  
	,FUT."DocumentoIdentidad"									AS "DocumentoIdentidad"
	,FUT."IdPortafolio"											AS "IdPortafolio"
	,FUT."CodigoPortafolio"										AS "CodigoPortafolio"
	,FUT."RazonSocial"											AS "Nombre"
	,FUT."Nemotecnico"											AS "Nemotecnico"
	,FUT."IdTipoInstrumento"									AS "IdTipoInstrumento"
	,FUT."IdInstrumento"										AS "IdInstrumento"	
	,TO_VARCHAR(FUT."FechaVencimientoOperacion",'DD-MM-YYYY')	AS "FechaVencimiento"
	,TO_VARCHAR(FUT."FechaOperacion",'DD-MM-YYYY')				AS "Fecha"	
	,FUT."InteresAbierto"										AS "InteresAbierto"
	,FUT."Cantidad"												AS "CantidadContratos"
	,FUT."Tamano"												AS "TamanoContratos"
	,FUT."PrecioMercadoAnterior"								AS "PrecioAnterior"
	,FUT."NombreTipoInstrumento"								AS "Mercado"
	
FROM "BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraFuturos"		FUT
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (FUT."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE 
	PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo 
AND (FUT."FechaOperacion"	<=:FechaSistema)	
 )
 
 SELECT 

	 FUT."IdCartera"								AS "IdCartera"
	,FUT."IdCliente"								AS "IdCliente"  
	,FUT."DocumentoIdentidad"						AS "DocumentoIdentidad"
	,FUT."IdPortafolio"								AS "IdPortafolio"
	,FUT."CodigoPortafolio"							AS "CodigoPortafolio"
	,FUT."Nombre"									AS "Nombre"
	,FUT."Nemotecnico"								AS "Nemotecnico"
	,FUT."IdTipoInstrumento"						AS "IdTipoInstrumento"
	,FUT."IdInstrumento"							AS "IdInstrumento"
	,FUT."FechaVencimiento"							AS "FechaVencimiento"
	,FUT."Fecha"									AS "Fecha"
	,FUT."InteresAbierto"							AS "InteresAbierto"
	,FUT."CantidadContratos"						AS "CantidadContratos"
	,FUT."TamanoContratos"							AS "TamanoContratos"
	,FUT."PrecioAnterior"							AS "PrecioAnterior"
	,FUT."Mercado"									AS "Mercado"
	,MOV."SaldoPorFechaEfectiva"					AS "Saldo"

FROM FUTURO																		AS FUT
INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS MOV
ON (MOV."IdOperacion" = FUT."IdCartera")
WHERE MOV."IdEstado" = 'CRT'