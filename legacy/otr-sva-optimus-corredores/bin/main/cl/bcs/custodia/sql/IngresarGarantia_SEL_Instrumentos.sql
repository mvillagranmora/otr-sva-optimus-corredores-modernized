/********************************************************** 
Proposito	: Script para recuperar información relacionado a instrumentos
autor		: NRM
fecha		: 31/05/2017
***********************************************************/

									
SELECT 
	   INS."IdInstrumento"
	  ,INS."IdTipoInstrumento"
	  ,INS."Nemotecnico"
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
WHERE INS."IdInstrumento"  in (:IdInstrumento);
