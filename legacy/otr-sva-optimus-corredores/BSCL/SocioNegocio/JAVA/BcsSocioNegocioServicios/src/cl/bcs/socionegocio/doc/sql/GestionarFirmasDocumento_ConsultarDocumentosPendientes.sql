SELECT CON.*
FROM  "BSCL.Plataforma.BD::DOC.Documento" 						DOC 
INNER JOIN "BSCL.Plataforma.BD::DOC.Anexo" 						ANX ON ANX."IdDocumento"	= DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo"
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"	CLI ON CLI."IdCliente"   	= DOC."IdCliente" 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"				CON ON CON."IdCliente" 		= CLI."IdCliente" OR CON."IdPersona" = CLI."IdPersona"  
WHERE ANX."IdAnexo" = :IdAnexo
AND CON."Estado" = 'PEN'
