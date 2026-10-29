/********************************************************** 
Proposito	: Obtiene Lote Padron del Instrumento
autor		: NRM
fecha		: 30/04/2020
***********************************************************/

									
SELECT 
	  INS."IdInstrumento"
	, INS."Nemotecnico"
	, INS."Tamano" AS "LotePadron"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS
WHERE INS."IdInstrumento" in (:IdInstrumento);