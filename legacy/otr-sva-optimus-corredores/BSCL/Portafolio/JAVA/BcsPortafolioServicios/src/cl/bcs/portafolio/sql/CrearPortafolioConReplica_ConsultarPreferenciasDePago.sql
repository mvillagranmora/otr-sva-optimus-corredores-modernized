SELECT
    PPP."IdPreferenciaPago",
    PPP."IdCuentaBancaria",
    PPP."IdTarjetaBancaria",
    PPP."IdEstado",	
    PPP."PorDefecto",
    PPP."IdPersonaRelacionada",
    PPP."IdCarga",
    TIPOP."RecibePagaPortafolio"
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PPP
INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" AS PP 
    ON PPP."IdPreferenciaPago" = PP."IdPreferenciaPago"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOP 
    ON TIPOP."IdTipoOperacion" = PP."IdTipoOperacion"
WHERE PPP."IdPortafolio" = :IdPortafolio
AND TIPOP."RecibePagaPortafolio" IN ('R', 'P')
AND PPP."IdEstado" = 'ACT'