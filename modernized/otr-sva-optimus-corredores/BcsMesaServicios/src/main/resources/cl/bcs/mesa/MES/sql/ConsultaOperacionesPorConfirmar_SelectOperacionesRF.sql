SELECT DISTINCT OP."IdOperacion"

,OP."FolioOperacion" 
,OP."EstadoConfirmacion"
,OP."IdTipoOperacion" AS "TipoOperacion"
,INS."Nombre" AS "NombreInstrumento"
,CLI."IdCliente"
,CLI."DocumentoIdentidad" AS "RutCliente"
,CLI."NombreCompleto" AS "NombreCliente"
,POR."CodigoPortafolio" || ' ' || POR."Proposito" AS "Portafolio"
,OP."IdMoneda" AS "MonedaPrincipal"
,OP."IdMonedaSecundaria" AS "MonedaSecundaria"
,OP."TCCierre"
,OP."TCCosto"
,OP."ParidadCierre"
,OP."IdTipoReajuste"
,OP."Tasa"
,OP."PlazoEnDias"
,OP."IdEstado" AS "EstadoOperacion"
,CAR."IdTipoCanalIngreso" AS "Canal"
,OP."FechaOperacion"

,OP."MontoOperacion"
,OP."MontoOperacionMoneda"
,OP."FechaVencimiento"
,OP."IdPortafolio" as "IdPortafolioCliente"
,OP."IdPortafolioCorredor" as "IdPortafolioCorredor"
,OP."IdClienteCorredor" as "IdClienteCorredor"
,OP."IdMesaOrigen" as "IdMesa"
FROM "BSCL.Mesa.BD::MES.Operacion" OP
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON OP."IdOperacion" = CAR."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON OP."IdOperacion" = RES."IdOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" INS ON OP."IdTipoOperacion" = INS."IdTipoOperacion"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON OP."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON OP."IdPortafolio" = POR."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INST ON INST."IdInstrumento" = OP."IdInstrumento"
WHERE OP."IdTipoProducto" = :IdTipoProducto 
AND OP."IdEmpresa" = :IdEmpresa
AND (OP."IdTipoOperacion" = :IdTipoOperacion or  COALESCE(:IdTipoOperacion,'0') = '0')
AND (OP."IdMonedaPrincipal" = :IdMonedaPrincipal OR COALESCE(:IdMonedaPrincipal,'0') = '0')
AND (OP."IdMonedaSecundaria" = :IdMonedaSecundaria OR COALESCE(:IdMonedaSecundaria,'0') = '0')
AND (OP."IdCliente" = :IdCliente OR COALESCE(:IdCliente,0) = 0)
AND (OP."FechaOperacion" LIKE CONCAT(:FechaOperacion,'%') OR COALESCE(:FechaOperacion,'0') = '0')
AND (OP."EstadoConfirmacion" =:EstadoConfirmacion OR COALESCE(:EstadoConfirmacion,'0') = '0')
AND (OP."IdEstado" = 'PEN' AND OP."EstadoConfirmacion" = 'NCF' OR (OP."IdEstado" = 'VIG' AND OP."EstadoConfirmacion" = 'CNF') OR (OP."IdEstado" = 'ANU' AND OP."EstadoConfirmacion" = 'RZD'));