INSERT INTO "BSCL.Plataforma.BD::DOC.Anexo"
("IdAnexo"
,"IdDocumento"
,"IdEstado"
,"NumeroFirmantesEmpresa"
,"NumeroFirmantesCliente"
,"NombreFirmanteEmpresa1"
, "NombreFirmanteEmpresa2"
,"NombreFirmanteEmpresa3"
,"NombreFirmanteCliente1"
,"NombreFirmanteCliente2"
,"NombreFirmanteCliente3"
,"FechaIngreso"
,"FechaModificacion"
,"FechaVigencia"
,"DocumentoFisicoReferencia"
,"NombreArchivoAdjunto"
, "FormatoArchivoAdjunto"
, "IdTipoCanalIngreso"
, "NumeroFirmasMinimas"
, "NumeroFirmasMaximas"
)values(
:IdAnexo
,:IdDocumento
,:IdEstado
,:NumeroFirmantesEmpresa
,:NumeroFirmantesCliente
,''
,''
,''
,''
,''
,''
,CURRENT_UTCTIMESTAMP
,CURRENT_UTCTIMESTAMP
,'0001-01-01'
,''
,:NombreArchivoAdjunto
,:FormatoArchivoAdjunto
,:IdTipoCanalIngreso
, :NumeroFirmasMinimas
, :NumeroFirmasMaximas);