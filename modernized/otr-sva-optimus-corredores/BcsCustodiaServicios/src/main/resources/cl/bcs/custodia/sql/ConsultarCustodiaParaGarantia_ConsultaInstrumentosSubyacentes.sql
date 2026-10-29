SELECT DISTINCT tis."IdTipoInstrumento" AS "IdTipoInstrumento"
FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" as tis	
inner join "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" bti ON bti."IdTipoInstrumento" = tis."IdTipoInstrumento" and bti."IdBolsa" = tis."IdBolsa" and bti."IdEstado" = 'ACT' and tis."IdEstado" = 'ACT'
inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" cti  on cti."IdTipoInstrumento" = tis."IdTipoInstrumento" and cti."IdEstado" ='VIG'--and cti."IdBolsa" = tis."IdBolsa" 
inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" ctis  on ctis."IdTipoInstrumento" = tis."IdTipoInstrumentoSubyacente" and ctis."IdEstado" ='VIG'--and ctis."IdBolsa" = tis."IdBolsa" 
inner join "BSCL.Plataforma.BD::INS.Bolsa" bolsa on bolsa."IdBolsa" = tis."IdBolsa" and Bolsa."IdEstado" = 'ACT' 
inner join "BSCL.Plataforma.BD::PLA.Estado" est on est."IdEstado" = tis."IdEstado";