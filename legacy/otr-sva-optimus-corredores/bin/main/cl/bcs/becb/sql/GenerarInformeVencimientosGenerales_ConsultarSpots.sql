
 WITH  formaDePagoRecibe AS  (	
 	SELECT
 		COMP."IdOperacion",
 		FPM."Nombre",
 		ROW_NUMBER() OVER (PARTITION BY COMP."IdOperacion" ORDER BY COMP."IdComprobantePagoOperacion" ASC) AS "Fila"
	FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" AS COMP
	LEFT JOIN "BSCL.Mesa.BD::MES.FormaDePagoMesa" AS FPM ON FPM."IdFormaDePago" = COMP."IdFormaDePago"
   WHERE COMP."IdTipoProducto" 	= 'MEME'
     AND COMP."RecibePaga"		= 'R'
 ), 
 	formaDePagoEntrega AS ( 
 	SELECT
 		COMP."IdOperacion",
 		FPM."Nombre",
 		ROW_NUMBER() OVER (PARTITION BY COMP."IdOperacion" ORDER BY COMP."IdComprobantePagoOperacion" ASC) AS "Fila"
	FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" AS COMP
	LEFT JOIN "BSCL.Mesa.BD::MES.FormaDePagoMesa" AS FPM ON FPM."IdFormaDePago" = COMP."IdFormaDePago"
   WHERE COMP."IdTipoProducto" 	= 'MEME'
	 AND COMP."RecibePaga"		= 'P'
 )
 
SELECT 
	 TO_VARCHAR("FechaOperacion", 'DD-MM-YYYY')	AS "FechaOperacion"
	,"NombreCliente" 		 					AS "NombreCliente"
	,"TipoOperacion"							AS "TipoOperacion"
	,"IdMonedaPrincipal"						AS "IdMonedaPrincipal"
	,"MontoMonedaPrincipal"  					AS "MontoPrimario"
	,"TipoCambioCierre" 	 					AS "Precio"
	,"IdMonedaSecundaria"						AS "IdMonedaSecundaria"
	,"MontoMonedaSecundaria" 					AS "MontoSecundario"
	,ENT."Nombre" 			 					AS "FormaPagoEntrega"
	,REC."Nombre" 			 					AS "FormaPagoRecibe"
	,"NombreCliente" 		|| '_' || 
	 "IdMonedaPrincipal" 	|| '_' ||
	 "IdMonedaSecundaria" 	 					AS "AgruparSeccion"	-- Campos para agrupar por seccion en hoja de Excel
	
 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"  AS MOV
 LEFT JOIN formaDePagoRecibe 	AS REC ON REC."IdOperacion" = MOV."IdOperacion" AND REC."Fila" = 1
 LEFT JOIN formaDePagoEntrega	AS ENT ON ENT."IdOperacion" = MOV."IdOperacion" AND ENT."Fila" = 1
WHERE "FechaRecibimos" 			= :p_FechaOperacion
  AND "Nemotecnico"  			='INTERB'
  AND MOV."Mesa" 				= 'MEME'
  AND MOV."IdEmpresa"			= :p_IdEmpresa
  AND MOV."Estado"  			NOT IN ('ANU', 'MOD')
ORDER BY "AgruparSeccion";
  