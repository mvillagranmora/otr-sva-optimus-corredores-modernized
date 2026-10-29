SELECT "IdOperacion"
      ,"FolioOperacion"
      ,"FechaHoraInicioEdicion"
      ,"CodigoUsuarioBloqueoEdicion"
      ,"IdTipoOperacion"
      ,"IdCliente"
FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones"
WHERE "IdEmpresa" = :IdEmpresa
  AND "BloqueoPorEdicion" = 'S'
  AND "IdTipoProducto" = 'MERF'
  AND "IdTipoOperacion" IN ('COMPAC','VENPAC');
