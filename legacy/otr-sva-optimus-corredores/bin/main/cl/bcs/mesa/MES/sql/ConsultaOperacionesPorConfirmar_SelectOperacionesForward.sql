SELECT 
 OP."FolioOperacion" --grilla
,CAR."FolioCartera"
,OP."EstadoConfirmacion"--grilla
,OP."IdTipoOperacion" AS "TipoOperacion"--grilla
,OP."IdMonedaPrincipal" AS "MonedaPrincipal"--grilla
,OP."IdMonedaSecundaria" AS "MonedaSecundaria"--grilla
,OP."TCCierre"--grilla
,OP."TCCosto"--grilla
,OP."ParidadCierre"--grilla
,OP."IdEstado" AS "EstadoOperacion"--grilla
,INS."Nemotecnico" AS "NombreInstrumento"--grilla
,INS."IdTipoInstrumento"
,CLI."DocumentoIdentidad" AS "RutCliente"--grilla
,CLI."NombreCompleto" AS "NombreCliente"--grilla
,POR."CodigoPortafolio" || ' ' || POR."Proposito" AS "Portafolio"--grilla
,CAR."MontoMonedaPrincipal"--grilla
,CAR."MontoMonedaSecundaria"--grilla

,CAR."IdTipoCanalIngreso" AS "Canal"--grilla
,OP."FechaOperacion"--
,OP."IdOperacion"--
,CLI."IdCliente"--
FROM "BSCL.Mesa.BD::MES.Operacion" OP
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON OP."IdOperacion" = CAR."IdOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON OP."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON OP."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON OP."IdPortafolio" = POR."IdPortafolio"
WHERE OP."IdTipoProducto" = :IdTipoProducto 
AND OP."IdEmpresa" = :IdEmpresa
AND (OP."IdTipoOperacion" = :IdTipoOperacion or  COALESCE(:IdTipoOperacion,'0') = '0')
AND (OP."IdMonedaPrincipal" = :IdMonedaPrincipal OR COALESCE(:IdMonedaPrincipal,'0') = '0')
AND (OP."IdMonedaSecundaria" = :IdMonedaSecundaria OR COALESCE(:IdMonedaSecundaria,'0') = '0')
AND (OP."IdCliente" = :IdCliente OR COALESCE(:IdCliente,0) = 0)
AND (OP."FechaOperacion" LIKE CONCAT(:FechaOperacion,'%') OR COALESCE(:FechaOperacion,'0') = '0')
AND (OP."EstadoConfirmacion" = (:EstadoConfirmacion) OR COALESCE(:EstadoConfirmacion,'0') = '0')
AND (CAR."IdEstado" = 'VIG' OR (CAR."IdEstado" = 'ANU' and OP."EstadoConfirmacion" = 'RZD' ))