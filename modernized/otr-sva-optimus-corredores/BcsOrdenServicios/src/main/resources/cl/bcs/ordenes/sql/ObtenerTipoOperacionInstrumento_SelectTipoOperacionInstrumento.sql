/**************************************************************
Proposito	: Script consulta detalle TIpo Operacion Instrumento
autor		: FP
fecha		: 05/03/2020
***********************************************************/

select 
	TI."IdTipoInstrumento",
	TTO."IdTipoOperacion",
	TTO."AccionOrden",
	TTO."IdTipoProducto",
	TTO."EsVentaCorta",
	IT."Nemotecnico"
from "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" TI
  		INNER join "BSCL.Plataforma.BD::INS.TipoOperacion" TTO ON TI."IdTipoOperacion" = TTO."IdTipoOperacion" 
  		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" IT ON TI."IdTipoInstrumento" = IT."IdTipoInstrumento"
 WHERE
 	TTO."AccionOrden" IS NOT NULL 
 	AND TTO."EsOrden" = 'S'
 	AND TTO."AccionOrden" = :TipoOperacion
 	AND IT."Nemotecnico" = :Nemotecnico
 
