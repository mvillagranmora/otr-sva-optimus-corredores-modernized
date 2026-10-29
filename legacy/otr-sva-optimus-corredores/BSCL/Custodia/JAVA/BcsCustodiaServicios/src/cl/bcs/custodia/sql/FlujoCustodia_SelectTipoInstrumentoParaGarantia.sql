/********************************************************** 
Proposito	: Recuperar los Tipos de Instrumentos Garantizados
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/

 SELECT "IdTipoInstrumentoGarantizado", "IdTipoInstrumentoAval" FROM ( SELECT
	CASE WHEN bti."IdTipoInstrumento" in ('FDO','FIP','FBC','FIC','FUF','IOA','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF','FBO') THEN 'DER' ELSE bti."IdTipoInstrumento" END AS "IdTipoInstrumentoGarantizado",
    aval."IdTipoInstrumentoPermitido" AS "IdTipoInstrumentoAval"
FROM "BSCL.Custodia.BD::GTA.TipoInstrumentoParaGarantia" aval
INNER JOIN "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" bti ON bti."IdBolsaTipoInstrumento" = aval."IdBolsaTipoInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tiGarantizado ON tiGarantizado."IdTipoInstrumento" = bti."IdTipoInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tiAval ON tiAval."IdTipoInstrumento" = aval."IdTipoInstrumentoPermitido"
INNER JOIN "BSCL.Plataforma.BD::INS.Bolsa" bolsa ON bolsa."IdBolsa" = bti."IdBolsa"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" estado ON estado."IdEstado" = aval."IdEstado"
WHERE bti."ExigeGarantia" = 'S'
  AND bolsa."IdEstado" = 'ACT'
  AND tiAval."IdEstado" = 'VIG'
  AND aval."IdEstado" = 'ACT')
 GROUP BY "IdTipoInstrumentoGarantizado", "IdTipoInstrumentoAval";